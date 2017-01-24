/*************************************************
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*************************************************/
package com.google.checkout.sample.event;

import com.google.checkout.sample.protocol.ProtocolException;

/**
 * The <b>MerchantCalculationCallbackHandler</b> defines the process for 
 * handling a &lt;merchant-calculation-callback&gt; request from Google 
 * Checkout. The constructor calls the {@see CallBackHandler} constructor 
 * to create an instance of {@see CallBackParser}. The constructor then adds 
 * itself as a listener for MerchantCalculationCallback events.
 * <br>
 * When a &lt;merchant-calculation-callback&gt; request is received, that 
 * request will be routed from the {@see CallBackServlet} to the 
 * {@see CallBackParser}. The {@see CallBackParser} will then call the 
 * <b>handleEvent</b> method of each class that implements the 
 * {@see MerchantCalculationCallbackListener}.
 * <br>
 * If you use this event handler in your Google Checkout implementation,
 * you will need to implement the <code>handleEvent</code> method to 
 * process the request and then send Google Checkout a 
 * &lt;merchant-calculation-results&gt; response containing the results of 
 * your custom calculations.
 *
 * @version 1.0 beta
 */
public class MerchantCalculationCallbackHandler 
  extends CallBackHandler implements MerchantCalculationCallbackListener {

  public MerchantCalculationCallbackHandler() throws ProtocolException {
    super();
    // _parser instance comes from CallBackHandler.
    _parser.addCallBackListener(this);
  }
  
  public void handleEvent(CallBackEvent event) {
    /*
     * +++ CHANGE ME +++
     * Implement this method to handle &lt;merchant-calculation-callback&gt;
     * requests. To retrieve the JAXB objects that were unmarshalled from the
     * merchant calculation callback request, you can use the following code:
     * <code>MerchantCalculationCallback merchantCalculationCallback = 
     *     event.getMerchantCalculationNote();</code>
     * You can then access individual components of the notification 
     * by calling the methods in the generated JAXB classes.
     * For example:<br>
     * <code>ShoppingCart shoppingCart = 
     *     merchantCalculationCallback.getShoppingCart();</code><br>
     * <code>String googleOrderNumber = 
     *     merchantCalculationCallback.getGoogleOrderNumber();</code>
     * <br><br>
     * When you receive a &lt;merchant-calculation-callback&gt; request, you
     * should respond with a &lt;merchant-calculation-results&gt; response.
     * Please see {@see MerchantCalculationResultBuilder} for methods used to
     * construct a &lt;merchant-calculation-results&gt; response.
     */
  }
}
