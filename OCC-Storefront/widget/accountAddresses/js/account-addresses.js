/**
 * Created by srekonda on 10/24/2016.
 */
/**
 * @fileoverview Account Addresses Widget
 */
define(function(require) {
  'use strict';

  var CCConstants = require('ccConstants');
  var ko = require('knockout');
  var navigation = require('navigation');
  var notifier = require('notifier');
  var spinner = require('spinner');
  var CCRestClient = require('ccRestClient');

  return {
    /** Widget root element id */
    WIDGET_ID: 'accountAddresses',
    isDelegatedAdmin: ko.observable(false),
    addresses: ko.observableArray([]),
    isEditMode: ko.observable(false),
    companyName: ko.observable(),
    addressType: ko.observable(),
    address1: ko.observable(),
    address2: ko.observable(),
    city: ko.observable(),
    country: ko.observable(),
    state: ko.observable(),
    postalCode: ko.observable(),
    phoneNumber: ko.observable(),
    isDefaultShippingAddress: ko.observable(false),
    isDefaultBillingAddress: ko.observable(false),
    defaultShippingAddress: {},
    defaultBillingAddress: {},
    countriesList: ko.observableArray(),
    stateList: ko.observableArray([]),
    subscriptionArray: [],
    isDirty: ko.observable(false),
    isCreateNewAddress: ko.observable(false),
    editingAddressId: ko.observable(),
    duplicateEditingAddressId: ko.observable(),
    // Postal Code Patterns
    postalCodePattern: ko.observable(''),
    US_POSTAL_CODE_PATTERN: "^[0-9]{5}([ -][0-9]{4})?$",
    CANADA_POSTAL_CODE_PATTERN: "^[abceghjklmnprstvxyABCEGHJKLMNPRSTVXY]{1}[0-9]{1}[a-zA-Z]{1} *[0-9]{1}[a-zA-Z]{1}[0-9]{1}$",
    DEFAULT_POSTAL_CODE_PATTERN: "^[0-9a-zA-Z]{1,}([ -][0-9a-zA-Z]{1,})?$",
    

    addressObject: ko.validatedObservable(),

    /** Default options for creating a spinner */
    spinnerOptions: {
      parent: '#CC-scheduledOrder-scheduleForm',
      posTop: '0',
      posLeft: '50%'
    },


    /**
     * Called when widget is first loaded:
     *    Bind callback methods context.
     *    Define computed properties.
     *    Define property validator and validation model.
     *    Define reference data for creating the scheduleMode select options (including opt groups).
     */
    onLoad: function(widget) {
      // Define property validation for Country
      widget.country.extend({
        required: {
          params: true,
          message: widget.translate('countryRequiredText')
        }
      });
      //Define property validation for address type
      widget.addressType.extend({
        required: {
          params: true,
          message: widget.translate('nickNameRequiredText')
        }
      });
      
      //Define property validation for company name
      widget.companyName.extend({
        required: {
          params: true,
          message: widget.translate('companyNameRequiredText')
        }
      });
      // Define property validation for State
      widget.state.extend({
        required: {
          params: true,
          onlyIf: function () { return widget.stateList().length > 0; },
          message: widget.translate('stateRequiredText')
        }
      });
      // Define property validation for Address
      widget.address1.extend({
        required: {
          params: true,
          message: widget.translate('addressRequiredText')
        }
      });
      // Define property validation for City
      widget.city.extend({
        required: {
          params: true,
          message: widget.translate('cityRequiredText')
        }
      });
      // Define property validation for Postal Code
      widget.postalCode.extend({
    	  required: { params: true, message: widget.translate('postalCodeRequiredText') },
          maxLength: {params: CCConstants.CYBERSOURCE_POSTAL_CODE_MAXIMUM_LENGTH,
                               message: widget.translate('maxlengthValidationMsg',{fieldName: widget.translate('postalCodeNameText'),maxLength:CCConstants.CYBERSOURCE_POSTAL_CODE_MAXIMUM_LENGTH}) },
          observablePattern: { params: widget.postalCodePattern, onlyIf: function () { return (widget.postalCodePattern() != ''); }, message: widget.translate('postalCodeInvalid')}
      });
      
      // Define property validation for Phone Number
      widget.phoneNumber.extend({
        required: {
          params: true,
          message: widget.translate('phoneNumberRequiredText')
        },pattern: {
          params: "^[0-9()+ -]+$",
          message: widget.translate('invalidPhoneNumber'),
        },
        maxLength: { 
          params:  CCConstants.CYBERSOURCE_PHONE_NUMBER_MAXIMUM_LENGTH,
          message: widget.translate('maxlengthValidationMsg',{fieldName: widget.translate('phoneNumberText'),maxLength: CCConstants.CYBERSOURCE_PHONE_NUMBER_MAXIMUM_LENGTH})
        }
      });
      widget.validationModel = ko.validatedObservable({
        country: widget.country,
        state: widget.state,
        address1: widget.address1,
        city: widget.city,
        postalCode: widget.postalCode,
        phoneNumber: widget.phoneNumber,
        addressType: widget.addressType,
        companyName: widget.companyName
      });
      widget.countriesList(widget.shippingCountries());
    },
    
    /**
     * Called:
     * 1] when the widget is loaded
     * 2] Any address related to the organization is updated or created and the user moves back to the address listing page
     */
    loadOrganizationAddresses: function() {
      var widget = this;
      var data = {};
      var url = CCConstants.END_POINT_GET_ADDRESSES;
      CCRestClient.request(url, data, widget.fetchAddressesSuccess.bind(widget), widget.fetchAddressesFailure.bind(widget))
    },
    
    /**
     * Success Call back function for a fetching organization addresses:
     * @pResponse {Response from the server}
     * @type {function}
     */
    fetchAddressesSuccess: function(pResponse) {
      var widget = this;
      widget.defaultBillingAddress = pResponse.billingAddress;
      widget.defaultShippingAddress = pResponse.shippingAddress;
      widget.replaceAllAddress2NullStringWithNull(pResponse.secondaryAddresses);
      widget.addresses(pResponse.secondaryAddresses);
    },
    
    /**
     * Failure Call back function for a fetching organization addresses:
     * @pError {Response from the server}
     * @type {function}
     */
    fetchAddressesFailure: function(pError) {
      var widget = this;
      notifier.clearError(widget.WIDGET_ID);
      notifier.clearSuccess(widget.WIDGET_ID);
      if (pError.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
          widget.user().handleSessionExpired();
          if (navigation.isPathEqualTo(widget.links().profile.route) || navigation.isPathEqualTo(widget.links().accountAddresses.route)) {
            navigation.doLogin(navigation.getPath(), widget.links().home.route);
          }
      }
      else {
        notifier.sendError(widget.WIDGET_ID, pError.message, true);
      }
    },
    
    /**
     * Invoked when user clicks on the edit or new button:
     * @isNewAddress Boolean to specify whether the mode is create/edit
     * @data Address reference in case of edit mode
     */
    handleCreateOrEditOrganizationAddress: function(isNewAddress, data) {
      var widget = this;
      widget.isEditMode(true);
      widget.resetAddressData();
      widget.subscribeForChanges();
      if (!isNewAddress) {
        widget.addressType(data.addressType);
        widget.address1(data.address.address1);
        widget.address2(data.address.address2);
        widget.city(data.address.city);
        widget.country(data.address.country);
        widget.companyName(data.address.companyName);
        widget.state(data.address.state);
        widget.postalCode(data.address.postalCode);
        widget.phoneNumber(data.address.phoneNumber);
        widget.isDefaultShippingAddress(data.address.isDefaultShippingAddress);
        if (widget.defaultBillingAddress !== null && data.address.repositoryId === widget.defaultBillingAddress.repositoryId) {
          widget.isDefaultBillingAddress(true);
        }
        if (widget.defaultShippingAddress !== null && data.address.repositoryId === widget.defaultShippingAddress.repositoryId) {
          widget.isDefaultShippingAddress(true);
        }
        widget.editingAddressId(data.address.repositoryId);
        widget.isCreateNewAddress(false);
      } else {
        widget.isCreateNewAddress(true);
      }
      widget.isDirty(false);
    },
    
    /**
     * Invoked when user clicks on  button to save the changes to the server:
     */
    handleUpdateOrganizationAddress: function() {
      var widget = this;
      if (widget.validationModel.isValid()) {
        var isCurrentAddressDuplicate = widget.isDuplicateNickname();
        if(isCurrentAddressDuplicate === false && widget.isCreateNewAddress()) {
          widget.createNewOrganizationAddress(widget.convertToData());
        } else {
          if(isCurrentAddressDuplicate !== false) {
            widget.duplicateEditingAddressId(isCurrentAddressDuplicate);
            $('#cc-update-address-ModalContainer').on('show.bs.modal', function() {
              $('#cc-update-address-ModalContainer-modal-dialog').show();
            });
                  // create new 'hidden' event handler
            $('#cc-update-address-ModalContainer').on('hide.bs.modal', function() {
              $('#cc-update-address-ModalContainer-modal-dialog').hide();
            });
                    // open modal
            $('#cc-update-address-ModalContainer').modal('show');
            return;
          } else {
            widget.updateOrganizationAddress(widget.convertToData(), widget.editingAddressId());
          }
        }
      } else {
        widget.validationModel.errors.showAllMessages();
      }
    },
    /**
     * Invoked when user clicks on Yes button on duplicate nickname dialog.
     */
    
    handleUpdateDuplicateOrganizationAddress: function() {
      var widget=this;
      widget.isCreateNewAddress(false);
      widget.updateOrganizationAddress(widget.convertToData(), widget.duplicateEditingAddressId());
      $('#cc-update-address-ModalContainer').modal('hide');
    },
    
    /**
     * Invoked when user clicks on No button on duplicate nickname dialog.
     */
    handleDuplicateOrganizationAddressDialogClose: function() {
      $('#cc-update-address-ModalContainer').modal('hide');   	
    },
  

    
    /**
     * Method which handles the object to be constructed and passed to the server:
     * @pData Address reference
     * @pEditingAddressId Id of address to be updated
     */
    updateOrganizationAddress: function(pData, pEditingAddressId) {
      var widget = this;
      widget.updateAddress(pData, pEditingAddressId);
    },
    
    /**
     * Method to POST the organization address data to the server:
     * @pData Address reference
     */
    createNewOrganizationAddress: function(pData) {
      var widget = this;
      CCRestClient.request(CCConstants.END_POINT_ADD_ADDRESSES, pData, widget.createOrUpdateSuccess.bind(widget), widget.saveFailure.bind(widget));
    },
    
    /**
     * Method to update the organization address data in the server:
     * @pData Address reference
     * @pEditingAddressId Id of address to be updated
     */
    updateAddress: function(pData, pEditingAddressId) {
      var widget = this;
      CCRestClient.request(CCConstants.END_POINT_UPDATE_ADDRESSES, pData, widget.createOrUpdateSuccess.bind(widget), widget.saveFailure.bind(widget), pEditingAddressId);
    },
    
    /**
     * Success Call back function for creating or updating organization addresses:
     * @pResponse {Response from the server}
     * @type {function}
     */
    createOrUpdateSuccess: function(pResponse) {
      var widget = this;
      widget.isEditMode(false);
      notifier.clearError(widget.WIDGET_ID);
      notifier.clearSuccess(widget.WIDGET_ID);
      notifier.sendSuccess(widget.WIDGET_ID, this.translate("organizationAddressUpdateSuccessText"), true);
      widget.resetAddressData();
      widget.loadOrganizationAddresses();
    },
    
    /**
     * Failure Call back function for creating or updating organization addresses:
     * @pResponse {Response from the server}
     * @type {function}
     */
    saveFailure: function(pError) {
      var widget = this;
      notifier.clearError(widget.WIDGET_ID);
      notifier.clearSuccess(widget.WIDGET_ID);
      if (pError.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
          widget.user().handleSessionExpired();
          if (navigation.isPathEqualTo(widget.links().profile.route) || navigation.isPathEqualTo(widget.links().accountAddresses.route)) {
            navigation.doLogin(navigation.getPath(), widget.links().home.route);
          }
      }
      else {
        notifier.sendError(widget.WIDGET_ID, pError.message ? pError.message : this.translate("organizationAddressUpdateFailureText"), true);
      }
    },
    
    /**
     * Invoked when the user clicks on the credit card icon in the address listing page for a particular address:
     * @pData {Address reference}
     * @type {function}
     */
    setDefaultBillingAddress: function(pData) {
      var widget = this;
      var updateData = {};
      if(widget.defaultBillingAddress && widget.defaultBillingAddress.repositoryId===pData.address.repositoryId){
          updateData[CCConstants.ORG_IS_DEFAULT_BILLING_ADDRESS] = false;
    	  widget.defaultBillingAddress={};
      }		
      else{
      	updateData[CCConstants.ORG_IS_DEFAULT_BILLING_ADDRESS] = true;
      }
      var address = {};
      address[CCConstants.ORG_ADDRESS] = updateData;
      address[CCConstants.ORG_ADDRESS_TYPE] = pData.addressType;
      widget.updateAddress(address, pData.address.repositoryId);
    },

    /**
     * Invoked when the user clicks on the shipping icon in the address listing page for a particular address:
     * @pData {Address reference}
     * @type {function}
     */
    setDefaultShippingAddress: function(pData) {
      var widget = this;
      var updateData = {};
      if(widget.defaultShippingAddress && widget.defaultShippingAddress.repositoryId===pData.address.repositoryId){
    	  updateData[CCConstants.ORG_IS_DEFAULT_SHIPPING_ADDRESS] = false;
    	  widget.defaultShipppingAddress={}; 
      }
      else{
    	updateData[CCConstants.ORG_IS_DEFAULT_SHIPPING_ADDRESS] = true;
      }
      var address = {};
      address[CCConstants.ORG_ADDRESS] = updateData;
      address[CCConstants.ORG_ADDRESS_TYPE] = pData.addressType;
      widget.updateAddress(address, pData.address.repositoryId);
    },

    /**
     * Invoked when the user clicks on the delete icon in the address listing page for a particular address:
     * @pData {Address reference}
     * @type {function}
     */
    removeOrganizationAddress: function(pData) {
      var widget = this;
      widget.editingAddressId(pData.repositoryId);
      var data = {};
      CCRestClient.request("deleteAddress", data, widget.createOrUpdateSuccess.bind(widget), widget.saveFailure.bind(widget), widget.editingAddressId());
    },
    
    /**
     * Invoked when the user clicks on the cancel button while either viewing the address or editing the address:
     * @pData {Address reference}
     * @type {function}
     */
    handleCancelUpdateOrganizationAddress: function() {
      var widget = this;
      if (widget.isDirty()) {
        // create new 'shown' event handler
        $('#cc-cancel-account-addresses-ModalContainer').on('show.bs.modal', function() {
          $('#cc-cancel-account-addresses-ModalContainer-modal-dialog').show();
        });
        // create new 'hidden' event handler
        $('#cc-cancel-account-addresses-ModalContainer').on('hide.bs.modal', function() {
          $('#cc-cancel-account-addresses-ModalContainer-modal-dialog').hide();
        });
        // open modal
        $('#cc-cancel-account-addresses-ModalContainer').modal('show');
      } else {
        widget.redirectToAccountAddressesListingPage(true);
      }
    },
    
    /**
     * Called when the user clicks on "Yes" button on the cancel modal:
     */
    handleModalUpdate: function() {
      var widget = this;
      var isSaveSuccess = widget.handleUpdateOrganizationAddress(true);
      $("#cc-cancel-account-addresses-ModalContainer").modal('hide');
    },
    
    /**
     * Called when the user clicks on "Discard" button on the cancel modal:
     */
    handleModalCancelUpdateDiscard: function() {
      var widget = this;
      widget.redirectToAccountAddressesListingPage(true); 
    },

    /**
     * Helper function when the user clicks on "No" button on the cancel modal:
     */
    redirectToAccountAddressesListingPage: function(isServiceCallNeeded) {
      var widget = this;
      widget.user().isSearchInitiatedWithUnsavedChanges(false);
      if(widget.interceptedLink != null && widget.interceptedLink != undefined && widget.interceptedLink != "" && !navigation.isPathEqualTo(widget.interceptedLink)) {
        navigation.goTo(widget.interceptedLink);
      }
      else {
        widget.isEditMode(false);
      }
      widget.isDirty(false);
      $("#cc-cancel-account-addresses-ModalContainer").modal('hide');
      $('body').removeClass('modal-open');
      $('.modal-backdrop').remove();
      notifier.clearError(widget.WIDGET_ID);
      widget.resetAddressData();
    },
    
    /**
     * Subscription function called for performing validations when any value in the subscription list is changed by the user:
     * @type {function}
     */
    subscribeForChanges: function() {
      var widget = this;
      widget.subscriptionArray.push(widget.addressType.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.address1.subscribe(function(newValue) {
          widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.address2.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.country.subscribe(function(newValue) {
          if ((widget.country() === undefined) || 
             (widget.country() === '')) {
//            widget.country(widget.countriesList()[0].countryCode);
        	  widget.country('');
        	  widget.state(null);
            widget.postalCodePattern('');
          } else {
            for (var i=0; i<widget.countriesList().length; i++) {
              if (widget.countriesList()[i].countryCode == widget.country()) {
                widget.country(widget.countriesList()[i].countryCode);
              }
            }
          }
          // reset state if one has been selected
          if ((widget.state() !== undefined) || 
             (widget.state() !== '')) {
            // needs to be null rather than empty string
            // or knockout resets to dropdown value
            widget.state(null);
          }
          
          // Update State List
          widget.stateList([]);
          for (var i=0; i<widget.countriesList().length; i++) {
            if (widget.countriesList()[i].countryCode === widget.country()) {
              widget.stateList(widget.countriesList()[i].regions);
              widget.state(widget.stateList()[0]);
              // Postal code pattern match. Currently hardcoded
              // into the JS file. Maybe the pattern can be sent
              // from the repository.
              if (widget.country() === CCConstants.UNITED_STATES) {
                widget.postalCodePattern(widget.US_POSTAL_CODE_PATTERN);
              }
              
              else if (widget.country() === CCConstants.CANADA) {
                widget.postalCodePattern(widget.CANADA_POSTAL_CODE_PATTERN);
              }
              else {
                widget.postalCodePattern(widget.DEFAULT_POSTAL_CODE_PATTERN);
              }
            }
          }
          widget.isDirty(true);
        }));
      
      widget.subscriptionArray.push(widget.state.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.city.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.companyName.subscribe(function(newValue) {
          widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.postalCode.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.phoneNumber.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.isDefaultShippingAddress.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
      widget.subscriptionArray.push(widget.isDefaultBillingAddress.subscribe(function(newValue) {
        widget.isDirty(true);
      }));
    },
    
    /**
     * Invoked when a address is selected from the address listing page to view the details:
     */
    resetAddressData: function() {
      var widget = this;
      widget.addressType(null);
      widget.address1(null);
      widget.address2(null);
      widget.city(null);
      widget.companyName(null);
//      widget.postalCode(null);	  
      if((widget.country() === undefined) || (widget.country() === null)) {
	    widget.country(); 
	  } else {
	    widget.country('');
	  }
      if((widget.state() === undefined) || (widget.state() === null)) {
	    widget.state(); 
	  } else {
	    widget.state('');
	  }
      widget.postalCode(null);
//      widget.country('');
//      widget.state('');
      widget.phoneNumber(null);
      widget.isDefaultBillingAddress(false);
      widget.isDefaultShippingAddress(false);
      widget.validationModel.errors.showAllMessages(false);
      widget.isCreateNewAddress(false);
      widget.editingAddressId(null);
      var length = widget.subscriptionArray.length;
      for(var i=0; i < length; i++) {
        widget.subscriptionArray[i].dispose();
      }
      widget.subscriptionArray = [];
    },
    /**
     * Called each time the widget is rendered:
     *    Ensure the user is authenticated, prompt to login if not.
     */
    beforeAppear: function(page) {
      var widget = this;
      // The user MUST be logged in to view this widget.
      if (!this.user().loggedIn() || this.user().isUserSessionExpired()) {
        navigation.doLogin(navigation.getPath(), this.links().home.route);
        return;
      } else {
        if (this.user().roles !== undefined && this.user().roles !== null && this.user().roles().length > 0) {
          var roleLength = this.user().roles().length;
          for (var role = 0; role < roleLength; role++) {
            if (this.user().roles()[role]["function"] == "admin") {
              this.isDelegatedAdmin(true);
              break;
            }
          }
        }
        else if(widget.user().roles().length == 0 && widget.user().id() == "") {
        	this.isDelegatedAdmin(true);
        	return;
        }
        if (!this.isDelegatedAdmin()) {
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
//          navigation.goTo('/403', true, false);
        }
        else {
        	widget.isEditMode(false);
          widget.loadOrganizationAddresses();
        }
      }
    },
    /**
     * Replaces "null" in address2 of all addresses in array with null
     */
    replaceAllAddress2NullStringWithNull: function(pAddresses) {
      var addressesLength = pAddresses.length;
      for(var i = 0; i < addressesLength; i++) {
        var currentAddress = pAddresses[i];
        if(currentAddress.address.address2 === "null") {
          currentAddress.address.address2 = null;
        }
      }
    },
    
    /**
     * Returns address id if nickname already exists
     * else returns false
     */
    isDuplicateNickname: function() {
      var widget = this;
      var addresseslength = widget.addresses().length;
      //Trimming trailing and leading whitespaces
      var currentAddressTypeTrimmed = widget.addressType().replace(/^\s+|\s+$/g, "");
      for(var i=0;i<addresseslength;i++){ 
        if(widget.addresses()[i].addressType === currentAddressTypeTrimmed){
          if(widget.isCreateNewAddress()) {
            return widget.addresses()[i].address.repositoryId;
          } else if(widget.addresses()[i].address.repositoryId !== widget.editingAddressId()) {
            return widget.addresses()[i].address.repositoryId;
          }
        }
      }
      return false;
    },
    
    /**
     * address to data object
     */
    convertToData: function(pData) {
      var widget = this;
      var data = {};
      data[CCConstants.ORG_ADDRESS_TYPE] = widget.addressType();
      var address = {};
      address[CCConstants.ORG_ADDRESS_1] = widget.address1();
      if(!(widget.address2() === null || widget.address2() === undefined || widget.address2() === "")) {
        address[CCConstants.ORG_ADDRESS_2] = widget.address2();
      }
      address[CCConstants.ORG_COUNTRY] = widget.country();
      if(widget.stateList().length <= 0) {
        address[CCConstants.ORG_STATE] = "";
      } else {
        address[CCConstants.ORG_STATE] = widget.state();
      }
      address[CCConstants.ORG_COMPANY_NAME] = widget.companyName();
      address[CCConstants.ORG_CITY] = widget.city();
      address[CCConstants.ORG_POSTAL_CODE] = widget.postalCode();
      address[CCConstants.ORG_PHONE_NUMBER] = widget.phoneNumber();
      address[CCConstants.ORG_IS_DEFAULT_BILLING_ADDRESS] = widget.isDefaultBillingAddress();
      address[CCConstants.ORG_IS_DEFAULT_SHIPPING_ADDRESS] = widget.isDefaultShippingAddress();
      data[CCConstants.ORG_ADDRESS] = address;
      return data;
    }
  };
});