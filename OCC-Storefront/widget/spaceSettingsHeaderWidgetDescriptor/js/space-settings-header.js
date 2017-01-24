/**
 * @fileoverview spaceSettingsHeaderWidget
 *
 */
/*global window: false */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['jquery','knockout', 'pubsub', 'notifier', 'ccLogger', 'CCi18n'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function($, ko, PubSub, notifier, logger, CCi18n) {

    "use strict";

    return {
      // Widget Id
      WIDGET_ID: "spaceSettingsHeader",

      // Observables
      showWidget: ko.observable(false),

      /**
       * Runs the first time the module is loaded on a page.
       * It is passed the widgetViewModel which contains the configuration from the server.
       */
      onLoad: function(widget) {

        //TODO: subscription to $Topics

        widget.backToMyAcctTxt = ko.computed(function() {
          var successText = '<< ' + CCi18n.t(
              'ns.spacesettingsheader:resources.spaceSettingsHeaderBackToAcctText'
            );
            return successText;
          }, widget);
      },

      /**
       * Runs late in the page cycle on every page where the widget will appear.
       */
      beforeAppear: function(page) {
        var widget = this;
        if (widget.user().loggedIn() == false) {
          widget.showWidget(false);
        }
        else {
          widget.showWidget(true);
        }
      }

    }; //return
  }
);
