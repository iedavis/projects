/**
 * @fileoverview Collection Hero Image.
 * Option.
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'collectionHeroImg',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko) {

    "use strict";

    var widget;

    return {

      collectionImgUrl : ko.observable(),

      onLoad: function(widgetModel) {
        widget = widgetModel;
        widget.urlCache = [];
      },

      beforeAppear: function(page) {

        var foundInCache = false;
        for(var i = 0; i < widget.urlCache.length; i++){
          if (widget.urlCache[i].categoryId === widget.category().id) {
            foundInCache = true;
            widget.collectionImgUrl(widget.urlCache[i].url);
            break;
          }
        }

        if (foundInCache === false) {

          var generalRoot = "/file/general/";
          var imageUrl = generalRoot + widget.imgNamePattern().replace('{id}', widget.category().id);
          var parentImageUrl = generalRoot + widget.imgNamePattern().replace('{id}', widget.category().fixedParentCategories[0].id);

          widget.collectionImgUrl('');

          $.get( imageUrl )
              .done(function() {
                  widget.collectionImgUrl( imageUrl );
                  widget.cacheItem(widget.collectionImgUrl());
              })
              .fail(function() {
                  $.get( parentImageUrl )
                      .done(function() {
                          widget.collectionImgUrl( parentImageUrl );
                          widget.cacheItem(widget.collectionImgUrl());
                      })
                      .fail(function() {
                          $.get( widget.defaultImageName() )
                              .done(function() {
                                  widget.collectionImgUrl( widget.defaultImageName() );
                                  widget.cacheItem(widget.collectionImgUrl());
                              })
                              .fail(function() {
                                  widget.collectionImgUrl('');
                                  widget.cacheItem(widget.collectionImgUrl());
                              });

                      });

              });

        }
      },

      cacheItem: function(url) {
        var cacheItem = {};
        cacheItem.categoryId = widget.category().id;
        cacheItem.url = url;
        widget.urlCache.push(cacheItem);
      }
    };
  }
);