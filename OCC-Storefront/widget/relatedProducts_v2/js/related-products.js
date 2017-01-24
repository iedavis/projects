/**
 * ViewModel to show related products on product details page
 * 
 */
define(['knockout', 'ccConstants', 'pageLayout/product' ],

    //-------------------------------------------------------------------
    // MODULE DEFINITION
    //-------------------------------------------------------------------
    function (ko, CCConstants, Product) {

      "use strict";
      var widgetModel;
      
      return {
        itemsPerRowInLargeDesktopView : ko.observable(4),
        itemsPerRowInDesktopView : ko.observable(4),
        itemsPerRowInTabletView : ko.observable(4),
        itemsPerRowInPhoneView : ko.observable(2),
        itemsPerRow : ko.observable(6),
        viewportWidth : ko.observable(),
        viewportMode : ko.observable(),
        isDeskTopMode : ko.observable(false),
        productGroups : ko.observableArray(),
        products : ko.observableArray(),
        spanClass : ko.observable(),
        relatedProductGroups : ko.observableArray(),
        relatedProducts: ko.observableArray([]),
        activeProdIndex: ko.observable(0),
        numberOfRelatedProductsToShow: ko.observable(),
        relatedProdsloaded: ko.observable(false),

        /**
         * Gets called before the widget gets loaded
         */
        beforeAppear: function () {
          var widget = this;
          widget.activeProdIndex(0);
          if(widget && widget.product && widget.product() && widget.product().relatedProducts) {
            widget.relatedProducts(widget.formatRelatedProducts(widget.product().relatedProducts));
          } else {
            widget.relatedProducts([]);
          }
          widget.relatedProdsloaded(true);
        },

        onLoad : function(widget) {
          widget.relatedProducts = ko.observableArray();
          widget.relatedProductGroups = ko.observableArray();

          /**
           * Formats the related products
           */
          widget.formatRelatedProducts = function(pProducts) {
            var formattedProducts = [];
            var productsLength = pProducts.length;
            for (var index = 0; index < productsLength; index++) {
              if (pProducts[index]) {
                formattedProducts.push(new Product(pProducts[index]));
              }
            }
            return formattedProducts;
          };
          
          /**
           * Groups the products based on the viewport
           */
          widget.relatedProductGroups = ko.computed(function() {
            var groups = [];
            if (widget.relatedProducts) {
              widget.numberOfRelatedProductsToShow = widget.numberOfRelatedProducts() < widget.relatedProducts().length ?
            		                                                                    widget.numberOfRelatedProducts() : widget.relatedProducts().length;
              for (var index = 0; index < widget.numberOfRelatedProductsToShow; index++) {
                if (index % widget.itemsPerRow() == 0) {
                  groups.push(ko.observableArray([ widget.relatedProducts()[index] ]));
                } else {
                  groups[groups.length - 1]().push(widget.relatedProducts()[index]);
                }
              }
            }
            return groups;
          }, widget);

          /**
           * Redefines the number of related products to show
           * in carousal when viewport changes
           */
          widget.updateSpanClass = function() {
            var classString ="";
            var phoneViewItems = 0,
            tabletViewItems = 0,
            desktopViewItems = 0,
            largeDesktopViewItems = 0;
            if (this.itemsPerRow() == this.itemsPerRowInPhoneView()) {
              phoneViewItems = 12 / this.itemsPerRow();
            }
            if (this.itemsPerRow() == this.itemsPerRowInTabletView()) {
              tabletViewItems = 12 / this.itemsPerRow();
            }
            if (this.itemsPerRow() == this.itemsPerRowInDesktopView()) {
              desktopViewItems = 12 / this.itemsPerRow();
            }
            if (this.itemsPerRow() == this.itemsPerRowInLargeDesktopView()) {
              largeDesktopViewItems = 12 / this.itemsPerRow();
            }
              
            if (phoneViewItems > 0) {
              classString += "col-xs-" + phoneViewItems;
            }
            if ((tabletViewItems > 0) && (tabletViewItems != phoneViewItems)) {
              classString += " col-sm-" + tabletViewItems;
            }
            if ((desktopViewItems > 0) && (desktopViewItems != tabletViewItems)) {
              classString += " col-md-" + desktopViewItems;
            }
            if ((largeDesktopViewItems > 0) && (largeDesktopViewItems != desktopViewItems)) {
              classString += " col-lg-" + largeDesktopViewItems;
            }
            
            widget.spanClass(classString);
          };
          
          /**
           * Checks the size of the current viewport and sets the viewport and itemsPerRow
           * mode accordingly
           * @param viewporttWidth the width of the selected viewport
           */
          widget.checkResponsiveFeatures = function(viewportWidth) {
            widget.isDeskTopMode(false);
            if (viewportWidth > CCConstants.VIEWPORT_LARGE_DESKTOP_LOWER_WIDTH) {
              if (widget.viewportMode() != CCConstants.LARGE_DESKTOP_VIEW) {
            	widget.viewportMode(CCConstants.LARGE_DESKTOP_VIEW);
                widget.itemsPerRow(widget.itemsPerRowInLargeDesktopView());
              }
            } else if (viewportWidth > CCConstants.VIEWPORT_TABLET_UPPER_WIDTH
                && viewportWidth <= CCConstants.VIEWPORT_LARGE_DESKTOP_LOWER_WIDTH) {
              if (widget.viewportMode() != CCConstants.DESKTOP_VIEW) {
                widget.viewportMode(CCConstants.DESKTOP_VIEW);
                widget.itemsPerRow(widget.itemsPerRowInDesktopView());
              }
            } else if (viewportWidth >= CCConstants.VIEWPORT_TABLET_LOWER_WIDTH
                && viewportWidth <= CCConstants.VIEWPORT_TABLET_UPPER_WIDTH) {
              if (widget.viewportMode() != CCConstants.TABLET_VIEW) {
                widget.viewportMode(CCConstants.TABLET_VIEW);
                widget.itemsPerRow(widget.itemsPerRowInTabletView());
              }
            } else if (widget.viewportMode() != CCConstants.PHONE_VIEW) {
              widget.viewportMode(CCConstants.PHONE_VIEW);
              widget.itemsPerRow(widget.itemsPerRowInPhoneView());
            }
            widget.updateSpanClass();
          };
          
          widget.checkResponsiveFeatures($(window)[0].innerWidth || $(window).width());
          $(window).resize(
            function() {
              widget.checkResponsiveFeatures($(window)[0].innerWidth || $(window).width());
              widget.viewportWidth($(window)[0].innerWidth || $(window).width());
            });

      },

      /**
       * Function to handle the left and right click to move the carousal
       */
      handleCarouselArrows : function(data, event) {
        // Handle left key
        if (event.keyCode == 37) {
          $('#collection-Carousel').carousel('prev');
        }
        // Handle right key
        if (event.keyCode == 39) {
          $('#collection-Carousel').carousel('next');
        }
      }
      
    }


});

