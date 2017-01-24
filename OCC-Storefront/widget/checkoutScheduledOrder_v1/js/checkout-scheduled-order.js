/**
 * @fileoverview Checkout Scheduled Order Widget.
 *
 */
/*global $ */
/*global define */
define(

    //-------------------------------------------------------------------
    // DEPENDENCIES
    //-------------------------------------------------------------------
    ['knockout', 'pubsub', 'ccLogger', 'notifier', 'spinner', 'ccConstants', 'jquery', 'ccRestClient', 'viewModels/checkoutScheduledOrder', 'ccDate', 'navigation'],

    //-------------------------------------------------------------------
    // MODULE DEFINITION
    //-------------------------------------------------------------------
    function (ko, pubsub, log, notifier, spinner, CCConstants, $, CCRestClient, CheckoutScheduledOrder, ccDate, navigation) {

      "use strict";

      return {
        errorMsg:                   ko.observable(),
        spinnerOptions : 
        {
          parent: '#CC-scheduledOrder-scheduleForm',
          posTop: '0',
          posLeft: '50%'
        },

          showScheduleSubscription:'',

          resourcesLoaded: function(widget) {
            widget.errorMsg(widget.translate('checkoutErrorMsg'));

            widget.order().schedule.extend({
              propertyWatch: widget.order().schedule()
            });

            widget.scheduleModeOptGroups= [
              {
                label: widget.translate('daily'),
                options: [
                  {
                    text: widget.translate('onceADay'),
                    value: CCConstants.SCHEDULE_MODE_ONCE_DAILY,
                    showDayOfWeek: false,
                    showDaysOfWeek: false,
                    showWeeksOfMonth: false
                  },
                ]
              },
              {
                label: widget.translate('weekly'),
                options: [
                  {
                    text:widget.translate('weekly'),
                    value: CCConstants.SCHEDULE_MODE_WEEKLY,
                    showDayOfWeek: true,
                    showDaysOfWeek: false,
                    showWeeksOfMonth: true
                  }
                ]
              },
              {
                label: widget.translate('monthly'),
                options: [
                  {
                    text: widget.translate('onceAMonth'),
                    value: CCConstants.SCHEDULE_MODE_ONCE_MONTHLY,
                    showDayOfWeek: false,
                    showDaysOfWeek: false,
                    showWeeksOfMonth: false
                  },
                  {
                    text: widget.translate('everyTwoMonths'),
                    value: CCConstants.SCHEDULE_MODE_BI_MONTHLY,
                    showDayOfWeek: false,
                    showDaysOfWeek: false,
                    showWeeksOfMonth: false
                  },
                  {
                    text: widget.translate('quarterly'),
                    value: CCConstants.SCHEDULE_MODE_QUARTERLY,
                    showDayOfWeek: false,
                    showDaysOfWeek: false,
                    showWeeksOfMonth: false
                  }
                ]
              }
            ];
            widget.order().schedule(new CheckoutScheduledOrder(widget, widget.scheduleModeOptGroups));
            $.Topic(pubsub.topicNames.SCHEDULED_ORDER_INFO_POPULATED).publishWith([{message: "success"}]);
          },

          onLoad: function (widget) {
        	$.Topic(pubsub.topicNames.USER_LOGOUT_SUBMIT).subscribe(widget.clearShowSchedule.bind(widget));

            //Subscription to redirect user to scheduled order details page.
            $.Topic(pubsub.topicNames.SCHEDULED_ORDER_SUBMISSION_SUCCESS).subscribe(
                    widget.handleScheduledOrderSubmissionSuccess.bind(widget));

          widget.showScheduleSubscription = widget.order().showSchedule.subscribe(function(event){
            var startDateElementId = '#CC-checkoutScheduledOrder-sstartdate';
            var endDateElementId = '#CC-checkoutScheduledOrder-senddatetype';
            //blur events of datepicker
            $(startDateElementId).on('keyup', function(event) {
              $(startDateElementId).off('blur').on('blur', function(event){
                var startDate = widget.setDefaultDate(startDateElementId);
                widget.order().schedule().startDate(startDate);
                $(startDateElementId).off('blur');
              });
            });

            $(endDateElementId).on('keyup', function(event) {
              $(endDateElementId).off('blur').on('blur', function(event){
                var endDate = widget.setDefaultDate(endDateElementId);
                widget.order().schedule().endDate(endDate);
                $(endDateElementId).off('blur');
              });
            });
          });
        },

        /**
         * Sets dafault date to current date to +1 if input is
         * currentDate/PreviousDates/Invalid.
         *
         * @private
         * @param {String} elementId - The form element id .
         * @return {String} - The date in String representation.
         */
        setDefaultDate: function(elementId){
          if(elementId){
            var inputValue = $(elementId).val();
            var inputDate = new Date(inputValue);
            var currentDate = new Date();
            if(inputDate <= currentDate){
              currentDate.setDate(currentDate.getDate() + 1);
              inputDate = currentDate;
            }
            if(inputDate != 'Invalid Date'){
              inputDate = ccDate.dateTimeFormatter(inputDate,null,"short");
              $(elementId).datepicker('update', inputDate).datepicker('fill');
            }
            else{
              inputDate = "";
            }
            return inputDate;
          }
        },

          /**
           * method to reset showSchedule flag
           */
          clearShowSchedule: function() {
          this.showScheduleSubscription.dispose();
        	this.order().showSchedule(false);
          },
          

          /**
           * @function
           * @name validateNow
           * Force all relevant member observables to perform their
           * validation now & display the errors (if any)
           */
          validateNow: function() {
            widget.order().validateSchedule();

            return (widget.isValid());
          },

          /**
           * Callback function for use in widget stacks.
           * Triggers internal widget validation.
           * @return true if we think we are OK, false o/w.
           */
          validate : function() {
            return widget.validateNow();		
          },
          
          /**
           * Callback for schedule order submit success
           * Redirects to schedule order details page
           */
          handleScheduledOrderSubmissionSuccess: function(data) {
            var widget = this;
            navigation.goTo(widget.order().contextData.global.links.scheduledOrders.route + "/" + data[0].scheduleOrderId);
            widget.order().isOrderSubmitted = true;
            widget.order().enableOrderButton(true);
            if (widget.order().isPaypalVerified()) {
              widget.order().clearPaypalData();
            }
            if (!widget.user().loggedIn()) {
              widget.order().reset();
            }
            widget.order().destroySpinner();
          },

          beforeAppear: function (page) {
            var widget = this;
            widget.order().showSchedule(false);
            widget.order().schedule(new CheckoutScheduledOrder(widget, widget.scheduleModeOptGroups));
            widget.order().schedule().reset();
            widget.order().schedule().resetModified();
          },
          
          

      };
    });