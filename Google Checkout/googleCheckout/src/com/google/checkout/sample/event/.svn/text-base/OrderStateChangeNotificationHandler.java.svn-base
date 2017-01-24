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
 * The <b>OrderStateChangeNotificationHandler</b> defines the process for 
 * handling an order state change notification request from Google Checkout. 
 * The constructor calls the {@see CallBackHandler} constructor to create 
 * an instance of {@see CallBackParser}. The constructor then adds itself 
 * as a listener for OrderStateChangeNotification events.
 * <br>
 * When a new order is received, the &lt;order-state-change-notification&gt;
 * request will be routed from the {@see CallBackServlet} to the 
 * {@see CallBackParser}. The {@see CallBackParser} will then call the 
 * <b>handleEvent</b> method of each class that implements the 
 * {@see OrderStateChangeNotificationListener}.
 * <br>
 * If you use this event handler in your Google Checkout implementation,
 * you will need to implement the <code>handleEvent</code> method to 
 * send the updated order status information to the appropriate internal 
 * systems so that those systems can update the order status and appropriately
 * respond to the order status change.
 *
 * @version 1.0 beta
 */
public class OrderStateChangeNotificationHandler extends CallBackHandler
    implements OrderStateChangeNotificationListener {

  public OrderStateChangeNotificationHandler() throws ProtocolException {
    super();
    // _parser instance comes from CallBackHandler.
    _parser.addCallBackListener(this);
  }
  
  public void handleEvent(CallBackEvent event) {
    /*
     * +++ CHANGE ME +++
     * Implement this method to handle order state change notifications.
     * To retrieve the JAXB objects that were unmarshalled from the
     * order state change notification, you can use the following code:
     * <code>OrderStateChangeNotification notification = 
     *     event.getOrderStateChangeNote();</code>
     * You can then access individual components of the order state change
     * notification by calling the methods in the generated JAXB classes.
     * For example:<br>
     * <code>FulfillmentOrderState newFulfillmentState = 
     *     notification.getNewFulfillmentOrderState();</code><br>
     * <code>String googleOrderNumber = notification.getGoogleOrderNumber();</code>
     */
  }
}
