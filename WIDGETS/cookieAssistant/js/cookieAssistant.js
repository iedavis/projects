/**
 * @fileoverview Cookie Assistant Widget.
 * http://cookieassistant.com/
 * Option.
 * @author ian.davis@oracle.com
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'cookieAssistant',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['jquery', 'knockout', 'pubsub', 'jquery.cookie'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub) {

    "use strict";

    return {

      onLoad: function(widget) {

        var cookieAssistantScpt = document.createElement('script');
        cookieAssistantScpt.id = 'cookieAssistantScpt'; cookieAssistantScpt.type = 'text/javascript';
        cookieAssistantScpt.src = 'http://app.cookieassistant.com/widget.js?token=-6gpqmL5RUDUSiq-2ZBJ2Q';
        document.getElementById('main').appendChild(cookieAssistantScpt);

        var cookieAssistantDiv = document.createElement('div');
        cookieAssistantDiv.id = 'cookie_assistant_container';
        document.getElementById('main').appendChild(cookieAssistantDiv);

      }
    };
  }
);