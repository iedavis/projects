/**
 * @fileoverview Universal Variable Widget.
 * Option.
 * @author ian.davis@oracle.com
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'universalVariable',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub) {

    "use strict";

    var widget;

    return {

      onLoad: function(widgetModel) {
        widget = widgetModel;

        window.universal_variable = {};
        window.universal_variable.version = "1.2.0";
        window.universal_variable.sessionStartTime = $.now();
        window.universal_variable.basketStartTime = window.universal_variable.sessionStartTime;

        this.cartChanged();
        this.userChanged();

        $.Topic(pubsub.topicNames.PAGE_READY).subscribe(this.pageReady);

        $.Topic(pubsub.topicNames.CART_UPDATED).subscribe(this.cartChanged);
        $.Topic(pubsub.topicNames.USER_LOGIN_SUCCESSFUL).subscribe(this.userChanged);
//        widget.user().firstName.subscribe(this.userChanged);

        $.Topic(pubsub.topicNames.ORDER_SUBMISSION_SUCCESS).subscribe(this.orderSuccessful);

        $.Topic("RECOMMENDATIONS_CHANGED").subscribe(this.recommendationsOffered);

      },



      productListUpdated: function(page){


      },

      pageReady: function(page){

        window.universal_variable.page = {};
        window.universal_variable.page.type = page.pageId;
        window.universal_variable.page.breadcrumb = ['Catalog'];
        if(document.location.port==='80'){window.universal_variable.page.environment = "Production";}
        else {window.universal_variable.page.environment = "Preview";}

        if(widget.product() !== undefined){
          if(widget.product().id !== undefined){
            window.universal_variable.product = {};
            window.universal_variable.product.id = widget.product().productNumber;
            window.universal_variable.product.sku_code = widget.product().productNumber;
            window.universal_variable.product.stock = 0;
            window.universal_variable.product.url = widget.product().route;
            window.universal_variable.product.name = widget.product().displayName;
            window.universal_variable.product.description = widget.product().description;
            window.universal_variable.product.currency = window.universal_variable.basket.currency;
            window.universal_variable.product.unit_price = widget.product().listPrice;
            window.universal_variable.product.unit_sale_price = widget.product().salePrice;
            if(widget.product().salePrice === null){
              window.universal_variable.product.unit_sale_price = widget.product().listPrice;
            }
          }
        }

        if(widget.category() !== undefined){



        }

        $.Topic("UV_READY").publish();

      },

      cartChanged: function(cart){
        window.universal_variable.basket = {};
        window.universal_variable.basket.id = widget.user().id() + '_' + window.universal_variable.basketStartTime;
        window.universal_variable.basket.currency = widget.cart().currencyCode();
        window.universal_variable.basket.subtotal = widget.cart().subTotal();
        window.universal_variable.basket.subtotal_include_tax = false;
        window.universal_variable.basket.tax = widget.cart().tax();
        window.universal_variable.basket.shipping_cost = widget.cart().shipping();
        window.universal_variable.basket.total = widget.cart().totalPrice();
        window.universal_variable.basket.line_items = [];

        if(widget.cart().items().length > 0){
          if(widget.cart().items()[0].productData() !== undefined){
            for (var p = 0; p < widget.cart().items().length; p++) {
              window.universal_variable.basket.line_items[p] = {};
              window.universal_variable.basket.line_items[p].product = {};
              window.universal_variable.basket.line_items[p].quantity = widget.cart().items()[p].quantity();
              window.universal_variable.basket.line_items[p].subtotal = widget.cart().items()[p].itemTotal();
              window.universal_variable.basket.line_items[p].product.id = widget.cart().items()[p].productData().productNumber;
              window.universal_variable.basket.line_items[p].product.url = location.origin + '/#!' + widget.cart().items()[p].productData().route;
              window.universal_variable.basket.line_items[p].product.sku_code = widget.cart().items()[p].productData().childSKUs[0].repositoryId;
              window.universal_variable.basket.line_items[p].product.stock = 0;
              window.universal_variable.basket.line_items[p].product.name = widget.cart().items()[p].productData().displayName;
              window.universal_variable.basket.line_items[p].product.description = widget.cart().items()[p].productData().description;
              window.universal_variable.basket.line_items[p].product.currency = window.universal_variable.basket.currency;
              window.universal_variable.basket.line_items[p].product.unit_price = widget.cart().items()[p].productData().listPrice;
              window.universal_variable.basket.line_items[p].product.unit_sale_price = widget.cart().items()[p].productData().salePrice;
              if(widget.cart().items()[p].productData().salePrice === null){
                window.universal_variable.basket.line_items[p].product.unit_sale_price = widget.cart().items()[p].productData().listPrice;
              }
            }
          }
        }
      },


      userChanged: function(user){
        window.universal_variable.user = {};

        if(document.cookie.indexOf('UVreturningVisitor=true') >= 0){
          window.universal_variable.user.returning = true;
        }
        else {
          window.universal_variable.user.returning = false;
          document.cookie = "UVreturningVisitor=true; expires=Sat, 1 Jan 2050 00:00:01 GMT";
        }

        if(document.cookie.indexOf('UVhasTransacted=true') >= 0){
          window.universal_variable.user.has_transacted = true;
        }
        else {
          window.universal_variable.user.has_transacted = false;
        }

        window.universal_variable.user.name = (widget.user().firstName() + " " + widget.user().lastName()).trim();
        window.universal_variable.user.username = "";
        window.universal_variable.user.user_id = widget.user().id();
        window.universal_variable.user.email = widget.user().emailAddress();


      },


      orderSuccessful: function(order){
        window.universal_variable.transaction = window.universal_variable.basket;

        if(document.cookie.indexOf('UVhasTransacted=true') < 0){
          document.cookie = "UVhasTransacted=true; expires=Sat, 1 Jan 2050 00:00:01 GMT";
          window.universal_variable.user.has_transacted = true;
        }

        window.universal_variable.basketStartTime = $.now();
        this.cartChanged();

      },


      recommendationsOffered: function(recsOffered){

        window.universal_variable.recommendation = {};
        window.universal_variable.recommendation.items = [];

        for (var p = 0; p < recsOffered.length; p++) {
          window.universal_variable.recommendation.items[p] = {};
          window.universal_variable.recommendation.items[p].id = recsOffered[p].id;
          window.universal_variable.recommendation.items[p].name = recsOffered[p].name;
          window.universal_variable.recommendation.items[p].description = recsOffered[p].description;
          window.universal_variable.recommendation.items[p].url = location.origin + '/#!' + recsOffered[p].url;
        }
      }
    };
  }
);