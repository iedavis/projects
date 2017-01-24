define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['jquery', 'knockout', 'ccConstants', 'pubsub'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function ($, ko, CCConstants, PubSub) {
    "use strict";

    return {
      elementName: 'payULatamGateway',
      
      merchantId:   ko.observable(''),
      accountId:   ko.observable(''),
      description:   ko.observable(''),
      referenceCode:   ko.observable(''),
      amount:   ko.observable(0),
      tax:   ko.observable(0),
      taxReturnBase:   ko.observable(0),
      currency:   ko.observable(''),
      signature:   ko.observable(''),
      algorithmSignature: ko.observable(''),
      test:   ko.observable(''),
      buyerEmail:   ko.observable(''),
      responseUrl:   ko.observable(''),
      confirmationUrl:   ko.observable(''),
      showPayULatam: ko.observable(false),
      buyerLanguage: ko.observable(''),
      details: {},
      onLoad: function(widget) {
        var self = this;
        if (((widget.order().paymentDetails().enabledTypes).indexOf(CCConstants.PAYULATAM_CHECKOUT_TYPE) != -1) && (widget.order().billingAddress()) &&
            ((widget.order().paymentDetails().payULatamCountryList).indexOf(widget.order().billingAddress().selectedCountry()) != -1)) {
          self.showPayULatam(true);
        } else {
          self.showPayULatam(false);
        }

        widget.order().billingAddress.subscribe(function() {
            if (((widget.order().paymentDetails().enabledTypes).indexOf(CCConstants.PAYULATAM_CHECKOUT_TYPE) != -1) && 
                ((widget.order().paymentDetails().payULatamCountryList).indexOf(widget.order().billingAddress().selectedCountry()) != -1)) {
              self.showPayULatam(true);
            } else {
              self.showPayULatam(false);
            }
          });

        /**
         * Inject the form values into the form in the frame.
         */
        widget.injectFormValues = function(details) {
          var keys = Object.keys(details);
          for (var i=0; i<keys.length; i++) {
            $("#"+keys[i]).val(details[keys[i]]);
          }
        };

        /**
         * Inject the form values into the form in the frame.
         */
        widget.injectActionURL = function(url) {
          $("#payULatam_payment_form")[0].setAttribute('action', url);
        };

        /**
         * Post the form in the frame.
         */
        widget.postForm = function() {
          $("#payULatam_payment_form")[0].submit();
        };
      
        /**
         * Fill the payment form fields and submit
         */
        $.Topic(PubSub.topicNames.PAYULATAM_WEB_CHECKOUT).subscribe(function() {
          var data = this;
          
          if (data.merchantId) {
            widget.payULatamGateway.details.merchantId = data.merchantId;  
          }
          if (data.accountId) {
            widget.payULatamGateway.details.accountId = data.accountId;
          }
          widget.payULatamGateway.details.description = "Store";
          if (data.paymentId) {
            widget.payULatamGateway.details.referenceCode = data.paymentId;
          }
          widget.payULatamGateway.details.buyerLanguage = widget.locale();
          widget.payULatamGateway.details.amount = widget.order().paymentGateway().amount;
          widget.payULatamGateway.details.tax = 0;
          widget.payULatamGateway.details.taxReturnBase = 0;
          widget.payULatamGateway.details.currency = widget.order().cart().currency.currencyCode;
          // to be able to use test credit cards ?
          if (data.environment && data.environment == CCConstants.ENV_PRODUCTION) {
            widget.payULatamGateway.details.test = "0";
          } else {
            widget.payULatamGateway.details.test = "1";
          }
          if (widget.order().user().loggedIn()) {
            widget.payULatamGateway.details.buyerEmail = widget.order().emailAddress();
          } else {
            widget.payULatamGateway.details.buyerEmail = widget.order().guestEmailAddress();
          }
          if (data.responseUrl) {
            widget.payULatamGateway.details.responseUrl = data.responseUrl;
          }
          if (data.confirmationUrl) {
            widget.payULatamGateway.details.confirmationUrl = data.confirmationUrl;
          }          
          widget.payULatamGateway.details.signature = data.signature;
          widget.payULatamGateway.details.algorithmSignature = data.signatureAlgorithm;
          widget.injectFormValues(widget.payULatamGateway.details);
          if (data.sopUrl) {
            widget.injectActionURL(data.sopUrl);
          }
          
          widget.postForm();
        });
        $.Topic(PubSub.topicNames.BILLING_ADDRESS_POPULATED).subscribe(function() {
          widget.order().billingAddress().selectedCountry.subscribe(function(newValue) {
            if (((widget.order().paymentDetails().enabledTypes).indexOf(CCConstants.PAYULATAM_CHECKOUT_TYPE) != -1) && 
                ((widget.order().paymentDetails().payULatamCountryList).indexOf(newValue) != -1)) {
              self.showPayULatam(true);
            } else {
              self.showPayULatam(false);
            }
          });
        });
      },
      
      /**
       * Handle checkout when PayU Latam is clicked
       */
      handleCheckoutWithPayULatam: function() {
        this.order().isPayULatamCheckout(true);
        this.order().isCashPayment(false);
        this.order().handlePlaceOrder();
      }

    };
  }
);
