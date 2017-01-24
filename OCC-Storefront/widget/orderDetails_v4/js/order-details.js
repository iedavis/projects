/**
 * @fileoverview Order History Details.
 * 
 */
define(
 
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'notifier', 'CCi18n', 'ccConstants', 'navigation'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub, notifier, CCi18n, CCConstants, navigation) {
  
    "use strict";

    return {
      claimedCouponMultiPromotions: ko.observableArray(),
        onLoad : function(widget) {

          widget.isGiftCardUsed = ko.computed(
                  function() {
                    if (widget.orderDetails()) {
                      var payments = widget.orderDetails().payments;
                      for ( var i = 0; i < payments.length; i++) {
                        if (payments[i].paymentMethod == CCConstants.GIFT_CARD_PAYMENT_TYPE) {
                            return true;
                        }
                      }  
                    }
                  }, widget);
          widget.isScheduledOrder = ko.computed(
              function() {
                if (widget.orderDetails()) {
                  if(widget.orderDetails().scheduledOrderName){
                    return true;
                  }
                }
              }, widget);
          
          widget.totalAmount = ko.computed(
                  function() {
                    if (widget.orderDetails()) {
                      var payments = widget.orderDetails().payments;
                      var remainingTotal = 0;
                      for ( var i = 0; i < payments.length; i++) {
                        if (payments[i].paymentMethod != CCConstants.GIFT_CARD_PAYMENT_TYPE) {
                          remainingTotal += payments[i].amount;
                        }
                      }
                    }
                    return remainingTotal;
                  }, widget);
        },  
      
      beforeAppear: function (page) {
        var widget = this;
        if (!widget.orderDetails() || !widget.user().loggedIn() || widget.user().isUserSessionExpired()) {
          navigation.doLogin(navigation.getPath(), widget.links().home.route);
        }
        widget.claimedCouponMultiPromotions.splice(0);
        if (widget.orderDetails()) {
          if (widget.orderDetails().discountInfo) {
            for (var prop in widget.orderDetails().discountInfo.claimedCouponMultiPromotions) {
              var promotions = widget.orderDetails().discountInfo.claimedCouponMultiPromotions[prop];
              var couponMultiPromotion = [];
              couponMultiPromotion.code = prop;
              couponMultiPromotion.promotions = promotions;
              widget.claimedCouponMultiPromotions.push(couponMultiPromotion);
            }
          }
        }
        
        widget.resetOrderDetails = function() {
          if (!(arguments[0].data.page.orderDetails && arguments[0].data.page.orderDetails.id)) {
            widget.orderDetails(null);
            $.Topic(pubsub.topicNames.PAGE_LAYOUT_LOADED).unsubscribe(widget.resetOrderDetails);
            $.Topic(pubsub.topicNames.PAGE_METADATA_CHANGED).unsubscribe(widget.resetOrderDetails);
          }
        };
        
        $.Topic(pubsub.topicNames.PAGE_LAYOUT_LOADED).subscribe(widget.resetOrderDetails);
        $.Topic(pubsub.topicNames.PAGE_METADATA_CHANGED).subscribe(widget.resetOrderDetails);
        
      },
      /**
       * Function to get the display name of a state 
       * countryCd - Country Code
       * stateCd - State Code
       */
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
      },
      
      /**
       * Function to get the display name of a state 
       */
      getStateName: function() {
        if (this.orderDetails && this.orderDetails() && this.orderDetails().shippingAddress && this.orderDetails().shippingAddress.regionName) {
          return this.orderDetails().shippingAddress.regionName;
        }
        return "";
      },
      
      /**
       * Function to get the display name of a Country 
       * countryCd - Country Code
       */
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
      
      /**
       * Function to get the display name of a Country 
       */
      getCountryName: function() {
        if (this.orderDetails && this.orderDetails() && this.orderDetails().shippingAddress && this.orderDetails().shippingAddress.countryName) {
          return this.orderDetails().shippingAddress.countryName;
        }
        return "";
      },

      /**
       * Function to check if the order is quoted or not
       */
      isQuoted: function() {
        if (this.orderDetails && this.orderDetails() && (this.orderDetails().state == CCConstants.QUOTED_STATES)) {
          return true;
        }
        return false;
      },

      /**
       * Function to check if the address object contains atleast 1 not null field
       */
      isAddressAvailable: function() {
        if (this.orderDetails && this.orderDetails() && this.orderDetails().shippingAddress) {
          for (var i in this.orderDetails().shippingAddress) {
            if (this.orderDetails().shippingAddress[i]) {
              return true;
            }
          }
          return false;
        }
      }
    }
  }
);
