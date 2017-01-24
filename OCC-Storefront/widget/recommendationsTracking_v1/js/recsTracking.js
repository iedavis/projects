define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['pubsub', 'ccLogger', 'ccRestClient', 'ccConstants', 'js/recsRequest', 'pageLayout/cart',  'storageApi', 'swmRestClient'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(pubsub, logger, ccRestClient, ccConstants, recsRequest, cartModel, storageApi, swmRestClient) {
  
    "use strict";
  
    /**********************************************
     * Default values for some of the configuration
     **********************************************/

    // default recommendations base path
    var REC_BASE_PATH = "pr/",
    
    /****************************************
     * Utility Functions
     ****************************************/

    /**
     * For Cart, get the productIds from the CartViewModel.
     *
     * @param model a CartViewModel instance
     * @return the array of productIds
     */
    getProductIdsFromCartViewModel = function(model) {
      var items = model.items(),
          item,
          length = items.length,
          productIds = [];
      
      while (length--) {
        // get a reference so we don't have to use the index
        item = items[length];
      
        // push onto the array of productIds
        productIds.push(item.productId);
      }
      
      return productIds;
    },

    /**
     * For Checkout, get the data  [lineItems, totalPrice] from the CartViewModel.
     *
     * @param model a CartViewModel instance
     * @return  [lineItems, totalPrice]
     */
    getProductDataFromCartViewModel = function(model) {
      var items = model.items(),
          item,
          length = items.length,
          lineItemInfo = [];
      var total_price = model.total();
      
      while (length--) {
        // get a reference so we don't have to use the index
        item = items[length];
      
        // push onto the array of line item product info
        lineItemInfo.push({
          productId: item.productId,
          quantity: item.quantity(),
          price: item.itemTotal()
        });
      }
      
      // return both lists
      return [lineItemInfo, total_price];
    },

    /**
     * Add cart resource to Recs request.
     * If the cart has items in it, include the pricelistGroupId and currencyCode.
     */
    addCartDetailsToRequest = function() {
      // If the cart contains items add the pricelistGroupId and currency
      if (cartModel.singleInstance.items().length > 0) {
        getRequest().addResource("cart", {
          cart: {
            productIds: getProductIdsFromCartViewModel(cartModel.singleInstance),
            pricelistGroupId: cartModel.singleInstance.cartPriceListGroupId(),
            currencyCode: cartModel.singleInstance.currencyCode()
          }
        });
      } else {
        // Otherwise just return the empty array or productIds.
        getRequest().addResource("cart", {
          cart: {
            productIds: getProductIdsFromCartViewModel(cartModel.singleInstance)
          }
        });
      }
    },

    /*
     * Extracts the page information from a PAGE_VIEW_CHANGED or PAGE_CONTEXT_CHANGED
     * pubsub event and returns it so that it can be added to the view.
     *
     * @param param The object passed as the parameter to the pubsub subscriber for the event
     * @return a hash of values used for describing the view to ther recs service
     */
    getViewContext = function(param) {
      var context = {},
          page = param.pageId,
          id = param.contextId;
    
      if (page == "product") {
        context.productId = id;

      } else if (page == "category") {
        context.category = id;
        
      } else if (page == "home" || page == "search" || page == "cart") {
        // there isn't really anything to log here
      }

      return context;
    },
    
    /*******************************************
     * Variables for storing shared information
     *******************************************/
    // This will contain the current visitorId.
    tracked_VISITOR_ID,
 
    // This will contain the current sessionId.
    tracked_SESSION_ID,
    
    // This will contain the current RESTRequest object.
    request,
    
    // This will contain the widgetViewModel.
    widget,
    
    // This is the list of pages where sending requests should be deferred.
    // TODO: not used, need figure out why/how to use this
    pagesToDeferSending = {},
    
    // This just contains the landing page referrer until it is sent to the server.
    // Once it is sent this will contain 0.
    referrer = document.referrer,
    
    /**
     * Gets the retailerId to use for Recs requests. This is the tenant ID used at provisioning.
     */
    getRetailerId = function() {
      return widget.site().tenantId;
    },

    /******************************************
     * Action Functions
     ******************************************/

    /**
     * Gets the current request for the module.
     * Lazy instantiates the request, so it will always return a valid object.
     *
     * @return the current RESTRequest object
     */
    getRequest = function() {
      // lazy instantiate the request object
      if (!request) {
        // set the session level variable to a new request
        request = new recsRequest.RESTRequest(requestPath);
      }

      // returning the current request object
      return request;
    },


    RECS_VISITOR_ID = "atgRecVisitorId",
    RECS_SESSION_ID = "atgRecSessionId",
    RECS_PRICELIST_GROUP_ID = "atgRecPricelistGroupId",
    RECS_CURRENCY_CODE = "atgRecCurrencyCode",
    RECS_SEND_CART = "atgRecSendCart",
    PAYU_CHECKOUT_INFO = "atgRecPayUCheckoutInfo",

    /**
     * Set and/or Return visitorId
     * @return visitorId
     */
    visitorId = function (set_value) {
      if (set_value) {
        storageApi.getInstance().setItem(RECS_VISITOR_ID, set_value);
        tracked_VISITOR_ID = set_value;
      }

      // get the value from local variable or the local storage
      return tracked_VISITOR_ID || storageApi.getInstance().getItem(RECS_VISITOR_ID);
    },

    /**
     * Set and/or Return sessionId. Reads it from and saves it to session storage.
     * @return sessionId
     */
    sessionId = function (set_value) {
      if (set_value) {
        storageApi.getInstance().setItem(RECS_SESSION_ID, set_value);
        tracked_SESSION_ID = set_value;
      }

      // get the value from local variable or the local storage
      return tracked_SESSION_ID || storageApi.getInstance().getItem(RECS_SESSION_ID);
    },

    /**
     * Return the full path to use for a RESTful request
     *
     * @param basePathFunc a function which takes resources as its argument and returns the base of the path
     * @param resource the Product Recommender or Plato resources to call
     * @return the path for requests.
     */
    requestPath = function (resource) {
      var requestPathStr = widget.recsHostPortPath + "/" +
        resource + "/3.0/json/" + 
        getRetailerId() +
        (visitorId() ? "/" + visitorId() : "") +
        (sessionId() ? "?sessionId=" + sessionId() : "");
      return requestPathStr;
    },

    /**
     * Stores the tracking information in memory so that requests can be tracked for a user.
     *
     * @param tracking the tracking json returned by the server
     */
    setTracking = function(tracking) {

      // If sessionId or visitorId is returned, set them
      if (tracking) {
        if (tracking.visitorId) {
          visitorId(tracking.visitorId);
        }
  
        if (tracking.sessionId) {
          sessionId(tracking.sessionId);
        }
      }
    },
     
    /**
     * slots data and tracking data need to be published to display widget
     *
     */
    handleSlots = function(slots) {
      var slotsData = slots;
      var haveRecsEvent = {}
      haveRecsEvent.data = slots;
      haveRecsEvent.visitorId = visitorId();
      haveRecsEvent.sessionId = sessionId();
      $.Topic(pubsub.topicNames.RECS_HAVE_RECS).publish(haveRecsEvent);
    },

    /**
     * page: this is a category page
     *
     */
    addCategoryPathAndSend = function(page, categoryId) {
      if ( categoryId != null) {
        if (widget.categoryPath == null) {
          widget.categoryPath = categoryId;
        } else {
          widget.categoryPath = categoryId + ">" +  widget.categoryPath;
        }
      }

      if (categoryId == ccConstants.ROOT_CATEGORY_ID || categoryId == null) {
        // send it
        send(page); 
        return;
      }
   
      var getCategoryPathSuccessCB = function(pResult) {
        var curCatId = null,
            fixedParentCategories = pResult.fixedParentCategories;
        if (Array.isArray(fixedParentCategories) &&
            fixedParentCategories.length > 0) {
          curCatId = fixedParentCategories[0].repositoryId;
        }
         addCategoryPathAndSend(page, curCatId); 
       };


      var getCategoryPathErrorCB = function(pResult) {
        logger.warning("[recsTracking] addCategoryPathAndSend failed.");
      };

      // on category page, need get category path before sending a request
      ccRestClient.request(ccConstants.ENDPOINT_COLLECTIONS_GET_COLLECTION, null,
        getCategoryPathSuccessCB,
        getCategoryPathErrorCB,
        categoryId
      );
    },

    /**
     * Sends the request to the Recs servers after performing some final setup.
     * Adds the url and referrer information and the view resource if it isn't already present.
     * Adds the setTracking handler.
     */
    send = function(curPage) {
      if (!widget.recsHostPortPath) {
         // in send function, if no rec_host configured, just return
         return;
      }

      if (!getRetailerId()) {
        return;
      }
       
      if (widget.localWishlists != null && widget.localWishlists != undefined) {
        if (Object.getOwnPropertyNames(widget.localWishlists).length != 0) {
          getRequest().addResource("wishlists", {"wishlists": widget.localWishlists});
        }
      }

      if (widget.slots != null && widget.slots != undefined) {
        if (Object.getOwnPropertyNames(widget.slots).length != 0) {
          getRequest().addResource("recommendations", {"slots": widget.slots});
        }
      }

      // the object to add on any extra view data to the request
      var viewData = {
        // we can't rely on the REFERER request header because it doesn't include anchor information
        url: document.location.href
      },
      
      // get a local reference to the request, using the getter to make sure it's instantiated
      req = getRequest();

      if (ccRestClient.profileId) {
        req.addParam({customerId: ccRestClient.profileId});
      }
      
      // add the referrer if this was the landing page
      if (referrer) {
        viewData.referrer = referrer;
        // make sure we don't send the referrer with every request
        referrer = 0;
      }

      // add the productId to the view so the recommender knows about it when making requests.
      if(curPage.pageId === 'product') {
        viewData.productId = curPage.contextId;
      }

      if (widget.categoryPath) {
        viewData.category = widget.categoryPath;
      }

      // add the the pageTitle the view. CC document tile is used here
      if (widget.pageTitle) {
        viewData.pageTitle = widget.pageTitle;
      }

      // add the storeId to the view. CC catalogId is used as Recs storeId
      if (widget.catalogId) {
        viewData.storeId = widget.catalogId;
        viewData.excludeDefaultStore = true;
      }
      
      // Add the pricelistGroupId and currencyCode to the view request, but only on the first request
      // or if the value changed in the session.
      var currentPricelistGroup = widget.site().selectedPriceListGroup();
      var storedPricelistGroupId = storageApi.getInstance().readFromSessionStorage(RECS_PRICELIST_GROUP_ID);
      var currentPricelistGroupId = currentPricelistGroup.repositoryId;
      if (storedPricelistGroupId !== currentPricelistGroupId) {
        if (currentPricelistGroupId != null && currentPricelistGroupId != undefined) {
          storageApi.getInstance().saveToSessionStorage(RECS_PRICELIST_GROUP_ID, currentPricelistGroupId); 
          viewData.pricelistGroupId = currentPricelistGroupId;
        } else {
          storageApi.getInstance().removeItem(RECS_PRICELIST_GROUP_ID);
        }
      }

      var storedCurrencyCode = storageApi.getInstance().readFromSessionStorage(RECS_CURRENCY_CODE);
      var currentCurrencyCode = currentPricelistGroup.currency.currencyCode;
      if (storedCurrencyCode !== currentCurrencyCode) {
        if (currentCurrencyCode != null && currentCurrencyCode != undefined) {
          storageApi.getInstance().saveToSessionStorage(RECS_CURRENCY_CODE, currentCurrencyCode); 
          viewData.currencyCode = currentCurrencyCode;
        } else {
          storageApi.getInstance().removeItem(RECS_CURRENCY_CODE);
        }
      }

      // this works fine because addResource will deep extend the params
      req.addResource("view", {view: viewData});

      // special case for PayU checkout, begin
      var  payUCheckoutInfo= storageApi.getInstance().getItem(PAYU_CHECKOUT_INFO);
      if (payUCheckoutInfo) {
         getRequest().addResource("checkout", {
           checkout: JSON.parse(payUCheckoutInfo)
         });
         // after adding to the resource, remove it from local storage.
         storageApi.getInstance().removeItem(PAYU_CHECKOUT_INFO);
      };
      // special case for PayU checkout, end

      // If this is the first request in the session or the cart was updated, send the cart details on the request.
      var sendCartToRecs = storageApi.getInstance().readFromSessionStorage(RECS_SEND_CART);
      if (sendCartToRecs === null || sendCartToRecs === undefined || sendCartToRecs === "true") {
        addCartDetailsToRequest();
        storageApi.getInstance().saveToSessionStorage(RECS_SEND_CART, "false");
      }

      // add the function to handle tracking codes
      req.addCallback(setTracking, "tracking");

      // add the function to handle recommendations data
      req.addCallback(handleSlots, "slots");

      // attempt to send it off to the server
      req.send();
      // clear the request object
      request = 0;
    },


    hashOfWishlists = {},

    /**
     * get all the productIds given an element of products   
     */
    getSpaceProductsIds = function(products) {
      var prodItems = products.items;
      var productIdsInSpace = [];
      if (prodItems && prodItems.length > 0) {
        var j = 0;
        for (j=0; j < prodItems.length; j++) {
          productIdsInSpace.push(prodItems[j].productProductId);
        }
      }
      return productIdsInSpace;
    },

    /**
     * get spaces first. if successful, populate the hashOfWishlist with prodIds.
     */
    getSpaces = function() {
      // make sure the configuration and login to social server
      var getSpacesSuccessCB = function(pResult) {
        var curSpaceId;
        var curSpaceProdIdsList = [];
        var items = pResult.items;
        var i = 0; 
        for (i=0; i<items.length; i++) {
          curSpaceId = items[i].spaceId;
          curSpaceProdIdsList = getSpaceProductsIds(items[i].products)
          if(curSpaceProdIdsList.length > 0){
            hashOfWishlists[curSpaceId] = curSpaceProdIdsList;
          }
        }
        if (Object.getOwnPropertyNames(hashOfWishlists).length != 0) {
          widget.localWishlists = hashOfWishlists;
        }
      };

      var getSpacesErrorCB = function(pResult) {
      };

      swmRestClient.init(widget.site().tenantId, widget.isPreview(), widget.locale());

      // get spaces and then productIds
      swmRestClient.request('GET', '/swm/rs/v1/sites/{siteid}/spaces?expand=products', '', 
         getSpacesSuccessCB, 
         getSpacesErrorCB, 
         {});
    };

    /********************************
     * Registration for pubsub topics
     ********************************/

    /**
     * Search Results
     */
    $.Topic(pubsub.topicNames.SEARCH_CREATE).subscribe(function(obj) {
      var text = this.searchText;

      if (text) {
      // make sure to trim off the * at the end of the search string
        getRequest().addResource("view", {
          view: {
            searchText: text.substr(0, text.length - 1)
          }
        });
      }
    });
    
    /**
     * Checkout Confirmation
     */
    $.Topic(pubsub.topicNames.ORDER_COMPLETED).subscribe(function(obj) {
      var productsInCart = getProductDataFromCartViewModel(cartModel.singleInstance);

      // get the full line item info for the checkout products
      // and the total price
      getRequest().addResource("checkout", {
        checkout: {
          products: productsInCart[0],
          totalPrice: productsInCart[1],
          pricelistGroupId: cartModel.singleInstance.cartPriceListGroupId(),
          currencyCode: cartModel.singleInstance.currencyCode()
        }
      });
      // done adding checkout with line items and total price
    });

    /**
     * PayU, different with other payments.
     */
    $.Topic(pubsub.topicNames.PAYULATAM_WEB_CHECKOUT).subscribe(function(obj) {
      var productsInCart = getProductDataFromCartViewModel(cartModel.singleInstance);
      if (productsInCart[1] > 0) {
        var checkoutInfo = {
          products: productsInCart[0],
          totalPrice: productsInCart[1],
          pricelistGroupId: cartModel.singleInstance.cartPriceListGroupId(),
          currencyCode: cartModel.singleInstance.currencyCode()
        };
        storageApi.getInstance().setItem(PAYU_CHECKOUT_INFO, JSON.stringify(checkoutInfo));
      }
      // PAYU, hold the checkout info in session, will send it out in next page, where tracking widget is loaded
    });

    /**
     * The page changed, either a pageId or a contextId change.
     * This even occurs at the very beginning of the page load cycle.
     */
    $.Topic(pubsub.topicNames.PAGE_CHANGED).subscribe(function(page) {
      // add the view for the page change
      getRequest().addResource("view", {
        view: getViewContext(page)
      });
    });

    /**
     * The cart was updated. Events are sent to this topic when items are added or removed by the user
     * and when items are removed from the cart during checkout order submission.
     */
    $.Topic(pubsub.topicNames.CART_UPDATED).subscribe(function(obj) {
      storageApi.getInstance().saveToSessionStorage(RECS_SEND_CART, "true");
    });
    
    // properties defined in the return are the actual module
    return {
      /**
       * Runs the first time the module is loaded on a page.
       * It is passed the widgetViewModel which contains the configuration from the server.
       */
      onLoad: function(widgetModel) {

        // Store the widgetViewModel so we can use it outside of onLoad.
        widget = widgetModel;

        var createCampaignRule = function (strategy, restriction) {
          var RULE_MAP = {
            "Top Sellers": "topSellers",
            "Browsed Together" : "topCobrowses",
            "Purchased Together" : "topCobuys",

            "In Brand": "inBrand",
            "In Collection": "inCategory"
          };

          var rules = []; 

          if (strategy == 'Top Sellers' && restriction == 'In Brand') {
            rules.push("topSellersInBrand");
          } else if (strategy == 'Top Sellers' && restriction == 'In Collection') {
            rules.push("topSellersInCategory");
          } else  {
            if (RULE_MAP[strategy]) {
              rules.push(RULE_MAP[strategy]);
            }
            if (RULE_MAP[restriction]) {
              rules.push(RULE_MAP[restriction]);
            }
          }
          return rules;
        }; 

        $.Topic(pubsub.topicNames.RECS_WANT_RECS).subscribe(function(obj) {
            // extract info from the event obj
            var idOfWidget = obj.id;
            var numRecs = obj.numRecs;
            var strategy = obj.strategy;
            var restriction = obj.restriction;
            var campaignRules = null;

            if (widget.slots == undefined || widget.slots == null) {
              widget.slots = {};
            }
            widget.slots[idOfWidget] = {};
            widget.slots[idOfWidget].numRecs = numRecs;
            campaignRules = createCampaignRule(strategy, restriction);
            widget.slots[idOfWidget].rule = campaignRules;
            widget.slots[idOfWidget].dataItems =  ["desc", "category", "brand", "repositoryid"];
            if (widget.excludeList.length > 0) {
              widget.slots[idOfWidget].exclude = widget.excludeList;
            }
        });

        // Intialize the wishlist by calling the swm server when onLoading.
        // TODO: The following ajax call can return later than the beforeAppear.
        // Therefore, the first time beforeAppear finishes, there could be no wishlist.
        if (widget.user().loggedIn()){
          getSpaces();
        }

        //merege newly added <spceId, prodId> to wishlist
        var mergeToLocalWishlists = function (addedSpaceId, addedProdId) {

          if (widget.localWishlists == null || widget.localWishlists == undefined) { 
            if (addedSpaceId != null && addedProdId != null) {  
              widget.localWishlists = {};
              widget.localWishlists[addedSpaceId] = [addedProdId];
            }
          } else {
            if (addedSpaceId != null && addedProdId != null) {  
              if (widget.localWishlists[addedSpaceId]) {
                widget.localWishlists[addedSpaceId].push(addedProdId);
              } else {
                widget.localWishlists[addedSpaceId] = [addedProdId];
              }
            }
          }

          if(widget.localWishlists) {
            getRequest().addResource("wishlists", {"wishlists": widget.localWishlists});
          }
        };

          
        $.Topic(pubsub.topicNames.USER_LOGIN_SUCCESSFUL).subscribe(function(obj) {
          getSpaces();
        });

        $.Topic(pubsub.topicNames.SOCIAL_SPACE_ADD_SUCCESS).subscribe(function(obj) {
          var prodId;
          if(widget && widget.product && widget.product()) {
            prodId = widget.product().id();
          }
          var spaceId = obj.spaceId;
          if (spaceId != null && prodId != null) {  
            mergeToLocalWishlists(spaceId, prodId);
          }
        });
      },
      
      /**
       * Runs late in the page cycle on every page where the widget will appear, which is
       * every page. 
       * It is passed the pageViewModel and the widgetViewModel is set on this.
       */
      beforeAppear: function(page) {

        // clear the slots resource in widget, because it changes when current page changes
        widget.slots = null;
        widget.categoryPath = null;
        widget.pageTitle = document.title;
        widget.catalogId = widget.user().catalogId();

        widget.excludeList = [];

        if(widget && widget.product && widget.product() && widget.product().relatedProducts) {
          var relatedProducts = widget.product().relatedProducts;
          var i;
          for (i=0; i<relatedProducts.length; i++) {
            widget.excludeList.push(relatedProducts[i].id);
          }
        }

        var whoWantRecsEvent = {}
        whoWantRecsEvent.pageId = page.pageId;
        $.Topic(pubsub.topicNames.RECS_WHO_WANT_RECS).publish(whoWantRecsEvent);

        var fetchRecsHostnameSuccessHandler = function(pResult) {
          
          var host = pResult.serviceData.host;
          var port = pResult.serviceData.port;
          var path = pResult.serviceData.path;

          var portPart = ""; 
          if (port && port !== null && port !== "" && port !== "0") {
             portPart = ':'+ port;
          }

          if (host && path) {
            widget.recsHostPortPath = "//" + host + portPart + "/" + path;
            if(page.pageId == "category")
              addCategoryPathAndSend(page, page.contextId);
            else
              send(page);
          }
        };

        var fetchRecsHostnameErrorHandler = function(pResult) {
          logger.warning("Failed to get Recs hostname.", pResult);
        };

        if (widget.recsHostPortPath) {
          if(page.pageId == "category")
            addCategoryPathAndSend(page, page.contextId);
          else
            send(page);
        } else {
          ccRestClient.request(ccConstants.ENDPOINT_MERCHANT_GET_EXTERNALDATA, null,
            fetchRecsHostnameSuccessHandler,
            fetchRecsHostnameErrorHandler,
            ccConstants.EXTERNALDATA_PRODUCTION_RECS
          );
        }
      },

      /**
       * Allows a requiring module to specify pages where the request should be deferred
       * to allow for more resources to be added before sending.
       *  TODO: not used, need figure out why/how to use this
       *
       * @param pageId the id of a page where the request should be deferred.
       */
      deferSendOnPage: function(pageId) {
        pagesToDeferSending[pageId] = 1;
      }
    };
  }
);
