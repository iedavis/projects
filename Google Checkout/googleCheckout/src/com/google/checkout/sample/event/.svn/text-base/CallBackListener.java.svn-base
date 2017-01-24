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

import java.util.EventListener;

/**
 * Base interface for call back listener for Google Checkout
 * 
 * @version 1.0 beta
 */
/**
 * The <b>CallBackListener</b> interface is the base interface for
 * handling Merchant Calculations API events and Notification API events.
 * Each of the various event listeners in this directory 
 * (NewOrderNotificationListener, ChargeNotificationListener, etc.) 
 * implements this interface.
 * <br>
 * Also see {@see CallBackEvent} and {@see CallBackHandler}
 * 
 * @version 1.0 beta
 */
public interface CallBackListener extends EventListener {
  /**
   * Handles charge back event from Google Checkout
   */
  public void handleEvent(CallBackEvent event);
}
