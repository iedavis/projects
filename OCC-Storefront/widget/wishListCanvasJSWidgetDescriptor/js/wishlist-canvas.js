/**
 * @fileoverview wishListCanvas
 *
 */
/*global window: false */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['jquery', 'knockout', 'pubsub', 'notifier', 'CCi18n', 'ccRestClient', 'ccConstants', 'facebookjs'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function($, ko, PubSub, notifier, CCi18n, ccRestClient, CCConstants, facebookjs) {

    "use strict";

    return {

      WIDGET_ID : "wishListCanvas",
      fromFBNotification: ko.observable(true),
      siteFbAppId: ko.observable(''),
      showWidget: ko.observable(false),
      
      /**
       * Runs once when the widget is loaded.
       */
      onLoad: function (widget) {
        facebookjs.init(widget.isPreview());
        if (window.FacebookCanvas) {
          widget.showWidget(true);
        }
        var loc = new String(document.location);  
        
        if (loc.indexOf("fb_source") == -1 ||
            loc.indexOf("ref") == -1 || 
            loc.indexOf("request_ids") == -1 || 
            loc.indexOf("notif_t") == -1) {
          widget.fromFBNotification(false);
          
        }
        
        $.Topic(PubSub.topicNames.SOCIAL_FACEBOOK_JS_READY).subscribe(
          function(){
            widget.processRequestAcceptance();
          }
        );
      },
      
      
    processRequestAcceptance: function() {
      var widget = this;
      if (!widget.fromFBNotification()){
        return;
      }
      var fb_source = widget.getParameterByName('fb_source');
      var requestids = widget.getParameterByName('request_ids');
      var reference = widget.getParameterByName('ref');
      var notification_type = widget.getParameterByName('notif_t');
            
      if (fb_source === 'notification' &&
          reference === 'notif' &&
          (notification_type === 'app_invite' ||
           notification_type === 'app_request')) {
          
        // requestids may contain a list of request IDs delimited commas, which
        // could be the case if there are old requests that haven't been deleted.
        // We just want the last one.
        var pos = requestids.lastIndexOf(","); 
        var requestID = requestids.substring(pos+1);
        FB.getLoginStatus(function(response) {
          if (response.status === 'connected') {
            widget.getFbRequestData(requestID);
          }
          else {
            widget.processLogin(requestID);
          }
        });
      }    
    },
    
    processLogin: function(requestID) {
      var widget = this;
      FB.login(function(response) {
        if (response.status === 'connected') {
          widget.getFbRequestData(requestID);
        }
      });
    },
      
    getFbRequestData: function(requestID) {
      var widget = this;
      // This call will prompt the user to approve access to his FB user profile
      FB.api('/' + requestID, 
        function(response){
          if (response.id != null) {
            // Do invitation flow
            window.location.assign("https://" + window.location.host + widget.links().wishlist.route + "?" + response.data);
            
            // delete request in FB
            FB.api(requestID, 'delete', function(response) {
              // success handling
            });
          }
        }
      );
    },
  
    getParameterByName: function(name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
        var results = regex.exec(location.href);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
      },

      /**
       * Reset widget
       */
      resetWidget : function() {
        
      },

      /**
       * Runs late in the page cycle on every page where the widget will appear.
       */
      beforeAppear : function(page) {

      }

    };
  }
);
