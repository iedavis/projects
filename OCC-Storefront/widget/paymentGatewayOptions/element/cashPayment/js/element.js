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
      elementName: 'cashPayment',
      
      isCashPaymentEnabled: ko.observable(false),
      cashEnabledCountries : [],

      onLoad: function(widget) {
        var self = this;
        
        /**
         * This method sets the cash enabled countries from the site settings.
         */

        widget.setCashEnabledCountries = function() {
          for (var key in widget.site().extensionSiteSettings) {
            var setingsObject = widget.site().extensionSiteSettings[key];
            if (setingsObject[CCConstants.PAYMENT_METHOD_TYPES] && 
                setingsObject[CCConstants.PAYMENT_METHOD_TYPES].split(",").indexOf(CCConstants.CASH_PAYMENT_TYPE) != -1 ) {
              if (setingsObject[CCConstants.SELECTED_COUNTRIES]) {
                self.cashEnabledCountries = setingsObject[CCConstants.SELECTED_COUNTRIES];
              }
            }
          }
        };
        
        /**
         * This method decides whether the cash payment option should be enabled or not.
         */ 
        widget.verifyCashPaymentToBeEnabled = function(country) {
          if (((widget.order().paymentDetails().enabledTypes).indexOf(CCConstants.CASH_PAYMENT_TYPE) != -1) && 
              ((self.cashEnabledCountries).indexOf(country) != -1)) {
            self.isCashPaymentEnabled(true);
          } else {
            self.isCashPaymentEnabled(false);
            widget.order().isCashPayment(false);
          }
        };
        
        widget.setCashEnabledCountries();
        
        $.Topic(PubSub.topicNames.SHIPPING_ADDRESS_POPULATED).subscribe(function() {
          widget.verifyCashPaymentToBeEnabled(widget.order().shippingAddress().selectedCountry());
          widget.order().shippingAddress().selectedCountry.subscribe(function(newValue) {
            widget.verifyCashPaymentToBeEnabled(newValue);
          });
        });
        
        widget.order().isCashPayment.subscribe(function(newValue) {
          if (widget.order().isCashPayment()) {
            widget.order().paymentDetails().isCardPaymentDisabled(true);
            widget.order().paymentDetails().resetPaymentDetails(self);
          } else {
            widget.order().paymentDetails().isCardPaymentDisabled(false);
          }
        });       
      }
    };
  }
);
