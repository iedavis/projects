/**
 * @fileoverview Checkout Cart Summary Widget. 
 * 
 */
/*global $ */
/*global define */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'ccLogger', 'CCi18n','notifier', 'ccConstants'], 
  
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, pubsub, log, CCi18n, notifier, CCConstants) {
  
    "use strict";
  
    return {
      onLoad: function(widget) {
        widget.noOfItemsToDisplay(parseInt(widget.noOfItemsToDisplay()));
        // Checks if the cart is editable for the order.
        widget.isCartEditable = function() {
          if (widget.cart().currentOrderId() && (widget.cart().currentOrderState() == CCConstants.QUOTED_STATES)) {
            return false;
          }
          return true;
        };
      }
    }; 
  }
);
