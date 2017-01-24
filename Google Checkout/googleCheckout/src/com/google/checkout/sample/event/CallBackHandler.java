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

import com.google.checkout.sample.protocol.CallBackParser;
import com.google.checkout.sample.protocol.ProtocolException;

/**
 * The <b>CallBackHandler</b> class is the base event handler
 * class for handling Merchant Calculations API events and Notification
 * API events. Each class that extends the <code>CallBackHandler</code> 
 * class also implements a listener interface that extends the 
 * {@see CallBackListener} interface.
 * @version 1.0 beta
 */
public abstract class CallBackHandler {
  
  protected CallBackParser _parser;

  protected CallBackHandler() throws ProtocolException {
    _parser = CallBackParser.getInstance();
  }
}
