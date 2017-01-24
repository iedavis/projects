/**
 * @fileoverview Cart Shipping Details Widget.
 *
 */
define(

    // -------------------------------------------------------------------
    // DEPENDENCIES
    // -------------------------------------------------------------------
    [ 'knockout', 'viewModels/address', 'ccConstants', 'pubsub', 'koValidate',
        'notifier', 'ccKoValidateRules', 'storeKoExtensions', 'ccLogger',
        'spinner', 'storageApi', 'CCi18n'],

    // -------------------------------------------------------------------
    // MODULE DEFINITION
    // -------------------------------------------------------------------

    function(ko, Address, CCConstants, pubsub, koValidate, notifier, rules,
        storeKoExtensions, logger, spinner, storageApi, CCi18n) {

      "use strict";
      var countryChangeSubscriber;
      var stateChangeSubscriber;
      var postalCodeChangeSubscriber;

      return {

        shippingAddress : ko.observable(),
        reloadShippingMethods : ko.observable(false),
        shippingOptions : ko.observableArray(),
        selectedShippingValue : ko.observable(),
        selectedShippingOption : ko.observable(),
        skipShipMethodNotification : ko.observable(false),
        displayShippingOptions : ko.observable(false),
        noShippingMethods : ko.observable(false),
        invalidShippingAddress : ko.observable(false),
        invalidShippingMethod : ko.observable(false),
        pricingError : ko.observable(false),
        showPreviousAddressInvalidError : ko.observable(false),
        previousSelectedCountryValid: ko.observable(false),
        isCartPriceUpdated : ko.observable(false),
        removeAdjacentShippingAmount : ko.observable(false),
        showShippingOptionDropDown : ko.observable(false),
        loadPersistedShipping : ko.observable(false),
        shippingMethodsLoaded : ko.observable(false),

        // Spinner resources
        DEFAULT_LOADING_TEXT : "Loading...'",
        methodsIndicator : '#shipping-options',
        methodsIndicatorOptions : {
          parent : '#shipping-options',
          posTop : '10%',
          posLeft : '5%'
        },

        /**
         * Called when resources have been loaded
         */
        resourcesLoaded : function(widget) {
          widget.initResourceDependents();
        },

        /**
         * Called when the widget is loaded the first time
         */
        onLoad : function(widget) {
          // Message to be displayed in the Message Panel if an error occurs
          widget.ErrorMsg = widget.translate('cartShippingErrorMsg');

          widget.cart().usingImprovedShippingWidgets(true);
          widget.shippingAddress.isData = true;
          widget.selectedShippingOption.isData = true;
          widget.reloadShippingMethods.isData = true;
          widget.clearInvalidShippingMethodError = true;

          /**
           * Sets up shipping options
           */
          widget.setupShippingOptions = function(obj) {
            if (widget.shippingAddress() != undefined
                && widget.shippingAddress().validateForShippingMethod()) {
              widget.invalidShippingAddress(false);
            }
          };

          /**
           * Resets shipping options
           */
          widget.resetShippingOptions = function(obj) {
            widget.invalidShippingAddress(false);
            widget.invalidShippingMethod(false);
            widget.pricingError(false);
            widget.noShippingMethods(false);
            widget.displayShippingOptions(false);
            widget.showShippingOptionDropDown(false);

            widget.selectedShippingOption(null);
            widget.selectedShippingValue(null);
            widget.shippingOptions.removeAll();

            widget.skipShipMethodNotification(false);
            $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_METHOD).publishWith(
                widget.selectedShippingOption(), [ {
                  message : "success"
                } ]);
          };

          /**
           * Sends notification if shipping address and method have changed
           */
          widget.sendShippingNotification = function() {
            if (widget.selectedShippingOption() != undefined
                && widget.selectedShippingOption() != ''
                && !widget.skipShipMethodNotification() && (widget.cart().shippingMethod() != widget.selectedShippingValue())) {
              widget.cart().shippingMethod(widget.selectedShippingValue());
              $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_METHOD).publishWith(
                  widget.selectedShippingOption(), [ {
                    message : "success"
                  } ]);
            }
            widget.skipShipMethodNotification(false);
          };
          
          /**
           * Utility function to determine whether the selected shipping method
           * occurs in the list of shipping options
           */
          widget.checkIfShippingMethodExists = function(selectedShippingMethod,
              shippingOptions) {
            return ko.utils.arrayFirst(widget.shippingmethods().shippingOptions(), function(
                shippingOption) {
              return selectedShippingMethod === shippingOption.repositoryId;
            });
          };

          /**
           * Handle case when there are no shipping methods available
           */
          widget.handleNoShippingMethods = function(obj) {
            widget.destroySpinner();
            widget.resetShippingOptions();
            widget.noShippingMethods(true);
          };

          /**
           * Handle case when shipping methods have been successfully loaded
           */
          widget.handleShippingMethodsLoaded = function(obj) {
            if (widget.shippingmethods().shippingOptions().length === 0) {
              widget.handleNoShippingMethods();
            } else {
              widget.noShippingMethods(false);
              widget.invalidShippingMethod(false);
              widget.skipShipMethodNotification(true);
              // TODO - According to Nev this will cause issues with the
              // observable model
              // as we're assigning a new array to the observable array.
              // May have to iterate through
              // widget.shippingmethods().shippingOptions()
              // and push these onto widget.shippingOptions.
              // However if we make our own copies then this complicates pricing
              // updates
              // as our copy won't have the updated pricing
              widget.removeAdjacentShippingAmount(false);
              widget.shippingMethodsLoaded(false);
              widget
                  .shippingOptions(widget.shippingmethods().shippingOptions());
              widget.isCartPriceUpdated(false);
              // Use selected shipping option stored in cart if this is in the
              // list of shipping options for the current address.
              if ((widget.cart() != undefined)
                  && (widget.cart().shippingMethod() != undefined)
                  && (widget.cart().shippingMethod() != '')
                  && (widget.checkIfShippingMethodExists(widget.cart()
                      .shippingMethod(), widget.shippingOptions))) {
                  // Flag this as a change in shipping method so that re-pricing occurs.
                  widget.selectedShippingValue(null);
                  widget.selectedShippingValue(widget.cart().shippingMethod());
                  widget.removeAdjacentShippingAmount(true);
              }
              // Otherwise use the default shipping method
              else {
            	if ((widget.cart().shippingMethod() != '')
            	    && (widget.checkIfShippingMethodExists(widget.cart()
                	.shippingMethod(), widget.shippingOptions) == null)){
                  // Notify User that selected shipping method no longer exists.
                  notifier.sendError(widget.typeId() + '-selectedShippingMethod',CCi18n.t('ns.cartshippingdetails:resources.shippingMethodNoLongerAvailable'), true);
                  widget.invalidShippingMethod(true);
                }
                if (widget.selectedShippingValue()) {
                  widget.clearInvalidShippingMethodError = false;
                  widget.cart().shippingMethod(widget.selectedShippingValue());      
                } else if(!widget.cart().shippingMethod()){
                  widget.selectedShippingValue(null);
                } else if (widget.shippingmethods().defaultShipping() != undefined) {
                  widget.clearInvalidShippingMethodError = false;
                  widget.selectedShippingValue(widget.shippingmethods().defaultShipping());
                }
              }

              // Show the available shipping options
              widget.destroySpinner();
              widget.displayShippingOptions(true);
              widget.setupShippingOptions();
              widget.showShippingOptionDropDown(true);
            }
          };

          /**
           * Handles case when loading of shipping methods failed
           */
          widget.handleShippingMethodsLoadFailed = function(obj) {
            widget.destroySpinner();
            notifier.sendError(widget.typeId() + '-shippingMethods',
                this.message, true);
            widget.resetShippingOptions();
            widget.invalidShippingAddress(true);
            widget.noShippingMethods(true);
            widget.showShippingOptionDropDown(false);
          };

          widget.disableLoadPersistedMethod = function(data) {
            widget.loadPersistedShipping(false);
            return true;
          };

          widget.optionsCaption = ko.computed(function() {
            return CCi18n.t('ns.cartshippingdetails:resources.selectShippingMethodText');
          });

          widget.optionsTextForShippingMethod = function(data) {
            if(data) {
              if (widget.removeAdjacentShippingAmount()) {
                return data.displayName;
              }
              if (data.isDummy) {
                return "";
              }
              return data.displayName + " ("+ widget.cart().currency.symbol + data.estimatedCostText() + ")";
            } else {
              return "";
            }
          };
          widget.shippingAddress.hasChanged.subscribe(function (hasChanged) {
            widget.notifyListenersOfShippingAddressUpdate(hasChanged);
            });
          widget.displayShippingMethodsDropdown = function(data, event) {
            var self = this;
            self.removeAdjacentShippingAmount(false);
            if(self.isCartPriceUpdated() || self.shippingmethods().shippingOptions().length == 0) {
              for (var i = 0; i < 6 && !self.shippingMethodsLoaded(); i++) {
                self.shippingOptions.push({displayName: "", repositoryId:"dummy" + i, isDummy:true});
              }
              self.isCartPriceUpdated(false);
              self.createSpinner();
              self.cart().shippingAddress(self.shippingAddress());
              self.cart().populateShipppingMethods();
            }
            return true;
            

          };

          widget.shippingOptionBlured = function(data, event) {
            var self = this;
            self.removeAdjacentShippingAmount(true);
            return true;
          };
            
          /**
           * Key down event handler for the shipping options list
           */
          widget.shippingOptionsKeyDownPressHandler = function(obj, data, event) {
            // Down arrow being pressed on last option in list - cycle round to
            // first option
            if ((data == widget.shippingOptions()[widget.shippingOptions().length - 1])
                && event.keyCode == 40) {
              var idLastShippingMethod = obj
                  + widget.shippingOptions()[0].repositoryId;
              $(idLastShippingMethod).attr('checked');
              $(idLastShippingMethod).focus();
              $(idLastShippingMethod).prop('checked', true);
              widget
                  .selectedShippingValue(widget.shippingOptions()[0].repositoryId);
            }
            // Up arrow being pressed on first option in list - cycle round to
            // last option
            else if ((data == widget.shippingOptions()[0])
                && event.keyCode == 38) {
              var idLastShippingMethod = obj
                  + widget.shippingOptions()[widget.shippingOptions().length - 1].repositoryId;
              $(idLastShippingMethod).attr('checked');
              $(idLastShippingMethod).focus();
              $(idLastShippingMethod).prop('checked', true);
              widget.selectedShippingValue(widget.shippingOptions()[widget
                  .shippingOptions().length - 1].repositoryId);
            } else {
              return true;
            }
          };

          // Methods to create and destroy spinner
          widget.destroySpinner = function() {
            $(widget.methodsIndicator).removeClass('loadingIndicator');
            spinner.destroyWithoutDelay($(widget.methodsIndicator));
          };
          widget.createSpinner = function() {
            $(widget.methodsIndicator).css('position', 'relative');
            widget.methodsIndicatorOptions.loadingText = widget.translate(
                'fetchingShippingMethodsText', {
                  defaultValue : this.DEFAULT_LOADING_TEXT
                });
            spinner.create(widget.methodsIndicatorOptions);
          };

          /**
           * Handles a change in the cart
           */
          widget.handleUpdatedCart = function(obj) {
            // When the cart changes (e.g. add to cart, remove from cart, update
            // quantity)
            // we need to update the shipping option costs that are displayed.
            // Such cart changes should trigger a cart re-price, so we'll wait
            // for the outcome of that before refreshing the shipping option costs.
        	if(widget.isActiveOnPage()) {
        	  widget.createSpinner();
        	}
            widget.isCartPriceUpdated(true);
          },

          /**
           * Detect and publish changes to the selected shipping method
           */
          widget.selectedShippingValue.subscribe(function(newValue) {
            if (newValue) {
              
              // clears invalid shipping method error only if user selects any
              // shipping option
              // but not if default shipping method is selected after shipping
              // options reload.
              if (widget.clearInvalidShippingMethodError) {
                notifier.clearError("OrderViewModel");
              } else {
                widget.clearInvalidShippingMethodError = true;
              }
              // Figure out which shipping option is currently selected and send
              // shipping notification
              for (var i = 0; i < widget.shippingOptions().length; i++) {
                if (widget.shippingOptions()[i].repositoryId === widget
                    .selectedShippingValue()) {
                  widget.selectedShippingOption(widget.shippingOptions()[i]);
                  widget.skipShipMethodNotification(false);
                  widget.sendShippingNotification();
                  if (widget.shippingMethodsLoaded()) {
                    widget.removeAdjacentShippingAmount(true);
                  } else {
                    widget.shippingMethodsLoaded(true);
                    widget.removeAdjacentShippingAmount(false);
                  }
                  if (widget.reloadShippingMethods()) {
                    widget.reloadShippingMethods(false);
                  } else {
                    // clear any previous error messages.
                    notifier.clearError(widget.typeId() + '-shippingMethods');
                  }
                  break;
                }
              }
            }
          });

          /**
           * When shipping address is changed, check whether it is valid If it
           * is, display shipping options.
           */
          widget.notifyListenersOfShippingAddressUpdate =  function (hasChanged) {
            if (hasChanged) {
              // If shipping address valid for shipping, request shipping
              // methods for this address
              if (widget.shippingAddress().validateForShippingMethod()) {
                
                // Ensure that other address fields are at least blank to prevent validation error.
                if (widget.shippingAddress().firstName() == undefined) 
                  widget.shippingAddress().firstName('');
                if (widget.shippingAddress().lastName() == undefined) 
                  widget.shippingAddress().lastName('');
                if (widget.shippingAddress().address1() == undefined)
                  widget.shippingAddress().address1('');
                if (widget.shippingAddress().city() == undefined)
                  widget.shippingAddress().city('');
                if (widget.shippingAddress().phoneNumber() == undefined)
                  widget.shippingAddress().phoneNumber('');
                
                // Reset flags
                widget.invalidShippingAddress(false);
                widget.invalidShippingMethod(false);
                widget.pricingError(false);
                widget.noShippingMethods(false);
                
                // Start spinner and request shipping methods for address
                // Only do this if there are items in the cart
                if (widget.cart() != undefined
                    && widget.cart().items() != undefined
                    && widget.cart().items().length > 0 && !widget.loadPersistedShipping()) {
                  widget.selectedShippingValue(null);
                  widget.cart().shippingMethod('');
                  widget.shippingmethods().shippingOptions.removeAll();
                  widget.showShippingOptionDropDown(true);
                  $.Topic(pubsub.topicNames.CHECKOUT_RESET_SHIPPING_METHOD).publish();
                  //make changes to order's country state zip
                  if (widget.order().shippingAddress()) {
                  widget.order().shippingAddress().selectedCountry(widget.shippingAddress().selectedCountry());
                  widget.order().shippingAddress().selectedState(widget.shippingAddress().selectedState());
                  widget.order().shippingAddress().postalCode(widget.shippingAddress().postalCode());
                  }
                }
                // Saving selected country and selected region to localStorage 
                var selectedCountryRegion = new Object();
                selectedCountryRegion.selectedCountry = widget.shippingAddress().selectedCountry();
                selectedCountryRegion.selectedState = widget.shippingAddress().selectedState();
                try {
                  widget.checkPreviousAddressValidity(widget);
                  storageApi.getInstance().setItem("selectedCountryRegion", JSON.stringify(selectedCountryRegion));
                } catch(pError) {
                }
              }
              // Otherwise send notification that the address is invalid
              else {
                widget.resetShippingOptions();
                widget.noShippingMethods(true);
              }
            }
          };

          // Handle event when user creates an account
          $.Topic(pubsub.topicNames.USER_AUTO_LOGIN_SUCCESSFUL)
              .subscribe(widget.sendShippingNotification);

          // Handle event where the shipping address is invalid
          $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_ADDRESS_INVALID)
              .subscribe(widget.resetShippingOptions);

          // Handle event where there are no shipping methods available
          $.Topic(pubsub.topicNames.NO_SHIPPING_METHODS).subscribe(
              widget.handleNoShippingMethods);

          $.Topic(pubsub.topicNames.CART_SHIPPING_ADDRESS_UPDATED).subscribe(
          function(obj) {
            if (widget.shippingAddress()) {
              widget.loadPersistedShipping(true);
              widget.showShippingOptionDropDown(true);
              var cartShippingMethod = widget.cart().shippingMethod();
              widget.shippingAddress().copyFrom(widget.cart().shippingAddress().toJSON(), widget.shippingCountriesPriceListGroup());
              widget.cart().populateShipppingMethods();
              widget.selectedShippingValue(cartShippingMethod);
            }
          });

          $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_ADDRESS_UPDATED).subscribe(
          function(obj) {
            if (widget.shippingAddress()) {
              var shippingMethod = widget.cart().shippingMethod();
              widget.shippingAddress().copyFrom(widget.order().shippingAddress().toJSON(), widget.shippingCountriesPriceListGroup());
              widget.selectedShippingValue(shippingMethod);
            }
          });

          $.Topic(pubsub.topicNames.INVALID_SHIPPING_METHOD).subscribe(
                  function() {
                    widget.selectedShippingValue(null);
                    notifier.sendError(widget.typeId() + '-selectedShippingMethod',CCi18n.t('ns.cartshippingdetails:resources.shippingMethodNoLongerAvailable'), true);
                    widget.invalidShippingMethod(true);
                  });

          
          // Handle event when shipping methods have been loaded
          $.Topic(pubsub.topicNames.SHIPPING_METHODS_LOADED).subscribe(
              widget.handleShippingMethodsLoaded);

          // Handle event when shipping methods could not be loaded
          $.Topic(pubsub.topicNames.LOAD_SHIPPING_METHODS_FAILED).subscribe(
              widget.handleShippingMethodsLoadFailed);

          // Handle case when shipping method is reset
          $.Topic(pubsub.topicNames.CHECKOUT_RESET_SHIPPING_METHOD).subscribe(
              function(obj) {
                widget.selectedShippingValue(null);
                widget.selectedShippingOption(null);
                widget.setupShippingOptions();
                if ( widget.shippingmethods() 
                	 &&  widget.shippingmethods().shippingOptions()
                	 &&  widget.shippingmethods().shippingOptions().length > 0 ){
                	widget.shippingmethods().shippingOptions.removeAll();
                }
              });

          // Handle event when order pricing succeeded
          $.Topic(pubsub.topicNames.ORDER_PRICING_SUCCESS).subscribe(
              function(obj) {
                widget.destroySpinner();
                widget.displayShippingOptions(true);
                widget.showShippingOptionDropDown(true);
                widget.setupShippingOptions();
                widget.pricingError(false);
                widget.invalidShippingAddress(false);
                widget.invalidShippingMethod(false);
              });

          // Handle event when cart pricing failed
          $.Topic(pubsub.topicNames.ORDER_PRICING_FAILED)
              .subscribe(
                  function(obj) {
                    widget.destroySpinner();
                    widget.invalidShippingAddress(false);
                    widget.invalidShippingMethod(false);
                    widget.pricingError(false);
                    widget.shippingMethodsLoaded(false);

                    // Handle case where cart pricing failed due to invalid
                    // shipping region
                    if (this && this.errorCode == CCConstants.INVALID_SHIPPING_COUNTRY_STATE) {
                        widget.invalidShippingAddress(true);
                        widget.displayShippingOptions(false);
                        widget.showShippingOptionDropDown(false);
                    }
                    // Handle case where cart pricing failed due to an invalid
                    // shipping method
                    else if (this && this.errorCode == CCConstants.INVALID_SHIPPING_METHOD) {
                        notifier.sendWarning(widget.typeId() + '-selectedShippingMethod', this.message, false);
                        widget.invalidShippingMethod(true);
                    }
                    // Handle pricing tax error
                    else if (this && this.errorCode == CCConstants.PRICING_TAX_REQUEST_ERROR) {
                        widget.resetShippingOptions();
                        notifier.sendWarning(widget.typeId() + '-pricingError', this.message, false);
                        widget.pricingError(true);
                    }
                    // Handle other pricing error
                    else {
                      if (this && this.message) {
                        notifier.sendWarning(widget.typeId() + '-pricingError', this.message, false);
                      }
                      widget.pricingError(true);
                    }
                  });

          // Detect cart changes
          $.Topic(pubsub.topicNames.CART_ADD).subscribe(
              widget.handleUpdatedCart);
          $.Topic(pubsub.topicNames.CART_REMOVE).subscribe(
              widget.handleUpdatedCart);
          $.Topic(pubsub.topicNames.CART_UPDATE_QUANTITY).subscribe(
              widget.handleUpdatedCart);

          // When cart has been updated due to a cart item or quantity change,
          // refresh the shipping options
          $.Topic(pubsub.topicNames.CART_UPDATED).subscribe(
              function(obj) {
                // If the cart has changed then we need to refresh the cost of
                // each shipping option
            	if(widget.isActiveOnPage()) {
                  widget.destroySpinner();
            	}
          });

          // If selected shipping method changed elsewhere, refresh it here
          $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_METHOD).subscribe(
              function(obj) {
                // If shipping option different from the one here, refresh the
                // local copy
            	  if (this && this.repositoryId && widget.shippingmethods() && widget.shippingmethods().shippingOptions() 
                    && widget.checkIfShippingMethodExists(this.repositoryId,
                    		widget.shippingmethods().shippingOptions())
                    && (widget.selectedShippingValue() == undefined || widget
                        .selectedShippingValue() !== this.repositoryId) && widget.cart().shippingMethod()) {
                  widget.skipShipMethodNotification(true);
                  widget.selectedShippingValue(this.repositoryId);
                }
              });
          
          // Detect changes in the user's shipping address
          $.Topic(pubsub.topicNames.USER_LOAD_SHIPPING).subscribe(function(obj) {
            // Use default shipping address of registered user if available
            if (widget.user().loggedIn() === true && widget.user().updatedShippingAddress) {
              var shippingAddresses = [];
              for (var k = 0; k < widget.user().updatedShippingAddressBook.length; k++) {
                var shippingAddress = new Address('user-shipping-address', widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry());
                shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
                shippingAddress.copyFrom(widget.user().updatedShippingAddressBook[k], widget.shippingCountriesPriceListGroup());
                // Save shipping address JS object to Address object.
                shippingAddress.resetModified();

                if (shippingAddress.isValid()) {
                  shippingAddresses.push(shippingAddress);
                }
                if (shippingAddress.isDefaultAddress() && shippingAddress.isValid() && widget.cart().shippingMethod() == "") {
                  widget.shippingAddress(shippingAddress);
                  if (!widget.loadPersistedShipping()) {
                    widget.selectedShippingValue(null);
                    widget.cart().shippingMethod('');
                    widget.shippingmethods().shippingOptions.removeAll();
                    widget.showShippingOptionDropDown(true);
                  }
                }
              }
              if (widget.shippingAddress() && !widget.shippingAddress().isValid() && shippingAddresses[0]) {
                widget.shippingAddress().copyFrom(shippingAddresses[0].toJSON(), widget.shippingCountriesPriceListGroup());
                
                var shippingAddressWithProductIDs = {};
                shippingAddressWithProductIDs[CCConstants.SHIPPING_ADDRESS_FOR_METHODS] = widget.shippingAddress();
                shippingAddressWithProductIDs[CCConstants.PRODUCT_IDS_FOR_SHIPPING] = widget.cart().getProductIdsForItemsInCart();
                widget.cart().updateShippingAddress.bind(shippingAddressWithProductIDs)();
              }
            }
            // Use cart address if a saved shipping address is not available.
            else if (widget.cart() != undefined && 
                widget.cart().shippingAddress() != undefined &&
                widget.cart().shippingAddress() != '') {
              var shippingAddress = new Address('cart-shipping-address',
                  widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry());
              shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
              shippingAddress.copyFrom(widget.cart().shippingAddress().toJSON(), widget.shippingCountriesPriceListGroup());
              shippingAddress.resetModified();
              widget.shippingAddress(shippingAddress);
            }
          });
          
          // Reset the shipping address when the user logs out
          $.Topic(pubsub.topicNames.USER_LOGOUT_SUCCESSFUL).subscribe(function(obj) {
            widget.shippingAddress().reset();
            storageApi.getInstance().removeItem("selectedCountryRegion");
          });

          $.Topic(pubsub.topicNames.USER_LOGIN_SUCCESSFUL).subscribe(function(obj) {
            storageApi.getInstance().removeItem("selectedCountryRegion");
          });

          // Refresh the shipping options
          widget.destroySpinner();
          widget.setupShippingOptions();
          
          //create a blank shipping address when widget loads
          var shippingAddress = new Address('blank-shipping-address',
                  widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget
                      .defaultShippingCountry());
              shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
              shippingAddress.resetModified();
              widget.shippingAddress(shippingAddress);
        },
        /**
         * Called before the widget appears every time
         */
        beforeAppear : function(page) {
          var widget = this;
          
          if (countryChangeSubscriber != undefined) {
            countryChangeSubscriber.dispose();
          }
          if (stateChangeSubscriber != undefined) {
            stateChangeSubscriber.dispose();
          }
          if (postalCodeChangeSubscriber != undefined) {
            postalCodeChangeSubscriber.dispose();
          }
          if (widget.cart().shippingMethod()) {
            widget.loadPersistedShipping(true);
          } else {
            widget.loadPersistedShipping(false);
          }
          
          widget.shippingMethodsLoaded(false);
          notifier.clearError(widget.typeId());
          widget.checkPreviousAddressValidity(widget);
          if (widget.showPreviousAddressInvalidError()) {
            notifier.sendError(widget.typeId(), CCi18n.t('ns.cartshippingdetails:resources.invalidPreviousAddress'), true);
            widget.showPreviousAddressInvalidError(false);
          }

          if (widget.shippingCountriesPriceListGroup().length == 0) {
            $.Topic(pubsub.topicNames.NO_SHIPPING_METHODS).publish();
          }

          // Use cart's shipping address from last time if set
          if (widget.cart() != undefined
              && widget.cart().shippingAddress() != undefined
              && widget.cart().shippingAddress() != ''
              && widget.shippingmethods() 
              && widget.shippingmethods().shippingOptions()
              && widget.shippingmethods().shippingOptions().length > 0 ) {
            var shippingAddress = new Address('cart-shipping-address',
                widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget
                    .defaultShippingCountry());
            shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
            // Save shipping address JS object to Address object.
            shippingAddress.copyFrom(widget.cart().shippingAddress().toJSON(), widget
                .shippingCountriesPriceListGroup());
            shippingAddress.resetModified();
            widget.shippingAddress(shippingAddress);
          } else if (widget.order() != undefined
                  && widget.order().shippingAddress() != undefined
                  && widget.order().shippingAddress() != '') {
            var shippingAddress = new Address('checkout-shipping-address',
                      widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget
                          .defaultShippingCountry());
            shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
            widget.shippingAddress(shippingAddress);
            widget.shippingAddress().copyFrom(widget.order().shippingAddress().toJSON(), widget.shippingCountriesPriceListGroup());
            var shippingMethod = widget.cart().shippingMethod();
            widget.selectedShippingValue(shippingMethod);
          }
          // Otherwise use the user's shipping address if set
          else if (widget.user().loggedIn() === true
              && widget.user().updatedShippingAddress) {
            var shippingAddresses = [];
            for (var k = 0; k < widget.user().updatedShippingAddressBook.length; k++) {
              var shippingAddress = new Address('user-shipping-address', widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget.defaultShippingCountry());
              shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
              shippingAddress.copyFrom(widget.user().updatedShippingAddressBook[k], widget.shippingCountriesPriceListGroup());
              // Save shipping address JS object to Address object.
              shippingAddress.resetModified();

              if (shippingAddress.isValid()) {
                shippingAddresses.push(shippingAddress);
              }
              if (shippingAddress.isDefaultAddress() && shippingAddress.isValid()) {
                widget.shippingAddress(shippingAddress);
                widget.selectedShippingValue(null);
                widget.cart().shippingMethod('');
                widget.shippingmethods().shippingOptions.removeAll();
                widget.showShippingOptionDropDown(true);
              }
            }
            widget.user().shippingAddressBook(shippingAddresses);
            if (widget.shippingAddress() && !widget.shippingAddress().isValid() && widget.user().shippingAddressBook()[0]) {
              widget.shippingAddress().copyFrom(widget.user().shippingAddressBook()[0].toJSON(), widget.shippingCountriesPriceListGroup());
              
              var shippingAddressWithProductIDs = {};
              shippingAddressWithProductIDs[CCConstants.SHIPPING_ADDRESS_FOR_METHODS] = widget.shippingAddress();
              shippingAddressWithProductIDs[CCConstants.PRODUCT_IDS_FOR_SHIPPING] = widget.cart().getProductIdsForItemsInCart();
              widget.cart().updateShippingAddress.bind(shippingAddressWithProductIDs)();
            }
            if (widget.shippingAddress() && !widget.shippingAddress().isValid()) {
              //create a blank address if the user is logged in and do not have any valid profile addresses
              var shippingAddress = new Address('blank-shipping-address',
                 widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget
                   .defaultShippingCountry());
              shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
                shippingAddress.resetModified();
              widget.shippingAddress(shippingAddress);
              widget.checkPreviousAddressValidity(widget);
              var previousSelectedCountryRegion = null;
              try {
                previousSelectedCountryRegion = storageApi.getInstance().getItem("selectedCountryRegion");
                if (previousSelectedCountryRegion && typeof previousSelectedCountryRegion == 'string') {
                  previousSelectedCountryRegion = JSON.parse(previousSelectedCountryRegion);
                }
              } catch(pError) {
              }
              if (previousSelectedCountryRegion && !widget.showPreviousAddressInvalidError()) {
                widget.shippingAddress().selectedCountry(previousSelectedCountryRegion.selectedCountry);
                widget.shippingAddress().selectedState(previousSelectedCountryRegion.selectedState);
                widget.previousSelectedCountryValid(false);
              } else if (previousSelectedCountryRegion && widget.previousSelectedCountryValid() && widget.showPreviousAddressInvalidError()) {
                widget.shippingAddress().selectedCountry(previousSelectedCountryRegion.selectedCountry);
                widget.showPreviousAddressInvalidError(false);
                widget.previousSelectedCountryValid(false);
              } else {
                widget.showPreviousAddressInvalidError(false);
              }
            }
          }
          // Otherwise provide blank shipping address if no user address is
          // available
          else {
            var shippingAddress = new Address('blank-shipping-address',
                widget.ErrorMsg, widget, widget.shippingCountriesPriceListGroup(), widget
                    .defaultShippingCountry());
            shippingAddress.countriesList(widget.shippingCountriesPriceListGroup());
            shippingAddress.resetModified();
            widget.checkPreviousAddressValidity(widget);
            var previousSelectedCountryRegion = null;
            try {
              previousSelectedCountryRegion = storageApi.getInstance().getItem("selectedCountryRegion");
              if (previousSelectedCountryRegion && typeof previousSelectedCountryRegion == 'string') {
                previousSelectedCountryRegion = JSON.parse(previousSelectedCountryRegion);
              }
            } catch(pError) {
            }
            if (previousSelectedCountryRegion && !widget.showPreviousAddressInvalidError()) {
              shippingAddress.selectedCountry(previousSelectedCountryRegion.selectedCountry);
              shippingAddress.selectedState(previousSelectedCountryRegion.selectedState);
              widget.previousSelectedCountryValid(false);
            } else if (previousSelectedCountryRegion && widget.previousSelectedCountryValid() && widget.showPreviousAddressInvalidError()) {
              shippingAddress.selectedCountry(previousSelectedCountryRegion.selectedCountry);
              widget.showPreviousAddressInvalidError(false);
              widget.previousSelectedCountryValid(false);
            } else if (widget.showPreviousAddressInvalidError()) {
              widget.showPreviousAddressInvalidError(false);
            }
            widget.shippingAddress(shippingAddress);
          }
         //Refer to gloabl shippingOptions, and populate the selectedShippingValue accordingly either from cart or default.
         widget.handleShippingMethodsLoaded();
         if (widget.shippingAddress().validateForShippingMethod()) {
           widget.showShippingOptionDropDown(true);
         }

          countryChangeSubscriber = widget.shippingAddress().country.subscribe(widget.notifyListenersOfShippingAddressUpdate);
          stateChangeSubscriber = widget.shippingAddress().state.subscribe(widget.notifyListenersOfShippingAddressUpdate);
          postalCodeChangeSubscriber = widget.shippingAddress().postalCode.subscribe(widget.notifyListenersOfShippingAddressUpdate);
          
        },

        /**
         * Called to initialise resource dependents
         */
        initResourceDependents : function() {
          var widget = this;

          // Listen for changes in the shipping address's properties
          widget.shippingAddress.extend({
            propertyWatch : widget.shippingAddress
          });
        },
        checkPreviousAddressValidity: function(widget) {
          var previousSelectedCountryRegion = null;
          try {
            previousSelectedCountryRegion = storageApi.getInstance().getItem("selectedCountryRegion");
            if (previousSelectedCountryRegion && typeof previousSelectedCountryRegion == 'string') {
              previousSelectedCountryRegion = JSON.parse(previousSelectedCountryRegion);
            }
          }
          catch(pError) {
          }
          if (previousSelectedCountryRegion) {
            var shippingCountries = widget.shippingCountriesPriceListGroup();
            for (var k = 0; k < shippingCountries.length; k++) {
              if (previousSelectedCountryRegion.selectedCountry === shippingCountries[k].countryCode) {
                widget.previousSelectedCountryValid(true);
                var regions = shippingCountries[k].regions;
                for (var j = 0; j < regions.length; j++) {
                  if (previousSelectedCountryRegion.selectedState === regions[j].abbreviation) {
                    break;
                  }
                }
                if (j < regions.length) {
                  break;
                }
              }
            }
            if (k === shippingCountries.length) {
              //show error message that previously entered shipping address is now not valid and clear local storage
              widget.showPreviousAddressInvalidError(true);
            } else {
              widget.showPreviousAddressInvalidError(false);
            }
          }
        },
      }; // end of return call
    } // end of function
);
