/**
 * @fileoverview Shopping Cart Summary Widget.
 * 
 */
define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'viewModels/giftProductListingViewModel', 'ccConstants', 'notifier',
      'CCi18n', 'jquery', 'viewModels/integrationViewModel'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, pubsub, giftProductListingViewModel, CCConstants, notifier, CCi18n, $, integrationViewModel) {
  
    "use strict";

    return {

      // This will hold the data displayed in gift selection modal
      currentGiftChoice: ko.observable(),
      selectedGiftSku: ko.observable(),
      cartItem: {},

      // Sends a message to the cart to remove this product
      handleRemoveFromCart: function() {
        $.Topic(pubsub.topicNames.CART_REMOVE).publishWith(
          this.productData(),[{"message":"success", "commerceItemId": this.commerceItemId}]);
      },
      focusOnField : function(data) {
        var field = data.source;
        field.focus();
      },

      updateQuantity: function(data, event, id) {
        
        if('click' === event.type || ('keypress' === event.type && event.keyCode === 13)) {
          if(data.updatableQuantity && data.updatableQuantity.isValid()) {
        	
            $.Topic(pubsub.topicNames.CART_UPDATE_QUANTITY).publishWith(
            data.productData(),[{"message":"success", "commerceItemId": data.commerceItemId}]);
        	 
            var button = $('#' + id);
            button.focus();
            button.fadeOut();
          }
        } else {
          this.quantityFocus(data, event);
        }
        
        return true;
      },

      quantityFocus: function (data, event) {   
        var field = $('#' + event.target.id);
        var button = field.siblings("p").children("button");
        button.fadeIn();
      },

      getGiftChoices: function() {
        giftProductListingViewModel.prototype.getGiftProductChoices(this);
      },

      changeGiftChoice: function(widget) {
        widget.cartItem = {
          "catRefId": this.catRefId,
          "productId": this.productId,
          "quantity": this.quantity()
        };

        // This data is needed to add giftWithPurchaseSelections for the new item which is selected.
        var giftData = {};
        giftData.giftWithPurchaseIdentifier = this.giftData[0].giftWithPurchaseIdentifier;
        giftData.promotionId = this.giftData[0].promotionId;
        giftData.giftWithPurchaseQuantity = this.giftData[0].giftWithPurchaseQuantity;
        widget.currentGiftChoice(giftData);

        // While changing the gift, add giftWithPurchaseSelections info to the product being modified
        widget.addGiftWithPurchaseSelectionsToItem(this);

        var getGiftChoiceData = {};
        getGiftChoiceData.giftWithPurchaseType = this.giftData[0].giftWithPurchaseType;
        getGiftChoiceData.giftWithPurchaseDetail = this.giftData[0].giftWithPurchaseDetail;
        getGiftChoiceData.id = null;
        giftProductListingViewModel.prototype.getGiftProductChoices(getGiftChoiceData);
      },

      // Adds giftWithPurchaseSelections information to the cart item
      addGiftWithPurchaseSelectionsToItem: function(item) {
        var giftWithPurchaseSelections = [];
        var data = {};
        data.giftWithPurchaseIdentifier = item.giftData[0].giftWithPurchaseIdentifier;
        data.promotionId = item.giftData[0].promotionId;
        data.giftWithPurchaseQuantity = item.giftData[0].giftWithPurchaseQuantity;
        data.catRefId = item.catRefId;
        data.productId = item.productId;
        giftWithPurchaseSelections.push(data);
        item.giftWithPurchaseSelections = giftWithPurchaseSelections;
      },

      handleGiftAddToCart: function () {
        // 'this' is widget view model

        var variantOptions = this.currentGiftChoice().giftChoice.variantOptionsArray;
        //get the selected options, if all the options are selected.
        var selectedOptions = this.getSelectedSkuOptions(variantOptions);
        var selectedOptionsObj = { 'selectedOptions': selectedOptions };
        var newProduct = $.extend(true, {}, this.currentGiftChoice().giftChoice.product, selectedOptionsObj);
        if (variantOptions.length > 0) {
          //assign only the selected sku as child skus
          newProduct.childSKUs = [this.selectedGiftSku()];
        }

        //If the gift being added is already present in the cart, do not trigger pricing
        if (this.cartItem && this.cartItem.catRefId && this.cartItem.productId) {
          var item = this.cart().getCartItem(this.cartItem.productId, this.cartItem.catRefId);
          if (item != null && item.giftWithPurchaseCommerceItemMarkers.length && newProduct.id == this.cartItem.productId
              && newProduct.childSKUs[0].repositoryId == this.cartItem.catRefId) {
            this.cartItem = {};
            this.hideGiftSelectionModal();
            return;
          }
        }

        // add gwp selections in the request
        newProduct.giftProductData = {
          "giftWithPurchaseIdentifier": this.currentGiftChoice().giftWithPurchaseIdentifier,
          "promotionId": this.currentGiftChoice().promotionId,
          "giftWithPurchaseQuantity" : (this.currentGiftChoice().giftWithPurchaseQuantityAvailableForSelection ?
              this.currentGiftChoice().giftWithPurchaseQuantityAvailableForSelection : this.currentGiftChoice().giftWithPurchaseQuantity)
        };

        newProduct.orderQuantity = newProduct.giftProductData.giftWithPurchaseQuantity;

        // Triggers price call
        this.cart().addItem(newProduct);
        this.cartItem = {};
        this.hideGiftSelectionModal();
      },

      // hide gift selection modal
      hideGiftSelectionModal: function() {
        $('#CC-giftSelectionpane').modal('hide');
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
      },

      // this method  returns a map of all the options selected by the user for the product
      getSelectedSkuOptions : function(variantOptions) {
        var selectedOptions = [];
        for (var i = 0; i < variantOptions.length; i++) {
          if (!variantOptions[i].disable()) {
            selectedOptions.push({'optionName': variantOptions[i].optionDisplayName, 'optionValue': variantOptions[i].selectedOption().key, 'optionId': variantOptions[i].actualOptionId, 'optionValueId': variantOptions[i].selectedOption().value});
          }
        }
        return selectedOptions;
      },

      // Checks if all variant values are selected
      allOptionsSelected: function () {
          var allOptionsSelected = true;
          if (!this.currentGiftChoice() || !this.currentGiftChoice().giftChoice) {
            allOptionsSelected = false;
          } else if (this.currentGiftChoice().giftChoice.variantOptionsArray.length > 0) {
            var variantOptions = this.currentGiftChoice().giftChoice.variantOptionsArray;
            for (var i = 0; i < variantOptions.length; i++) {
              if (! variantOptions[i].selectedOption.isValid() && !variantOptions[i].disable()) {
                allOptionsSelected = false;
                this.selectedGiftSku(null);
                break;
              }
            }
            
            if (allOptionsSelected) {
              // get the selected sku based on the options selected by the user
              var selectedSKUObj = this.getSelectedSku(variantOptions);
              if (selectedSKUObj === null) {
                return false;
              }
              this.selectedGiftSku(selectedSKUObj);
            }
            this.refreshSkuStockStatus(this.selectedGiftSku());
          }
          
          return allOptionsSelected;
        },

        //refreshes the stockstatus based on the variant options selected
        refreshSkuStockStatus : function(selectedSKUObj) {
          var key;
          var product = this.currentGiftChoice().giftChoice;
          if (selectedSKUObj === null) {
            key = 'stockStatus';
          } else {
            key = selectedSKUObj.repositoryId;
          }
          var stockStatusMap = product.stockStatus();
          for (var i in stockStatusMap) {
            if (i == key) {
              if (stockStatusMap[key] == CCConstants.IN_STOCK) {
                product.inStock(true); 
              } else {
                product.inStock(false);
              }
              return;
            }
          }
        },

        //this method returns the selected sku in the product, Based on the options selected
        getSelectedSku : function(variantOptions) {
          var childSkus = this.currentGiftChoice().giftChoice.product.childSKUs;
          var selectedSKUObj = {};
          for (var i = 0; i < childSkus.length; i++) {
            selectedSKUObj =  childSkus[i];
            for (var j = 0; j < variantOptions.length; j++) {
              if ( !variantOptions[j].disable() && childSkus[i].dynamicPropertyMapLong[variantOptions[j].optionId] != variantOptions[j].selectedOption().value ) {
                selectedSKUObj = null;
                break;
              }
            }
            if (selectedSKUObj !== null) {
              return selectedSKUObj;
            }
          }
          return null;
        },

      onLoad: function(widget) {
        var self = this;

        $.Topic(pubsub.topicNames.GET_GIFT_CHOICES_SUCCESSFUL).subscribe(function() {
          // Currently only one product is returned as a gift
          var product = this[0].products[0];
          var placeHolderItemId = this[1];
          
          if (placeHolderItemId != null) {
            for (var i = 0 ; i < widget.cart().placeHolderItems().length; i++) {
              if (placeHolderItemId == widget.cart().placeHolderItems()[i].id) {
                widget.cart().placeHolderItems()[i].giftChoice = product;
                widget.cart().placeHolderItems()[i].giftChoice.itemTotal = 0;
                widget.currentGiftChoice(widget.cart().placeHolderItems()[i]);
                break;
              }
            }
          } else { // A request was made by shopper to change the gift choice
            product.itemTotal = 0;
            var giftData = widget.currentGiftChoice();
            giftData.giftChoice = product;
            widget.currentGiftChoice(giftData);
          }
          
          // Get stock status of the product
          product.stockStatus.subscribe(function(newValue) {
            if (product.stockStatus().stockStatus === CCConstants.IN_STOCK) {
              product.inStock(true);
            } else {
              product.inStock(false);
              widget.hideGiftSelectionModal();
              notifier.sendError('shoppingCartSummary', CCi18n.t('ns.shoppingcartsummary:resources.gwpItemNotAvailable'), true);
            }
            product.showStockStatus(true);
          });
          var firstchildSKU = product.childSKUs()[0];
          if (firstchildSKU) {
            var skuId = firstchildSKU.repositoryId();
            if (product.variantOptionsArray.length > 0) {
              skuId = '';
            }
            product.showStockStatus(false);
            var catalogId = null;
            if (widget.user().catalog) {
              catalogId = widget.user().catalog.repositoryId;
            }
            product.getAvailability(product.id(), skuId, catalogId);
          } else {
            product.showStockStatus(true);
            product.inStock(false);
          }
        });

        // Need to remove giftWithPurchaseSelections from cart items if pricing was not triggered
        $(document).on('hidden.bs.modal', '#CC-giftSelectionpane', function () {
          ko.utils.arrayForEach(widget.cart().items(), function(item) {
            if (item.giftWithPurchaseSelections) {
              delete item.giftWithPurchaseSelections;
            }
          });
        });

        // <select> tag in bootstrap modal, changes the modal's position. So always displaying the modal on the top of the browser.
        if (navigator.userAgent.toLowerCase().indexOf('safari') > -1 && navigator.userAgent.toLowerCase().indexOf('chrome') == -1) {
          $(document).on('show.bs.modal', '#CC-giftSelectionpane', function(){
            $('body').scrollTop(0);
          });
          $(document).on('shown.bs.modal', '#CC-giftSelectionpane', function(){
            $('#CC-giftSelectionpane').find('select').blur(function(){
              $('body').scrollTop(0);
            });
          });
        }

        widget.handlePlaceHolderRemove = function() {
          widget.cart().removePlaceHolderFromCart(this);
        };

        $.Topic(pubsub.topicNames.GIFT_CHOICES_NOT_AVAILABLE).subscribe(function() {
          widget.hideGiftSelectionModal();
          notifier.sendError('shoppingCartSummary', CCi18n.t('ns.shoppingcartsummary:resources.gwpItemNotAvailable'), true);
        });
        $.Topic(pubsub.topicNames.CART_ADD_SUCCESS_CPQ).subscribe(self.handleClose);
      },
      // Returns the reconfiguration frame document.
      getReconfigurationFrameDocument : function() {
        var iframe = document.getElementById("cc-cpqReconfiguration-frame");
        if (iframe) {
          return iframe.contentDocument || iframe.contentWindow.document;
        }
      },

      // Returns the reconfiguration form.
      getReconfigurationForm : function() {
        var iframeDocument = this.getReconfigurationFrameDocument();
        if (iframeDocument) {
          return iframeDocument.getElementById("reconfiguration_form");
        }
      },

      // Reloads the reconfiguration frame.
      reloadReconfigurationFrame : function() {
        var iframe = document.getElementById("cc-cpqReconfiguration-frame");
        if (iframe) {
          iframe.src = iframe.src;
        }
      },

      // Handle the reconfigure button click on a line item
      handleReconfigure: function(widget) {
         // Handle opening the reconfiguration i-frame here
         var self=this;
         integrationViewModel.getInstance().iFrameId = "cc-cpqReconfiguration-frame";
         var reconfigurationDetails = new Object();
         reconfigurationDetails.configuratorId = self.configuratorId;
         reconfigurationDetails.locale = widget.locale();
         reconfigurationDetails.currency = widget.site()
             .selectedPriceListGroup().currency.currencyCode;

       // Injecting the reconfiguration form values
         var keys = Object.keys(reconfigurationDetails);
         var frameDocument = widget.getReconfigurationFrameDocument();
         if (frameDocument) {
           for (var i = 0; i < keys.length; i++) {
             var element = frameDocument.getElementById(keys[i]);
             if (element) {
               element.value = reconfigurationDetails[keys[i]];
             }
           }
         }
         widget.getReconfigurationForm().action = widget.site().extensionSiteSettings.CPQConfigurationSettings.ReConfigurationURL;
         widget.getReconfigurationForm().submit();
         $('#cc-re-cpqmodalpane').modal({
           backdrop: 'static',
           keyboard: false
         });
         $('#cc-re-cpqmodalpane').modal('show');
         $('#cc-re-cpqmodalpane').on('hidden.bs.modal', function() {
             widget.reloadReconfigurationFrame();
         });
      },

      handleClose: function() {
          // Close the reconfiguration modal.
          $('#cc-re-cpqmodalpane').modal('hide');
      }  
    };
  }
);
