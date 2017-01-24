/**
 * @fileoverview Organization Address Selector Widget.
 */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'viewModels/address', 'ccConstants', 'pubsub',
    'koValidate', 'notifier', 'ccKoValidateRules', 'storeKoExtensions',
    'spinner', 'navigation', 'storageApi', 'CCi18n'
  ],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------

  function(ko, Address, CCConstants, pubsub, koValidate, notifier,
    rules, storeKoExtensions, spinner, navigation, storageApi, CCi18n) {

    "use strict";
    
    var shippingAddressChangeSubscriber;

    return {

      addressSetAfterWebCheckout: ko.observable(false),
      showPreviousAddressInvalidError: ko.observable(false),
      previousSelectedCountryValid: ko.observable(false),
      organizationAddressBook: ko.observableArray(),
      selectedBillingAddressId: ko.observable(),
      selectedShippingAddressId: ko.observable(),  

      // Spinner resources
      shippingAddressIndicator: '#shippingAddress',
      shippingAddressIndicatorOptions: {
        parent: '#shippingAddress',
        posTop: '50px',
        posLeft: '30%'
      },

      /**
       * Repopulate this form with up to date info from the User view model.
       */
      reloadAddressInfo: function () {

        var widget = this;

        if (widget.shippingCountriesPriceListGroup().length == 0) {
          widget.destroySpinner();
          $.Topic(pubsub.topicNames.NO_SHIPPING_METHODS).publish();
        }

        if (shippingAddressChangeSubscriber != undefined) {
          shippingAddressChangeSubscriber.dispose();
        }
        
        var eventToFire = pubsub.topicNames.VERIFY_SHIPPING_METHODS;
        if (widget.user().loggedIn() === true && widget.user().organizationAddressBook && widget.user().organizationAddressBook.length > 0) {
          var organizationAddresses = [];
          widget.selectedBillingAddressId('');
          widget.selectedShippingAddressId('');
          for (var k = 0; k < widget.user().organizationAddressBook.length; k++) {
            var organizationAddress = new Address('user-saved-shipping-address', widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry());
            organizationAddress.countriesList(widget.shippingCountriesPriceListGroup());
            organizationAddress.copyFrom(widget.user().organizationAddressBook[k], widget.shippingCountriesPriceListGroup());
            // Save shipping address JS object to Address object.
            organizationAddress.resetModified();
            
            organizationAddresses.push(organizationAddress);
            if ((widget.user().contactShippingAddress && widget.user().contactShippingAddress.repositoryId ===  widget.user().organizationAddressBook[k].repositoryId)
                && !widget.addressSetAfterWebCheckout()) {
              widget.order().shippingAddress().copyFrom(organizationAddress.toJSON(), widget.shippingCountriesPriceListGroup());
              widget.order().updateShippingAddress.bind(widget.order().shippingAddress())();
              widget.selectedShippingAddressId(widget.user().organizationAddressBook[k].repositoryId);
            }
            if ((widget.user().contactBillingAddress && widget.user().contactBillingAddress.repositoryId ===  widget.user().organizationAddressBook[k].repositoryId)
                && !widget.addressSetAfterWebCheckout()) {
              widget.order().billingAddress().copyFrom(organizationAddress.toJSON(), widget.billingCountries());
              widget.selectedBillingAddressId(widget.user().organizationAddressBook[k].repositoryId);
            }
            widget.addressSetAfterWebCheckout(false);
            
          }
          widget.organizationAddressBook(organizationAddresses);
          eventToFire = pubsub.topicNames.POPULATE_SHIPPING_METHODS;
        }
        shippingAddressChangeSubscriber = widget.order().shippingAddress.hasChanged.subscribe(widget.notifyListenersOfShippingAddressUpdate);
		
		if (widget.order().shippingAddress() && widget.order().shippingAddress().validateForShippingMethod()) {
          var shippingAddressWithProductIDs = {};
          shippingAddressWithProductIDs[CCConstants.SHIPPING_ADDRESS_FOR_METHODS] = widget.order().shippingAddress();
          shippingAddressWithProductIDs[CCConstants.PRODUCT_IDS_FOR_SHIPPING] = widget.cart().getProductIdsForItemsInCart();
          $.Topic(eventToFire).publishWith(shippingAddressWithProductIDs, [{
            message: "success"
          }]);
        }
      },

     /**
      * Set the dependencies for the widget.
      */     
     initResourceDependents: function() {
        var widget = this;
        // Message to be displayed in the Message Panel if an error occurs
        widget.ErrorMsg = widget.translate('checkoutErrorMsg');

        widget.order().billingAddress(new Address('checkout-billing-address', widget.ErrorMsg, widget, widget.billingCountries(), widget.defaultBillingCountry()));
        $.Topic(pubsub.topicNames.BILLING_ADDRESS_POPULATED).publishWith([{message: "success"}]);
        widget.order().shippingAddress(new Address('checkout-shipping-address', widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry()));
        $.Topic(pubsub.topicNames.SHIPPING_ADDRESS_POPULATED).publishWith([{message: "success"}]);
        
        widget.order().billingAddress.extend({
          propertyWatch: widget.order().billingAddress()
        });
        widget.order().shippingAddress.extend({
          propertyWatch: widget.order().shippingAddress()
        });

        /**
         * @function
         * @name isValid
         * Determine whether or not the current widget object is valid
         * based on the validity of its component parts. This will not
         * cause error messages to be displayed for any observable values
         * that are unchanged and have never received focus on the
         * related form field(s).
         * @return boolean result of validity test
         */
        widget.isValid = ko.computed(function() {
          if (widget.order().isPaypalVerified()) {
            return widget.order().shippingAddress().isValid();
          } else {

            return (widget.order().billingAddress().isValid() && widget.order().shippingAddress().isValid());
          }
        });

        /**
         * @function
         * @name validateNow
         * Force all relevant member observables to perform their
         * validation now & display the errors (if any)
         */
        widget.validateNow = function() {

          // call order methods to generate correct
          // error messages, if required.
          widget.order().validateBillingAddress();
          widget.order().validateShippingAddress();

          return (widget.isValid());
        };

        /**
         * Callback function for use in widget stacks.
         * Triggers internal widget validation.
         * @return true if we think we are OK, false o/w.
         */
        widget.validate = function() {
          return widget.validateNow();
        };

        /**
         * Handle for shipping address selection.
         */
        widget.selectShippingAddress = function (address) {
          widget.order().shippingAddress().copyFrom(address.toJSON(), widget.shippingCountriesPriceListGroup());
          widget.order().shippingAddress().resetModified();
          return true;
        };

        /**
         * Handle for billing address selection.
         */        
        widget.selectBillingAddress = function (address) {
          widget.order().billingAddress().copyFrom(address.toJSON(), widget.billingCountries());
          widget.order().billingAddress().resetModified();
          return true;
        };
        
        /**
         * Listener for shipping address update.
         */
        widget.notifyListenersOfShippingAddressUpdate = function (hasChanged) {
          if(hasChanged && widget.order().shippingAddress().isValid()) {
            var shippingAddressWithProductIDs = {};
            shippingAddressWithProductIDs[CCConstants.SHIPPING_ADDRESS_FOR_METHODS] = widget.order().shippingAddress();
            shippingAddressWithProductIDs[CCConstants.PRODUCT_IDS_FOR_SHIPPING] = widget.cart().getProductIdsForItemsInCart();
              
            widget.cart().updateShippingAddress.bind(shippingAddressWithProductIDs)();
            
          } else if(hasChanged && widget.order().shippingAddress().validateForShippingMethod()) {
            // Handle case where address is sufficiently completed to calculate shipping & tax
            // Ensure that required fields have at least blank values
            if (widget.order().shippingAddress().firstName() == undefined)
              widget.order().shippingAddress().firstName('');
            if (widget.order().shippingAddress().lastName() == undefined)
              widget.order().shippingAddress().lastName('');
            if (widget.order().shippingAddress().address1() == undefined)
              widget.order().shippingAddress().address1('');
            if (widget.order().shippingAddress().city() == undefined)
              widget.order().shippingAddress().city('');
            if (widget.order().shippingAddress().phoneNumber() == undefined)
              widget.order().shippingAddress().phoneNumber('');

            var shippingAddressWithProductIDs = {};
            shippingAddressWithProductIDs[CCConstants.SHIPPING_ADDRESS_FOR_METHODS] = widget.order().shippingAddress();
            shippingAddressWithProductIDs[CCConstants.PRODUCT_IDS_FOR_SHIPPING] = widget.cart().getProductIdsForItemsInCart();

            widget.cart().updateShippingAddress.bind(shippingAddressWithProductIDs)();

          } else if (hasChanged && !widget.order().shippingAddress().isValid()) {
              $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_ADDRESS_INVALID).publish();
          }
        };
        
      },

      /**
       * Called before the widget appears every time.
       */
      beforeAppear: function (page) {
        var widget = this;
        widget.reloadAddressInfo();
        if (widget.order().isPaypalVerified()) {
          // On successful return from paypal site
          widget.createSpinner();
          // Fetches the data to populate the checkout widgets
          widget.order().getOrder();
        }
      },

      /**
       * Set the dependencies before onload.
       */
      resourcesLoaded: function(widget) {
        widget.initResourceDependents();
      },

      /**
        Organization address selector Widget.
        @private
        @name checkout-customer-details
        @property {observable Address} billingAddress object representing the customer's
                                       billing address.
        @property {observable Address} shippingAddress object representing the customer's
                                       shipping address.
      */
      onLoad: function(widget) {

        widget.order().billingAddress.isData = true;
        widget.order().shippingAddress.isData = true;

        widget.resetListener = function(obj) {
          widget.order().billingAddress().reset();
          widget.order().shippingAddress().reset();
        };

        $.Topic(pubsub.topicNames.ORDER_SUBMISSION_SUCCESS).subscribe(widget.resetListener);

        widget.destroySpinner = function() {
          $(widget.shippingAddressIndicator).removeClass('loadingIndicator');
          spinner.destroyWithoutDelay(widget.shippingAddressIndicator);
        };

        widget.shippingAddressDuringPaypalCheckout = function(paypalShippingAddress) {
          var shippingAddress = new Address('user-paypal-shipping-address', widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry());
          if (paypalShippingAddress && (widget.cart().items().length > 0) && widget.order().isPaypalVerified()) {
            // Save shipping address JS object to Address object.
            shippingAddress.copyFrom(paypalShippingAddress, widget.shippingCountriesPriceListGroup());
            shippingAddress.resetModified();
          } else if (widget.user().loggedIn() === true && widget.user().updatedShippingAddress && (widget.cart().items().length > 0)) {
            // Save shipping address JS object to Address object.
            shippingAddress.copyFrom(widget.user().updatedShippingAddress, widget.shippingCountriesPriceListGroup());
            shippingAddress.resetModified();
          }
          widget.order().shippingAddress().copyFrom(shippingAddress.toJSON(), widget.shippingCountriesPriceListGroup());
          widget.destroySpinner();
        };

        $.Topic(pubsub.topicNames.PAYPAL_CHECKOUT_SHIPPING_ADDRESS).subscribe(widget.shippingAddressDuringPaypalCheckout.bind(this));

        widget.shippingAddressDuringWebCheckout = function(webShippingAddress) {
          var shippingAddress = new Address('user-web-shipping-address', widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry());
          if (webShippingAddress && (widget.cart().items().length > 0)) {
            // Save shipping address JS object to Address object.
            shippingAddress.copyFrom(webShippingAddress, widget.shippingCountriesPriceListGroup());
            shippingAddress.resetModified();
          }
          widget.order().shippingAddress().copyFrom(shippingAddress.toJSON(), widget.shippingCountriesPriceListGroup());
          widget.addressSetAfterWebCheckout(true);
          widget.destroySpinner();
        };

        $.Topic(pubsub.topicNames.WEB_CHECKOUT_SHIPPING_ADDRESS).subscribe(widget.shippingAddressDuringWebCheckout.bind(this));

        $.Topic(pubsub.topicNames.GET_INITIAL_ORDER_FAIL).subscribe(function() {
            widget.destroySpinner();
        });

        widget.createSpinner = function() {
          $(widget.shippingAddressIndicator).css('position','relative');
          $(widget.shippingAddressIndicator).addClass('loadingIndicator');
          spinner.create(widget.shippingAddressIndicatorOptions);
        };
      }
    };
  }
);    
