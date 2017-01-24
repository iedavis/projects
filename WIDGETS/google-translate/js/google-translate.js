/**
 * @fileoverview Pintrest PinIt Widget. 
 * @author Ian Davis
 */
define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'google-translate',
  
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'CCi18n', 'pubsub'],
  
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, CCi18n, pubsub) {

    "use strict";

    var widget;

    return{

      onLoad: function(widgetModel) {

        widget = widgetModel;

          $("head").append('<meta name="google-translate-customization" content="abc749ad0c4a6cb-e2e5f33b5aac1419-gbeafda6efb4eef83-e"></meta>');

      },

      widgetWillAppearOnPage: function(page){




        function googleTranslateElementInit() {
          new google.translate.TranslateElement({pageLanguage: 'en', layout: google.translate.TranslateElement.InlineLayout.SIMPLE}, 'google_translate_element');
        }


        $("#google_translate_element").append('<script type="text/javascript" src="//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit"></script>');






      }

    };
  }
);