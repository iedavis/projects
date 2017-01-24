/**
 * @fileoverview Customer Profile Widget.
 * 
 */
define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'navigation', 'viewModels/address', 'notifier', 'ccConstants', 'ccPasswordValidator', 'CCi18n', 'swmRestClient', 'ccRestClient'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, PubSub, navigation, Address, notifier, CCConstants, CCPasswordValidator, CCi18n, swmRestClient, CCRestClient) {
  
    "use strict";
        
    return {
      
      WIDGET_ID:        "customerProfile",
      ignoreBlur:   ko.observable(false),
      // Property to show edit screen.
      isUserDetailsEdited:                ko.observable(false),
      isUserProfileShippingEdited:        ko.observable(false),
      isUserProfileDefaultAddressEdited : ko.observable(false),
      isUserProfilePasswordEdited:        ko.observable(false),
      interceptedLink: ko.observable(null),
      isUserProfileInvalid: ko.observable(false),
      showSWM : ko.observable(true),
      isProfileLocaleNotInSupportedLocales : ko.observable(),
      
      beforeAppear: function (page) {
         // Every time the user goes to the profile page,
         // it should fetch the data again and refresh it.
        var widget = this;
        
        widget.user().ignoreEmailValidation(false);
        // Checks whether the user is logged in or not
        // If not the user is taken to the home page
        // and is asked to login.
        if (widget.user().loggedIn() == false) {
          navigation.doLogin(navigation.getPath(), widget.links().home.route);
        } else if (widget.user().isSessionExpiredDuringSave()) {
          widget.user().isSessionExpiredDuringSave(false);
        } else {
          //reset all the password detals
          widget.user().resetPassword();
          this.getSubmittedOrderCountForProfile(widget);
          widget.showViewProfile(true);
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
        }
      },
      
      onLoad: function(widget) {
        var self = this;
        var isModalVisible = ko.observable(false);
        var clickedElementId = ko.observable(null);
        var isModalSaveClicked = ko.observable(false);
        
        widget.ErrorMsg = widget.translate('updateErrorMsg');
        widget.passwordErrorMsg = widget.translate('passwordUpdateErrorMsg');
        
        widget.getProfileLocaleDisplayName = function() {
          //Return the display name of profile locale
          for (var i=0; i<widget.site().additionalLanguages.length; i++) {
            if (widget.user().locale() === widget.site().additionalLanguages[i].name) {
              return widget.site().additionalLanguages[i].displayName; 
            }
          }
        };
        
        //returns the edited locale to be displayed in non-edit mode
        widget.getFormattedProfileLocaleDisplayName = function(item) {
          return item.name.toUpperCase() + ' - ' + item.displayName;
        };
        
        // Clear edit profiles and set it to view only mode.
        widget.showViewProfile = function (refreshData) {
          // Fetch data in case it is modified or requested to reload.
          // Change all div tags to view only.
          if(refreshData) {
            widget.user().getCurrentUser(false);
          }
          widget.isUserDetailsEdited(false);
          widget.isUserProfileShippingEdited(false);
          widget.isUserProfileDefaultAddressEdited(false);
          widget.isUserProfilePasswordEdited(false);
        };
        
        // Reload shipping address.
        widget.reloadShipping = function() {
          //load the shipping address details
          if (widget.user().updatedShippingAddressBook) {
            var shippingAddresses = [];
            for (var k = 0; k < widget.user().updatedShippingAddressBook.length; k++)
            {
              var shippingAddress = new Address('user-shipping-address', widget.ErrorMsg, widget, widget.shippingCountries(), widget.defaultShippingCountry());
              shippingAddress.countriesList(widget.shippingCountries());
              
              shippingAddress.copyFrom(widget.user().updatedShippingAddressBook[k], widget.shippingCountries());
              shippingAddress.resetModified();
              shippingAddress.country(widget.user().updatedShippingAddressBook[k].countryName);
              shippingAddress.state(widget.user().updatedShippingAddressBook[k].regionName);
              shippingAddresses.push(shippingAddress);
            }
              
            widget.user().shippingAddressBook(shippingAddresses);
          }
        };

        /**
         * Ignores password validation when the input field is focused.
         */
        widget.passwordFieldFocused = function(data, event) {
          if (this.ignoreBlur && this.ignoreBlur()) {
            return true;
          }
          this.user().ignorePasswordValidation(true);
          return true;
        };

        /**
         * Password is validated when the input field loses focus (blur).
         */
        widget.passwordFieldLostFocus = function(data, event) {
          if (this.ignoreBlur && this.ignoreBlur()) {
            return true;
          }
          this.user().ignorePasswordValidation(false);
          return true;
        };

        /**
         * Ignores confirm password validation when the input field is focused.
         */
        widget.confirmPwdFieldFocused = function(data, event) {
          if (this.ignoreBlur && this.ignoreBlur()) {
            return true;
          }
          this.user().ignoreConfirmPasswordValidation(true);
          return true;
        };

        /**
         * Confirm password is validated when the input field loses focus (blur).
         */
        widget.confirmPwdFieldLostFocus = function(data, event) {
          if (this.ignoreBlur && this.ignoreBlur()) {
            return true;
          }
          this.user().ignoreConfirmPasswordValidation(false);
          return true;
        };

        /**
         * Ignores the blur function when mouse click is up
         */
        widget.handleMouseUp = function() {
            this.ignoreBlur(false);
            this.user().ignoreConfirmPasswordValidation(false);
            return true;
          };
          
          /**
           * Ignores the blur function when mouse click is down
           */
          widget.handleMouseDown = function() {
            this.ignoreBlur(true);
            this.user().ignoreConfirmPasswordValidation(true);
            return true;
          };
          
        widget.hideModal = function () {
          if(isModalVisible() || widget.user().isSearchInitiatedWithUnsavedChanges()) {
            $("#CC-customerProfile-modal").modal('hide');
            $('body').removeClass('modal-open');
            $('.modal-backdrop').remove();
          }
        };
        
        widget.showModal = function () {
          $("#CC-customerProfile-modal").modal('show');
          isModalVisible(true);
        };
        
        // Handle cancel update.
        widget.handleCancelUpdate = function () {
          widget.showViewProfile(false);
          widget.handleCancelUpdateDiscard();
        };
        
        // Discards user changes and reset the data which was saved earlier.
        widget.handleCancelUpdateDiscard = function () {
          // Resets every thing.
          widget.showViewProfile(true);
          widget.user().resetPassword();
          widget.user().editShippingAddress(null);
          widget.reloadShipping();
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          widget.hideModal();
        };
        
        // Discards user changes and navigates to the clicked link.
        widget.handleModalCancelUpdateDiscard = function () {
          widget.handleCancelUpdate();
          if ( widget.user().isSearchInitiatedWithUnsavedChanges() ) {
            widget.hideModal();
            widget.user().isSearchInitiatedWithUnsavedChanges(false);
          }
          else {
            widget.navigateAway();
          }
        };
        
        // Add new Shipping address, then display for editing.
        widget.handleCreateShippingAddress = function () {
          var addr = new Address('user-shipping-address', widget.ErrorMsg, widget, widget.shippingCountries(), widget.defaultShippingCountry());
          widget.editShippingAddress(addr);
        },
        
        widget.editShippingAddress = function (addr) {
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          widget.user().editShippingAddress(addr);
          widget.isUserProfileShippingEdited(true);
          $('#CC-customerProfile-sfirstname').focus();
          if (widget.shippingCountries().length == 0) {
            $('#CC-customerProfile-shippingAddress-edit-region input').attr('disabled', true);
          }
        };
        
        widget.handleSelectDefaultShippingAddress = function (addr) {
          widget.selectDefaultShippingAddress(addr);
          widget.isUserProfileDefaultAddressEdited(true);
        };
        
        widget.handleRemoveShippingAddress = function (addr) {
          widget.user().shippingAddressBook.remove(addr);
          widget.user().deleteShippingAddress(true);
          
          // If addr was the default address, reset the default address to be the first entry.
          if (addr.isDefaultAddress() && widget.user().shippingAddressBook().length > 0) {
            widget.selectDefaultShippingAddress(widget.user().shippingAddressBook()[0]);
          }
          
          // If we delete the last user address, notify other modules that might have
          // cached it.
          if (widget.user().shippingAddressBook().length === 0) {
          	$.Topic(PubSub.topicNames.USER_PROFILE_ADDRESSES_REMOVED).publish();
          }
          
          widget.handleUpdateProfile();
        };
        
        widget.selectDefaultShippingAddress = function (addr) {
          widget.user().selectDefaultAddress(addr);
        };
        
        // Display My Profile to edit.
        widget.editUserDetails = function () {
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          widget.isUserDetailsEdited(true);
          $('#CC-customerProfile-edit-details-firstname').focus();
        };
         
        //Displays the Password edit option
        widget.editUserPassword = function () {
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          widget.isUserProfilePasswordEdited(true);
          widget.user().isChangePassword(true);
          $('#CC-customerProfile-soldPassword').focus();
        };
        
        // Handles User profile update
        widget.handleUpdateProfile = function () {

          if(widget.isUserProfilePasswordEdited()) {
            widget.user().isPasswordValid();
          }
          
          // Sends message for update
          $.Topic(PubSub.topicNames.USER_PROFILE_UPDATE_SUBMIT).publishWith(widget.user(), [{message: "success"}]);
        };
        
        // Handles User profile update and navigates to the clicked link.
        widget.handleModalUpdateProfile = function () {
          isModalSaveClicked(true);
          if ( widget.user().isSearchInitiatedWithUnsavedChanges() ) {
            widget.handleUpdateProfile();
            widget.hideModal();
            widget.user().isSearchInitiatedWithUnsavedChanges(false);
            return;
          }
          if (clickedElementId != "CC-loginHeader-myAccount") {
            widget.user().delaySuccessNotification(true);
          }
          widget.handleUpdateProfile();
        };
        
        
       // Handles if data does not change. 
        $.Topic(PubSub.topicNames.USER_PROFILE_UPDATE_NOCHANGE).subscribe(function() {
          // Resetting profile.
          widget.showViewProfile(false);
          // Hide the modal.
          widget.hideModal();
        });

        //handle if the user logs in with different user when the session expiry prompts to relogin
        $.Topic(PubSub.topicNames.USER_PROFILE_SESSION_RESET).subscribe(function() {
          // Resetting profile.
          widget.showViewProfile(false);
          // Hide the modal.
          widget.hideModal();
		  //get the count of the orders
          widget.getSubmittedOrderCountForProfile(widget);
        });
        
        // Handles if data is invalid.
        $.Topic(PubSub.topicNames.USER_PROFILE_UPDATE_INVALID).subscribe(function() {
          notifier.sendError(widget.WIDGET_ID, widget.ErrorMsg, true);
          if (isModalSaveClicked()) {
            widget.isUserProfileInvalid(true);
            isModalSaveClicked(false);
          }
          widget.user().delaySuccessNotification(false);
          // Hide the modal.
          widget.hideModal();
        });
        
        // Handles if user profile update is saved.
        $.Topic(PubSub.topicNames.USER_PROFILE_UPDATE_SUCCESSFUL).subscribe(function() {
          // update user in Social module
          if (widget.displaySWM) {
            var successCB = function(result){
              $.Topic(PubSub.topicNames.SOCIAL_SPACE_SELECT).publish();
              $.Topic(PubSub.topicNames.SOCIAL_SPACE_MEMBERS_INFO_CHANGED).publish();
            };
            var errorCB = function(response, status, errorThrown){};
            
            var json = {};
            if (widget.user().emailMarketingMails()) {
              json = {
                firstName : widget.user().firstName()
                , lastName : widget.user().lastName()
              };
            }
            else {
              json = {
	                firstName : widget.user().firstName()
	                , lastName : widget.user().lastName()
	                , notifyCommentFlag : '0'
                  , notifyNewMemberFlag : '0'
	            };
	          }
	          
            swmRestClient.request('PUT', '/swm/rs/v1/users/{userid}', json, successCB, errorCB, {
              "userid" : swmRestClient.apiuserid
            });
          }
          
          widget.showViewProfile(true);
          // Clears error message.
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          if (!widget.user().delaySuccessNotification()) {
            notifier.sendSuccess(widget.WIDGET_ID, widget.translate('updateSuccessMsg'), true);
          }
          widget.hideModal();
          if (isModalSaveClicked()) {
            isModalSaveClicked(false);
            widget.navigateAway();
          }
        });
        
        // Handles if user profile update is failed.
        $.Topic(PubSub.topicNames.USER_PROFILE_UPDATE_FAILURE).subscribe(function(data) {
          if (isModalSaveClicked()) {
            widget.isUserProfileInvalid(true);
            isModalSaveClicked(false);
          }
          widget.user().delaySuccessNotification(false);
          // Hide the modal.
          widget.hideModal();
          if (data.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            widget.user().isSessionExpiredDuringSave(true);
            navigation.doLogin(navigation.getPath());
          } else {
            var msg = widget.passwordErrorMsg;
            notifier.clearError(widget.WIDGET_ID);
            notifier.clearSuccess(widget.WIDGET_ID);
            if (data.errorCode == CCConstants.USER_PROFILE_OLD_PASSWORD_INCORRECT) {
              $('#CC-customerProfile-soldPassword-phone-error').css("display", "block");
              $('#CC-customerProfile-soldPassword-phone-error').text(data.message);
              $('#CC-customerProfile-soldPassword-phone').addClass("invalid");
              $('#CC-customerProfile-spassword1-error').css("display", "block");
              $('#CC-customerProfile-spassword1-error').text(data.message);
              $('#CC-customerProfile-soldPassword').addClass("invalid");
            } else if (data.errorCode == CCConstants.USER_PROFILE_PASSWORD_POLICIES_ERROR) {
              $('#CC-customerProfile-spassword-error').css("display", "block");
              $('#CC-customerProfile-spassword-error').text(CCi18n.t('ns.common:resources.passwordPoliciesErrorText'));
              $('#CC-customerProfile-spassword').addClass("invalid");
              $('#CC-customerProfile-spassword-embeddedAssistance').css("display", "block");
              var embeddedAssistance = CCPasswordValidator.getAllEmbeddedAssistance(widget.passwordPolicies(), true);
              $('#CC-customerProfile-spassword-embeddedAssistance').text(embeddedAssistance);
            } else if (data.errorCode === CCConstants.USER_PROFILE_INTERNAL_ERROR) {
              msg = data.message;
              // Reloading user profile and shipping data in edit mode.
              widget.user().getCurrentUser(false);
              widget.reloadShipping();
            } else if (data.errors && data.errors.length > 0 && 
              (data.errors[0].errorCode === CCConstants.USER_PROFILE_SHIPPING_UPDATE_ERROR)) {
              msg = data.errors[0].message;
            } else {
              msg = data.message;
            }
            notifier.sendError(widget.WIDGET_ID, msg, true);
            widget.hideModal();
          }
        });
        
        $.Topic(PubSub.topicNames.USER_LOAD_SHIPPING).subscribe(function() {
          widget.reloadShipping();
        });
        
        $.Topic(PubSub.topicNames.UPDATE_USER_LOCALE_NOT_SUPPORTED_ERROR).subscribe(function() {
          widget.isProfileLocaleNotInSupportedLocales(true);
        });
        
        /**
         *  Navigates window location to the interceptedLink OR clicks checkout/logout button explicitly.
         */
        widget.navigateAway = function() {

          if (clickedElementId === "CC-header-checkout" || clickedElementId === "CC-loginHeader-logout" || clickedElementId === "CC-customerAccount-view-orders" 
              || clickedElementId === "CC-header-language-link" || clickedElementId.indexOf("CC-header-languagePicker") != -1) {
            widget.removeEventHandlersForAnchorClick();
            widget.showViewProfile(false);
            // Get the DOM element that was originally clicked.
            var clickedElement = $("#"+clickedElementId).get()[0];
            clickedElement.click();
          } else if (clickedElementId === "CC-loginHeader-myAccount") {
            // Get the DOM element that was originally clicked.
            var clickedElement = $("#"+clickedElementId).get()[0];
            clickedElement.click();
          } else {
            if (!navigation.isPathEqualTo(widget.interceptedLink)) {
              navigation.goTo(widget.interceptedLink);
              widget.removeEventHandlersForAnchorClick();
            }
          }
        };
        
        // handler for anchor click event.
        var handleUnsavedChanges = function(e, linkData) {
          var usingCCLink = linkData && linkData.usingCCLink;
          
          widget.isProfileLocaleNotInSupportedLocales(false);
          // If URL is changed explicitly from profile.
          if(!usingCCLink && !navigation.isPathEqualTo(widget.links().profile.route)) {
            widget.showViewProfile(false);
            widget.removeEventHandlersForAnchorClick();
            return true;
          }
          if (widget.user().loggedIn()) {
            clickedElementId = this.id;
            widget.interceptedLink = e.currentTarget.pathname;
            if (widget.user().isUserProfileEdited()) {
              widget.showModal();
              usingCCLink && (linkData.preventDefault = true);
              return false;
            }
            else {
              widget.showViewProfile(false);
            }
          }
        };
        
        var controlErrorMessageDisplay = function(e) {
          widget.isProfileLocaleNotInSupportedLocales(false);
        };
        
        /**
         *  Adding event handler for anchor click.
         */
        widget.addEventHandlersForAnchorClick = function() {
          $("body").on("click.cc.unsaved","a",handleUnsavedChanges);
          $("body").on("mouseleave", controlErrorMessageDisplay);
        };
        
        /**
         *  removing event handlers explicitly that has been added when anchor links are clicked.
         */
        widget.removeEventHandlersForAnchorClick = function(){
          $("body").off("click.cc.unsaved","a", handleUnsavedChanges);
        };
        
        /**
         *  returns true if any of the user details OR password OR shipping details edit is clicked.
         */ 
        widget.user().isUserProfileEdited = ko.computed({
          read: function() {
            return ( widget.isUserDetailsEdited() || widget.isUserProfilePasswordEdited() || widget.isUserProfileShippingEdited() || widget.isUserProfileDefaultAddressEdited() );
          },
          owner: widget
        });
      },
      
      //Get count of placed orders for logged-in user
      getSubmittedOrderCountForProfile: function(widget) {
        widget.load(CCConstants.ENDPOINT_GET_ORDER_COUNT_FOR_PROFILE, '0', null,
          //success callback 
          function(data) {
            widget.user().countOfSubmittedOrders(data.numberOfOrders);
          },
          //error callback
          function(data) {
          },
          widget
        );
      }      
    };
  }
);
