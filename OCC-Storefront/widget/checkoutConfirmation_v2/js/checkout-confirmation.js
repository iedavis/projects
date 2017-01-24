/**
 * @fileoverview Order Confirmation Widget. 
 * Displays a confirmation of the order placed by the user.
 */
define(
 
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'CCi18n', 'pubsub', 'notifier', 'ccConstants', 'spinner', 'ccRestClient'], 
  
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, CCi18n, PubSub, notifier, CCConstants, spinner,  ccRestClient) {
  
  "use strict";

  return {
    WIDGET_ID:        "checkoutConfirmation",
    isPending:    ko.observable(false),
    resourcesLoaded: function(widget) {      
      // Create observable to mark the resources loaded, if it's not already there
      if (typeof widget.checkoutResourcesLoaded == 'undefined') {
        widget.checkoutResourcesLoaded = ko.observable(false);
      }
      // Notify the computeds relying on resources
      widget.checkoutResourcesLoaded(true);
    },
            
    onLoad: function(widget) {      
      if (widget.confirmation()) {
        
        // Create observable to mark the resources loaded, if it's not already there
        if (typeof widget.checkoutResourcesLoaded == 'undefined') {
          widget.checkoutResourcesLoaded = ko.observable(false);
        }
        
        // i18n strings required for table summary attributes
        widget.yourOrderText = ko.computed(function() {
          if (widget.checkoutResourcesLoaded()) {
            var messageText = CCi18n.t(
              'ns.checkoutconfirmation:resources.yourOrderText', {}
            );          
            return messageText;
          }
          else {
            return '';
          }
          
        }, widget);
        
        widget.shipToText = ko.computed(function() {
          if (widget.checkoutResourcesLoaded()) {
            var messageText = CCi18n.t(
              'ns.checkoutconfirmation:resources.shipToText', {}
            );          
            return messageText;
          }
          else {
            return '';
          }
          
        }, widget);
        
        widget.shippingMethodText = ko.computed(function() {
          if (widget.checkoutResourcesLoaded()) {
            var messageText = CCi18n.t(
              'ns.checkoutconfirmation:resources.shippingMethodText', {}
            );          
            return messageText;
          }
          else {
            return '';
          }
          
        }, widget);
        
        // Parameterized i18n strings
        widget.orderDate = ko.computed(function() {
          var orderDateString = widget.ccDate(widget.confirmation().creationDate, null, null, CCConstants.MEDIUM);
          return orderDateString;
          
        }, widget);
        
        widget.orderTime = ko.computed(function() {
          var orderTimeString = widget.ccDate(widget.confirmation().creationDate, null, null, CCConstants.TIME);
          return orderTimeString;
          
        }, widget);
        
        widget.thankyouMsg = ko.computed(function() {
          if (widget.checkoutResourcesLoaded()) {
            var linkText = CCi18n.t(
              'ns.checkoutconfirmation:resources.thankyouMsg', 
              {
                orderDate: widget.orderDate(), 
                orderTime: widget.orderTime()
              }
            );          
          return linkText;
          }
          else {
            return '';
          }
          
        }, widget);
        
        widget.orderNumberMsg = ko.computed(function() {
          if (widget.checkoutResourcesLoaded()) {
            var linkText = CCi18n.t(
              'ns.checkoutconfirmation:resources.orderNumberMsg', 
              {
                orderNumber: widget.confirmation().id
              }
            );          
            return linkText;
          }
          else {
            return '';
          }
          
        }, widget);
      }
    },
    
    beforeAppear: function (page) {
      var widget = this;
      if (widget.confirmation().state === CCConstants.PENDING_PAYMENT) {
        widget.isPending(true);
      } else {
        widget.isPending(false);
      }
      
      //remove the spinner
      $('#loadingModal').hide();
      spinner.destroy(0);
      if (widget.user().errorMessageKey() != '' ) {
        notifier.sendError(widget.WIDGET_ID, widget.translate(widget.user().errorMessageKey()), true);
      } else if (widget.user().successMessageKey() != '' ) {
        notifier.sendSuccess(widget.WIDGET_ID, widget.translate(widget.user().successMessageKey()));
      } else if(ccRestClient.getStoredValue(CCConstants.PAYULATAM_CHECKOUT_REGISTRATION) != null){
    	  var regStatus = ccRestClient.getStoredValue(CCConstants.PAYULATAM_CHECKOUT_REGISTRATION);
    	  if(regStatus == CCConstants.PAYULATAM_CHECKOUT_REGISTRATION_SUCCESS){
    		  notifier.sendSuccess(widget.WIDGET_ID,widget.translate('loginSuccessText'));
    	  }
    	  else if(regStatus == CCConstants.PAYULATAM_CHECKOUT_REGISTRATION_FAILURE){
    		  notifier.sendError(widget.WIDGET_ID, widget.translate('loginFailureText'), true);
    	  }
    	  ccRestClient.clearStoredValue(CCConstants.PAYULATAM_CHECKOUT_REGISTRATION);
      }
      widget.user().errorMessageKey('');
      widget.user().successMessageKey('');
    },
    
    getCountryDisplayName: function(countryCd) {
      if (this.shippingCountries()) {
        for (var i in this.shippingCountries()) {
          var countryObj = this.shippingCountries()[i];
          if (countryObj.countryCode === countryCd) {
            return countryObj.displayName;
          }
        }
      }
      return "";
    },
    
    getStateDisplayName: function(countryCd, stateCd) {
      if (this.shippingCountries()) {
        for (var i in this.shippingCountries()) {
          var countryObj = this.shippingCountries()[i];
          if (countryObj.countryCode === countryCd) {
            for (var j in countryObj.regions) {
              var stateObj = countryObj.regions[j];
        	  if (stateObj.abbreviation === stateCd) {
        	    return stateObj.displayName;
              }
            }
          }
        }
      }
      return "";
    }
  };
});
