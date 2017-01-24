define(

 

  //-------------------------------------------------------------------

  // DEPENDENCIES

  //-------------------------------------------------------------------

  ['jquery', 'knockout', 'ccLogger'],

 

  //-------------------------------------------------------------------

  // Module definition

  //-------------------------------------------------------------------

  function($, ko, CCLogger) {

 

    'use strict';

 

    return {
Costcenter : ko.observable(),
PhoneNumber : ko.observable(),
WorkLocation : ko.observable(),


      onLoad : function(widget) {
	  for (var i=0; i< widget.user().dynamicProperties().length; i++){
	    if (widget.user().dynamicProperties()[i].id() == 'Costcenter') {
	      widget.Costcenter(widget.user().dynamicProperties()[i].value());
		} 
		else if (widget.user().dynamicProperties()[i].id() == 'PhoneNumber') {
	      widget.PhoneNumber(widget.user().dynamicProperties()[i].value());
		} 
		else if (widget.user().dynamicProperties()[i].id() == 'WorkLocation') {
	      widget.WorkLocation(widget.user().dynamicProperties()[i].value());
		} 		
		
	  }
	

widget.PhoneNumber.subscribe(function() {
	  for (var i=0; i< widget.user().dynamicProperties().length; i++){
	    if (widget.user().dynamicProperties()[i].id() == 'PhoneNumber') {
		  widget.user().dynamicProperties()[i].value(widget.PhoneNumber());
		  break;
		}
	  }
});
widget.WorkLocation.subscribe(function() {
	  for (var i=0; i< widget.user().dynamicProperties().length; i++){
	    if (widget.user().dynamicProperties()[i].id() == 'WorkLocation') {
		  widget.user().dynamicProperties()[i].value(widget.WorkLocation());
		  break;
		}
	  }
});
      }

    }

  }

);