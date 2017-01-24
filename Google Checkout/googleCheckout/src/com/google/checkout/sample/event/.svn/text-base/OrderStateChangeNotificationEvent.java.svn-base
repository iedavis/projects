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

import com.google.checkout.schema._2.OrderStateChangeNotification;


/**
 * The <b>OrderStateChangeNotificationEvent</b> class returns a JAXB object
 * that maps to an &lt;order-state-change-notification&gt; event.
 * 
 * @version 1.0 beta
 */
public class OrderStateChangeNotificationEvent extends CallBackEvent {
  public OrderStateChangeNotificationEvent(OrderStateChangeNotification stateChange) {
    super(stateChange);
  }

  /**
   * Gets the notification source
   */  
  public OrderStateChangeNotification getOrderStateChangeNote() {
    return (OrderStateChangeNotification) getSource();
  }
  
}
