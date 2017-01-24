/**
 * @fileoverview Scheduled Order Widget
 */
define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['ccConstants', 'knockout', 'pubsub', 'navigation', 'notifier', 'spinner', 'ccDate', 'pageLayout/scheduled-order'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (CCConstants, ko, pubsub, navigation, notifier, spinner, ccDate, ScheduledOrderViewModel) {

  "use strict";

  return {

    /** Widget root element id */
    WIDGET_ID: 'scheduledOrderRegion',

    /** Default options for creating a spinner on body*/
    scheduledOrderBodyIndicator: '#CC-scheduledOrder-body',
    scheduledOrderBodyIndicatorOptions : {
      parent:  '#CC-scheduledOrder-body',
      posTop: '35em',
      posLeft: '50%'
    },

    /** Default options for creating a spinner on widget*/
    scheduledOrderIndicator: '#CC-scheduledOrder',
    scheduledOrderIndicatorOptions : {
      parent:  '#CC-scheduledOrder',
      posTop: '20em',
      posLeft: '50%'
    },

    /** Scheduled order viewmodel observable */
    scheduledOrder :ko.observable(),

    /** Display observable for page load*/
    display : ko.observable(false),

    /** Claimed Coupon Multi Promotions **/
    claimedCouponMultiPromotions: ko.observableArray(),

    /** Holds true if a valid back link is available **/
    isBackLinkAvailable: ko.observable(false),

    /**
     * Called when widget is first loaded:
     *    Bind callback methods context.
     *    Define computed properties.
     *    Define property validator and validation model.
     *    Define reference data for creating the scheduleMode select options (including opt groups).
     */
    onLoad: function (widget) {

      widget.scheduledOrder(ScheduledOrderViewModel.getInstance());

      $("body").attr("id" ,"CC-scheduledOrder-body")

      // Bind callback methods context.
      widget.saveScheduledOrderSuccess = widget.saveScheduledOrderSuccess.bind(widget);
      widget.saveScheduledOrderError = widget.saveScheduledOrderError.bind(widget);
      widget.deleteScheduledOrderSuccess = widget.deleteScheduledOrderSuccess.bind(widget);
      widget.deleteScheduledOrderError = widget.deleteScheduledOrderError.bind(widget);

      // Define computed properties.
      widget.suspended = ko.pureComputed({read: widget.isSuspended, write: widget.setSuspended}, widget);
      widget.parsedStartDate = ko.pureComputed({read: widget.getStartDate, write: widget.setStartDate}, widget);
      widget.parsedEndDate = ko.pureComputed({read: widget.getEndDate, write: widget.setEndDate}, widget);

      // Define property validator.
      widget.scheduledOrder().name.extend({
        required: {
          params: true,
          message: widget.translate('nameRequired')
        },
        maxLength: {
          params: CCConstants.REPOSITORY_STRING_MAX_LENGTH,
          message: widget.translate('maxlengthValidationMsg', {
            fieldName: widget.translate('nameText'),
            maxLength:CCConstants.REPOSITORY_STRING_MAX_LENGTH
          })
        }
      });

      widget.scheduledOrder().startDate.extend({
        required: {
          params: true,
          message: widget.translate('startDateRequired')
        }
      });

       widget.scheduledOrder().endDate.extend({
        validation: [{
            validator: function(obj) {
              var endDate = new Date(obj);
              var startDate = new Date(widget.scheduledOrder().startDate());
              return startDate < endDate;
            },
            message: widget.translate('endDateGreaterThanStartDateText'),
            onlyIf: function() {
              return widget.scheduledOrder().endDate();
            }
          }]
      });

      widget.scheduledOrder().daysOfWeek.extend({
        validation: [{
          validator: function(obj, params) {
            return obj.length >= params;
          },
          message: widget.translate('daysOfWeekRequired'),
          params: 1,
          onlyIf: function() {
            return widget.isDaysOfWeekEnabled();
          }
        }]
      });

      widget.scheduledOrder().weeksInMonth.extend({
        validation: [{
          validator: function(obj, params) {
            return obj.length >= params;
          },
          message: widget.translate('weeksInMonthRequired'),
          params: 1,
          onlyIf: function() {
            return widget.isWeeksInMonthEnabled();
          }
        }
        // This is disabled currently, May useful in future.
        // To re-enable uncomment below code block.
        // {
        //   validator: function(obj, params) {
        //     return obj.length < params;
        //   },
        //   message: widget.translate('weeksInMonthInvalidInput'),
        //   params: 5,
        //   onlyIf: function() {
        //     return widget.isWeeksInMonthEnabled() &&
        //       widget.scheduledOrder().daysOfWeek().length === 7;
        //   }
        // }
        ]
      });

      // Experimenting with validatedObservable functionality
      // so we can collectively check if all the properties are
      // valid using the isValid property
      // could also use ko.validation.group but would have to
      // manually add an errors collection
      widget.validationModel = ko.validatedObservable({
        name: widget.scheduledOrder().name,
        startDate: widget.scheduledOrder().startDate,
        endDate: widget.scheduledOrder().endDate,
        daysOfWeek: widget.scheduledOrder().daysOfWeek,
        weeksInMonth: widget.scheduledOrder().weeksInMonth
      });

      // Define Reference data for creating the scheduleMode select options (including opt groups)
      widget.scheduleModeOptGroups = [
        {
          label: widget.translate('daily'),
          options: [
            {
              text: widget.translate('onceADay'),
              value: CCConstants.SCHEDULE_MODE_ONCE_DAILY
            }
          ]
        },
        {
          label: widget.translate('weekly'),
          options: [
            {
              text: widget.translate('weekly'),
              value: CCConstants.SCHEDULE_MODE_WEEKLY
            }
          ]
        },
        {
          label: widget.translate('monthly'),
          options: [
            {
              text: widget.translate('onceAMonth'),
              value: CCConstants.SCHEDULE_MODE_ONCE_MONTHLY
            },
            {
              text: widget.translate('everyTwoMonths'),
              value: CCConstants.SCHEDULE_MODE_BI_MONTHLY
            },
            {
              text: widget.translate('quarterly'),
              value: CCConstants.SCHEDULE_MODE_QUARTERLY
            }
          ]
        }
      ];

      // Define a create spinner function with spinner options
      widget.createSpinner = function(pSpinner, pSpinnerOptions) {
        $(pSpinner).css('position', 'relative');
        $(pSpinner).addClass('loadingIndicator');
        spinner.create(pSpinnerOptions);
      };

      // Define a destroy spinner function with spinner id
      widget.destroySpinner = function(pSpinnerId) {
        $(pSpinnerId).removeClass('loadingIndicator');
        spinner.destroyWithoutDelay(pSpinnerId);
      };

      widget.isGiftCardUsed = ko.computed( function() {
        if (widget.scheduledOrder().templateOrder() && widget.scheduledOrder().templateOrder().payments) {
          var payments = widget.scheduledOrder().templateOrder().payments;
          for ( var i = 0; i < payments.length; i++) {
            if (payments[i].paymentMethod == CCConstants.GIFT_CARD_PAYMENT_TYPE) {
              return true;
            }
          }
        }
      });


      widget.claimedCouponMultiPromotionsArray = ko.computed( function() {
        if (widget.scheduledOrder().templateOrder() && widget.scheduledOrder().templateOrder().discountInfo) {
          for (var prop in widget.scheduledOrder().templateOrder().discountInfo.claimedCouponMultiPromotions) {
            var promotions = widget.scheduledOrder().templateOrder().discountInfo.claimedCouponMultiPromotions[prop];
            var couponMultiPromotion = [];
            couponMultiPromotion.code = prop;
            couponMultiPromotion.promotions = promotions;
            widget.claimedCouponMultiPromotions.push(couponMultiPromotion);
          }
        }
      });
    },

    /**
     * Called each time the widget is rendered:
     *    Ensure the user is authenticated, prompt to login if not.
     */
    beforeAppear: function (page) {
      var widget= this;

      widget.isBackLinkAvailable(true);
      if (!widget.isPreview() && !widget.historyStack.length) {
        this.isBackLinkAvailable(false);
      }
      
      // The user MUST be logged in to view this widget.
      if (!this.user().loggedIn() || this.user().isUserSessionExpired()) {
        navigation.doLogin(navigation.getPath(), this.links().home.route);

        return;
      }
      widget.claimedCouponMultiPromotions([]);

      //reset scheduled order when layout is changed
      widget.resetOrderDetails = function() {
        if (!(arguments[0].data.page.repositoryId === 'scheduledOrderPage')) {
          widget.scheduledOrder().reset();
          widget.display(false);
          widget.claimedCouponMultiPromotions([]);
          $.Topic(pubsub.topicNames.PAGE_LAYOUT_LOADED).unsubscribe(widget.resetOrderDetails);
          $.Topic(pubsub.topicNames.PAGE_METADATA_CHANGED).unsubscribe(widget.resetOrderDetails);
        }
      };

      //load the template after the data is loaded from rest endpoint
      widget.loadTemplate = function() {
        widget.validationModel.errors.showAllMessages(false);
        widget.destroySpinner(widget.scheduledOrderBodyIndicator);
        widget.display(true);
        if(this.message) {
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          notifier.sendError(widget.WIDGET_ID, this.message, true);
        }
        $.Topic(pubsub.topicNames.SCHEDULED_ORDER_LOAD_SCUCCESS).unsubscribe(widget.loadTemplate);
        widget.registerDatepickerEvents();
      };
      
      //Notify user if error occur when fetching data from server
      widget.loadErrorTemplate = function(){
        widget.validationModel.errors.showAllMessages(false);
        widget.destroySpinner(widget.scheduledOrderBodyIndicator);
        notifier.clearError(widget.WIDGET_ID);
        notifier.clearSuccess(widget.WIDGET_ID);
        notifier.sendError(widget.WIDGET_ID, this.message, true);
        $.Topic(pubsub.topicNames.SCHEDULED_ORDER_LOAD_ERROR).unsubscribe(widget.loadErrorTemplate);
      };
      
      $.Topic(pubsub.topicNames.PAGE_LAYOUT_LOADED).subscribe(widget.resetOrderDetails);
      $.Topic(pubsub.topicNames.PAGE_METADATA_CHANGED).subscribe(widget.resetOrderDetails);
      $.Topic(pubsub.topicNames.SCHEDULED_ORDER_LOAD_SCUCCESS).subscribe(widget.loadTemplate);
      $.Topic(pubsub.topicNames.SCHEDULED_ORDER_LOAD_ERROR).subscribe(widget.loadErrorTemplate);

      widget.createSpinner(widget.scheduledOrderBodyIndicator, widget.scheduledOrderBodyIndicatorOptions);
      var scheduledOrderId = "";
      if(page.contextId) scheduledOrderId = page.contextId;
      else scheduledOrderId = page.path.split("/")[1];
      widget.scheduledOrder().load(scheduledOrderId.trim());
    },

    /**
     * Register date picker events.
     */
    registerDatepickerEvents: function(){
      var startDateElementId = '#CC-scheduledOrder-startDate';
      var endDateElementId = '#CC-scheduledOrder-endDate';
      var widget = this;
      //blur events of datepicker
      $(startDateElementId).on('keyup', function(event) {
        $(startDateElementId).off('blur').on('blur', function(event){
          var startDate = widget.setDefaultDate(startDateElementId);
          widget.scheduledOrder().startDate(startDate);
          $(startDateElementId).off('blur');
        });
      });

      $(endDateElementId).on('keyup', function(event) {
        $(endDateElementId).off('blur').on('blur', function(event){
          var endDate = widget.setDefaultDate(endDateElementId);
          widget.scheduledOrder().endDate(endDate);
          $(endDateElementId).off('blur');
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
     * Determine if the scheduled order is inactive.
     *
     * @private
     * @return {boolean} - true is the scheduled order state is inactive, false otherwise.
     */
    isSuspended: function () {
      return this.scheduledOrder().state() === CCConstants.SCHEDULED_ORDER_STATE_INACTIVE;
    },

    /**
     * Activate/deactivate the scheduled order.
     *
     * @private
     * @param {boolean} suspend - If true set the scheduled order state to inactive, otherwise set to active.
     */
    setSuspended: function (suspend) {
      if (suspend === true) {
        this.scheduledOrder().state(CCConstants.SCHEDULED_ORDER_STATE_INACTIVE);
      }
      else {
        this.scheduledOrder().state(CCConstants.SCHEDULED_ORDER_STATE_ACTIVE);
      }
    },

    /**
     * parse the start date to mm/dd/yyyy format.
     *
     * @function
     * @private
     * @returns {object} - The formatted moment date object.
     */
    getStartDate: function() {
    	var startDate=this.scheduledOrder().startDate();
      if(startDate) {
        return ccDate.dateTimeFormatter(startDate,null,"short");
      } else {
        return "";
      }
    },

    /**
     * Set the start date to the model.
     *
     * @function
     * @private
     * @param {String} startDate - The new start date.
     */
    setStartDate: function(startDate) {
      this.scheduledOrder().startDate(startDate);
    },

    /**
     * parse the end date to mm/dd/yyyy format.
     *
     * @function
     * @private
     * @returns {object} - The formatted moment object, if end date is not null.
     */
    getEndDate: function() {
      var endDate = this.scheduledOrder().endDate();
      if (endDate) {
        return ccDate.dateTimeFormatter(endDate, null, "short");
      }
      else {
        return "";
      }
    },

    /**
     * Set the end date to the model.
     *
     * @function
     * @private
     * @param {String} endDate - The new end date.
     */
    setEndDate: function(endDate) {
      this.scheduledOrder().endDate(endDate);
    },

    /**
     * Determine if the daysOfWeek checkbox group should be enabled--daysOfWeek is only editable if the
     * scheduleMode is weekly.
     *
     * @private
     * @returns {boolean} - true if daysOfWeek should be enabled and false otherwise.
     */
    isDaysOfWeekEnabled: function () {
      var scheduleMode = this.scheduledOrder().scheduleMode();
      var isEnabled =
        scheduleMode === CCConstants.SCHEDULE_MODE_WEEKLY;
      return isEnabled;
    },

    // Determine if days of week is enabled
    daysOfWeekEnabled : function () {
      return ko.pureComputed(this.isDaysOfWeekEnabled, this);
    },

    /**
     * Determine if the weeksInMonth checkbox group should be enabled--weeksInMonth is only editable if the
     * scheduleMode is weekly.
     *
     * @private
     * @returns {boolean} - true if weeksInMonth should be enabled and false otherwise.
     */
    isWeeksInMonthEnabled: function () {
      var scheduleMode = this.scheduledOrder().scheduleMode();
      var isEnabled =
        scheduleMode === CCConstants.SCHEDULE_MODE_WEEKLY;
      return isEnabled;
    },

    // Determine if weeks in month is enabled
    weeksInMonthEnabled : function () {
      return ko.pureComputed(this.isWeeksInMonthEnabled, this);
    },

    /**
     * Determine if the weeksInMonth checkbox element should be set as readonly.
     *
     * @private
     * @param {HTMLInputElement} element - The checkbox element that will be tested in this computed property.
     * @returns {boolean} - true if the element should be readonly and false otherwise.
     */
    isWeeksInMonthReadonly: function (element) {
      var scheduledOrder = this.scheduledOrder()
      var scheduleMode = scheduledOrder.scheduleMode();
      var weeksInMonth = scheduledOrder.weeksInMonth();
      var daysOfWeek = scheduledOrder.daysOfWeek();
      var isReadonly =
        scheduleMode === CCConstants.SCHEDULE_MODE_WEEKLY &&
        weeksInMonth.length === 4 &&
        daysOfWeek.length === 7 &&
        !element.checked;

      return isReadonly;
    },

    /**
     * Create a computed property for the given weeksInMonth checkbox element, that will determine if the element
     * should be set as readonly.
     *
     * This is used to ensure that, when in weekly mode it is not possible to check/select all five weeks in month.
     * The reason being, that selecting all five weeks in month automatically changes the schedule mode to once a day,
     * and automatic mode change may be a disorienting user experience.
     *
     * @param {HTMLInputElement} element - The checkbox element that will be tested in this computed property.
     * @returns {ko.pureComputed<boolean>} - A computed property that is true if the element should be readonly and
     *    false otherwise.
     */
    weeksInMonthReadonly: function (element) {
      // Currently this feature is disabled. May useful in future.
      // To re-enable, uncomment below line and remove statement "return false;'
      // return ko.pureComputed(this.isWeeksInMonthReadonly.bind(this, element));

      return false;
    },

    /**
     * close the Scheduled Order if the page is not marked as dirty.
     */
    closeScheduledOrder: function () {
      var widget = this;
      if(this.scheduledOrder().dirtyFlag.isDirty()){
        // create new 'shown' event handler
        $('#cc-cancel-scheduleOrder-ModalContainer').one('show.bs.modal', function () {
          $('#cc-cancel-scheduleOrder-ModalPane').show();
        });

        // create new 'hidden' event handler
        $('#cc-cancel-scheduleOrder-ModalContainer').one('hide.bs.modal', function () {
          $('#cc-cancel-scheduleOrder-ModalPane').hide();
        });

        // open modal
        $('#cc-cancel-scheduleOrder-ModalContainer').modal('show');
      }
      else {
        widget.browserBack();
      }
    },

    /**
     * Save the changes to the Scheduled Order.
     */
    saveScheduledOrder: function () {
      if (this.validationModel.isValid()) {
        this.createSpinner(this.scheduledOrderIndicator, this.scheduledOrderIndicatorOptions);
        this.scheduledOrder().save(this.saveScheduledOrderSuccess, this.saveScheduledOrderError);
      }
      else {
        this.validationModel.errors.showAllMessages();
        notifier.clearError(this.WIDGET_ID);
        notifier.clearSuccess(this.WIDGET_ID);
        notifier.sendError(this.WIDGET_ID, this.translate('validationModalErrorMessage'), true);
      }
    },

    /**
     * Called when saveScheduledOrder was successful:
     *    Display notification and redirect.
     *
     * @private
     * @param {Object} result - The new Scheduled Order state.
     */
    saveScheduledOrderSuccess: function (result) {
      this.destroySpinner(this.scheduledOrderIndicator);
      notifier.sendSuccess(this.WIDGET_ID,this.translate('saveScheduledOrderSuccessMessage'), true);
    },

    /**
     * Called when saveScheduledOrder failed:
     *    Display notification.
     *
     * @private
     * @param {Object} result - The error state.
     */
    saveScheduledOrderError: function (result) {
      notifier.sendError(this.WIDGET_ID, this.translate('saveScheduledOrderErrorMessage', {result: result}), true);
      this.destroySpinner(this.scheduledOrderIndicator);
    },

    /**
     * Delete the current Scheduled Order.
     *
     * @param {ScheduleOrderViewModel} scheduledOrder - The current Schedule Order view model instance.
     */
    deleteScheduledOrder: function (scheduledOrder) {
      this.closeModalById('#cc-scheduleOrder-Modal');
      this.createSpinner(this.scheduledOrderIndicator, this.scheduledOrderIndicatorOptions);
      this.scheduledOrder().remove(this.deleteScheduledOrderSuccess, this.deleteScheduledOrderError);
    },

    /**
     * Called when deleteScheduledOrder was successful:
     *    Display notification and redirect.
     *
     * @private
     */
    deleteScheduledOrderSuccess: function () {
      notifier.clearError(this.WIDGET_ID);
      notifier.clearSuccess(this.WIDGET_ID);
      notifier.sendSuccessToPage(
        this.WIDGET_ID,
        this.translate('deleteScheduledOrderSuccessMessage'),
        true,
        null,
        true
      );
      this.destroySpinner(this.scheduledOrderIndicator);
      this.browserBack();
    },

    /**
     * Called when deleteScheduledOrder failed:
     *    Display notification.
     *
     * @private
     * @param {Object} result - The error state.
     */
    deleteScheduledOrderError: function (result) {
      notifier.clearError(this.WIDGET_ID);
      notifier.clearSuccess(this.WIDGET_ID);
      notifier.sendError(this.WIDGET_ID, this.translate('deleteScheduledOrderErrorMessage', {result: result}), true);
      this.destroySpinner(this.scheduledOrderIndicator);
    },

    /**
     * Redirect to the Schedule Order List page.
     *
     * @private
     */
    redirectToListingPage: function () {
      var widget = this;
      widget.closeModalById('#cc-cancel-scheduleOrder-ModalContainer');
      widget.user().validateAndRedirectPage(widget.links().profile.route);
    },
    
    /**
     * merge scheduled order items wit the cart items and navigate to cart page.
     *
     * @private
     */
    mergeToCart : function mergeToCart(data) {
    	var widget = this;

        widget.closeModalById('#cc-placeOrder-ModalContainer');
        widget.cart().mergeCart(true);
        widget.cart().loadTemplateOrderItems(data.scheduledOrder().templateOrderId());

      },
      
    /**
     * Go to the browser back
     */
    browserBack: function() {
      var widget = this;
      if(widget.isBackLinkAvailable()) {
        window.history.go(-1);
      } else {
        navigation.goTo(widget.links().profile.route);
      }
    },

    /**
     * Close the Modal based on Id.
     *
     * @param {String} modalId - The modal id.
     */
    closeModalById: function (modalId) {
      $(modalId).modal('hide');
      $('body').removeClass('modal-open');
      $('.modal-backdrop').remove();
    }
  };
});