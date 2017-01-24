/**
 * @fileoverview Notifications Widget.
 * 
 * 
 */
define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'notifier'],
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, pubsub, notifier) {
    
    "use strict";
    
    return {
      templateName: ko.observable(),
      timeoutMessageTypes: ko.observableArray(),
      isTemplateMessage: ko.observable(false),
      
      onLoad: function(widget) {
        widget.timeoutMessageTypes(['info', 'success', 'warning']);
        // Subscribe to the notification call. Whenever there is a notification
        // call, activate a timer that runs on the timeout configuration value.
        // Close the notification once the time runs out.
        $.Topic(pubsub.topicNames.NOTIFICATION_ADD).subscribe(function() {
          // Check if we want the notification to stick to the top of the page
          // If so add the class 'fixed-to-top'
          var element = $('#cc-notifications');
          if (widget.fixed()) {
            if (!element.hasClass('fixed-to-top')) {
              element.addClass('fixed-to-top');
            }
          } else {
            if (element.hasClass('fixed-to-top')) {
              element.removeClass('fixed-to-top');
            }
          }
          var message = this;
          var widgetId = message.id();
          var type = message.type();
          // Check if the message if one of the custom types. If so override
          // the body text to add custom HTML.
          if (widget.isTemplateMessage()) {
            var element = $(".cc-notification-message").html($('<div />').attr("data-bind", "template: '" + widget.templateName() + "'"));
            ko.cleanNode($(element)[0]);
            ko.applyBindings(widget.viewModel, $(element)[0]);
          }
          if (widget.timeoutEnabled() && (widget.timeoutEnabled() != "false") && widget.timeout() && (widget.timeoutMessageTypes.indexOf(type) > -1)) {
            setTimeout(
                function() {
                  notifier.clearMessage(widgetId, type);
                }, parseInt(widget.timeout() * 1000));
          }
          // Reset the template flag.
          widget.isTemplateMessage(false);
        });
        // Subscribe to the template based notification call.
        // Send the actual notifications message. Also update the 
        // isTemplateMessage flag so that the template is rendered.
        $.Topic(pubsub.topicNames.NOTIFICATION_TEMPLATE_ADD).subscribe(function(message, viewModel, templateName) {
          widget.viewModel = viewModel;
          widget.templateName(templateName);
          // Set the template flag.
          widget.isTemplateMessage(true);
          // Send the actual notification.
          $.Topic(pubsub.topicNames.NOTIFICATION_ADD).publishWith(
              message, [{message:"success"}]);
        });
     }
    };
  }
);
