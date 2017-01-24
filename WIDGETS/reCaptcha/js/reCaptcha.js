/**
 * @fileoverview Google reCaptcha
 * Option.
 * @author ian.davis@oracle.com
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'reCaptcha',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'ccConstants', '//www.google.com/recaptcha/api/js/recaptcha_ajax.js'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub, CCConstants) {

    "use strict";

    var widget;

    return {

      onLoad: function(widgetModel) {
        widget = widgetModel;

        var PUBLIC_ID = '6Lefq_YSAAAAAHqNqmi_yAnqVR1VZuc9qFYsxnkT';

//        $('#CC-userRegistration').append('<div id="reCaptcha-anchor"/>');

        Recaptcha.create(PUBLIC_ID,
          "reCaptcha-anchor",
          {
            theme: "white",
            lang : 'en'
          }
        );


      }
    };
  }
);