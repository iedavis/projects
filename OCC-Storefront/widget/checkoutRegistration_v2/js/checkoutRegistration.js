define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'ccConstants', 'CCi18n', 'pubsub', 'notifier', 'ccPasswordValidator', 'ccRestClient', 'navigation'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, CCConstants, CCi18n, PubSub, notifier, CCPasswordValidator, CCRestClient, navigation) {
  
    "use strict";
        
    return {
      isSessionExpired:    ko.observable(false),
      ignoreBlur:       ko.observable(false),
      WIDGET_ID:        "checkoutRegistration",
      ignoreEmailValidation: ko.observable(true),
      currentLogin : ko.observable(''),
      
      beforeAppear: function (page) {
        var widget = this;
        widget.user().ignoreEmailValidation(false);
        widget.order().checkoutOption(CCConstants.GUEST);
        $('#registered').hide();
        $('#guest').show();
        $('#remember').hide();
        //widget.user().resetDetails();
        widget.order().createAccount(false);
        // To display success notification after redirection from customerProfile page.
        if (widget.user().delaySuccessNotification()) {
          notifier.sendSuccess(widget.WIDGET_ID, widget.translate('updateSuccessMsg'), true);
          widget.user().delaySuccessNotification(false);
        }
      },
      
      onLoad: function(widget) {
        var self = this;
        widget.ErrorMsg = widget.translate('checkoutErrorMsg');
        widget.order().guestEmailAddress.extend({
          required:{ params: true,message: CCi18n.t('ns.common:resources.emailAddressRequired')},
          maxLength:{ params: CCConstants.CYBERSOURCE_EMAIL_MAXIMUM_LENGTH, message:CCi18n.t('ns.common:resources.maxLengthEmailAdd',{maxLength:CCConstants.CYBERSOURCE_EMAIL_MAXIMUM_LENGTH})},
          email:{ params: true, onlyIf: function () { return (!widget.ignoreEmailValidation()); }, message: CCi18n.t('ns.common:resources.emailAddressInvalid')}});
        // The register/login toggle
        widget.order().checkoutOption.subscribe(function(value) {
          if (value === CCConstants.LOGIN) {
            $('#guest').hide();
            $('#registered').fadeIn('slow');
            if (widget.isSessionExpired()) {
              $('#CC-checkoutRegistration-login-password').focus();
              widget.user().password.isModified(false);
            }
          } else if (value === CCConstants.GUEST) {
            $('#guest').fadeIn('slow');
            $('#registered').hide();
            widget.resetSessionData();
          }
          if (!widget.user().loggedIn()) {
            widget.order().reset();
          }
        });
        
        // The Account creation subscription
        widget.order().createAccount.subscribe(function(value) {
          if(value === true) {
            $('#remember').fadeIn('slow');
            widget.user().emailAddress(widget.order().guestEmailAddress());
          } else {
            $('#remember').fadeOut('slow');
            // Removes values, clears trigger message and removes validation errors
            widget.user().resetDetails();
          }
        });

        widget.guestEmailAddressFocused = function() {
          widget.ignoreEmailValidation(true);
          return true;
        };

        widget.guestEmailAddressLostFocus = function() {
          widget.ignoreEmailValidation(false);
          return true;
        };
        
        widget.user().emailAddress.subscribe(function(newValue) {
          if (widget.user().emailAddress.isValid()) {
            $.Topic(PubSub.topicNames.CHECKOUT_EMAIL_ADDRESS).publishWith(
                widget.user().emailAddress(),[{message:"success"}]);
          }
        });
        
        widget.order().guestEmailAddress.subscribe(function(newValue) {
          if (widget.order().guestEmailAddress.isValid()) {
            $.Topic(PubSub.topicNames.CHECKOUT_EMAIL_ADDRESS).publishWith(
                widget.order().guestEmailAddress(),[{message:"success"}]);
            if(widget.order().createAccount()) {
              widget.user().emailAddress(widget.order().guestEmailAddress());
            }
          }
        });
          
        $.Topic(PubSub.topicNames.USER_RESET_PASSWORD_SUCCESS).subscribe(function(obj) {
          widget.hideAllSections();
          $('#CC-checkoutForgotPasswordMessagePane').show();
        });
        
        $.Topic(PubSub.topicNames.USER_RESET_PASSWORD_FAILURE).subscribe(function(obj) {
          $('#CC-checkoutModalPane').modal('hide');
          notifier.sendError(widget.WIDGET_ID, obj.message, true);
        });

        // Checks if email address is entered if shipping address was entered during checkout with paypal
        $.Topic(PubSub.topicNames.PAYPAL_EMAIL_VALIDATION).subscribe(function(obj) {
          widget.order().guestEmailAddress.isModified(true);
        });

        //User creation failure
        $.Topic(PubSub.topicNames.USER_CREATION_FAILURE).subscribe(function(data) {
          if (data && data.widgetId === 'checkoutRegistration') {
            widget.user().errorMessageKey('loginFailureText');
            if(widget.order().paymentGateway() && widget.order().paymentGateway().type == CCConstants.PAYULATAM_CHECKOUT_TYPE){
            	CCRestClient.setStoredValue(CCConstants.PAYULATAM_CHECKOUT_REGISTRATION, CCConstants.PAYULATAM_CHECKOUT_REGISTRATION_FAILURE);
             }
          }
        });

        // Registration successful and login successful
        $.Topic(PubSub.topicNames.USER_AUTO_LOGIN_SUCCESSFUL).subscribe(function(data) {
          if (data && data.widgetId === 'checkoutRegistration') {
            widget.user().successMessageKey('loginSuccessText');
            if(widget.order().paymentGateway() && widget.order().paymentGateway().type == CCConstants.PAYULATAM_CHECKOUT_TYPE){
            	CCRestClient.setStoredValue(CCConstants.PAYULATAM_CHECKOUT_REGISTRATION, CCConstants.PAYULATAM_CHECKOUT_REGISTRATION_SUCCESS);
           }
          }
        });

        // Registration successful and login unsuccessful
        $.Topic(PubSub.topicNames.USER_AUTO_LOGIN_FAILURE).subscribe(function(data) {
          if (data && data.widgetId === 'checkoutRegistration') {
            widget.user().successMessageKey('loginSuccessText');
            if(widget.order().paymentGateway() && widget.order().paymentGateway().type == CCConstants.PAYULATAM_CHECKOUT_TYPE){
            	CCRestClient.setStoredValue(CCConstants.PAYULATAM_CHECKOUT_REGISTRATION, CCConstants.PAYULATAM_CHECKOUT_REGISTRATION_SUCCESS);
           }
          }
        });

        $.Topic(PubSub.topicNames.USER_LOGIN_SUCCESSFUL).subscribe(function(){
          if (widget.isSessionExpired() && widget.currentLogin() != '' && widget.user().login() != '' && widget.currentLogin() != widget.user().login()) {
            $.Topic(PubSub.topicNames.USER_SESSION_RESET).publish();
            widget.isSessionExpired(false);
          }
          widget.currentLogin(widget.user().login());
          notifier.clearError(CCConstants.CHECKOUT_SESSION_EXPIRED);
          widget.isSessionExpired(false);
          widget.order().checkoutOption(CCConstants.GUEST);
        });

        // Handle Login
        widget.handleLogin = function(data,event) {
          if ('click' === event.type || (('keydown' === event.type || 'keypress' === event.type) && event.keyCode === 13)) {
            widget.user().updateLocalData(true, false);
            $.Topic(PubSub.topicNames.USER_LOGIN_SUBMIT).publishWith(widget.user(), [{message: "success"}]);
          }
          return true;
        };
        
        // Handle cancel
        widget.handleCancel = function() {
          widget.resetSessionData();
          widget.user().handleCancel();
          widget.order().checkoutOption(CCConstants.GUEST);
        };
        
        //This method resets the shipping and payment data if the user session expires and selects guest checkout
        widget.resetSessionData = function() {
          if (widget.isSessionExpired()) {
            widget.user().clearUserData();
            $.Topic(PubSub.topicNames.USER_SESSION_RESET).publish();
            notifier.clearError(CCConstants.CHECKOUT_SESSION_EXPIRED);
            widget.isSessionExpired(false);
          }
        };
        
        // Handle logout
        widget.handleLogout = function() {
          widget.user().updateLocalData(widget.user().loggedinAtCheckout(), true);
          $.Topic(PubSub.topicNames.USER_LOGOUT_SUBMIT).publishWith(widget.user(), [{message: "success"}]);
        };
        
        /**
         * Invoked when forgotten Password link is clicked.
         */
        widget.showForgotPasswordSection = function(data,event) {
          $('#CC-checkoutModalPane').modal('show');
          widget.hideAllSections();
          $('#CC-checkoutForgotPasswordSectionPane').show();
          data.emailAddressForForgottenPwd('');
          data.emailAddressForForgottenPwd.isModified(false);
          data.forgotPasswordMsg(CCi18n.t('ns.common:resources.forgotPwdText'));
          $('#CC-checkoutModalPane').on('shown.bs.modal', function() {
            $('#CC-checkoutForgotPwd-input').focus();
            data.emailAddressForForgottenPwd.isModified(false);
          });
        };
        
        /**
         * Resets the password for the entered email id.
         */
        widget.resetForgotPassword = function(data,event) {
          if ('click' === event.type || (('keydown' === event.type || 'keypress' === event.type) && event.keyCode === 13)) {
            data.ignoreEmailValidation(false);
            data.emailAddressForForgottenPwd.isModified(true);
            if(data.emailAddressForForgottenPwd  && data.emailAddressForForgottenPwd.isValid()) {
              data.resetForgotPassword();
            }
          }
          return true;
        };
        
        /**
         * Ignores email validation when it is focused.
         */
        widget.emailAddressFocused = function(data, event) {
          if(this.ignoreBlur && this.ignoreBlur()) {
            return true;
          }
          this.user().ignoreEmailValidation(true);
          return true;
        };
        
        /**
         * Email is validated when the input field loses focus(blur).
         */
        widget.emailAddressLostFocus = function(data, event) {
          if (this.ignoreBlur && this.ignoreBlur()) {
            return true;
          }
          this.user().ignoreEmailValidation(false);
          return true;
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
         * Ignores the blur function when mouse click is down outside the modal dialog(backdrop click).
         */
        widget.handleModalDownClick = function(data, event) {
          if (event.target === event.currentTarget) {
            this.ignoreBlur(true);
          }
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

        /**
         * Callback function for use in widget stacks.
         * Triggers internal widget validation.
         * @return true if we think we are OK, false o/w.
         */
        widget.validate = function() {

          this.order().errorFlag = false;

          this.order().validateCheckoutRegistration();

          return !this.order().errorFlag;
        };
        
        $.Topic(PubSub.topicNames.ORDER_SUBMISSION_FAIL).subscribe(function(){
          widget.user().resetPassword();
          widget.user().errorMessageKey('');
          widget.user().successMessageKey('');
          //added to handle session expiry
          if (this.errorCode == CCConstants.CHECKOUT_SESSION_EXPIRED_ERROR || 
            this.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            if (this.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
              notifier.sendError(widget.WIDGET_ID, 
                CCi18n.t('ns.checkoutRegistration:resources.userSessionExpiredErrorMsg'), true);
            }
            widget.user().isSesExpDuringPlaceOrder(true);
            widget.user().handleSessionExpired();
            widget.user().populateUserFromLocalData(true);
            widget.currentLogin(widget.user().login());
            widget.order().checkoutOption(CCConstants.LOGIN);
            $('#CC-checkoutRegistration-login-password').focus();
            widget.user().password.isModified(false);
            widget.isSessionExpired(true);
          }
        });
        
        $.Topic(PubSub.topicNames.USER_PASSWORD_GENERATED).subscribe(function(data) {
          widget.hideAllSections();
          widget.user().showCreateNewPasswordMsg(true);
          widget.user().createNewPasswordError(CCi18n.t('ns.common:resources.createNewPasswordError'));
          widget.user().resetPassword();
          widget.user().hasFieldLevelError(false);
          $('#CC-createNewPasswordCheckout-oldPassword-error').empty();
          $('#CC-createNewPasswordCheckout-password-error').empty();
          $('#CC-createNewPasswordCheckout-password-embeddedAssistance').empty();
          $('#CC-createNewPasswordCheckout-oldPassword').removeClass("invalid");
          $('#CC-createNewPasswordCheckout-password').removeClass("invalid");
          $('#CC-createNewPasswordPaneCheckout').show();
          $('#CC-checkoutModalPane').modal('show');
          $('#CC-checkoutModalPane').on('shown.bs.modal', function () {
            $('#CC-createNewPasswordCheckout-oldPassword').focus();
            widget.user().oldPassword.isModified(false);
          });
        });
        
        $.Topic(PubSub.topicNames.USER_PASSWORD_EXPIRED).subscribe(function(data) {
          $('#CC-checkoutModalPane').modal('show');
          widget.hideAllSections();
          $('#CC-checkoutForgotPasswordSectionPane').show();
          widget.user().password('');
          widget.user().password.isModified(false);
          widget.user().emailAddressForForgottenPwd('');
          widget.user().emailAddressForForgottenPwd.isModified(false);
          widget.user().forgotPasswordMsg(CCi18n.t('ns.common:resources.resetPwdText'));
          $('#CC-checkoutModalPane').on('shown.bs.modal', function() {
            $('#CC-checkoutForgotPwd-input').focus();
            widget.user().emailAddressForForgottenPwd.isModified(false);
          });
        });
        
        $.Topic(PubSub.topicNames.USER_PROFILE_PASSWORD_UPDATE_FAILURE).subscribe(function(data) {
          widget.user().showCreateNewPasswordMsg(false);
          if (data.errorCode == CCConstants.UPDATE_EXPIRED_PASSWORD_OLD_PASSWORD_INCORRECT) {
            $('#CC-createNewPasswordCheckout-oldPassword-error').css("display", "block");
            $('#CC-createNewPasswordCheckout-oldPassword-error').text(CCi18n.t('ns.common:resources.oldPasswordsDoNotMatch'));
            $('#CC-createNewPasswordCheckout-oldPassword').addClass("invalid");
            widget.user().hasFieldLevelError(true);
          } else if (data.errorCode == CCConstants.USER_EXPIRED_PASSWORD_POLICIES_ERROR) {
            $('#CC-createNewPasswordCheckout-password-error').css("display", "block");
            $('#CC-createNewPasswordCheckout-password-error').text(CCi18n.t('ns.common:resources.passwordPoliciesErrorText'));
            $('#CC-createNewPasswordCheckout-password').addClass("invalid");
            $('#CC-createNewPasswordCheckout-password-embeddedAssistance').css("display", "block");
            var embeddedAssistance = CCPasswordValidator.getAllEmbeddedAssistance(widget.passwordPolicies(), true);
            $('#CC-createNewPasswordCheckout-password-embeddedAssistance').text(embeddedAssistance);
            widget.user().hasFieldLevelError(true);
          } else {
            widget.user().createNewPasswordError(data.message);
            widget.user().showCreateNewPasswordMsg(true);
            widget.user().hasFieldLevelError(false);
          }
        });
        
        $.Topic(PubSub.topicNames.USER_PROFILE_PASSWORD_UPDATE_SUCCESSFUL).subscribe(function(data) {
          widget.hideAllSections();
          $('#CC-createNewPasswordMessagePaneCheckout').show();
          $('#CC-checkoutModalPane').on('hide.bs.modal', function () {
            if ($('#CC-createNewPasswordMessagePaneCheckout').css('display') == 'block') {
              widget.user().handleLogin();
            }
          });
        });
        
        $.Topic(PubSub.topicNames.USER_SESSION_EXPIRED).subscribe(function() {
          if (widget.user().isUserSessionExpired() && widget.links().checkout.route.indexOf(navigation.getRelativePath()) >= 0) {
            notifier.sendError(widget.WIDGET_ID, 
              CCi18n.t('ns.checkoutRegistration:resources.userSessionExpiredErrorMsg'), true);
            widget.isSessionExpired(true);
            widget.order().checkoutOption(CCConstants.LOGIN);
          }
        });
        
        /**
         * Hides all the sections of  modal dialogs.
         */
        widget.hideAllSections = function() {
          $('#CC-checkoutForgotPasswordMessagePane').hide();
          $('#CC-checkoutForgotPasswordSectionPane').hide();
          $('#CC-createNewPasswordMessagePaneCheckout').hide();
          $('#CC-createNewPasswordPaneCheckout').hide();
        };
        
        /**
         * this method is triggered when the user clicks on the save 
         * on the create new password model
         */
        widget.savePassword = function(data, event) {
          if ('click' === event.type || (('keydown' === event.type || 'keypress' === event.type) && event.keyCode === 13)) {
            if (this.user().isPasswordValid()) {
              this.user().updateExpiredPassword();
            }
          }
          return true;
        };
        
      }
    };
  }
);
