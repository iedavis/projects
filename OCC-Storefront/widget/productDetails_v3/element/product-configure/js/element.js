define(
    // -------------------------------------------------------------------
    // DEPENDENCIES
    // -------------------------------------------------------------------
    ['jquery', 'knockout', 'viewModels/integrationViewModel', 'pubsub'],

    // -------------------------------------------------------------------
    // MODULE DEFINITION
    // -------------------------------------------------------------------
    function($, ko, integrationViewModel, pubsub) {
      "use strict";

      return {
        elementName : 'product-configure',

        // Handle on load events
        onLoad: function(widget) {
          var self = this;
          $.Topic(pubsub.topicNames.CART_ADD_SUCCESS_CPQ).subscribe(self.handleClose);
        },
        
        // Validate if the configurable button should be enabled.
        validateConfigurable : function(widget) {
          var configureButtonFlag = widget.allOptionsSelected()
              && widget.stockStatus() && widget.quantityIsValid();
          // Check if the product has a selected SKU. If so, check if that is
          // configurable
          if ((widget.variantOptionsArray().length > 0) && widget.selectedSku()) {
            configureButtonFlag = configureButtonFlag
                && widget.selectedSku().configurable;
          } else {
            // Check if the product is configurable. Since the product has only
            // one sku,
            // it should have the SKU as configurable.
            configureButtonFlag = configureButtonFlag
                && widget.product().isConfigurable();
          }

          if (!configureButtonFlag) {
            $('#cc-prodDetailsConfigure').attr("aria-disabled", "true");
          }
          return configureButtonFlag;
        },

        // Returns the configuration frame document.
        getFrameDocument : function() {
          var iframe = document.getElementById("cc-cpqConfiguration-frame");
          if (iframe) {
            return iframe.contentDocument || iframe.contentWindow.document;
          }
        },

        // Returns the configuration form.
        getConfigurationForm : function() {
          var iframeDocument = this.getFrameDocument();
          if (iframeDocument) {
            return iframeDocument.getElementById("configuration_form");
          }
        },

        // Reloads the configuration frame.
        reloadFrame : function() {
          var iframe = document.getElementById("cc-cpqConfiguration-frame");
          if (iframe) {
            iframe.src = iframe.src;
          }
        },

        // Handle the configure button click
        handleConfigure : function(widget) {
          // Handle opening the i-frame here
          var self = this;
          var configurableSku;
          var configurableSkuDetails = new Object();
          integrationViewModel.getInstance().iFrameId = "cc-cpqConfiguration-frame"; 
          // Update the item quantity on the integration view model to the item
          // quantity on the widget.
          if (widget.itemQuantity && widget.itemQuantity()) {
            integrationViewModel.getInstance().itemQuantity = widget.itemQuantity();
          }
          if ((widget.variantOptionsArray().length > 0) && widget.selectedSku()) {
            // If product has variant options and more than one SKU
            configurableSku = widget.selectedSku();
            configurableSkuDetails.model = configurableSku.model
            configurableSkuDetails.product_line = configurableSku.productLine;
            configurableSkuDetails.product_family = configurableSku.productFamily;
          } else {
            // If product has only one SKU
            configurableSku = widget.product().childSKUs()[0];
            configurableSkuDetails.model = configurableSku.model();
            configurableSkuDetails.product_line = configurableSku.productLine();
            configurableSkuDetails.product_family = configurableSku
                .productFamily();
          }
          configurableSkuDetails.locale = widget.locale();
          configurableSkuDetails.currency = widget.site()
              .selectedPriceListGroup().currency.currencyCode;

          // Injecting the configuration form values
          var keys = Object.keys(configurableSkuDetails);
          var frameDocument = self.getFrameDocument();
          if (frameDocument) {
            for (var i = 0; i < keys.length; i++) {
              var element = frameDocument.getElementById(keys[i]);
              if (element) {
                element.value = configurableSkuDetails[keys[i]];
              }
            }
          }
          self.getConfigurationForm().action = widget.site().extensionSiteSettings.CPQConfigurationSettings.ConfigurationURL;
          self.getConfigurationForm().submit();
          $('#cc-cpqmodalpane').modal({
            backdrop: 'static',
            keyboard: false
          });
          $('#cc-cpqmodalpane').modal('show');
          $('#cc-cpqmodalpane').on('hidden.bs.modal', function() {
            self.reloadFrame();
          });
       },
       handleClose: function() {
         // Close the modal.
         // Can add all the SF cleanup items here if needed.
         $('#cc-cpqmodalpane').modal('hide');
       }
    };
});
