/**
 * @fileoverview Delegated Admin Contacts List Widget.
 *
 *
 */
/*global $ */
/*global define */
define(
  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub', 'ccLogger', 'notifier', 'ccConstants', 'jquery', 'ccRestClient', 'navigation', 'spinner', 'viewModels/delegatedAdminContacts'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub, log, notifier, CCConstants, $, CCRestClient, navigation, spinner, DelegatedAdminContacts) {

    "use strict";

    return {
      WIDGET_ID: "delegatedAdminContacts",
      interceptedLink: null,
      contactDetailsMode: ko.observable(false),
      contactSearchValue: ko.observable(""),
      fetchSize: ko.observable(15),
      firstName: ko.observable(null),
      isFirstNameModified: false,
      contactId: ko.observable(null),
      lastName: ko.observable(null),
      isLastNameModified: false,
      emailAddress: ko.observable(null),
      isEmailAddressModified: false,
      roles: ko.observable(null),
      isRolesModified: false,
      buyerPermission: ko.observable(true),
      buyerRoleRepositoryCode: null,
      adminRoleRepositoryCode: null,
      adminPermission: ko.observable(false),
      status: ko.observable(true),
      isAdmin: ko.observable(false),
      isStatusModified: false,
      state: ko.observable(null),
      isCurrentUser: ko.observable(false),
      delegatedAdminContactsListGrid: ko.observableArray([]),
      roleFilterText: ko.observable("All"),
      isDelegatedAdminFormEdited: false,
      subscriptions: [],
      sortDirections: ko.observable({"firstName":"both","lastName":"both","email":"both"}),
      listingIndicatorOptions: {
        parent: '#CC-delegatedAdminContactsList',
        posTop: '10%',
        posLeft: '50%'
      },

      onLoad: function(widget) {
        var self = this;
        widget.isDelegatedAdminFormEdited = false;
        widget.listingViewModel = ko.observable();
        widget.listingViewModel(new DelegatedAdminContacts());
        widget.listingViewModel().itemsPerPage = widget.fetchSize();
        widget.listingViewModel().blockSize = widget.fetchSize();
        widget.fetchSize.subscribe(function(value) {
            widget.listingViewModel().itemsPerPage = widget.fetchSize();
            widget.listingViewModel().blockSize = widget.fetchSize();
            widget.listingViewModel().refinedFetch();
        });

        // Define property validation for first name.
        widget.firstName.extend({
          required: {
            params: true,
            message: widget.translate('firstNameRequired'),
          },
          maxLength: {
            params: CCConstants.REPOSITORY_STRING_MAX_LENGTH,
            message: widget.translate('maxlengthValidationMsg', {
              fieldName: widget.translate('firstNameText'),
              maxLength: CCConstants.REPOSITORY_STRING_MAX_LENGTH
            })
          }
        });

        // Define property validation for last name.
        widget.lastName.extend({
          required: {
            params: true,
            message: widget.translate('lastNameRequired'),
          },
          maxLength: {
            params: CCConstants.REPOSITORY_STRING_MAX_LENGTH,
            message: widget.translate('maxlengthValidationMsg', {
              fieldName: widget.translate('lastNameText'),
              maxLength: CCConstants.REPOSITORY_STRING_MAX_LENGTH
            })
          }
        });

        // Define property validation for email address.
        widget.emailAddress.extend({
          required: {
            params: true,
            message: widget.translate('emailAddressRequired'),
          },
          maxLength: {
            params: CCConstants.REPOSITORY_STRING_MAX_LENGTH,
            message: widget.translate('maxlengthValidationMsg', {
              fieldName: widget.translate('emailAddressText'),
              maxLength: CCConstants.REPOSITORY_STRING_MAX_LENGTH
            })
          },
          pattern: {
            params: /^([\d\w-\.]+@([\d\w-]+\.)+[\w]{2,4})?$/,
            message: widget.translate('invalidEmailAddress'),
          }
        });

        // Define property validation for roles.
        widget.roles.extend({
          required: {
            params: true,
            message: widget.translate('rolesRequired'),
          }
        });

        widget.validationModel = ko.validatedObservable({
          firstName: widget.firstName,
          lastName: widget.lastName,
          emailAddress: widget.emailAddress,
          roles: widget.roles
        });
        
        widget.delegatedAdminContactsListGrid = ko.computed(function() {
          var numElements, start, end, width;
          var rows = [];
          var orders;
            var startPosition, endPosition;
            // Get the orders in the current page
            startPosition = (widget.listingViewModel().currentPage() - 1) * widget.listingViewModel().itemsPerPage;
            endPosition = startPosition + parseInt(widget.listingViewModel().itemsPerPage,10);
            orders = widget.listingViewModel().data.slice(startPosition, endPosition);
          
          if (!orders) {
            return;
          }
          numElements = orders.length;
          width = parseInt(widget.listingViewModel().itemsPerRow(), 10);
          start = 0;
          end = start + width;
          while (end <= numElements) {
            rows.push(orders.slice(start, end));
            start = end;
            end += width;
          }
          if (end > numElements && start < numElements) {
            rows.push(orders.slice(start, numElements));
          }
          return rows;
        }, widget);

        $.Topic(pubsub.topicNames.DELEGATED_CONTACTS_LIST_FAILED).subscribe(function(data) {
          if (this.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            widget.user().handleSessionExpired();
            if (navigation.isPathEqualTo(widget.links().profile.route) || navigation.isPathEqualTo(widget.links().delegatedAdminContacts.route)) {
              navigation.doLogin(navigation.getPath(), widget.links().home.route);
            }
          } else {
        	  notifier.clearError(widget.WIDGET_ID);
              notifier.clearSuccess(widget.WIDGET_ID);
              notifier.sendError("delegatedAdminContacts", this.message ? this.message : this.translate('ListingErrorMsg'), true);
          }
        });
      },
      
      /**
       * Called when toggle switch is operated via keyboard:
       */
      toggleSwitch: function(data, event) {
    	  if((event.keyCode || event.charCode) && ((event.keyCode == 13 || event.charCode == 13) || (event.keyCode == 32 || event.charCode == 32))) {
    		  if(this.status()) {
    			  this.status(false);
    		  }
    		  else if(!this.status()) {
    			  this.status(true);
    		  }
    		  return true;
    	  }
    	  else if(event.keyCode == 9) {
    		  return true;
    	  }
      },
      
      /**
       * Called each time a contact is clicked to view the details from the contacts listing page:
       * @data : Contact information that needs to be populated 
       * @index : Index of the row object in the listing table. We will need this to refresh the table with the details later
       */
      clickContactDetails: function(data, index) {
        var widget = this;
        widget.resetContactData();
        if (data && data[0] !== null && data[0] !== undefined) {
          widget.contactId(data[0].repositoryId);
          widget.firstName(data[0].firstName);
          widget.lastName(data[0].lastName);
          widget.emailAddress(data[0].email);
          if(widget.user().emailAddress() == widget.emailAddress()) {
        	  widget.isCurrentUser(true);
          }
          if (data[0].active) {
            widget.status(true);
          } else {
            widget.status(false);
          }
          if (data[0].roles) {
            var roleLength = data[0].roles.length;
            for (var role = 0; role < roleLength; role++) {
              if (data[0].roles[role]["function"] === CCConstants.ADMIN_LARGE_TEXT) {
                widget.adminPermission(true);
                widget.isAdmin(true);
                this.roles(true);
              } else if (data[0].roles[role]["function"] === CCConstants.BUYER_LARGE_TEXT) {
                widget.buyerPermission(true);
                this.roles(true);
              }
            }
          }
        } else {
          this.roles(true);
          this.isFirstNameModified = true;
          this.isLastNameModified = true;
          this.isEmailAddressModified = true;
          this.isRolesModified = true;
          this.isStatusModified = true;
        }
        widget.subscribeForChanges();
        widget.isDelegatedAdminFormEdited = false;
        widget.contactDetailsMode(true);
        $("#cc-contact-first-name").focus();
        
        return false;
      },
      roleFilterChanged: function(data, index){
        var widget = this;
        if(widget.roleFilterText()=="Administrator"){
          widget.listingViewModel().roleValue = widget.adminRoleRepositoryCode;
          widget.listingViewModel().refinedFetch();
        }else{
          widget.listingViewModel().roleValue = "";
          widget.listingViewModel().refinedFetch();
        }
      },

      /**
  	   * Subscription function called for performing validations when any value in the subscription list is changed by the user:
  	   * @type {function}
  	   */
      subscribeForChanges: function() {
        this.subscriptions.push(this.firstName.subscribe(function(value) {
          this.isDelegatedAdminFormEdited = true ;
          this.isFirstNameModified = true;
        }, this));
        this.subscriptions.push(this.lastName.subscribe(function(value) {
            this.isDelegatedAdminFormEdited = true;
            this.isLastNameModified = true;
        }, this));
        this.subscriptions.push(this.emailAddress.subscribe(function(value) {
            this.isDelegatedAdminFormEdited = true;
            this.isEmailAddressModified = true;
        }, this));
        this.subscriptions.push(this.status.subscribe(function(value) {
            this.isDelegatedAdminFormEdited = true;
            this.isStatusModified = true;
        }, this));
        this.subscriptions.push(this.buyerPermission.subscribe(function(value) {
            this.isDelegatedAdminFormEdited = true;
            this.roles(true);
            this.isRolesModified = true;
        }, this));
        this.subscriptions.push(this.adminPermission.subscribe(function(value) {
            this.isDelegatedAdminFormEdited = true;
            this.roles(true);
            this.isRolesModified = true;
        }, this));
      },
      
      /**
  	   * Called when the user enters a contact in the search box on the contact listing page:
  	   * @page {param}
  	   * @type {function}
  	   */
      searchContactDetails: function(data, event) {
        var widget = this;
        if(this.contactSearchValue() != "") {
        	this.contactSearchValue(this.contactSearchValue().trim());
        }
        widget.listingViewModel().searchValue = this.contactSearchValue();
        widget.listingViewModel().refinedFetch();
      },
      
      /**
  	   * Called before the widget is displayed on the page each and every time:
  	   * @page {param}
  	   * @type {function}
  	   */
      beforeAppear: function(page) {
        var widget = this;
        if (widget.user().loggedIn() == false) {
          navigation.doLogin(navigation.getPath(), widget.links().home.route);
        } else {
          if (widget.user().roles !== undefined && widget.user().roles !== null && widget.user().roles().length > 0) {
            var roleLength = widget.user().roles().length;
            for (var role = 0; role < roleLength; role++) {
              if (widget.user().roles()[role]["function"] == "admin") {
                widget.listingViewModel().isDelegatedAdmin(true);
                widget.adminRoleRepositoryCode = widget.user().roles()[role]["repositoryId"];
              } else if (widget.user().roles()[role]["function"] == "buyer") {
                widget.buyerRoleRepositoryCode = widget.user().roles()[role]["repositoryId"];
              }
            }
          } else if(widget.user().roles().length == 0 && widget.user().id() == "") {
            widget.listingViewModel().isDelegatedAdmin(true);
            return;
          }
          if (!widget.listingViewModel().isDelegatedAdmin()) {
            notifier.clearError(widget.WIDGET_ID);
            notifier.clearSuccess(widget.WIDGET_ID);
//            navigation.goTo('/403', true, false);
          }else{
            widget.listingViewModel().clearOnLoad = true;
            widget.listingViewModel().load(1, 1);
            widget.contactDetailsMode(false);
          }
        }
      },


      /**
       * Called when the user clicks on "Save" button to save the contact data to the server:
       * @type {function}
       */
      saveContactData: function(isRedirectionNeeded) {
        //We will need to check if the data entered is valid.
        var widget = this;
        if (this.validationModel.isValid()) {
          var contactData = this.koToJS();
          if(widget.isDelegatedAdminFormEdited) {
            if (this.contactId() !== null) {
              CCRestClient.request(CCConstants.ENDPOINT_UPDATE_CONTACTS_BY_ORGANIZATION, contactData, this.updateMemberSuccessFunction.bind(this,isRedirectionNeeded), this.updateMemberFailureFunction.bind(this), this.contactId());
            } else {
              CCRestClient.request(CCConstants.ENDPOINT_CREATE_CONTACTS_BY_ORGANIZATION, contactData, this.createMemberSuccessFunction.bind(this,isRedirectionNeeded), this.createMemberFailureFunction.bind(this));
            }
          }else{
        	  this.redirectToContactListingPage(true);
          }
          /*if(isRedirectionNeeded == true) {
        	  this.redirectToContactListingPage(true);
          }*/
        } else {
          this.validationModel.errors.showAllMessages();
          notifier.clearError(widget.WIDGET_ID);
          notifier.clearSuccess(widget.WIDGET_ID);
          notifier.sendError(this.WIDGET_ID, this.translate('ErrorMsg'), true);
          return;
        }
      },
      
      /**
       * Success Call back function for a contact creation:
       * @data {Response from the server}
       * @type {function}
       */
      createMemberSuccessFunction: function(isRedirectionNeeded,data) {
    	notifier.sendSuccess(this.WIDGET_ID, this.translate('ContactCreationSuccessMsg'), true);
    	this.isDelegatedAdminFormEdited = false;
    	if(this.adminPermission()) {
    		this.isAdmin(true);
    	}
      	if(data && data.id) {
      		this.contactId(data.id);
      	} 
      	if(isRedirectionNeeded == true) {
      	  this.redirectToContactListingPage(true);
        }
      },
      
      /**
       * Failure Call back function for a contact creation:
       * @data {Response from the server}
       * @type {function}
       */
      createMemberFailureFunction: function(data) {
    	var widget = this;
        notifier.clearError(this.WIDGET_ID);
        notifier.clearSuccess(this.WIDGET_ID);
        if (data.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            widget.user().handleSessionExpired();
            if (navigation.isPathEqualTo(widget.links().profile.route) || navigation.isPathEqualTo(widget.links().delegatedAdminContacts.route)) {
              navigation.doLogin(navigation.getPath(), widget.links().home.route);
            }
        }
        else {
          notifier.sendError(widget.WIDGET_ID, data.message, true);
        }
      },
        
      /**
       * Success Call back function for update contact:
       * @data {Response from the server}
       * @type {function}
       */
      updateMemberSuccessFunction: function(isRedirectionNeeded,data) {
        notifier.clearError(this.WIDGET_ID);
        notifier.clearSuccess(this.WIDGET_ID);
        notifier.sendSuccess(this.WIDGET_ID, this.translate('SuccessMsg'), true);
        this.isDelegatedAdminFormEdited = false;
        if (this.adminPermission()) {
          this.isAdmin(true);
        }
        if(isRedirectionNeeded == true) {
      	  this.redirectToContactListingPage(true);
        }
      },
      
      /**
       * Failure Call back function for update contact:
       * @data {Response from the server}
       * @type {function}
       */
      updateMemberFailureFunction: function(data) {
    	var widget = this;
        notifier.clearError(this.WIDGET_ID);
        notifier.clearSuccess(this.WIDGET_ID);
        if (data.status == CCConstants.HTTP_UNAUTHORIZED_ERROR) {
            widget.user().handleSessionExpired();
            if (navigation.isPathEqualTo(widget.links().profile.route) || navigation.isPathEqualTo(widget.links().delegatedAdminContacts.route)) {
              navigation.doLogin(navigation.getPath(), widget.links().home.route);
            }
        }
        else {
          notifier.sendError(this.WIDGET_ID, data.message, true);
        }
      },
      
      /**
       * Function for JSON object construction that needs to be passed as a pay load to the server during contact creation and edit:
       * @type {function}
       */
      koToJS: function() {
        var widget = this;
        var data = {};
        if(this.isFirstNameModified) {
          data[CCConstants.FIRST_NAME_TEXT] = this.firstName();
        }
        if(this.isLastNameModified) {
          data[CCConstants.LAST_NAME_TEXT] = this.lastName();
        }
        if(this.isEmailAddressModified) {
          data[CCConstants.EMAIL_ADDRESS_TEXT] = this.emailAddress();
        }
        if(this.isStatusModified) {
          data[CCConstants.ACTIVE_TEXT] = this.status();
        }
        if(!this.contactId()) {
          data[CCConstants.RECEIVE_MAIL_TEXT] = CCConstants.YES_TEXT;
        }
        if(this.isRolesModified) {
          data[CCConstants.ROLES_TEXT] = [];
          if (this.buyerPermission()) {
            var buyerRoleObject = {};
            buyerRoleObject["function"] = "buyer";
            data[CCConstants.ROLES_TEXT].push(buyerRoleObject);
          }
          if (this.adminPermission()) {
            var adminRoleObject = {};
            adminRoleObject["function"] = "admin";
            data[CCConstants.ROLES_TEXT].push(adminRoleObject);
          }
        }
        return data;
      },

      /**
       * Called when the user clicks on "Cancel" button. We will check if the form is dirty and display a data discard modal:
       */
      cancelContactData: function() {
        if (this.isDelegatedAdminFormEdited) {
          // create new 'shown' event handler
          $('#cc-cancel-contacts-ModalContainer').on('show.bs.modal', function() {
            $('#cc-cancel-contacts-ModalContainer-modal-dialog').show();
          });
          // create new 'hidden' event handler
          $('#cc-cancel-contacts-ModalContainer').on('hide.bs.modal', function() {
            $('#cc-cancel-contacts-ModalContainer-modal-dialog').hide();
            $("div").remove(".modal-backdrop");
            $("body").removeClass("no-scroll modal-open");
            $("body").addClass("modal-close");
          });
          // open modal
          $('#cc-cancel-contacts-ModalContainer').modal('show');
        } else {
          this.redirectToContactListingPage(true);
        }
      },


      /**
       * Called when the user presses "Enter" key for search after the search term is entered:
       */
      searchContactKeyPress: function(data, event) {
          var widget = this;
          try {
            if (event.which == 13) {
              widget.searchContactDetails();
                return false;
            }
            return true;
          }catch(e){}
      },
      
      /**
       * Called when the user clicks on "Yes" button on the cancel modal:
       */
      handleModalUpdate: function() {
    	var isSaveSuccess = this.saveContactData(true);
        $("#cc-cancel-contacts-ModalContainer").modal('hide');
    	$('body').removeClass('modal-open');
    	$('.modal-backdrop').remove();
      },
      
      /**
       * Called when the user clicks on "No" button on the cancel modal:
       */
      handleModalCancelUpdateDiscard: function() {
    	  var widget = this;
    	  widget.redirectToContactListingPage(true); 
      },

      /**
       * Helper function when the user clicks on "No" button on the cancel modal:
       */
      redirectToContactListingPage: function(isServiceCallNeeded) {
    	if(isServiceCallNeeded) {
    	  if(this.contactSearchValue() != "") {
              this.contactSearchValue(this.contactSearchValue().trim());
          }
    	  this.listingViewModel().searchValue = this.contactSearchValue();
    	  if(this.interceptedLink == "/cart") {
    		  this.interceptedLink = "/delegatedAdminContacts";
    	  }
    	  if(this.interceptedLink == null || this.interceptedLink == undefined || this.interceptedLink == "" || this.interceptedLink == "/delegatedAdminContacts") {
    	    this.listingViewModel().refinedFetch();
    	  }
    	}
        this.user().isSearchInitiatedWithUnsavedChanges(false);
        if(this.interceptedLink != null && this.interceptedLink != undefined && this.interceptedLink != "" && !navigation.isPathEqualTo(this.interceptedLink)) {
        	this.isDelegatedAdminFormEdited = false;
        	navigation.goTo(this.interceptedLink);
        }
        else {
            this.contactDetailsMode(false);
        }
        this.isDelegatedAdminFormEdited = false;
        $("#cc-cancel-contacts-ModalContainer").modal('hide');
        $('body').removeClass('modal-open');
        $('.modal-backdrop').remove();
        notifier.clearError(this.WIDGET_ID);
        //notifier.clearSuccess(this.WIDGET_ID);
      },
      
      /**
       * Called when the user sorts the contact list based on first name, last name, email:
       * @param sortOrder : order of sort either ascending or descending
       * @param sortTerm : attribute on which the sort needs to be performed
       */
      clickContactSort: function(sortOrder,sortTerm){
        var widget=this;
       widget.sortDirections()[sortTerm]=sortOrder;
       if(sortTerm=="firstName"){
         widget.sortDirections()["lastName"]="both";
         widget.sortDirections()["email"]="both";
       }else if(sortTerm=="lastName"){
         widget.sortDirections()["firstName"]="both";
         widget.sortDirections()["email"]="both";
       }else if(sortTerm=="email"){
         widget.sortDirections()["lastName"]="both";
         widget.sortDirections()["firstName"]="both";
       }
       widget.sortDirections.valueHasMutated();
        var sortString=sortTerm+":"+sortOrder;
        widget.listingViewModel().sortProperty=sortString;
        widget.listingViewModel().refinedFetch();
      
      },

      /**
       * Invoked when a contact is selected from the contact listing page to view the details:
       */
      resetContactData: function() {
        this.firstName(null);
        this.interceptedLink = null;
        this.lastName(null);
        this.emailAddress(null);
        this.isDelegatedAdminFormEdited = false;
        this.roles(null);
        this.buyerPermission(true);
        this.adminPermission(false);
        this.status(true);
        this.state(null);
        this.contactId(null);
        this.isAdmin(false);
        this.isCurrentUser(false);
        this.isFirstNameModified = false;
        this.isLastNameModified = false;
        this.isEmailAddressModified = false;
        this.isStatusModified = false;
        this.isRolesModified = false;
        this.validationModel.errors.showAllMessages(false);
        var length = this.subscriptions.length;
		for(var i=0; i < length; i++) {
			this.subscriptions[i].dispose();
		}
      }

    };
  });