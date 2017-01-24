/**
 * @fileoverview Checkout Payment Details Widget. 
 * 
 */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'ccConstants', 'pubsub', 'koValidate', 'notifier', 
   'storeKoExtensions', 'ccKoValidateRules', 'CCi18n'], 
  
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, CCConstants, pubsub, koValidate, notifier,
           storeKoExtensions, rules, CCi18n) {
  
    "use strict";
  
    return {
      
      getMonths: function() {
        var widget = this;
        widget.order().paymentDetails().monthList().splice(0);
        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '01',
            month: widget.translate('januaryText')
          }),
          value: '01'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '02',
            month: widget.translate('februaryText')
          }),
          value: '02'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '03',
            month: widget.translate('marchText')
          }),
          value: '03'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '04',
            month: widget.translate('aprilText')
          }),
          value: '04'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '05',
            month: widget.translate('mayText')
          }),
          value: '05'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '06',
            month: widget.translate('juneText')
          }),
          value: '06'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '07',
            month: widget.translate('julyText')
          }),
          value: '07'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '08',
            month: widget.translate('augustText')
          }),
          value: '08'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '09',
            month: widget.translate('septemberText')
          }),
          value: '09'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '10',
            month: widget.translate('octoberText')
          }),
          value: '10'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '11',
            month: widget.translate('novemberText')
          }),
          value: '11'
        });

        widget.order().paymentDetails().monthList().push({
          name: widget.translate('monthDropDownFormatter', {
            number: '12',
            month: widget.translate('decemberText')
          }),
          value: '12'
        });

      },

    addValidation: function() {
      var widget = this;
      var notificationMsg = widget.translate('paymentDetailsError');
      
      widget.order().paymentDetails().nameOnCard.extend({
          required: {
            params: true,
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('nameOnCardRequired')
          }
        });

      widget.order().paymentDetails().cardType.extend({
          required: {
            params: true,         
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('cardTypeRequired')
          }
        });

      widget.order().paymentDetails().cardNumber.extend({
          required: {
            params: true,
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('cardNumberRequired')
          },
          maxLength: {
            params:  CCConstants.CYBERSOURCE_CARD_NUMBER_MAXIMUM_LENGTH,
            message: widget.translate('cardNumberMaxLength',{maxLength:CCConstants.CYBERSOURCE_CARD_NUMBER_MAXIMUM_LENGTH})
          },
          creditcard: {
            params: {
              iin: widget.order().paymentDetails().cardIINPattern,
              length: widget.order().paymentDetails().cardNumberLength
            },
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('cardNumberInvalid')
          }
        });

      widget.order().paymentDetails().cardCVV.extend({
          required: {
            params: true,
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('cardCVVRequired')
          },
          minLength: {
            params: 3,
            message: widget.translate('cardCVVNumberMinLength')
          },
          maxLength: {
            params: 4,
            message: widget.translate('cardCVVNumberMaxLength')
          },
          number: {
            param: true,
            message: widget.translate('cardCVVNumberInvalid')
          },
          cvv: {
            params: widget.order().paymentDetails().cvvLength,
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('cardCVVInvalid')
          }
        });

      widget.order().paymentDetails().endMonth.extend({
          required: {
            params: true,
            onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
            message: widget.translate('endMonthRequired')
          },
          endmonth: {
            params: widget.order().paymentDetails().endYear,
            message: widget.translate('endMonthInvalid')
          }
        });

      widget.order().paymentDetails().endYear.extend({
            required: {
              params: true,
              onlyIf: function () { return !widget.order().paymentDetails().isCardPaymentDisabled(); },
              message: widget.translate('endYearRequired')
            }
          });

          if (widget.order().paymentDetails().endYearList().length) {
        	  widget.order().paymentDetails().endYear.extend({
              max: {
                params: widget.order().paymentDetails().endYearList()[(widget.order().paymentDetails().endYearList().length - 1)].value,
                message: widget.translate('endYearInvalid')
              },
              min: {
                params: widget.order().paymentDetails().endYearList()[0].value,
                message: widget.translate('endYearInvalid')
              }
            });
          }

    },

      initResourceDependents:function(resources) {
        this.getMonths();
        this.addValidation();
      },

      resourcesLoaded: function(widget) {
        widget.order().paymentDetails().endMonthPlaceholderText(widget.translate('endMonthPlaceholder'));
        widget.order().paymentDetails().endYearPlaceholderText(widget.translate('endYearPlaceholder'));
        widget.order().paymentDetails().cardTypePlaceholderText(widget.translate('cardTypePlaceholder'));
        widget.initResourceDependents(widget.resources);
      },

      beforeAppear: function (page) {
        var widget = this;
        widget.order().paymentDetails().cardCVV(null);
        widget.order().paymentDetails().nameOnCard.isModified(false);
        widget.order().paymentDetails().cardType.isModified(false);
        widget.order().paymentDetails().cardNumber.isModified(false); 
        widget.order().paymentDetails().cardCVV.isModified(false);
        widget.order().paymentDetails().endMonth.isModified(false);
        widget.order().paymentDetails().endYear.isModified(false);
      },
        
      onLoad: function(widget) {
        
        var YEAR_LIST_LENGTH = 20;

        widget.order().paymentDetails().nameOnCard.isData = true;
        widget.order().paymentDetails().cardType.isData   = true;
        widget.order().paymentDetails().cardNumber.isData = true; 
        widget.order().paymentDetails().cardCVV.isData    = true;
        widget.order().paymentDetails().endMonth.isData   = true;
        widget.order().paymentDetails().endYear.isData    = true;
        
        widget.order().paymentDetails().selectedCardType.isData   = true;
        widget.order().paymentDetails().selectedEndMonth.isData   = true;
        widget.order().paymentDetails().selectedEndYear.isData    = true;
        
        widget.order().paymentDetails().cardIINPattern.isData     = true;
        widget.order().paymentDetails().cardNumberLength.isData   = true;
        widget.order().paymentDetails().cvvLength.isData          = true;
        widget.order().paymentDetails().startDateRequired.isData  = true;
		
        widget.order().showSchedule.subscribe(function(newValue) {
          if (newValue) {
            if (!widget.order().paymentDetails().isCardEnabledForScheduledOrder()) {
              widget.order().paymentDetails().resetPaymentDetails(self);
            }
          }
        });
        
        widget.getEndYears = function () {
          var endYear = new Date().getFullYear();
          
          widget.order().paymentDetails().endYearList.removeAll();
          
          for(var i=0; i<YEAR_LIST_LENGTH; i++) {
            widget.order().paymentDetails().endYearList().push({name:endYear, value: endYear});
            ++endYear;
          }
        };

        /**
         * Callback function for use in widget stacks.
         * Triggers internal widget validation.
         * @return true if we think we are OK, false o/w.
         */
        widget.validate = function() {
          widget.order().errorFlag = false;

          widget.order().validateCheckoutPaymentDetails();

          return !widget.order().errorFlag;
        }

        $.Topic(pubsub.topicNames.CART_UPDATED).subscribe(function(obj) {
          if(widget.order().amountRemaining() == 0 || widget.order().isCashPayment()) {
            widget.order().paymentDetails().isCardPaymentDisabled(true);
            widget.order().paymentDetails().resetPaymentDetails(obj);
          }
          else {
        	widget.order().paymentDetails().isCardPaymentDisabled(false);
          }
        });
                
        
        widget.cardClicked = function(card) {
          if(card && card.value !== "") {
            for(var i=0; i<widget.order().paymentDetails().cardTypeList().length; i++) {
              if(widget.order().paymentDetails().cardTypeList()[i].value === card.value) {
                widget.order().paymentDetails().selectedCardType(card.value);
                break;
              }
            }
          }
        };
        
        /**
         * Set up the popover and click handler 
         * @param {Object} widget
         * @param {Object} event
         */
        widget.cvvMouseOver = function(widget, event) {
          // Popover was not being persisted between
          // different loads of the same 'page', so
          // popoverInitialised flag has been removed
          
          // remove any previous handlers
          $('.cvvPopover').off('click');
          $('.cvvPopover').off('keydown');
        
          var options = new Object();
          options.trigger = 'manual';
          options.html = true;              
          
          // the button is just a visual aid as clicking anywhere will close popover
          options.title = widget.translate('cardCVVPopupTitle')
                          + "<button id='cardCVVPopupCloseBtn' class='close btn pull-right'>"
                          + widget.translate('escapeKeyText')
                          + " &times;</button>";
                          
          options.content = widget.translate('cardCVVPopupText');
          
          $('.cvvPopover').popover(options);
          $('.cvvPopover').on('click', widget.cvvShowPopover);
          $('.cvvPopover').on('keydown', widget.cvvShowPopover);
        };
        
        widget.cvvShowPopover = function(e) {
          // if keydown, rather than click, check its the enter key
          if(e.type === 'keydown' && e.which !== CCConstants.KEY_CODE_ENTER) {
            return;
          }
          
          // stop event from bubbling to top, i.e. html
          e.stopPropagation();
          $(this).popover('show');
          
          // toggle the html click handler
          $('html').on('click', widget.cvvHidePopover);
          $('html').on('keydown', widget.cvvHidePopover);
          
          $('.cvvPopover').off('click');
          $('.cvvPopover').off('keydown');
        };
        
        widget.cvvHidePopover = function(e) {
          // if keydown, rather than click, check its the escape key
          if(e.type === 'keydown' && e.which !== CCConstants.KEY_CODE_ESCAPE) {
            return;
          }
          
          $('.cvvPopover').popover('hide');
          
          $('.cvvPopover').on('click', widget.cvvShowPopover);
          $('.cvvPopover').on('keydown', widget.cvvShowPopover);
          
          $('html').off('click');
          $('html').off('keydown');
          
          $('.cvvPopover').focus();
        };
        
        widget.order().paymentDetails().selectedCardType.subscribe(function(newValue) {
          if (widget.order().paymentDetails()) {
        	if(!newValue || newValue === '') {
            widget.order().paymentDetails().cardType(undefined);
            return;
          }
          for(var i=0; i<widget.order().paymentDetails().cardTypeList().length; i++) {
            if(widget.order().paymentDetails().cardTypeList()[i].value === widget.order().paymentDetails().selectedCardType()) {
              // set acard type to pass on
              widget.order().paymentDetails().cardType(widget.order().paymentDetails().cardTypeList()[i].value);
              
              // set validation fields
              widget.order().paymentDetails().cardIINPattern(widget.order().paymentDetails().cardTypeList()[i].iin);      
              widget.order().paymentDetails().cardNumberLength(widget.order().paymentDetails().cardTypeList()[i].length);
              widget.order().paymentDetails().cvvLength(widget.order().paymentDetails().cardTypeList()[i].cvvLength);
              widget.order().paymentDetails().startDateRequired(widget.order().paymentDetails().cardTypeList()[i].startDateRequired);
              break;
            }
          }
          }
        });
        
        widget.selectFromList = function(value, listObs, targetObs) {
          if(!value || value === '') {
            targetObs(undefined);
            return;
          }
          for(var i=0; i<listObs().length; i++) {
            if(listObs()[i].value === value) {
              targetObs(value);
              break;
            }
          }
        };
                
        widget.order().paymentDetails().selectedEndMonth.subscribe(function(newValue) {
        	if (widget.order().paymentDetails()) {
        	widget.selectFromList(newValue, widget.order().paymentDetails().monthList, widget.order().paymentDetails().endMonth);
        	}
        });
        
        widget.order().paymentDetails().selectedEndYear.subscribe(function(newValue) {
        	if (widget.order().paymentDetails()) {
        	widget.selectFromList(newValue, widget.order().paymentDetails().endYearList, widget.order().paymentDetails().endYear);
        	}
        });

        // Initialise values
        // Must be done before adding validation rules
        widget.getEndYears();
        
      },      
    };
  }
);
