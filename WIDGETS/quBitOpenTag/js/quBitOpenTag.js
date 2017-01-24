/**
 * @fileoverview QuBit OpenTag.
 * Option.
 * @author ian.davis@oracle.com
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'quBitOpenTag',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub, CCConstants) {

    "use strict";

    var widget;

    return {

      onLoad: function(widgetModel) {

        widget = widgetModel;

        var tokens = widgetModel.openTagScript().split("/");
        var scriptName = tokens[tokens.length - 1];

        if(scriptName.substr(0,8) === 'opentag-' &&
           scriptName.substr(scriptName.length-3,3) === '.js'){

          $.Topic("UV_READY").subscribe(this.uvReady);

        } else {
          console.log('OpenTag script filename is non-standard. It has not been loaded.');
        }

      },

      uvReady: function(){

        if( $('OpenTagScript').length === 0 ){
//        First time through
          $('head').append('<script id="OpenTagScript" src="' + widget.openTagScript() + '" async defer></script>');
        } else {

//        Simulate a Page reload to QuBit script
          console.log('Simulate Page View to QuBit');

        }


      }
    };
  }
);