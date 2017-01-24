/**
 * @fileoverview CyberSource Payment Authorization.
 *
 */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout','pubsub', 'notifier', 'ccConstants', 'CCi18n', 'ccLogger', 'jquery'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function (ko, pubsub, notifier, CCConstants, CCi18n, log, $) {

    "use strict";

    return {

      authDetails: {},
      details: {},
      TRANSACTION_TYPE: 'authorization,create_payment_token',
      PAYMENT_METHOD: 'card',
      UNSIGNED_FIELDS: 'card_number',
      WIDGET_ID: 'CyberSourceWidget',
      DEFAULT_REASON_CODE: '1000',
      DEFAULT_DECLINE_MESSAGE: '(1000) Your credit card has been declined, please use an alternate card.',
      DEFAULT_TIMEOUT_MESSAGE: 'We experienced a technical problem (timeout) while authorizing payment. Please try again in a little while.',
      DEFAULT_LOAD_TIMEOUT: 60,

      onLoad: function (widget) {
        
        function unwrap(pValue) {
          return ko.unwrap(pValue);
        }
        
        function equalsIgnoreCase(pValue1, pValue2) {
          
          if (pValue1 === pValue2) {
            return true;
          }
          else if (pValue1.toLowerCase && pValue2.toLowerCase) {
            return (pValue1.toLowerCase() === pValue2.toLowerCase());
          }
          
          return false;
        }
        
        /**
         * Returns the main payment frame.
         */
        widget.getFrame = function() {
          return document.getElementById("cc-cybersourcePaymentAuthorization-frame");
        };
        
        /**
         * Returns the main payment frame document.
         */
        widget.getFrameDocument = function() {
          var iframe = widget.getFrame();
            if (iframe) {
              return iframe.contentDocument || iframe.contentWindow.document;
            }
        };
        
        /**
         * Returns the payment form in the main payment frame.
         */
        widget.getPaymentForm = function() {
          var iframeDocument = widget.getFrameDocument();
          if (iframeDocument) {
            return iframeDocument.getElementById("payment_form");
          }
        };
        
        /**
         * Creates the signature if iframe is loaded
         */
        widget.createSignatureIfIframeIsLoaded = function(authDetails, pTryCount) {
          if (pTryCount === 0) {
            widget.order().createSpinner();
          }
          
          // If the iframe is loaded properly in the page with its contents.
          if (widget.paymentFormLoaded()) {
            widget.createSignature(authDetails);
            widget.order().createSpinner();
          }
          // If the iframe is not loaded yet, wait for it to load but timeout if it can't be loaded.
          else {
            //In IE11 the onload we attach to the frame gets called when the iframe element is rendered
            //without any content, and wipes out the spinner above. So, we add something to show it again.
            if (pTryCount === 0) {
              widget.order().createSpinner();
            }
            if (pTryCount <= widget.DEFAULT_LOAD_TIMEOUT) { 
              setTimeout( function() {
                widget.createSignatureIfIframeIsLoaded(authDetails, (pTryCount+1));
              }, 500);
            } else {
              log.error('Payment Form not loading.');
              widget.handleErrors();
              widget.order().destroySpinner();
            }
          }
        };
        
        /**
         * Check if the payment form is in the iFrame.
         */
        widget.paymentFormLoaded = function() {
          try {
            if (widget.getPaymentForm()) {
              return true;
            }
          }
          catch (e) {
            // This is called recursively if the form doesn't load, so not logging an error.
            // Logs an error if the re-try count is exhausted.
          }
          return false;
        };
        
        /**
         * Create the signature.
         */
        widget.createSignature = function(authDetails) {

          widget.details = widget.getSignatureDetails(authDetails);
          var keys = Object.keys(widget.details);

          widget.details.signed_field_names = widget.getSignedFields(keys);

          widget.create('payment', 'id', widget.details,  widget.site().siteInfo.repositoryId,
            //success callback
            function (data) {
              widget.details.card_number      = unwrap(widget.authDetails.paymentDetails.cardNumber);
              widget.details.access_key       = data.access_key;
              widget.details.signed_date_time = data.signed_date_time;
              widget.details.profile_id       = data.profile_id;
              widget.details.transaction_uuid = data.transaction_uuid;
              widget.details.signature        = data.signature;
              widget.details.ignore_avs       = data.ignore_avs;

              widget.injectFormValues(widget.details);

              widget.injectActionURL(data.sopURL);

              widget.postForm();

              var paymentGroups = authDetails.orderDetails.payments;
              var paymentGrpId = "";
              // These changes are done for MPG . Currently only Single PG is supported.
              for (var i=0 ; i<paymentGroups.length ; i++){
                if(paymentGroups[i] &&
                    paymentGroups[i].gatewayName == CCConstants.CYBERSOURCE_GATEWAY_ID 
                    && paymentGroups[i].uiIntervention == CCConstants.SOP) {
                  paymentGrpId = paymentGroups[i].paymentGroupId;
                  break;
                }
              }
              
              var messageDetails = [{message: "success",
                    transactionuuid: data.transaction_uuid,
                    orderid: authDetails.orderDetails.id,
                    orderuuid: authDetails.orderDetails.uuid,
                    paymentGroupId: paymentGrpId,
                    gatewayname: CCConstants.CYBERSOURCE_GATEWAY_ID}];

              $.Topic(pubsub.topicNames.PAYMENT_GET_AUTH_RESPONSE).publish(messageDetails);

            },
            //error callback
            function (data) {
              if (data && data.message && data.message !== '') {
                // error message received, i18n occurs on server
                if (data.errorCode != CCConstants.INVALID_SHIPPING_METHOD) {
                  notifier.sendErrorToPage(widget.WIDGET_ID, data.message, true, 'checkout');
                }
              } else {
                // unknown error - use generic fail message
                notifier.sendErrorToPage(widget.WIDGET_ID, CCi18n.t('ns.common:resources.orderSubmissionFailed'), true, 'checkout' );
              }
              $.Topic(pubsub.topicNames.ORDER_SUBMISSION_FAIL).publish([{message: "fail"}]);
              widget.order().destroySpinner();
            }
          );

        };

        /**
         * Return the fields required for the signature.
         */
        widget.getSignatureDetails = function(authDetails) {
          var details = new Object();

          details.access_key           = '';
          details.profile_id           = '';
          details.transaction_uuid     = '';
          details.signed_field_names   = '';
          details.signed_date_time     = '';
          details.locale               = 'en';
          details.ignore_avs           = '';
          details.transaction_type     = widget.TRANSACTION_TYPE;
          details.payment_method       = widget.PAYMENT_METHOD;
          details.unsigned_field_names = widget.UNSIGNED_FIELDS;
          details.reference_number     = authDetails.orderDetails.id;
          // These changes are done for MPG . Currently only Single PG is supported.
          var paymentGroups = authDetails.orderDetails.payments;
          details.amount = 0;
          for (var i=0 ; i<paymentGroups.length ; i++){
            if(paymentGroups[i] &&
                paymentGroups[i].gatewayName == CCConstants.CYBERSOURCE_GATEWAY_ID 
                && paymentGroups[i].uiIntervention == CCConstants.SOP) {
              details.amount = paymentGroups[i].amount;
              break;
            }
          }
          details.currency             = authDetails.orderDetails.priceInfo.currencyCode;

          details.card_type        = widget.getCardType(authDetails.paymentDetails);
          details.card_expiry_date = unwrap(authDetails.paymentDetails.endMonth)+'-'+unwrap(authDetails.paymentDetails.endYear);
          details.card_cvn         = unwrap(authDetails.paymentDetails.cardCVV);

          details.bill_to_forename            = authDetails.billingAddress.firstName;
          details.bill_to_surname             = authDetails.billingAddress.lastName;
          details.bill_to_phone               = authDetails.billingAddress.phoneNumber;
          details.bill_to_address_line1       = authDetails.billingAddress.address1;
          details.bill_to_address_line2       = authDetails.billingAddress.address2;
          details.bill_to_address_city        = authDetails.billingAddress.city;
          // SelectedState will be undefined for the countries that have no states.
          if (authDetails.billingAddress.selectedState != undefined) {
            details.bill_to_address_state     = authDetails.billingAddress.selectedState;
          }
          details.bill_to_address_country     = authDetails.billingAddress.selectedCountry;
          details.bill_to_address_postal_code = authDetails.billingAddress.postalCode;
          details.bill_to_email               = authDetails.emailAddress;

          //merchant data
          details.merchant_defined_data1 = widget.site().siteInfo.repositoryId;

          return details;
        };

        /**
         * Return the CyberSource card code for the selected card.
         */
        widget.getCardType = function(paymentDetails) {
          var validCards = widget.payment().cards;

          for (var i=0; i<validCards.length; i++) {
            if (equalsIgnoreCase(validCards[i].value, unwrap(paymentDetails.cardType))) {
              return validCards[i].code;  
            }
          }
        };

        /**
         * Return the comma seperated list of the signed fields.
         */
        widget.getSignedFields = function(keys) {

          var fields = '';
          for (var i=0; i<keys.length; i++) {
            fields += keys[i];
            if (i<keys.length - 1)
            {
              fields+=',';
            }
          }
          return fields;
        };
        
        /**
         * Destroys spinner when output iframe gets loaded with data.
         */
        widget.destroySpinnerOnResponse = function() {
          var outputFrame = document.getElementById('cc-cybersourcePaymentAuthorization-output-frame');
          outputFrame.onload = function() {
              widget.order().destroySpinner();
          };
        };

        /**
         * Inject the form values into the form in the frame.
         */
        widget.injectFormValues = function(details) {
          var keys = Object.keys(details);
          var frameDocument = widget.getFrameDocument();
          if (frameDocument) {
            for (var i=0; i<keys.length; i++) {
              var element = frameDocument.getElementById(keys[i]);
              if (element) {
                element.value = details[keys[i]];
              }
            }
          }
        };

        /**
         * Inject the form values into the form in the frame.
         */
        widget.injectActionURL = function(url) {
          widget.getPaymentForm().action = url;
        };

        /**
         * Post the form in the frame.
         */
        widget.postForm = function() {
          widget.getPaymentForm().submit(); 
        };

        /**
         * Reload the iFrame with the original src.
         */
        widget.reloadFrame = function() {
          var iframe = widget.getFrame();
          if (iframe) {
            iframe.src = iframe.src;
          }
        };

        /**
         * Handle a payment authorization decline for CyberSource payments.
         * Defined default message only used when the resource can not be found.
         */
        widget.handleDecline = function(data) {
          var defaultMessage = widget.translate(widget.DEFAULT_REASON_CODE, {defaultValue: widget.DEFAULT_DECLINE_MESSAGE}, false);
          var message;
          try {
            message = widget.translate(data.responsedata.reasonCode, {defaultValue: defaultMessage}, true);
          } catch(e) {
            message = defaultMessage;
          }
          
          if(data.responsedata.reasonCode == CCConstants.PAYMENT_REVERSAL_FAILED) {
            widget.order().id('');
          }
          widget.handleAuthError();

          notifier.sendErrorToPage(widget.WIDGET_ID, message, true, 'checkout');
        };

        /**
         * Handle listening for changes on the checkout page and reset the iframe.
         */
        widget.handleAuthError = function() {
          $.Topic(pubsub.topicNames.CHECKOUT_EMAIL_ADDRESS).subscribe(widget.clearErrors);
          $.Topic(pubsub.topicNames.CHECKOUT_BILLING_ADDRESS).subscribe(widget.clearErrors);
          $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_ADDRESS).subscribe(widget.clearErrors);
          $.Topic(pubsub.topicNames.CHECKOUT_PAYMENT_DETAILS).subscribe(widget.clearErrors);

          widget.reloadFrame();
        };

        /**
         * Clear Notifier Errors.
         */
        widget.clearErrors = function() {
          notifier.clearError(widget.WIDGET_ID);
          $.Topic(pubsub.topicNames.CHECKOUT_EMAIL_ADDRESS).unsubscribe(widget.clearErrors);
          $.Topic(pubsub.topicNames.CHECKOUT_BILLING_ADDRESS).unsubscribe(widget.clearErrors);
          $.Topic(pubsub.topicNames.CHECKOUT_SHIPPING_ADDRESS).unsubscribe(widget.clearErrors);
          $.Topic(pubsub.topicNames.CHECKOUT_PAYMENT_DETAILS).unsubscribe(widget.clearErrors);
        };

        /**
         * Handle payment authorization time out.
         * Defined default message only used when the resource can not be found.
         */
        widget.handleTimeout = function() {
          widget.handleAuthError();
          notifier.sendErrorToPage(widget.WIDGET_ID, widget.translate('paymentErrorText', {defaultValue: widget.DEFAULT_TIMEOUT_MESSAGE}), true, 'checkout');
        };
        
        /**
         * Handle unexpected errors.
         */
        widget.handleErrors = function() {
          try {
            $.Topic(pubsub.topicNames.ORDER_SUBMISSION_FAIL).publish([{message: "fail"}]);
          }
          catch(e) {
            log.error('Error Handling Order Fail');
            log.error(e);
          }
          try {
            widget.handleTimeout();
          }
          catch(e) {
            log.error('Error Handling Timeout');
            log.error(e);
          }
        };
        
        // Handle payment auth request
        $.Topic(pubsub.topicNames.ORDER_AUTHORIZE_PAYMENT).subscribe(function(obj) {
          if (obj[0].details) {
            widget.authDetails = obj[0].details;
            widget.createSignatureIfIframeIsLoaded(widget.authDetails, 0);
          }
        });
        
        // Handle payment auth decline
        $.Topic(pubsub.topicNames.PAYMENT_AUTH_DECLINED).subscribe(function(data) {
          if (data[0] && data[0].gatewayName && data[0].gatewayName === CCConstants.CYBERSOURCE_GATEWAY_ID) {
            widget.handleDecline(data[0]);
          }
        });

        // Handle payment auth timeout
        $.Topic(pubsub.topicNames.PAYMENT_AUTH_TIMED_OUT).subscribe(function(data) {
          if (data[0] && data[0].gatewayName && data[0].gatewayName === CCConstants.CYBERSOURCE_GATEWAY_ID) {
            widget.handleTimeout();
          }
        });
      },

      beforeAppear: function (page) {
        $(window).scrollTop(0);
      }
    };
  }
);
