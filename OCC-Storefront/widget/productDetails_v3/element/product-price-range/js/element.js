define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['jquery', 'knockout', 'pubsub'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function ($, ko, pubsub) {
    "use strict";

    return {
      elementName: 'price-range',
      minPrice: ko.observable(),
      maxPrice: ko.observable(),

      /**
       * Populate this element's minPrice and maxPrice observables with the product's
       * current price range values. Also subscribe to the PRODUCT_CHANGED_PRICE event
       * so that the price range data can be updated appropriately.
       */
      onLoad: function (widget) {
        this.populatePrices(widget);

        // This subscription will ensure that when the user navigates to another product,
        // or the is a single variant SKU, the price range data will be updated.
        $.Topic(pubsub.topicNames.PRODUCT_PRICE_CHANGED).subscribe(function(data) {
          this.populatePrices(widget);
        }.bind(this))

      },

      /**
       * Populate the minPrice and maxPrice observables.
       */
      populatePrices: function(widget) {
        this.minPrice(widget.product().minPrice);
        this.maxPrice(widget.product().maxPrice);
      }

    };
  });
