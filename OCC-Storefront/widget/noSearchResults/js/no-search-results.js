/**
 * @fileoverview No Search Results Widget. 
 * 
 */
define(

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'CCi18n',
   'ccConstants', 'pubsub','jquery'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, CCi18n, CCConstants, pubsub, $) {  
  
    "use strict";
  
    return {

      showErrorMsg: ko.observable(false),
      /**
        No Search Results Found Widget.
        @private
        @name no-search-results
        @property {observable GuidedNavigationViewModel} view model object representing the guided navigation details
       */
      onLoad: function(widget) {
        var self = this;  
        var resourceKey = 'ns.common:resources.noProductsFound';
        widget.searchTerm = ko.observable("");

        /**
         * @function
         * @name getResultsText
         * Uses the resourceKey to get the text compiled with argumented data
         * @return string result of search message
         */      
        widget.getResultsText = ko.computed(function() {
          // check if search error has occurred.
          var searchMessage = '';
          if (widget.showErrorMsg()) {
            searchMessage = widget.translate('searchFailedText');
          } else if (widget.searchTerm()) {
            searchMessage = CCi18n.t(resourceKey,
                {
                  searchterm: widget.searchTerm()
                });
          } else {
            searchMessage = CCi18n.t('ns.common:resources.noProductsFoundNoSearchTerm');
          }
          return searchMessage;
          });

        /**
         * @function
         * @name setSearchTerm
         * Sets the searchTerm from the publishWith data
         */        
        self.setSearchTerm = function() {
          if ((typeof this != "undefined") & (this.length > 0)) { 
            widget.showErrorMsg(false);
            widget.searchTerm(this[0]);
          }
        };
      
        $.Topic(pubsub.topicNames.SEARCH_TERM).subscribe(self.setSearchTerm);
        $.Topic(pubsub.topicNames.SEARCH_FAILED_TO_PERFORM).subscribe(function(obj) {
          widget.showErrorMsg(true);
        });
      }
    };
  }
);
