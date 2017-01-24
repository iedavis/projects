/**
 * @fileoverview Order History Widget.
 * 
 */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'viewModels/orderHistoryViewModel', 'notifier', 'CCi18n', 'ccConstants', 'navigation'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub, OrderHistoryViewModel, notifier, CCi18n, CCConstants, navigation) {
  
    "use strict";

    return {

      WIDGET_ID:        "orderHistory",
      ordersArray: ko.observableArray([]),
    	
      beforeAppear: function (page) {
        var widget = this;
        if (widget.user().loggedIn() == false) {
          navigation.doLogin(navigation.getPath(), widget.links().home.route);
        } else {
          widget.historyViewModel().clearOnLoad = true;
          widget.historyViewModel().load(1, 1);
        }
      },
        
      onLoad: function(widget) {
    	var self = this;
        widget.historyViewModel = ko.observable();
        widget.historyViewModel(new OrderHistoryViewModel());
        //Create historyGrid computed for the widget
        widget.historyGrid = ko.computed(function() {
          var numElements, start, end, width;
          var rows = [];
          var orders;
          if (($(window)[0].innerWidth || $(window).width()) > CCConstants.VIEWPORT_TABLET_UPPER_WIDTH) {
            var startPosition, endPosition;
            // Get the orders in the current page
            startPosition = (widget.historyViewModel().currentPage() - 1) * widget.historyViewModel().itemsPerPage;
            endPosition = startPosition + widget.historyViewModel().itemsPerPage;
            orders = widget.historyViewModel().data.slice(startPosition, endPosition);
          } else {
            orders = widget.historyViewModel().data();
          }
          if (!orders) {
            return;
          }
          numElements = orders.length;
          width = parseInt(widget.historyViewModel().itemsPerRow(), 10);
          start = 0;
          end = start + width;
          while (end <= numElements) {
            rows.push(orders.slice(start, end));
            start = end;
            end += width;
          }
          if (end > numElements && start < numElements) {
            rows.push(orders.slice(start, numElements));
          }
          return rows;
        }, widget);

        $.Topic(pubsub.topicNames.ORDERS_GET_HISTORY_FAILED).subscribe(function(data) {
          if (this.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            widget.user().handleSessionExpired();
            if (navigation.isPathEqualTo(widget.links().profile.route) || navigation.isPathEqualTo(widget.links().orderHistory.route)) {
              navigation.doLogin(navigation.getPath, widget.links().home.route);
            }
          } else {
            navigation.goTo('/profile');
          }
        });
      },

      clickOrderDetails: function (data, event) {
        var widget = this;
        widget.user().validateAndRedirectPage(this.links().orderDetails.route+'/'+ data.orderId);
        return false;
      }
    };
  }
);
