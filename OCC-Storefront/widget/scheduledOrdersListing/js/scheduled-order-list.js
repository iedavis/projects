/**
 * @fileoverview Scheduled Order List Widget.
 *
 */
/*global $ */
/*global define */
define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'ccLogger', 'notifier', 'ccConstants', 'jquery', 'ccRestClient', 'navigation','spinner', 'viewModels/scheduledOrderList'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, pubsub, log, notifier, CCConstants, $, CCRestClient, navigation,spinner, ScheduledOrderList) {

    "use strict";

    return {
      errorMsg:						ko.observable(),
      WIDGET_ID:					"scheduleOrderList",
      listingIndicatorOptions: {
        parent: '#CC-scheduledOrderList',
        posTop: '10%',
        posLeft: '50%'
      },

      onLoad: function (widget) {

        var self = this;
        widget.listingViewModel = ko.observable();
        widget.listingViewModel(new ScheduledOrderList());
        //Create scheduledOrderListGrid computed for the widget
        widget.scheduledOrderListGrid = ko.computed(function() {
          var numElements, start, end, width;
          var rows = [];
          var orders;
          if (($(window)[0].innerWidth || $(window).width()) > CCConstants.VIEWPORT_TABLET_UPPER_WIDTH) {
            var startPosition, endPosition;
            // Get the orders in the current page
            startPosition = (widget.listingViewModel().currentPage() - 1) * widget.listingViewModel().itemsPerPage;
            endPosition = startPosition + widget.listingViewModel().itemsPerPage;
            orders = widget.listingViewModel().data.slice(startPosition, endPosition);
          } else {
            orders = widget.listingViewModel().data();
          }
          if (!orders) {
            return;
          }
          numElements = orders.length;
          width = parseInt(widget.listingViewModel().itemsPerRow(), 10);
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

        $.Topic(pubsub.topicNames.SCHEDULE_ORDERS_LIST_FAILED).subscribe(function(data) {
          if (this.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            widget.user().handleSessionExpired();
            if (navigation.isPathEqualTo(widget.links().profile.route)) {
              navigation.doLogin(navigation.getPath(), widget.links().home.route);
            }
          } else {
            notifier.clearError(widget.WIDGET_ID);
            notifier.clearSuccess(widget.WIDGET_ID);
            notifier.sendError(widget.WIDGET_ID, data.message, true);
            navigation.goTo('/profile');
          }
        });
      },
      beforeAppear: function (page) {
        var widget = this;
        if (widget.user().loggedIn() == false) {
          navigation.doLogin(navigation.getPath(), widget.links().home.route);
        } else {
          widget.listingViewModel().clearOnLoad = true;
          widget.listingViewModel().load(1, 1);
        }
      }

    };
  });
