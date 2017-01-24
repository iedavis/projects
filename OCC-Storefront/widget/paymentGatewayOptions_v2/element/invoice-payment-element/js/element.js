/**
 * @fileoverview Invoice Request Element.
 *
 * This element handles the Invoice Payment related functionality.
 *
 */
define(

    // -------------------------------------------------------------------
    // DEPENDENCIES
    // -------------------------------------------------------------------
    [ 'knockout','ccConstants','pubsub' ],

    // -------------------------------------------------------------------
    // MODULE DEFINITION
    // -------------------------------------------------------------------
    function(ko, CCConstants, pubsub) {

      "use strict";

      return {

        elementName: 'invoicePayment',
        isInvoicePaymentEnabled : ko.observable(false),

        onLoad : function(widget) {
          var self = this;
          
          widget.order().poNumber.extend({ 
              maxLength: {params: CCConstants.PONUMBER_MAXIMUM_LENGTH, 
                          message: widget.translate('maxlengthValidationMsg',{fieldName: widget.translate('purchaseOrderNumberMsg'),maxLength:CCConstants.PONUMBER_MAXIMUM_LENGTH}) }});
        
          /** This method checks whether invoice payment is in the 
           *  enabled payment types list and sets the flag to display
           *  or not to display the element.
           */
          widget.checkInvoiceEnabled = function() {
            if ((widget.order().paymentDetails().enabledTypes).indexOf(CCConstants.INVOICE_PAYMENT_TYPE) != -1) {
              self.isInvoicePaymentEnabled(true);
            } else {
              self.isInvoicePaymentEnabled(false);
              widget.order().isInvoicePayment(false);
            }
          };

          widget.checkInvoiceEnabled();

          /** This method listens to the invoice payment method selection,
           *  If invoice payment is selected it disables the card details.
           */
          widget.order().isInvoicePayment.subscribe(function(invoicePaymentChange) {
            if (widget.order().isInvoicePayment()) {
              widget.order().paymentDetails().isCardPaymentDisabled(true);
              widget.order().paymentDetails().resetPaymentDetails(self);
            } else {
              widget.order().paymentDetails().isCardPaymentDisabled(false);
            }
          });

          /**
           * @function
           * @name isValid
           * Determine whether or not the current widget object is valid
           * based on the validity of its component parts.It validates 
           * PO number length.
           * @return boolean result of validity test
           */   
          widget.isValid = function() {
            return widget.order().poNumber.isValid();
          };
          
          
          /**
           * @function
           * @name validateNow
           * Force all relevant member observables to perform their
           * validation now & display the errors (if any)
           */
          widget.validateNow = function() {
            widget.order().poNumber.isModified(true);
            return(widget.isValid());
          };
          
          /** Validates the PO number length before place order. */
          $.Topic(pubsub.topicNames.CHECKOUT_VALIDATE_NOW).subscribe(function(obj) {
              if (!widget.order().isPaypalVerified() && widget.order().isInvoicePayment()) {
                if(!widget.validateNow()) {
                  $.Topic(pubsub.topicNames.CHECKOUT_NOT_VALID).publishWith(
                    this, [{message: "success"}]);
                }
              }
          });
          
          $.Topic(pubsub.topicNames.CART_UPDATED).subscribe(function(obj) {
            if(widget.order().isInvoicePayment()) {
              widget.order().paymentDetails().isCardPaymentDisabled(true);
              widget.order().paymentDetails().resetPaymentDetails(obj);
            }
          });
        }
      };
    });