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

import com.google.checkout.sample.crypto.CryptUtil;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The <b>OrderTransactionBuilderTest</b> class contains unit tests for the
 * methods in the <b>OrderTransactionBuilder</b> class.
 * @version 1.0 beta
 */
public class OrderTransactionBuilderTest extends TestCase {
  private String _shoppingCartClearTxt = "<?xml version=\"1.0\" " +
      "encoding=\"UTF-8\"?>\n<shopping-cart><items><item><item-name>" +
      "BlackBerry 8700c</item-name><item-description>personal " +
      "communications device</item-description><unit-price " +
      "currency=\"USD\">249.99</unit-price><quantity>1</quantity>" +
      "</item><item><item-name>Samsung MP3 Player</item-name>" +
      "<item-description>Mp3 player</item-description>" +
      "<unit-price currency=\"USD\">399.00</unit-price><quantity>1</quantity>" +
      "</item></items></shopping-cart>";

  private DocumentBuilder _docBuilder;
  
  protected void setUp() throws Exception {
    super.setUp();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    _docBuilder = factory.newDocumentBuilder();
  }
  
  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.getInstance()'
   */
  public void testgetInstance() throws Exception {
    OrderTransactionBuilder pBuilderOne = OrderTransactionBuilder.getInstance();
    OrderTransactionBuilder pBuilderTwo = OrderTransactionBuilder.getInstance();
    assertNotNull(pBuilderOne);
    assertNotNull(pBuilderTwo);
    assertSame(pBuilderOne, pBuilderTwo);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.encode64(Document)'
   */
  public void testEncode64() throws Exception {
    StringReader xmlString = new StringReader(_shoppingCartClearTxt);
    InputSource source = new InputSource(xmlString);
    Document doc = _docBuilder.parse(source);
    String base64TestString = OrderTransactionBuilder.encode64(doc);
    String clearTestString = CryptUtil.base64Decode(base64TestString);
    assertEquals(_shoppingCartClearTxt, clearTestString);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.unmarshal(Document)'
   */
  public void testUnmarshal() throws Exception {
    StringReader xmlString = new StringReader(_shoppingCartClearTxt);
    InputSource source = new InputSource(xmlString);
    Document doc = _docBuilder.parse(source);
    String clearTestString = OrderTransactionBuilder.unmarshal(doc);
    assertEquals(_shoppingCartClearTxt, clearTestString);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.chargeOrder(String, float)'
   */
  public void testChargeOrderStringFloat() throws Exception {
    String chargeOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<charge-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"345678\"><amount currency=\"USD\">555.00" +
        "</amount></charge-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.chargeOrder("345678", 555.00F);
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(chargeOrderXml, testXml);    
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.chargeOrder(String, float, String)'
   */
  public void testChargeOrderStringFloatString() throws Exception {
    String chargeOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<charge-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"98765\"><amount currency=\"JPY\">234.89" +
        "</amount></charge-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.chargeOrder("98765", 234.89F, "JPY");
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(chargeOrderXml, testXml);    
    
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.cancelOrder(String, String, String)'
   */
  public void testCancelOrder() throws Exception {
    String cancelOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<cancel-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"3422455\"><comment>None</comment>" +
        "<reason>duplicated order</reason></cancel-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.cancelOrder("3422455", "duplicated order", "None");
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    assertEquals(cancelOrderXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.processOrder(String)'
   */
  public void testProcessOrder() throws Exception {
    String processOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<process-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"2343\"/>";
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.processOrder("2343");
    String processTestXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(processOrderXml, processTestXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.refundOrder(String, String)'
   */
  public void testRefundOrderStringString() throws Exception {
    String fullRefundXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<refund-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"123456\"><reason>goods returned</reason>" +
        "</refund-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.refundOrder("123456", "goods returned");
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(fullRefundXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.refundOrder(String, String, float)'
   */
  public void testRefundOrderStringStringFloat() throws Exception {
    String fullRefundXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<refund-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"12345678\"><amount currency=\"USD\">43.00</amount>" +
        "<reason>not working</reason></refund-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.refundOrder("12345678", "not working", 43.0F, "USD");
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(fullRefundXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.refundOrder(String, String, float, String)'
   */
  public void testRefundOrderStringStringFloatString() throws Exception {
    String refundXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<refund-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"34567890\"><amount currency=\"GBP\">100.00" +
        "</amount><reason>overcharged</reason></refund-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.refundOrder("34567890", "overcharged", 100.0F, 
      "GBP");
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(refundXml, testXml);    
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.addTrackingData(String, String, String)'
   */
  public void testAddTrackingDataStringStringString() throws Exception {
    String addTrackingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<add-tracking-data " +
        "xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"ZA123456789\"><tracking-data>" +
        "<carrier>UPS</carrier><tracking-number>1Z33483487293" +
        "</tracking-number></tracking-data></add-tracking-data>";

    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.addTrackingData("ZA123456789", "1Z33483487293",
      "UPS");
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    assertEquals(addTrackingXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.trackOrder(String, String, String)'
   */
  public void testTrackOrderStringStringString() throws Exception {
    String addTrackingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<deliver-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"12345\"><send-email>false</send-email>" +
        "<tracking-data><carrier>USPS</carrier>" +
        "<tracking-number>Z3439Z3432</tracking-number>" +
        "</tracking-data></deliver-order>";
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.trackOrder("12345", "Z3439Z3432", "USPS", false);
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(addTrackingXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.trackOrder(String, String, String, boolean)'
   */
  public void testTrackOrderStringStringStringBoolean() throws Exception {
    String addTrackingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<deliver-order xmlns=\"http://checkout.google.com/schema/2\" " +
    "google-order-number=\"12345\"><send-email>true</send-email>" +
    "<tracking-data><carrier>DHL</carrier>" +
    "<tracking-number>Z3439Z3432</tracking-number>" +
    "</tracking-data></deliver-order>";    

      OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.trackOrder("12345", "Z3439Z3432", "DHL", true);
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(addTrackingXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.markOrderDelivered(String)'
   */
  public void testMarkOrderDeliveredString() throws Exception {
    String markDeliveredXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<deliver-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"93476640\"><send-email>false</send-email>" +
        "</deliver-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.markOrderDelivered("93476640", false);
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(markDeliveredXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.markOrderDelivered(String, boolean)'
   */
  public void testMarkOrderDeliveredStringBoolean() throws Exception {
    String markDeliveredXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<deliver-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"93476640\"><send-email>true</send-email>" +
        "</deliver-order>";
    
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.markOrderDelivered("93476640", true);
    String testXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(markDeliveredXml, testXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.archiveOrder(String)'
   */
  public void testArchiveOrder() throws Exception {
    String archiveOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<archive-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"2343\"/>";
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.archiveOrder("2343");
    String archiveTestXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(archiveOrderXml, archiveTestXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.unarchiveOrder(String)'
   */
  public void testUnarchiveOrder() throws Exception {
    String unArchiveOrderXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<unarchive-order xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"545443\"/>";
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.unarchiveOrder("545443");
    String unArchiveTestXml = OrderTransactionBuilder.unmarshal(doc);
    
    assertEquals(unArchiveOrderXml, unArchiveTestXml);
  }
  
  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.addMerchantOrderNumber(String, String)'
   */
  public void testAddMerchantOrderNumberStringString() throws Exception {
    String addMerchantOrderNumberXml = "<?xml version=\"1.0\" " +
        "encoding=\"UTF-8\"?>\n<add-merchant-order-number " +
        "xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"IE09847201\">" +
        "<merchant-order-number>09348-3493478</merchant-order-number>" +
        "</add-merchant-order-number>";
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.addMerchantOrderNumber("IE09847201", "09348-3493478");
    String addOrderNumTestXml = OrderTransactionBuilder.unmarshal(doc);
    assertEquals(addMerchantOrderNumberXml, addOrderNumTestXml);
  }

  /*
   * Test method for 'com.google.checkout.sample.protocol.OrderTransactionBuilder.sendBuyerMessage(String, String, boolean)'
   */
  public void testSendBuyerMessageStringStringBoolean() throws Exception {
    String sendBuyerMessageXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<send-buyer-message " +
        "xmlns=\"http://checkout.google.com/schema/2\" " +
        "google-order-number=\"SS123873438\">" +
        "<message>thank you for your order</message>" +
        "<send-email>true</send-email></send-buyer-message>";
    OrderTransactionBuilder pBuilder = OrderTransactionBuilder.getInstance();
    Document doc = pBuilder.sendBuyerMessage("SS123873438",
      "thank you for your order", true);
    String sendBuyMsgTestXml = OrderTransactionBuilder.unmarshal(doc);
    assertEquals(sendBuyerMessageXml, sendBuyMsgTestXml);

  }
}