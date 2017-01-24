/**
 * @fileoverview spaceInviteAcceptWidget
 *
 */
/*global window: false */
define(

//-------------------------------------------------------------------
// DEPENDENCIES
//-------------------------------------------------------------------
['jquery', 'knockout', 'pubsub', 'notifier', 'ccLogger', 'CCi18n', 'swmRestClient', 'spinner', 'navigation', 'storageApi'],

//-------------------------------------------------------------------
// MODULE DEFINITION
//-------------------------------------------------------------------
function($, ko, PubSub, notifier, logger, CCi18n, swmRestClient, spinner, navigation, storageApi) {

  "use strict";

  return {

    // Observables
    inviteToken: ko.observable(''),

    /**
     * Runs the first time the module is loaded on a page.
     * It is passed the widgetViewModel which contains the configuration from the server.
     */
    onLoad : function(widget) {
      widget.WIDGET_ID = "spaceInviteAccept";
      swmRestClient.init(widget.site().tenantId, widget.isPreview(), widget.locale());
      
      $.Topic(PubSub.topicNames.SOCIAL_SPACE_UNAVAILABLE).subscribe(function(obj) {
        widget.resetWidget();
      });


      
      // subscribe to when name/value parameters appear in the url
      $.Topic(PubSub.topicNames.PAGE_PARAMETERS).subscribe(
        function () {
          widget.checkPageParametersCallback(this);
        }
      );
      
    },

    /**
     * Runs late in the page cycle on every page where the widget will appear.
     */
    beforeAppear : function(page) {
      var widget = this;
      // SC-4109, If IE9 AND User is logged in AND Invite in progress 
      // AND MORE
      if ( page.pageId === 'wishlist' && widget.user().loggedIn() && storageApi.getInstance().getItem("social.invite"))
      {
        widget.joinMemberToSpace(storageApi.getInstance().getItem("social.invite"));
      }
    },
    
    /**
     * Executed when name/value parameters are detected in the page url.
     */
    checkPageParametersCallback : function(page) {
      var widget = this;

      var processInvitationCB = function() {
        // show modal content right away
        widget.cart().emptyCart();
        storageApi.getInstance().setItem('social.invite', page.parameters.invite);
        navigation.doLogin(navigation.getPath(), widget.links().wishlist.route);
        
        // if the user cancel or exits the login/registration modal, cancel the invite process.
        $('#CC-headermodalpane').one('hidden.bs.modal', function() {
          if( !widget.user().loggedIn() ){
            widget.resetInvitationDetected();
          }
        });
      };

      // if on spaces page and has invite parameter, execute accept invite flow.
      if (page.pageId === 'wishlist' && page.parameters.invite) {
        // log them out and prompt for login
        swmRestClient.clear();
        widget.space().inviteAcceptInProgress(true);
        
        widget.space().showSpace(false);
        widget.inviteToken(page.parameters.invite);
        $.Topic(PubSub.topicNames.USER_LOGOUT_SUBMIT).publishWith(widget.user(), [{message: "success"}]);
        window.setTimeout(processInvitationCB, 2000);
      }
    },


    /**
     * Method to make a REST call to add a member to a space.
     */
    joinMemberToSpace: function(tokenParam) {
      var widget = this;

      //widget.spinnerShow();

      var json = {
        invitationToken: tokenParam
      };

      var successCB = function(result) {
        //widget.spinnerHide();
        widget.resetWidget();

        if (result.response.code.indexOf("200.4") === 0){

          var inviteAcceptedMsg = ko.computed(function() {
            var successText = CCi18n.t(
              'ns.spaceinviteaccept:resources.invitationAccepted',
              {
                userdisplayname: widget.user().firstName()
              }
            );
            return successText;
          }, widget);
          
          notifier.sendSuccess(widget.WIDGET_ID, ( inviteAcceptedMsg() ), true);

          $.Topic(PubSub.topicNames.SOCIAL_SPACE_MEMBER_JOIN).publish({"spaceid" : result.spaceId});
        }
        else {
          // invalid invitation (for whatever reason), is treated as expired
          notifier.sendError(widget.WIDGET_ID, widget.translate('invitationExpiredError'), true);
          // error occurred, show default space
          $.Topic(PubSub.topicNames.SOCIAL_REFRESH_SPACES).publish(true);
        }

        widget.resetInvitationDetected();
      };

      var errorCB = function(err) {
        //widget.spinnerHide();
        widget.resetWidget();
        try {
          var errResponse = JSON.parse(err);
          if (errResponse['o:errorCode'] === "409.0") {
            notifier.sendSuccess(widget.WIDGET_ID, widget.translate('invitationAlreadyAccepted'), true);
          }
          else if (errResponse['o:errorCode'] ==="404.0") {
            notifier.sendError(widget.WIDGET_ID, widget.translate('spaceNotFound'), true);
          }
          else {
            notifier.sendError(widget.WIDGET_ID, widget.translate('memberJoinError'), true);
          }
        } catch (e) {
          logger.error("error occurred while attempting to accept invitation: " + e);
        }

        widget.resetInvitationDetected();
        // error occurred, show default space
        $.Topic(PubSub.topicNames.SOCIAL_REFRESH_SPACES).publish(true);
      };
      swmRestClient.request('POST', '/swm/rs/v1/members', json, successCB, errorCB);
    },

    /**
     * Clear all error messages in the notification bar for this widget
     */
    clearAllErrorNotifications : function() {
      var widget = this;
      notifier.clearError(widget.WIDGET_ID);
    },

    /**
     * Clear all success messages in the notification bar for this widget
     */
    clearAllSuccessNotifications : function() {
      var widget = this;
      notifier.clearSuccess(widget.WIDGET_ID);
    },

    /**
     * Show the spinner
     */
    spinnerShow : function() {
      var widget = this;
      var productLoadingOptions = {
        parent : '#SWM-spinner',
        selector : '#SWM-spinner-area',
        posTop : '-90px',
        posLeft: '48%',
        processingText: widget.translate('pleaseWaitProcessingMsg'),
        processingPosTop: '30%'
      };
      spinner.create(productLoadingOptions);
      $('.cc-spinner').css('position', 'fixed');
      $('.cc-spinner').css('z-index', '2000');
      spinner.loadCheck(null);
    },

    /**
     * Hide the spinner
     */
    spinnerHide : function() {
      var widget = this;
      spinner.destroy();
    },

    /**
     * Reset Widget (visual elements)
     */
    resetWidget : function() {
      var widget = this;
    },

    /** Resets the observable related to detecting whether an invitation is present.
     */
    resetInvitationDetected : function() {
      var widget = this;
      widget.space().inviteAcceptInProgress(false);
      storageApi.getInstance().removeItem("social.invite");
      widget.inviteToken('');
    }
  };
});
