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
import com.google.checkout.sample.event.ChargeNotificationEvent;
import com.google.checkout.sample.event.ChargeNotificationListener;
import com.google.checkout.sample.event.ChargebackNotificationEvent;
import com.google.checkout.sample.event.ChargebackNotificationListener;
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
import com.google.checkout.schema._2.NewOrderNotification;
import com.google.checkout.schema._2.OrderStateChangeNotification;
import com.google.checkout.schema._2.RefundAmountNotification;
import com.google.checkout.schema._2.RiskInformationNotification;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

/**
 * @version 1.0 beta
 */
public class CallBackParserTest extends TestCase {

  /**
   * The following variables each contain valid XML requests in the
   * Google Checkout schema; these sample requests are used to test
   * that the methods in the {@see CallBackHandler} class execute properly.
   */
  private String newOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
      "<new-order-notification serial-number=\"ERAF9348SDF-DFA3982990K-" +
      "34J8349\" xmlns=\"http://checkout.google.com/schema/2\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
      "  <shopping-cart>\n" + 
      "    <items>\n" + 
      "      <item>\n" + 
      "        <item-name>Motorola Q</item-name>\n" + 
      "        <item-description>Motorola smart phone</item-description>\n" + 
      "        <unit-price currency=\"token\">599.0</unit-price>\n" + 
      "        <quantity>1</quantity>\n" + 
      "      </item>\n" + 
      "    </items>\n" + 
      "  </shopping-cart>\n" + 
      "  <google-order-number>AH12345678</google-order-number>\n" + 
      "  <buyer-shipping-address id=\"primary\">\n" + 
      "    <address1>1600 Amphitheatre Parkway</address1>\n" + 
      "    <city>Mountain View</city>\n" + 
      "    <region>Northern Cal</region>\n" + 
      "    <postal-code>94043</postal-code>\n" + 
      "    <country-code>1</country-code>\n" + 
      "  </buyer-shipping-address>\n" + 
      "  <buyer-billing-address id=\"primary\">\n" + 
      "    <address1>1600 Amphitheatre Parkway</address1>\n" + 
      "    <city>Mountain View</city>\n" + 
      "    <region>Northern Cal</region>\n" + 
      "    <postal-code>94043</postal-code>\n" + 
      "    <country-code>1</country-code>\n" + 
      "  </buyer-billing-address>\n" + 
      "  <buyer-marketing-preferences>\n" + 
      "    <email-allowed>true</email-allowed>\n" + 
      "  </buyer-marketing-preferences>\n" + 
      "  <order-adjustment>\n" + 
      "    <shipping>\n" + 
      "      <merchant-calculated-shipping-adjustment>\n" + 
      "        <shipping-name>UPS Ground</shipping-name>\n" + 
      "        <shipping-cost currency=\"USD\">24.45</shipping-cost>\n" + 
      "      </merchant-calculated-shipping-adjustment>\n" + 
      "    </shipping>\n" + 
      "  </order-adjustment>\n" + 
      "  <order-total currency=\"USD\">623.45</order-total>\n" + 
      "  <fulfillment-order-state>NEW</fulfillment-order-state>\n" + 
      "  <financial-order-state>REVIEWING</financial-order-state>\n" + 
      "  <timestamp>2001-12-31T12:00:00</timestamp>\n" + 
      "</new-order-notification>\n"; 
  
  private String chargeOrderXml = "<?xml version=\"1.0\" " +
      "encoding=\"UTF-8\"?>\n<charge-amount-notification " +
      "serial-number=\"ER83429-32SER321139874-3486GG937\" " +
      "xmlns=\"http://checkout.google.com/schema/2\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
      "  <google-order-number>AH12345678</google-order-number>\n" + 
      "  <latest-charge-amount currency=\"USD\">55.0</latest-charge-amount>\n" + 
      "  <total-charge-amount currency=\"USD\">55.0</total-charge-amount>\n" + 
      "  <timestamp>2001-12-31T12:00:00</timestamp>\n" + 
      "</charge-amount-notification>\n";
  
  private String chargeBackXml = "<?xml version=\"1.0\" " +
      "encoding=\"UTF-8\"?>\n<chargeback-amount-notification " +
      "serial-number=\"IUE947280-309DK836PKJ-3CHNVC348\" " +
      "xmlns=\"http://checkout.google.com/schema/2\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
      "  <google-order-number>AH12345678</google-order-number>\n" + 
      "  <latest-chargeback-amount currency=\"USD\">10.0" +
      "</latest-chargeback-amount>\n" + 
      "  <total-chargeback-amount currency=\"GBP\">20.0" +
      "</total-chargeback-amount>\n" + 
      "  <timestamp>2001-12-31T12:00:00</timestamp>\n" + 
      "</chargeback-amount-notification>\n";
  
  private String orderStateXml = "<?xml version=\"1.0\" " +
      "encoding=\"UTF-8\"?>\n<order-state-change-notification " +
      "serial-number=\"PER3830JJ-K34092389-347812KHHG30\" " +
      "xmlns=\"http://checkout.google.com/schema/2\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
      "  <google-order-number>AH12345678</google-order-number>\n" + 
      "  <new-fulfillment-order-state>PROCESSING" +
      "</new-fulfillment-order-state>\n" + 
      "  <new-financial-order-state>CHARGEABLE</new-financial-order-state>\n" + 
      "  <previous-fulfillment-order-state>NEW" +
      "</previous-fulfillment-order-state>\n" + 
      "  <previous-financial-order-state>REVIEWING" +
      "</previous-financial-order-state>\n" + 
      "  <timestamp>2001-12-31T12:00:00</timestamp>\n" + 
      "</order-state-change-notification>\n";
  
  private String refundXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
      "<refund-amount-notification " +
      "serial-number=\"DFK34097-34IF893478-3426734DKF\" " +
      "xmlns=\"http://checkout.google.com/schema/2\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + 
      "  <google-order-number>AH12345678</google-order-number>\n" + 
      "  <latest-refund-amount currency=\"token\">344.34" +
      "</latest-refund-amount>\n" + 
      "  <total-refund-amount currency=\"token\">918.33" +
      "</total-refund-amount>\n" + 
      "  <timestamp>2001-12-31T12:00:00</timestamp>\n" + 
      "</refund-amount-notification>\n";
  
  private String riskAlertXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
      "<risk-information-notification " +
      "serial-number=\"34IERJIU0384-3KJ8987DFQ23-23192\"" +
      " xmlns=\"http://checkout.google.com/schema/2\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
      "  <google-order-number>AH12345678</google-order-number>\n" + 
      "  <risk-information>\n" + 
      "    <eligible-for-protection>true</eligible-for-protection>\n" + 
      "    <billing-address id=\"primary\">\n" + 
      "      <address1>1600 Amphitheatre Parkway</address1>\n" + 
      "      <city>Mountain View</city>\n" + 
      "      <region>Northern Cal</region>\n" + 
      "      <postal-code>94043</postal-code>\n" + 
      "      <country-code>1</country-code>\n" + 
      "    </billing-address>\n" + 
      "    <avs-response>Y</avs-response>\n" + 
      "    <cvn-response>M</cvn-response>\n" + 
      "    <partial-cc-number>4338</partial-cc-number>\n" + 
      "    <buyer-account-age>1</buyer-account-age>\n" + 
      "    <ip-address>123.45.678.90</ip-address>\n" + 
      "  </risk-information>\n" + 
      "  <timestamp>2001-12-31T12:00:00</timestamp>\n" + 
      "</risk-information-notification>\n";
  
  // Define objects that implement the various listeners in the /event directory
  private CallBackParser _handler;
  private ChargebackEventHandler _chargeBackHandler;
  private ChargeEventHandler _chargeHandler;
  private NewOrderEventHandler _newOrderHandler;
  private OrderStatusEventHandler _orderStatusHandler; 
  private RefundEventHandler _refundHandler;
  private RiskAlertEventHandler _riskHandler;
  
  /**
   * The <b>setUp</b> method creates event handlers and associates those
   * event handlers with their corresponding event listeners by
   * calling the relevant {@see addCallBackListener} method.
   */
  protected void setUp() throws Exception {
    super.setUp();
    _chargeBackHandler = new ChargebackEventHandler();
    _chargeHandler = new ChargeEventHandler();
    _newOrderHandler = new NewOrderEventHandler();
    _orderStatusHandler = new OrderStatusEventHandler();
    _refundHandler = new RefundEventHandler();
    _riskHandler = new RiskAlertEventHandler();
    _handler = CallBackParser.getInstance();
    _handler.addCallBackListener(_chargeBackHandler);
    _handler.addCallBackListener(_chargeHandler);
    _handler.addCallBackListener(_newOrderHandler);
    _handler.addCallBackListener(_orderStatusHandler);
    _handler.addCallBackListener(_refundHandler);
    _handler.addCallBackListener(_riskHandler);
  }
  
  /**
   * The <b>testGetInstance</b> method tests the
   * 'com.google.checkout.sample.protocol.CallBackHandler.getInstance() method.
   */
  public void testGetInstance() throws Exception {
    CallBackParser handler1 = CallBackParser.getInstance();
    CallBackParser handler2 = CallBackParser.getInstance();
    assertNotNull(handler1);
    assertNotNull(handler2);
    assertSame(handler1, handler2);
  }

  /**
   * The <b>testParseCallBack</b> method tests the
   * 'com.google.checkout.sample.protocol.CallBackHandler.parseCallBack (InputSource)' method
   */
  private void testParseCallBack(String xml) throws Exception {
    StringReader reader = new StringReader(xml);
    InputSource source = new InputSource(reader);
    _handler.parseCallBack(source);
  }
  
  /**
   * The <b>testParseCallBackReader</b> method tests the
   * 'com.google.checkout.sample.protocol.CallBackHandler.parseCallBack (Reader)' method
   */
  public void testParseCallBackReader() throws Exception {
    StringReader reader = new StringReader(newOrderXml);
    CallBackParser handler = CallBackParser.getInstance();
    handler.parseCallBack(reader);
  }

  /**
   * The <b>testParseCallBackInputStream</b> method tests the
   * 'com.google.checkout.sample.protocol.CallBackHandler.parseCallBack (InputStream)'
   * method
   */
  public void testParseCallBackInputStream() throws Exception {
    ByteArrayInputStream stream 
      = new ByteArrayInputStream(newOrderXml.getBytes());
    CallBackParser handler = CallBackParser.getInstance();
    handler.parseCallBack(stream);
  }

  /**
   * The <b>testParseCallBackInputSource</b> method verifies that the application
   * recognizes each of the six different types of Notification API
   * requests as valid XML.
   */
  public void testParseCallBackInputSource() throws Exception {
    testParseCallBack(newOrderXml);
    testParseCallBack(chargeOrderXml);
    testParseCallBack(chargeBackXml);
    testParseCallBack(orderStateXml);
    testParseCallBack(refundXml);
    testParseCallBack(riskAlertXml);
  }

  /**
   * The <b>testAddCallBackListenerChargebackListener</b> method adds a
   * ChargebackNotificationListener to a ChargebackNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.addCallBackListener(ChargebackNotificationListener).
   * This method then verifies that the listener was properly added.
   */
  public void testAddCallBackListenerChargebackListener() throws Exception {
    int beforeAdd = _handler.getListenerSize(ChargebackNotificationEvent.class);
    ChargebackEventHandler chargeBack = new ChargebackEventHandler();
    _handler.addCallBackListener(chargeBack);
    int afterAdd = _handler.getListenerSize(ChargebackNotificationEvent.class);
    assertEquals(beforeAdd + 1, afterAdd);
  }

  /**
   * The <b>testRemoveCallBackListenerChargebackListener</b> method removes a
   * ChargebackNotificationListener from a ChargebackNotificationEvent
   * by calling 'com.google.checkout.sample.protocol.CallBackHandler.removeCallBackListener(ChargebackNotificationListener).
   * This method then verifies that the listener was properly removed.
   */
  public void testRemoveCallBackListenerChargebackListener() throws Exception {
    int beforeRemove = _handler.getListenerSize(ChargebackNotificationEvent.class);
    _handler.removeCallBackListener(_chargeBackHandler);
    int afterRemove = _handler.getListenerSize(ChargebackNotificationEvent.class);
    assertEquals(beforeRemove - 1, afterRemove);
  }

  /**
   * The <b>testAddCallBackListenerChargeListener</b> method adds a
   * ChargeNotificationListener to a ChargeNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.addCallBackListener(ChargeNotificationListener);
   * this method then verifies that the listener was properly added.
   */
  public void testAddCallBackListenerChargeListener() throws Exception {
    int beforeAdd = _handler.getListenerSize(ChargeNotificationEvent.class);
    ChargeEventHandler chargeHandler = new ChargeEventHandler();
    _handler.addCallBackListener(chargeHandler);
    int afterAdd = _handler.getListenerSize(ChargeNotificationEvent.class);
    assertEquals(beforeAdd + 1, afterAdd);

  }

  /**
   * The <b>testRemoveCallBackListenerChargeListener</b> method removes a
   * ChargeNotificationListener from a ChargeNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.removeCallBackListener(ChargeNotificationListener).
   * This method then verifies that the listener was properly removed.
   */
  public void testRemoveCallBackListenerChargeListener() throws Exception {
    int beforeRemove = _handler.getListenerSize(ChargeNotificationEvent.class);
    _handler.removeCallBackListener(_chargeHandler);
    int afterRemove = _handler.getListenerSize(ChargeNotificationEvent.class);
    assertEquals(beforeRemove - 1, afterRemove);
  }

  /**
   * The <b>testAddCallBackListenerNewOrderListener</b> method adds a
   * NewOrderNotificationListener to a NewOrderNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.addCallBackListener(NewOrderNotificationListener).
   * This method then verifies that the listener was properly added.
   */
  public void testAddCallBackListenerNewOrderListener() throws Exception {
    int beforeAdd = _handler.getListenerSize(NewOrderNotificationEvent.class);
    NewOrderEventHandler newOrderHandler = new NewOrderEventHandler();
    _handler.addCallBackListener(newOrderHandler);
    int afterAdd = _handler.getListenerSize(NewOrderNotificationEvent.class);
    assertEquals(beforeAdd + 1, afterAdd);

  }

  /**
   * The <b>testRemoveCallBackListenerNewOrderListener</b> method removes a
   * NewOrderNotificationListener from a NewOrderNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.removeCallBackListener(NewOrderNotificationListener).
   * This method then verifies that the listener was properly removed.
   */
  public void testRemoveCallBackListenerNewOrderListener() throws Exception {
    int beforeRemove = _handler.getListenerSize(NewOrderNotificationEvent.class);
    _handler.removeCallBackListener(_newOrderHandler);
    int afterRemove = _handler.getListenerSize(NewOrderNotificationEvent.class);
    assertEquals(beforeRemove - 1, afterRemove);
  }

  /**
   * The <b>testAddCallBackListenerOrderStatusChangedListener</b> method adds an
   * OrderStatusChangedNotificationListener to an OrderStatusChangedNotificationEvent
   * by calling 'com.google.checkout.sample.protocol.CallBackHandler.addCallBackListener(OrderStatusChangedNotificationListener).
   * This method then verifies that the listener was properly added.
   */
  public void testAddCallBackListenerOrderStatusChangedListener() throws Exception {
    int beforeAdd = _handler.getListenerSize(OrderStateChangeNotificationEvent.class);
    OrderStatusEventHandler statusHandler = new OrderStatusEventHandler();
    _handler.addCallBackListener(statusHandler);
    int afterAdd = _handler.getListenerSize(OrderStateChangeNotificationEvent.class);
    assertEquals(beforeAdd + 1, afterAdd);

  }

  /**
   * The <b>testRemoveCallBackListenerOrderStatusChangedListener</b> method 
   * removes a OrderStatusChangedNotificationListener from a 
   * OrderStatusChangedNotificationEvent by calling 
   * 'com.google.checkout.sample.protocol.CallBackHandler.removeCallBackListener(OrderStatusChangedNotificationListener).
   * This method then verifies that the listener was properly removed.
   */
  public void testRemoveCallBackListenerOrderStatusChangedListener() throws Exception {
    int beforeRemove = _handler.getListenerSize(OrderStateChangeNotificationEvent.class);
    _handler.removeCallBackListener(_orderStatusHandler);
    int afterRemove = _handler.getListenerSize(OrderStateChangeNotificationEvent.class);
    assertEquals(beforeRemove - 1, afterRemove);
  }

  /**
   * The <b>testAddCallBackListenerRefundListener</b> method adds a
   * RefundNotificationListener to a RefundNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.addCallBackListener(RefundNotificationListener).
   * This method then verifies that the listener was properly added.
   */
  public void testAddCallBackListenerRefundListener() throws Exception {
    int beforeAdd = _handler.getListenerSize(RefundNotificationEvent.class);
    RefundEventHandler refundHandler = new RefundEventHandler();
    _handler.addCallBackListener(refundHandler);
    int afterAdd = _handler.getListenerSize(RefundNotificationEvent.class);
    assertEquals(beforeAdd + 1, afterAdd);

  }

  /**
   * The <b>testRemoveCallBackListenerRefundListener</b> method removes a
   * RefundNotificationListener from a RefundNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.removeCallBackListener(RefundNotificationListener).
   * This method then verifies that the listener was properly removed.
   */
  public void testRemoveCallBackListenerRefundListener() throws Exception {
    int beforeRemove = _handler.getListenerSize(RefundNotificationEvent.class);
    _handler.removeCallBackListener(_refundHandler);
    int afterRemove = _handler.getListenerSize(RefundNotificationEvent.class);
    assertEquals(beforeRemove - 1, afterRemove);
  }

  /**
   * The <b>testAddCallBackListenerRiskAlertListener</b> method adds a
   * RiskInformationNotificationListener to a RiskInformationNotificationEvent
   * by calling 'com.google.checkout.sample.protocol.CallBackHandler.addCallBackListener(RiskInformationNotificationListener).
   * This method then verifies that the listener was properly added.
   */
  public void testAddCallBackListenerRiskAlertListener() throws Exception {
    int beforeAdd = _handler.getListenerSize(RiskInformationNotificationEvent.class);
    RiskAlertEventHandler riskHandler = new RiskAlertEventHandler();
    _handler.addCallBackListener(riskHandler);
    int afterAdd = _handler.getListenerSize(RiskInformationNotificationEvent.class);
    assertEquals(beforeAdd + 1, afterAdd);
  }

  /**
   * The <b>testRemoveCallBackListenerRiskAlertListener</b> method removes a
   * RiskAlertNotificationListener from a RiskAlertNotificationEvent by calling
   * 'com.google.checkout.sample.protocol.CallBackHandler.removeCallBackListener(RiskAlertNotificationListener).
   * This method then verifies that the listener was properly removed.
   */
  public void testRemoveCallBackListenerRiskAlertListener() throws Exception {
    int beforeRemove = _handler.getListenerSize(RiskInformationNotificationEvent.class);
    _handler.removeCallBackListener(_riskHandler);
    int afterRemove = _handler.getListenerSize(RiskInformationNotificationEvent.class);
    assertEquals(beforeRemove - 1, afterRemove);
  }
  
  private class CallBackEventHandler {
    protected String _event = "None";
    
    public String getStatus() {
      return _event;
    }
  }
  
  private class ChargebackEventHandler extends CallBackEventHandler 
      implements ChargebackNotificationListener {
    public void handleEvent(CallBackEvent event) {
      ChargebackNotificationEvent chargeBackEvent = (ChargebackNotificationEvent) event;
      ChargebackAmountNotification chargeBack 
        = chargeBackEvent.getChargeBackNote();
      assertNotNull(chargeBack.getClass());
    }
  }
  
  private class ChargeEventHandler extends CallBackEventHandler
      implements ChargeNotificationListener {
    public void handleEvent(CallBackEvent event) {
      ChargeNotificationEvent chargeEvent = (ChargeNotificationEvent) event;
      ChargeAmountNotification chargeNote = chargeEvent.getChargeAmountNote();
      assertNotNull(chargeNote.getClass());
    }
  }

  private class NewOrderEventHandler extends CallBackEventHandler
      implements NewOrderNotificationListener {
    public void handleEvent(CallBackEvent event) {
      NewOrderNotificationEvent newOrder = (NewOrderNotificationEvent) event;
      NewOrderNotification newOrderNote = newOrder.getNewOrderNote();
      assertNotNull(newOrderNote.getClass());
    }
  }

  private class OrderStatusEventHandler extends CallBackEventHandler
      implements OrderStateChangeNotificationListener {
    public void handleEvent(CallBackEvent event) {
      OrderStateChangeNotificationEvent statusEvent = (OrderStateChangeNotificationEvent) event;
      OrderStateChangeNotification stateChange 
        = statusEvent.getOrderStateChangeNote();
      assertNotNull(stateChange.getClass());
    }
  }

  private class RefundEventHandler extends CallBackEventHandler
      implements RefundNotificationListener {
    public void handleEvent(CallBackEvent event) {
      RefundNotificationEvent refundEvent = (RefundNotificationEvent) event;
      RefundAmountNotification refundNote = refundEvent.getRefundAmountNote();
      assertNotNull(refundNote.getClass());
    }
  }

  private class RiskAlertEventHandler extends CallBackEventHandler
      implements RiskInformationNotificationListener {
    public void handleEvent(CallBackEvent event) {
      RiskInformationNotificationEvent riskEvent = (RiskInformationNotificationEvent) event;
      RiskInformationNotification riskNote = riskEvent.getRiskNote();
      assertNotNull(riskNote.getClass());
    }
  }
}