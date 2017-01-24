/**
 * @fileoverview Collection Hero Image.
 * Option.
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'metaLocator',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
//  ['knockout', 'https://maps.googleapis.com/maps/api/js?v=3&sensor=false'],
  ['knockout'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko) {

    "use strict";

    var widget;

    return {

      onLoad: function(widgetModel) {
        widget = widgetModel;
      },

      beforeAppear: function(page) {
        $.getScript( 'https://maps.googleapis.com/maps/api/js?v=3&sensor=false' )
          .done(function( script, textStatus ) {
              $('#storeLocatorAnchorPoint').append("<iframe src='http://code.metalocator.com/index.php?option=com_locator&view=directory&layout=combined&tmpl=component&framed=1&source=js&Itemid=" + widget.metaLocatorId() + "' id='metaLocatorFrame' frameborder='no' width='100%' height='" + widget.frameHeight() + "' ></iframe>");
          });
      }
    };
  }
);
