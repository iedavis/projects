/**
 * @fileoverview GiftCard Widget.
 * 
 * 
 * 
 * This widget is useful for handling GiftCard related functionality.
 * 
 */
define(

    // -------------------------------------------------------------------
    // DEPENDENCIES
    // -------------------------------------------------------------------
    [ 'knockout', 'viewModels/giftCardViewModel', 'pubsub', 'ccConstants',
        'koValidate', 'ccKoValidateRules', 'CCi18n' ],

    // -------------------------------------------------------------------
    // MODULE DEFINITION
    // -------------------------------------------------------------------
    function(ko, GiftCardViewModel, pubsub, CCConstants, koValidate, rules,
        CCi18n) {

      "use strict";

      return {

        giftCardViewModel : ko.observable(),
        isDisplayGiftCardDetails : ko.observable(),
        giftCardErrorMessage : ko.observable(),
        isDispalyErrorPins : ko.observable(false),

        self : this,
        /**
         * beforeAppear function
         */
        beforeAppear : function(page) {
          var widget = this;
          widget.giftCardViewModel().clearPin();
        },

        initResourceDependents : function() {
          var widget = this;

          widget.isValid = function() {

            return (widget.giftCardViewModel().validateNow());

          };
          
          widget.isDisplayGiftCardDetails.subscribe(function(value) {
        	if(!value){
        		widget.giftCardErrorMessage('');
                widget.giftCardViewModel().reset();
        	}  
          });

          widget.resetGiftCard = function() {

            widget.isDisplayGiftCardDetails(false); 
            
          };
          
          widget.handleGiftCardPricingError = function(errorData,giftCardObj) {

        	  giftCardObj.giftCardPin('');
        	  giftCardObj.isApplyGiftCardClicked(false);

        	  widget.giftCardErrorMessage(errorData.message);
        	  
        	  widget.giftCardViewModel().giftCardPin('');
        	  widget.giftCardViewModel().isApplyGiftCardClicked(false);
        	  
        	  
          };
          
          widget.order().isCashPayment.subscribe(function(newValue) {
        	if (newValue) {
              widget.resetGiftCard();
        	}
          });

          widget.order().isInvoicePayment.subscribe(function(paymentTypeInvoice) {
            if (paymentTypeInvoice) {
              widget.resetGiftCard();
            }
          });
          
          widget.order().showSchedule.subscribe(function(newValue) {
            if (newValue) {
              if (!widget.order().paymentDetails().isGiftCardEnabledForScheduledOrder()) {
                widget.resetGiftCard();               
              }
            }
          });
          
          widget.displayErrorPins = function() {
            widget.isDispalyErrorPins(true);
          };

          $.Topic(pubsub.topicNames.GIFTCARD_UPDATE_FROM_CART).subscribe(
              widget.resetGiftCard);
          $.Topic(pubsub.topicNames.GIFTCARD_PRICING_FAILED).subscribe(
              widget.handleGiftCardPricingError);
          $.Topic(pubsub.topicNames.PAGE_CHANGED).subscribe(function(value){
        	  widget.isDispalyErrorPins(false);
          	});
          $.Topic(pubsub.topicNames.SHOW_GIFT_CARD_ERROR_PANEL).subscribe(
              widget.displayErrorPins);
          $.Topic(pubsub.topicNames.GIFTCARD_REAPPLY_PINS).subscribe(function(value){
        	  widget.giftCardErrorMessage(''); 
          	});

        },

        handleReapplyPins : function() {
          var giftCardObj = this;

          if (giftCardObj.validateNow()) {
            giftCardObj.isApplyGiftCardClicked(true);
            $.Topic(pubsub.topicNames.GIFTCARD_REAPPLY_PINS).publish(giftCardObj);
          }
        },

        onLoad : function(widget) {
          widget.giftCardViewModel(new GiftCardViewModel());
        },

        resourcesLoaded : function(widget) {
          widget.initResourceDependents();
        },

        /**
         * This function handles functionality of applying a coupon
         */
        handleApplyGiftCard : function() {
          var widget = this;
          widget.giftCardErrorMessage('');

          if (widget.isValid()) {
            if (widget.giftCardAlreadyApplied(widget.giftCardViewModel()
                .giftCardNumber())) {
              widget.giftCardViewModel().giftCardPin('');
              widget.giftCardErrorMessage(widget
                  .translate(CCConstants.DUPLICATE_GIFTCARD));
              widget.giftCardViewModel().isApplyGiftCardClicked(false);
              
            } else {
              var giftCardObj = new GiftCardViewModel();
              giftCardObj.giftCardNumber(widget.giftCardViewModel()
                  .giftCardNumber());
              giftCardObj.giftCardPin(widget.giftCardViewModel().giftCardPin());
              if(widget.giftCardViewModel().customProperties && (Object.keys(widget.giftCardViewModel().customProperties).length - 1) > 0) {
                giftCardObj.customProperties = widget.giftCardViewModel().customProperties;
              }
              widget.cart().addGiftCard(giftCardObj);
              widget.handleEnableApplyCodeButton();
            }
          }
        },

        /**
         * This function returns true if the couponCode is already applied.
         * 
         */
        giftCardAlreadyApplied : function(giftCardNum) {
          var widget = this;
          var giftCardNum = giftCardNum.trim();
          var alreadyApplied = false;
          var gcards = widget.cart().giftCards();
          for ( var i = 0; i < gcards.length; i++) {
            if (gcards[i].giftCardNumber() == giftCardNum) {
              alreadyApplied = true;
              break;
            }
          }
          return alreadyApplied;
        },

        /**
         * This function is used to handle disable Apply Code button for a
         * specific time when it is clicked and enable again.
         */
        handleEnableApplyCodeButton : function() {
          var widget = this;
          widget.giftCardViewModel().isApplyGiftCardClicked(true);
        }
      };
    });
