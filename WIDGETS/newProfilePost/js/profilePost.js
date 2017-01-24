/**
 * @fileoverview Profile Post.
 * Option.
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'profilePost',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout', 'pubsub'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko, pubsub) {

    "use strict";

    var widget;

    return {

      onLoad: function(widgetModel) {
        widget = widgetModel;
        $.Topic(pubsub.topicNames.USER_CREATION_SUCCESSFUL).subscribe(this.postNewProfile);
      },


      postNewProfile: function(data) {
        var newUser = {};
        newUser.id = data.id;
        newUser.firstName = widget.user().firstName();
        newUser.lastName = widget.user().lastName();
        newUser.emailAddress = widget.user().emailAddress();
        newUser.approvedMarketingEmails = widget.user().emailMarketingMails();

        var postUrl = widget.postUrl();

        if (postUrl !== '') {
          $.post( postUrl , newUser )
            .done(function( data ) {
              console.log( "All good!" + data );
            })
            .fail(function( data ) {
              console.log( "Something went wrong!" + data );
            });
        } else {
          console.log( "No Target URL is defined" );
        }
      }
    };
  }
);