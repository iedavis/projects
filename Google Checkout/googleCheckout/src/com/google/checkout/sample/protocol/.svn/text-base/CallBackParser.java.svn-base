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
package com.google.checkout.sample.protocol;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.CallBackListener;
import com.google.checkout.sample.event.ChargeNotificationEvent;
import com.google.checkout.sample.event.ChargeNotificationListener;
import com.google.checkout.sample.event.ChargebackNotificationEvent;
import com.google.checkout.sample.event.ChargebackNotificationListener;
import com.google.checkout.sample.event.MerchantCalculationCallbackEvent;
import com.google.checkout.sample.event.MerchantCalculationCallbackListener;
import com.google.checkout.sample.event.NewOrderNotificationEvent;
import com.google.checkout.sample.event.NewOrderNotificationListener;
import com.google.checkout.sample.event.OrderStateChangeNotificationEvent;
import com.google.checkout.sample.event.OrderStateChangeNotificationListener;
import com.google.checkout.sample.event.RefundNotificationEvent;
import com.google.checkout.sample.event.RefundNotificationListener;
import com.google.checkout.sample.event.RiskInformationNotificationEvent;
import com.google.checkout.sample.event.RiskInformationNotificationListener;
import com.google.checkout.schema._2.ChargeAmountNotification;
import com.google.checkout.schema._2.ChargebackAmountNotification;
import com.google.checkout.schema._2.MerchantCalculationCallback;
import com.google.checkout.schema._2.NewOrderNotification;
import com.google.checkout.schema._2.OrderStateChangeNotification;
import com.google.checkout.schema._2.RefundAmountNotification;
import com.google.checkout.schema._2.RiskInformationNotification;

import com.sun.xml.bind.JAXBObject;

import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * The CallBackParser class registers listeners that wait for certain
 * events, such as new order notifications or merchant calculations
 * callbacks, and then trigger the appropriate event handlers when
 * those events occur.
 * 
 * @version 1.0 beta
 */
public class CallBackParser extends AbstractProtocolBuilder {
  
  private static CallBackParser _handler;
  
  /**
   * Table that keeps track of event class and a set of listeners
   */
  private static Hashtable _eventTable;
  
  /**
   * Default construction
   */
  private CallBackParser() throws JAXBException, ProtocolException {
    _eventTable = new Hashtable();
  }
  
  /**
   * Gets default instance
   */
  public synchronized static CallBackParser getInstance() 
      throws ProtocolException {
    
    if (_handler == null) {
      try {
        _handler = new CallBackParser();
      } catch (JAXBException jaxbEx) {
        throw new ProtocolException(jaxbEx);
      }
    }
    
    return _handler;
  }
  
  /**
   * The <b>parseCallBack(Reader reader)</b>  method converts a Reader 
   * object associated with an XML request to an InputSource object and 
   * then calls the <code>parseCallBack(InputSource inputSource)</code>
   * method to trigger the appropriate event to handle the request.
   * @param reader A Reader object from which the XML notification is parsed
   * @throws ProtocolException if the XML does not comply with the
   * Google Checkout schema
   */
  public void parseCallBack(Reader reader) throws ProtocolException {
    parseCallBack(new InputSource(reader));
  }

  /**
   * The <b>parseCallBack(InputStream in)</b> method converts an InputStream
   * object associated with an XML request to an InputSource object and then 
   * calls the <code>parseCallBack(InputSource inputSource)</code> method
   * to trigger the appropriate event to handle the request.
   * @param in An InputStream object from which the XML notification is parsed
   * @throws ProtocolException if the XML does not comply with the
   * Google Checkout schema
   */
  public void parseCallBack(InputStream in) throws ProtocolException {
    parseCallBack(new InputSource(in));
  }

  /**
   * The <b>parseCallBack (InputSource inputSource)</b> method parses an 
   * asynchronous API request that you receive from Google Checkout -- such 
   * as a Notification API request -- into JAXB objects. It then triggers 
   * the appropriate event to handle that request. Events correspond to 
   * Merchant Calculations CallBack requests as well as any of the different 
   * types of Notification API requests. 
   * @param inputSource input source from which to parse the CallBack XML data
   * @throws ProtocolException if the XML does not comply with the
   * Google Checkout schema
   */
  public void parseCallBack(InputSource inputSource)
      throws ProtocolException {

    // Parse the incoming notification to JAXB objects
    Object mappedObject = (JAXBObject)parseToJAXB(inputSource);

    /*
     * Identify the type of request that has been received and trigger the 
     * appropriate CallBackEvent:
     *
     *    <merchant-calculation-callback> - MerchantCalculationCallback
     *    <new-order-notification> - NewOrderNotificationEvent
     *    <risk-information-notification> - RiskInformationNotificationEvent
     *    <order-state-change-notification> - OrderStateChangeNotificationEvent
     *    <charge-amount-notification> - ChargeAmountNotificationEvent
     *    <chargeback-amount-notification> - ChargebackAmountNotificationEvent
     *    <refund-amount-notification> - RefundAmountNotificationEvent
     */
    if (mappedObject instanceof ChargeAmountNotification) {
      fireEvent(new ChargeNotificationEvent((ChargeAmountNotification)mappedObject));
    } else if (mappedObject instanceof ChargebackAmountNotification) {
      fireEvent(new ChargebackNotificationEvent((ChargebackAmountNotification)mappedObject));
    } else if (mappedObject instanceof NewOrderNotification) {
      fireEvent(new NewOrderNotificationEvent((NewOrderNotification)mappedObject));
    } else if (mappedObject instanceof OrderStateChangeNotification) {
      fireEvent(new OrderStateChangeNotificationEvent((OrderStateChangeNotification)mappedObject));
    } else if (mappedObject instanceof RefundAmountNotification) {
      fireEvent(new RefundNotificationEvent((RefundAmountNotification)mappedObject));
    } else if (mappedObject instanceof RiskInformationNotification) {
      fireEvent(new RiskInformationNotificationEvent((RiskInformationNotification)mappedObject));
    } else if (mappedObject instanceof MerchantCalculationCallback) {
      fireEvent(new MerchantCalculationCallbackEvent((MerchantCalculationCallback)mappedObject));
    } else {
      throw new ProtocolException("Unknown callback notification: " + 
        mappedObject.toString());
    }
  }
  
  /**
   * The <b>fireEvent</b> method retrieves a list of listeners associated with 
   * an event -- for example, the ChargeNotificationEvent is associated with the
   * {@see ChargeNotificationListener}
   * -- and calls the handleEvent method for each associated listener.
   * @param event The JAXB object parsed from the input source.
   */
  private void fireEvent(CallBackEvent event) {
    if (!_eventTable.containsKey(event.getClass())) {
      return;
    }
    
    List listenerList = (List)_eventTable.get(event.getClass());
    if (listenerList != null) {
      for (int i = 0; i < listenerList.size(); i++) {
        CallBackListener listener = (CallBackListener) listenerList.get(i);
        listener.handleEvent(event);
      }
    }
  }
  
  /**
   * The <b>addCallBackListener</b> method associates a particular listener 
   * with a particular type of event; when that event occurs, the specified
   * listener will be invoked using the 
   * {@see fireEvent}
   * method.
   * @param event The type of event to be associated with the listener.
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  private void addCallBackListener(Class event, CallBackListener listener) {
    List list = null;
    if (!_eventTable.containsKey(event)) {
      list = new ArrayList();
      _eventTable.put(event, list);
    } else {
      list = (List)_eventTable.get(event);
    }
    
    list.add(listener);
  }
  
  /**
   * The <b>removeCallBackListener</b> method removes the association between 
   * an event and a particular type of listener.
   * @param event The type of event that will no longer be associated with the
   * specified listener.
   * @param listener The <code>CallBackListener</code> that should no longer 
   * listen for the specified event.
   */
  private void removeCallBackListener(Class event,
      CallBackListener listener) {
    List list = (List)_eventTable.get(event);
    if (list != null && list.contains(listener)) {
      list.remove(listener);
    }
  }
  
  /**
   * The <b>getListenerSize</b> method is used in unit tests that verify that 
   * the various addCallBackListener and removeCallBackListener methods work.
   * @param event Class object of a <code>CallBackEvent</code>
   * @return The number of registered listeners for an event
   */
  protected int getListenerSize(Class event) {
    if (_eventTable.containsKey(event)) {
      if (_eventTable.get(event) != null) {
        return ((List)_eventTable.get(event)).size();
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }

  /**
   * This method adds a ChargebackNotificationListener to a
   * {@see ChargebackNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void addCallBackListener(ChargebackNotificationListener listener) {
    addCallBackListener(ChargebackNotificationEvent.class, listener);
  }
  
  /**
   * This method stops the ChargebackNotificationListener from listening for the
   * {@see ChargebackNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(ChargebackNotificationListener listener) {
    removeCallBackListener(ChargebackNotificationEvent.class, listener);
  }
  
  /**
   * This method adds a ChargeNotificationListener to a
   * {@see ChargeNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */  
  public void addCallBackListener(ChargeNotificationListener listener) {
    addCallBackListener(ChargeNotificationEvent.class, listener);
  }

  /**
   * This method stops the ChargeNotificationListener from listening for the 
   * {@see ChargeNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(ChargeNotificationListener listener) {
    removeCallBackListener(ChargeNotificationEvent.class, listener);
  }

  /**
   * This method adds a NewOrderNotificationListener to a
   * {@see NewOrderNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */  
  public void addCallBackListener(NewOrderNotificationListener listener) {
    addCallBackListener(NewOrderNotificationEvent.class, listener);
  }

  /**
   * This method stops the NewOrderNotificationListener from listening for the 
   * {@see NewOrderNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(NewOrderNotificationListener listener) {
    removeCallBackListener(NewOrderNotificationEvent.class, listener);
  }
  
  /**
   * This method adds an OrderStateChangeNotificationListener to a
   * {@see OrderStateChangeNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */  
  public void addCallBackListener(OrderStateChangeNotificationListener listener) {
    addCallBackListener(OrderStateChangeNotificationEvent.class, listener);
  }
  
  /**
   * This method stops the OrderStateChangeNotificationListener from listening for the 
   * {@see OrderStateChangeNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(OrderStateChangeNotificationListener listener) {
    removeCallBackListener(OrderStateChangeNotificationEvent.class, listener);
  }
  
  /**
   * This method adds a RefundNotificationListener to a
   * {@see RefundNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */  
  public void addCallBackListener(RefundNotificationListener listener) {
    addCallBackListener(RefundNotificationEvent.class, listener);
  }
  
  /**
   * This method stops the RefundNotificationListener from listening for the 
   * {@see RefundNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(RefundNotificationListener listener) {
    removeCallBackListener(RefundNotificationEvent.class, listener);
  }
  
  /**
   * This methods adds a RiskInformationNotificationListener for a 
   * {@see RiskInformationNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */  
  public void addCallBackListener(RiskInformationNotificationListener listener) {
    addCallBackListener(RiskInformationNotificationEvent.class, listener);
  }
  
  /**
   * This method stops the RiskInformationNotificationListener from
   * listening for the {@see RiskInformationNotificationEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(RiskInformationNotificationListener listener) {
    removeCallBackListener(RiskInformationNotificationEvent.class, listener);
  }
  
  /**
   * This methods adds a MerchantCalculationCallbackListener for a 
   * {@see MerchantCalculationCallbackEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */  
  public void addCallBackListener(MerchantCalculationCallbackListener listener) {
    addCallBackListener(MerchantCalculationCallbackEvent.class, listener);
  }
  
  /**
   * This method stops the MerchantCalculationCallbackListener from listening for the 
   * {@see MerchantCalculationCallbackEvent}
   * @param listener The <code>CallBackListener</code> associated with the event
   */
  public void removeCallBackListener(MerchantCalculationCallbackListener listener) {
    removeCallBackListener(MerchantCalculationCallbackEvent.class, listener);
  }
}