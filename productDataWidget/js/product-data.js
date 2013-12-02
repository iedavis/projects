/**
 * @fileoverview Product Details Widget.
 * 
 * @author jonathan.mayne@oracle.com
 */
define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'product-data',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'ccConstants', 'koValidate', 'imageZoom'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, pubsub, CCConstants, koValidate) {

    "use strict";

    return {
      
     stockStatus: ko.observable(true),
     variantOptionsArray: ko.observableArray([]),
     selectedSku: ko.observable(),
     
     onLoad: function (widget) {
       //make the list and sale price observable.
       // widget.product().listPrice = ko.observable(widget.product().listPrice);
       // widget.product().salePrice = ko.observable(widget.product().salePrice);
       widget.makePricesObservable(widget);
     },
     
     widgetWillAppearOnPage: function (page) {
       var widget = this;
       widget.makePricesObservable(widget);
       this.populateVariantOptions(widget);
       var catalogId = null;
       if (widget.user().catalog) {
         catalogId = widget.user().catalog.repositoryId;
       }
       var id = new Array(widget.product().childSKUs[0].repositoryId, widget.product().id, catalogId);
       this.getAvailability(widget, id);
       this.getPrices(widget, widget.product().id, widget.product().childSKUs[0].repositoryId);
     },
      
     //Get product availability
     getAvailability: function(widget, id) {
       widget.load(CCConstants.ENDPOINT_GET_PRODUCT_AVAILABILITY, id, null,
         //success callback 
         function(data) {
           if(data.stockStatus === 'IN_STOCK'){
             widget.stockStatus(true);
           }else{
             widget.stockStatus(false);
           }
         },
         //error callback
         function(data) {
           widget.stockStatus(false);
         },
         widget);
      },
      
      //Get the product prices
      getPrices: function(widget, productId, skuId) {
        // Not using the sku ID, because currently only use the product level 
        // prices for product details. We will need to update this for options support.
        widget.load(CCConstants.ENDPOINT_GET_PRODUCT_PRICES, productId, null,
          //success callback 
          function(data) {
            if (data.list) {
              widget.product().listPrice(data.list);
            }
            if (data.sale || data.sale === 0) {
              widget.product().salePrice(data.sale);
            }
          },
          //error callback
          function(data) {
            // do nothing
          },
          widget);
      },
      
      //make the list and sale price observable.
      makePricesObservable: function(widget) {
        if (!ko.isObservable(widget.product().listPrice)) {
          widget.product().listPrice = ko.observable(widget.product().listPrice);
        }
        if (!ko.isObservable(widget.product().salePrice)) {
          widget.product().salePrice = ko.observable(widget.product().salePrice);
        }
        widget.loaded(true);        
      },
      
      // Handles loading a default 'no-image' as a fallback
      cancelZoom: function(element) {
        $(element).parent().removeClass('zoomContainer-CC');
      },
      
      //this method populates productVariantOption model to display the variant options of the product
      populateVariantOptions: function(widget) {
        var options = widget.productVariantOptions();
        if (options !== null && options.length > 0) {
          var optionsArray = [];
          for (var i = 0; i < options.length; i++) {
            var optionValues = this.mapOptionsToArray(options[i].optionValueMap);
            var productVariantOption = this.productVariantModel(options[i].optionName, options[i].mapKeyPropertyAttribute, optionValues, widget);
            optionsArray.push(productVariantOption);
          }
          widget.variantOptionsArray(optionsArray);
        } else {
          widget.variantOptionsArray([]);
        }
      },

      /*this create view model for variant options this contains
      name of the option, possible list of option values for the option
      selected option to store the option selected by the user.
      ID to map the selected option*/       
      productVariantModel: function(optionDisplayName, optionId, optionValues, widget) {
        var productVariantOption = {};
        productVariantOption.optionDisplayName = optionDisplayName;
        productVariantOption.optionId = optionId;
        productVariantOption.optionValues = optionValues;
        productVariantOption.optionCaption = widget.translate('optionCaption', {optionName: optionDisplayName}, true);
        productVariantOption.selectedOption = ko.observable();
        productVariantOption.selectedOption.extend({ required: { params: true, message: widget.translate('optionRequiredMsg', {optionName: optionDisplayName}, true) }});       
        return productVariantOption;
      },
      
      //this method convert the map to array of key value object and sort them based on the enum value 
      //to use it in the select binding of knockout
      mapOptionsToArray : function(variantOptions) {
        var optionArray = [];
        for (var key in variantOptions) {
          if (variantOptions.hasOwnProperty(key)) {
            optionArray.push({ key: key, value: variantOptions[key] }); 
          }          
        }
        optionArray.sort(function (option1, option2) {
          if (option1.value > option2.value) {
            return 1;
          } else if (option1.value < option2.value) {
            return -1;
          } else {
            return 0;
          }
        });
        return optionArray;
      },
     
      //this method returns the selected sku in the product, Based on the options selected
      getSelectedSku : function(variantOptions) {
        var childSkus = this.product().childSKUs;
        var selectedSKUObj = {};
        for (var i = 0; i < childSkus.length; i++) {
          selectedSKUObj =  childSkus[i];
          for (var j = 0; j < variantOptions.length; j++) {
            if ( childSkus[i].dynamicPropertyMapLong[variantOptions[j].optionId] != variantOptions[j].selectedOption().value) {
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
    
      // this method  returns a map of all the options selected by the user for the product
      getSelectedSkuOptions : function(variantOptions) {
        var selectedOptions = [];
        for (var i = 0; i < variantOptions.length; i++) {          
          selectedOptions.push({'optionName': variantOptions[i].optionDisplayName, 'optionValue': variantOptions[i].selectedOption().key});
        }
        return selectedOptions;
      },
      
      // this method validated if all the options of the product are selected
      validateAddToCart : function() {        
        if (!this.stockStatus()) {
          return false;
        }
        var variantOptions = this.variantOptionsArray();
        for (var i = 0; i < variantOptions.length; i++) {
          if (! variantOptions[i].selectedOption.isValid()) {
            return false;
          }
        }
        // get the selected sku based on the options selected by the user
        var selectedSKUObj = this.getSelectedSku(variantOptions);
        if (selectedSKUObj === null) {
          return false;
        }
        this.selectedSku(selectedSKUObj);
        return true;
      },

      // Sends a message to the cart to add this product
      handleAddToCart: function() {
        var variantOptions = this.variantOptionsArray();
        
        //get the selected options, if all the options are selected.
        var selectedOptions = this.getSelectedSkuOptions(variantOptions);
        
        var selectedOptionsObj = { 'selectedOptions': selectedOptions };
        
        var newProduct = $.extend(true, {}, this.product(), selectedOptionsObj);
        
        //assign only the selected sku as child skus
        newProduct.childSKUs = [this.selectedSku()];
        
        // set the prices to the observable value
        newProduct.listPrice = newProduct.listPrice();
        newProduct.salePrice = newProduct.salePrice();
        
        $.Topic(pubsub.topicNames.CART_ADD).publishWith(
          newProduct,[{message:"success"}]);
      }
    };
  }
);
