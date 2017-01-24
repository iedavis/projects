/**
 * @fileoverview Cookie Consent Widget.
 * Option.
 * @author ian.davis@oracle.com
 */

define(
  //-------------------------------------------------------------------
  // PACKAGE NAME
  //-------------------------------------------------------------------
  'cookieConsent',

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['knockout'],

  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function(ko) {

    "use strict";

    var geoIpURLs = ["http://freegeoip.net/json/", "http://ipinfo.io/json"];

    var widget;

    return {

      onLoad: function(widgetModel) {

        widget = widgetModel;

//      Has consent been given in a previous session?
        if(document.cookie.indexOf('cookieConsentAccepted=true') < 0){

          var countryCode;

//        Pick a random server from the array of servers
          $.getJSON( geoIpURLs[Math.floor(Math.random() * geoIpURLs.length)] )
            .done(function( geoIpData ) {
              if (geoIpData.country_code) {
                countryCode = geoIpData.country_code;
              }
              else if (geoIpData.country) {
                countryCode = geoIpData.country;
              }
              else { countryCode = "EU"; }

              widget.decideToDisplay(countryCode);
            })

            .fail(function( jqxhr, textStatus, error ) {
              widget.decideToDisplay("EU");
            });
        }
      },

      decideToDisplay: function(countryCode){

        if(widget.consentRequiredCountries().indexOf(countryCode) >= 0){
//        Construct the dialog box
          setTimeout(function(){
            widget.constructDialogDIV();
          },1500);
        }
      },

      constructDialogDIV: function(){

        var cookieConsentDiv;
        cookieConsentDiv = document.createElement('div');
        cookieConsentDiv.id = 'cookieConsentContainer';
        document.getElementById('wrapper').appendChild(cookieConsentDiv);

        cookieConsentDiv.style.position = "fixed";
        cookieConsentDiv.style.width = "310px";
        cookieConsentDiv.style.backgroundColor = getComputedStyle($('.footer')[0]).color;
        cookieConsentDiv.style.backgroundColor = getComputedStyle($('.header-background')[0]).backgroundColor;
        cookieConsentDiv.style.padding = "10px";
        cookieConsentDiv.style.border = "1px solid #ccc";
        cookieConsentDiv.style.zIndex = "99999";
        cookieConsentDiv.style.maxWidth = "90%";
        cookieConsentDiv.style.borderRadius = "10px";
        cookieConsentDiv.style.fontSize = "11px";
        cookieConsentDiv.style.boxShadow = "0px 5px 10px rgba(0,0,0,0.2)";
        cookieConsentDiv.style.top = "10px";

        if(widget.messagePosition() === "left"){
          cookieConsentDiv.style.left = "10px";
        }
        else{
          cookieConsentDiv.style.right = "10px";
        }


        var cookieConsentChildDiv = document.createElement('div');
        cookieConsentChildDiv.id = 'cookieConsentTitle';
        cookieConsentDiv.appendChild(cookieConsentChildDiv);
        cookieConsentChildDiv.innerHTML = "<h3 style='line-height: 80%;margin-top: 0px;'>Cookie Consent</h3>";

        cookieConsentChildDiv = document.createElement('div');
        cookieConsentChildDiv.id = 'cookieConsentBody1';
        cookieConsentDiv.appendChild(cookieConsentChildDiv);
        cookieConsentChildDiv.innerHTML = "<p>This website uses Cookies to improve your user experience. By continuing, you are accepting Cookies on this website.</p>";

        cookieConsentChildDiv = document.createElement('div');
        cookieConsentChildDiv.id = 'cookieConsentBody2';
        cookieConsentDiv.appendChild(cookieConsentChildDiv);
        cookieConsentChildDiv.innerHTML = "<p>For more details, see our <u><a href='#!/privacy'>Privacy Policy</a></u></p>";

        cookieConsentChildDiv = document.createElement('div');
        cookieConsentChildDiv.id = 'cookieConsentButton';
        cookieConsentDiv.appendChild(cookieConsentChildDiv);
        cookieConsentChildDiv.innerHTML = "<a class='btn btn-mini'>Accept</a>";


        $('#cookieConsentButton').click(function(){
          document.getElementById('cookieConsentContainer').style.display = 'none';
//          document.cookie = "cookieConsentAccepted=true; expires=Sat, 1 Jan 2050 00:00:01 GMT";
        });
      }
    };
  }
);