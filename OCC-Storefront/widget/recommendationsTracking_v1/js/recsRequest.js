define(
  //-------------------------------------------------------------------------
  // Note: there is a copy of this file in the recommendations widget
  //-------------------------------------------------------------------------

  //-------------------------------------------------------------------
  // DEPENDENCIES
  //-------------------------------------------------------------------
  ['jquery'],
    
  //-------------------------------------------------------------------
  // MODULE DEFINITION
  //-------------------------------------------------------------------
  function($) {

    // default recommendation host
    var REC_HOST = "recs.atgsvcs.com",

    // default recommendations base path
    REC_BASE_PATH = "pr/",

    // An Array of sent requests, used for testing and debugging
    // Requests have two properties, url and params
    REQS = [],

    /**
     * Defines a public version of the passed function reference to be used
     * to encapsulate calls which must be made from outside the library.
     * It is worth noting that these names will not be consistent or
     * related in any way to the original name of the function.
     *
     * @param callback The function to associate with a public callback
     *
     * @return The globally accessible name of the public function
     */
    registerPublicCallback = function(callback) {
      // generate a unique name for the callback
      // + is used here as the unary operator which converts the date object 
      // to a numeric timestamp.  This is favored over the getTime method to 
      // reduce code size.
      var publicName = "cb" + (+new Date);
      
      // ensure it doesn't exist, if it does append zeros until it doesn't
      while (window[publicName]) {
        publicName += 0;
      }
      
      // assign the function to the public property
      window[publicName] = callback;

      // return the globally accessible name of the function
      return "window." + publicName;
    },

    /** 
     * Perform a deep clone of an object.
     * Can strip parameters that shouldn't be sent to the server.
     * These are:
     *  Params that start with a "-" character
     *
     * @param obj The Object that is to be deep cloned
     * @param strip A boolean that specifies whether or not properties should be stripped
     * @return A cloned copy of the Object, possibly stripped of some params
     */
    clone = function(obj, strip) {
      // if object evaluates to false or isn't iterable
      if (!obj || typeof(obj) != 'object') return obj;

      // create a new Array or Object
      var cloned = (obj.constructor === Array)? [] : {};

      // iterate over the property, cloning its children
      $.each(obj, function(key, value) {
        // if we're supposed to strip, check for properties beginning with "-"
        if (!isNaN(key) || !strip || key.charAt(0) != "-") {
          cloned[key] = clone(value, strip);
        }
      });

      return cloned;
    },

    /**
     * Add or replace the protocol with the correct one if the url isn't relative.
     * This function is safe to run more than once on the same string.
     *
     * @param url A url to modify, either just a path or a full address with or without protocol
     * @param forceHttps setting to true will change the protocol of the returned url to https
     * @return The full url to use for the request
     */
    URLize = function (url, forceHttps) {
      // only match if the url is not relative to the domain
      return url.replace(/^(.*:*\/\/)*([^\/]+[^\.])\//,
             function (matched, protocol, domain) {
               return (forceHttps ? "https:" : document.location.protocol) + "//" + domain + "/";
             });
    },

    /**
     * Recursive function to flatten JSON for transmission as URI arguments
     *
     * @param json JSON Object to flatten
     * @param prepend String to prepend to the aruments.  Used for recursion
     *
     * @returns flattened and encoded URI argument string
     */
    flatten = function (json, prepend) {
      var i, key, flat = [], prepend_key;
      
      // add a dot if we have ancestors
      prepend = prepend ? prepend + "." : "";

      // loop through the keys at this level
      for (key in json) {
        // ignore any property with an undefined or null value
        if (json[key] != null && json.hasOwnProperty(key)) {
          prepend_key = prepend + key;
          // Values can be Objects, Arrays, Strings, Numerics or Booleans.
          if (json[key].constructor == Object) {
            // It's an Object/Hash.  Recurse into it.  If it's empty, don't set the key.
            i = flatten(json[key], prepend_key);
            if (i) flat[flat.length] = i;

          } else if (json[key].constructor == Array) {
            // if it is array of hashes, convert it 
            i = json[key].length;
            if (i && json[key][0].constructor == Object) { // if the element of the array is Object, instead of a primitive
               json[key] = convertArrayOfHashesToHashOfParallelLists(json[key]); 

               i = flatten(json[key], prepend_key);
               if (i) flat[flat.length] = i;
            } else {
              // It's an Array of primitives.  Loop through it and add them as a comma delimited list.
              // Encode the value and escape backslashes and commas.
              for (; i--;)
                json[key][i] = encodeURIComponent(json[key][i].replace(/(,|\\)/g, "\\$1"));
              flat[flat.length] = prepend_key + "=" + json[key].join(",");
            }
          } else {
            // It's a String, Numeric or Boolean.  Just add the property as a param.
            flat[flat.length] = prepend_key + "=" + encodeURIComponent(json[key].toString());
          }
        }
      }
      
      return flat.join("&");
    },

    /**
     *
     * convert an object of ArrayOfHashes
     *                   to HashOfParallelLists
     * e.g.
        "products": [
          { "productId" :"SECURITY",
            "quantity": "3",
            "price": "159.99"},

          { "productId": "MUSICIAN",
            "quantity": "1",
            "price": "120.00"},

          { "productId": "SECURITY",
            "quantity": "1",
            "price": "0.00"}
          ]
       
        to

        "products": {
          "productId": ["SECURITY", "MUSICIANS", "SECURITY"],
          "quantity":  ["3", "1", "1"],
          "price":     ["159.99", "120.00", "0.00"]
        },
     *
     */ 
    convertArrayOfHashesToHashOfParallelLists = function (objectArrayOfHashes) {
      var objectHashOfParallelLists = {}, 
          j, property,
          arrayLength = objectArrayOfHashes.length;

      if (arrayLength) {
        // first, we create hash of empty arrays, using each property name as key
        // we can't assume each element (e.g product) has the same properties (e.g. productId, quantity, price)
        $.each(objectArrayOfHashes, function(arrayIndex, arrayElement) {
          // arrayIndex is 0, 1 , 2 .. arrayElement is a hash, e.g a product
          $.each(arrayElement, function(propName) {
             objectHashOfParallelLists[propName] = [];
          });
        });

        // then, we iterate through the ArrayOfHashes and populate the HashOfArrays
        $.each (objectHashOfParallelLists, function(propertyName, propertyArray) {
          for (j=0; j < arrayLength; j++) {
            var value = objectArrayOfHashes[j][propertyName];
            propertyArray.push(value == undefined ? '' : '' + value); // use an empty string if undefined or null
          }
        });
      }

      return objectHashOfParallelLists;
    },

    /**
     * Returns the full url for use in GET requests, regardless of the mechanism used to send it
     *
     * @param url The full url of the request, including domain but minus any parameters or protocol
     * @param params (optional) a map, converted into query string name=value pairs
     * @param forceHttps setting to true will change the protocol of the returned url to https
     *
     * @return The full url to use in a GET request with protocol, path and parameters all added on
     */
    getRequestURL = function(url, params, forceHttps) {
      // add the protocol to the url and set the domain to the default if not set
      url = URLize(url, forceHttps);

      // record the request we're about to make
      REQS.push({url: url, params: params});

      // tack the params onto the url if they exist
      if (params) {
        // append using an & if there's already a ?
        url += (~url.indexOf("?") ? "&" : "?") + flatten(params);
      }
      
      return url;
    },

    /**
     * Manufactures a SCRIPT element and appends it to the HEAD.
     * This is for use in both server calls on browsers that do not support CORS XMLHttpRequest 
     * as well as for simply loading JavaScript onto the page.
     *
     * @param url The full url of the request, including domain but minus any parameters or protocol
     * @param params (optional) a map, converted into query string name=value pairs
     * @param callback (optional) a function to call when the script has loaded
     * @param forceHttps (optional) flag to force the URL to HTTPS or not
     */
    JSRequest = function(url, params, callback, forceHttps) {
      // get the configured character set in which the response will arrive
      var charset = "utf-8", // we assume for now that all Cloud Commerce pages are UTF-8 encoded
          // use variables for easier debugging
          src     = getRequestURL(url, params),
          script  = document.createElement("script");
          
      script.charset = charset;
      script.src = (forceHttps ? src.replace(/^http:/i, 'https:') : src);

      if (callback) {
        try {
          if (script.onload !== undefined) {
            script.onload = callback;
          } else {
            script.onreadystatechange = function() { // old IEs
              if (script.readyState in {loaded:1, complete:1}) { 
                script.onreadystatechange = null;
                callback();
              }
            }
          }
        } catch(e) {
        }
      }

      // make the request by appending a script tag to the head element
      $('head').append(script);
    },

    /**
     * Defines the RESTRequest class
     * Instances of this class should be used to make requests to the Optimization Services servers
     *
     * @constructor
     * @param pathFunc a function which takes resources as its argument and returns the path of the request
     * @param configParams the top level configuration to pass with this request, defaults to ATGSvcs.CFG
     */
    RESTRequest = function (pathFunc, configParams) {
      var resources = [], // an array of resources to call with this request
          callbacks = [], // an array of callback functions and associated namespaces
          params = configParams || {},

      /**
       * Private function to the RESTRequest class which handles delegating the responses
       * to each individual callback function based on namespaces.
       *
       * @param responseJSON The raw response JSON returned by the server
       */
      requestCallback = function(responseJSON) {
        var i = 0,
            namespace;

        if (responseJSON) {
          // call the callbacks that were set first, first
          // i is instantiated at the top of the function
          for (; i < callbacks.length; i++) {
            // shorthand for the namespace
            namespace = responseJSON[callbacks[i][1]];
            // if there is namespaced data, call the function
            if (namespace) {
              callbacks[i][0](namespace);
            }
          }
        }
      };

      /**
       * Adds a resource to query in this request, along with the parameters to pass.
       * Parameters added using this function will be used to extend those already added.
       *
       * @param resource The resource string to request on the server, for example "view"
       * @param json The json to pass to the resource (technically may define json for any resource)
       */
      this.addResource = function(resource, json) {
        // add the resource unless it already exists
        var i = resources.length;
        while (i--) {
          if (resources[i] == resource) break;
        }
        // i will be -1 if resource was not found
        !~i && resources.push(resource);

        // reorder the array to make sure "view" is first element and "recommendations" is the last.
        // pipelined resources are in the form of view/recommendations or view/cart/recommendations only
        var indexOfElement = $.inArray("view", resources);
        // Prepend the view resource if it was found in the resources array, there is more than one resource
        // and it is not the first element
        if (indexOfElement != -1 && (resources.length > 1) && (indexOfElement != 0)) {
          resources.splice(indexOfElement, 1);
          resources.unshift("view");
        }
        indexOfElement = $.inArray("recommendations", resources);
        // Add the recommendations resource if it was found in the resources array, there is more than one resource
        // and it is not the last element
        if (indexOfElement != -1 && (resources.length > 1) && (indexOfElement != resources.length-1)) {
          resources.splice(indexOfElement, 1);
          resources.push("recommendations");
        }

        // deep extend the params Object with the passed json
        $.extend(true, params, json);
      };

      /**
       * Adds a callback function to be passed response data from the request to the server.
       * The callback will be called with the response data contained in the specified namespace.
       *
       * @param callback A reference to a function to call when the request returns
       * @param namespace The namespace of the response data to pass to the callback function
       */
      this.addCallback = function(callback, namespace) {
        // add the callback function to the array for later use
        callbacks.push([callback, namespace]);
      };
      
      /*
       * Adds parameter in a object that countains name value pair
       */
      this.addParam = function(obj) {
        var paramName,
            paramValue;

        for (paramName in (obj || {})) {
          paramValue = obj[paramName];

          // if the param isn't an Object or Array, its a scalar
          // we only want to keep scalars to return
          // we also want to exempt retailerId as it is passed in the request path
          if (typeof paramValue != "object" && paramName != "retailerId") {
            // we don't have to clone this because scalars will be copied
            // on assignment
            params[paramName] = paramValue;
          }
        }
      };

      /**
       * Causes the request to be sent to the server.
       * After this function is called the RESTRequest object may be reused to make more requests, 
       * but it is not recommended.
       *
       * @param {*} isPlato - if set to a truthy value then this is a plato request
       */
      this.send = function (forceHttps) {
        // bail out if there are no resources defined
        if (!resources.length) return;

        var url = URLize(pathFunc(resources.join("/")), forceHttps),
        charset = "utf-8", // we assume for now that all Cloud Commerce pages are UTF-8 encoded
        localWin = window, // get a local reference to window, as we'll be checking for the existance of attributes
        json = localWin.JSON,
        request, postbody, responseText,

        // clone and strip unwanted params
        requestParams = clone(params, 1);

        // if we have XMLHttpRequest and JSON, try using XHR
        if (localWin.XMLHttpRequest && json && json.stringify && json.parse) {
          // get a new XHR object
          request = new XMLHttpRequest();

          // if CORS is supported, use it
          if ("withCredentials" in request) {

            // record the request we're about to make
            REQS.push({url: url, params: requestParams});

            // serialize the params
            // defend against Prototype.js's broken implementation of Array.prototype.toJSON, which breaks stringify
            // by causing stringify(["foo"]) to end up as ""[\"foo\"]"" instead of the correct "["foo"]"
            postbody = (localWin.Prototype && $.isFunction(Object.toJSON)) ? Object.toJSON(requestParams) : json.stringify(requestParams);

            // open a POST request
            request.open("POST", url, true);
            // when the server returns a response, call the passed callback function 
            // with the parsed results
            request.onreadystatechange = function () {
              responseText = $.trim(this.responseText);
              if(this.readyState == 4 && this.status == 200 && responseText) {
                requestCallback(json.parse(responseText));
              }
            };
            // allow the server to set third party cookies
            request.withCredentials = "true";
            // set our content-type header including the charset
            request.setRequestHeader("Content-Type", "text/plain;charset=" + charset);
            // send the request with the serialized JSON request
            request.send(postbody);
            // don't try to send it again
            return;
          }
        }

        // assign a callback parameter to the registered public function for this class instance callback
        requestParams.callback = registerPublicCallback(requestCallback);

        // don't pass a callback or forceHttps since neither behavior is necessary
        JSRequest(url, requestParams);
      };
    }

  return {
    RESTRequest: RESTRequest,
    JSRequest: JSRequest
  };
});
