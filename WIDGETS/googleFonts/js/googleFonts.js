/**
 * @fileoverview Google Fonts.
 * Option.
 * @author ian.davis@oracle.com
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'googleFonts',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko) {

    "use strict";

    var fontFamilyString = '';
    var fontCharSetsString = 'latin';
    var fontStyleString = '';

    return {

      onLoad: function(widgetModel) {
        widgetModel.constructCSSstrings('body', widgetModel.baseFontFamily(), widgetModel.baseFontStyle(), widgetModel.baseFontCharSets());
        widgetModel.constructCSSstrings('h1', widgetModel.h1FontFamily(), widgetModel.h1FontStyle(), widgetModel.h1FontCharSets());
        widgetModel.constructCSSstrings('h2', widgetModel.h2FontFamily(), widgetModel.h2FontStyle(), widgetModel.h2FontCharSets());
        widgetModel.constructCSSstrings('h3', widgetModel.h3FontFamily(), widgetModel.h3FontStyle(), widgetModel.h3FontCharSets());
        widgetModel.constructCSSstrings('p', widgetModel.pFontFamily(), widgetModel.pFontStyle(), widgetModel.pFontCharSets());
        widgetModel.injectHTML();
      },

      constructCSSstrings: function(elementType, fontFamily, fontStyle, fontCharSets) {
        if (fontFamily !== '') {
          fontFamilyString = fontFamilyString + fontFamily.replace(' ', '+');
          if (fontStyle !== '') fontFamilyString = fontFamilyString + ':' + fontStyle;
          fontFamilyString = fontFamilyString + '|';

          if (fontCharSets !== '') fontCharSetsString = fontCharSetsString + ',' + fontCharSets;

          fontStyleString = fontStyleString + elementType + "{font-family:'" + fontFamily + "'";
          if (fontStyle !== '') fontStyleString = fontStyleString + ";font-weight:" + fontStyle;
          fontStyleString = fontStyleString + '} ';
        }
      },

      injectHTML: function() {
        if (fontFamilyString !== '') {
          $("head").append("<link href='https://fonts.googleapis.com/css?family=" + fontFamilyString + "&subset=" + fontCharSetsString + "' rel='stylesheet' type='text/css'>");
          $("head").append("<style> " + fontStyleString + " </style>");
        }
      }
    };
  }
);