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

import com.google.checkout.schema._2.ChargebackAmountNotification;

/**
 * The <b>ChargebackNotificationEvent</b> class returns a JAXB object
 * that maps to a &lt;chargeback-amount-notification&gt; event.
 * 
 * @version 1.0 beta
 */
public class ChargebackNotificationEvent extends CallBackEvent {
  public ChargebackNotificationEvent(ChargebackAmountNotification chargeBack) {
    super(chargeBack);
  }
  
  /**
   * Gets the notification source
   */  
  public ChargebackAmountNotification getChargeBackNote() {
    return (ChargebackAmountNotification) getSource();
  }
}
