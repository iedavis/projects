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

import com.google.checkout.sample.crypto.Base64Coder;
import com.google.checkout.sample.util.StringUtil;
import com.google.checkout.schema._2.Money;
import com.google.checkout.schema._2.ObjectFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * The <b>AbstractProtocolBuilder</b> class defines standard methods for 
 * marshalling Java objects (by converting them to XML documents) and 
 * unmarshalling XML documents (by converting them to JAXB objects). 
 * It also provides a standard method for creating 
 * {@see Money}
 * objects, which are used to build several different types of API requests.
 * @version 1.0 beta
 */
public abstract class AbstractProtocolBuilder {

  private static Unmarshaller _xmlUnMarshaller;
  private static Marshaller _xmlMarshaller;
  private static DocumentBuilder _domBuilder;

  protected ObjectFactory _objectFact;
  
  /**
   * Logging
   */
  private static Logger _logger 
    = Logger.getLogger(AbstractProtocolBuilder.class.getName());
  
  protected AbstractProtocolBuilder() throws JAXBException, ProtocolException {
    _objectFact = new ObjectFactory();
    _xmlMarshaller = createXmlMarshaller();
    _xmlUnMarshaller = createXmlUnMashaller();
    _domBuilder = createDomBuilder();
  }
  
  /**
   * The Unmarshaller converts XML documents to JAXB objects. The 
   * Unmarshaller is used to process incoming Merchant Calculations Callback
   * requests and Notification API requests.
   */
  private static Unmarshaller createXmlUnMashaller() throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("com.google.checkout.schema._2");
    return jc.createUnmarshaller();
  }
  
  /**
   * The Marshaller converts JAXB objects to XML documents for Checkout
   * API requests, Order Processing API requests and Merchant Calculations API
   * responses.
   */
  private static Marshaller createXmlMarshaller() throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance("com.google.checkout.schema._2");
    Marshaller xmlMarshaller = jc.createMarshaller();
    xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return xmlMarshaller;
  }
  
  /**
   * The DocumentBuilder builds Document objects so that JAXB objects can
   * be converted to XML documents.
   */
  private static DocumentBuilder createDomBuilder() throws ProtocolException {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      return dbf.newDocumentBuilder();
    } catch (ParserConfigurationException parserExcept) {
      _logger.severe("not able to create DOM builder: " + 
        parserExcept.getMessage());
      throw new ProtocolException(parserExcept);
    }
  }

  /**
   * The <b>encode64</b> method calls the domToString method to convert a
   * DOM object to a base64-encoded string.
   * @param doc Document object to be converted to string
   * @return A base64-encoded string format of the DOM object
   */
  public static String encode64(Document doc) throws ProtocolException {
    return domToString(doc, true);
  }
  
  /**
   * The <b>unmarshal</b> method calls the <code>domToString</code> method 
   * to convert a DOM object to a plain-text string.
   * @param doc Document object to be converted to string
   * @return A plain-text string format of the DOM object
   */
  public static String unmarshal(Document doc) throws ProtocolException {
    return domToString(doc, false);
  }
  
  /**
   * The <b>domToString</b> method converts a DOM object to either a plain-text
   * or a base64-encoded string.
   * @param doc Document object to be converted to string
   * @param toBase64 Boolean flag that indicates whether the return value
   * should be base64 encoded.
   * @return A string format of the DOM object
   */
  private static String domToString(Document doc, boolean toBase64)
      throws ProtocolException {
    if (doc == null) {
      throw new ProtocolException("document cannot be null");
    }
    
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer trans = tf.newTransformer();
      StringWriter sw = new StringWriter();
      trans.transform(new DOMSource(doc), new StreamResult(sw));
      if (toBase64) {
        return Base64Coder.encode(sw.toString());
      } else {
        return sw.toString();
      }
    } catch (TransformerException tEx) {
      throw new ProtocolException(tEx.getMessage());
    }
  }
  
  /**
   * The <b>convertToDOM</b> method converts a JAXBElement to a DOM Document.
   * @param element The JAXB element to be converted to a DOM Document.
   * @return Document object
   */
  public final Document convertToDOM(Object element) 
      throws ProtocolException {
    Document doc = null;
    try {
      doc = _domBuilder.newDocument();
      _xmlMarshaller.marshal(element, doc);
    } catch (JAXBException jaxb) {
      throw new ProtocolException(jaxb.getMessage());
    }
    return doc;
  }
  
  /**
   * The <b>parseToJAXB</b> method receives a Merchant Calculations API or
   * Notification API request (in XML format) from Google Checkout
   * and converts that request to JAXB objects.
   * @param inputSource The XML source data to be converted to JAXB objects
   * @throws A ProtocolException if the input source does not comply with 
   * the Google Checkout schema
   */
  public final Object parseToJAXB(InputSource inputSource) 
      throws ProtocolException {
    UnmarshallerHandler unmarshallerHandler
      = _xmlUnMarshaller.getUnmarshallerHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    try {
      XMLReader xmlReader = factory.newSAXParser().getXMLReader();
      xmlReader.setContentHandler(unmarshallerHandler);
      xmlReader.parse(inputSource);
      return unmarshallerHandler.getResult();
    } catch (SAXException saxEx) {
      throw new ProtocolException(saxEx);
    } catch (ParserConfigurationException configEx) {
      throw new ProtocolException(configEx);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx);
    } catch (IOException jaxbEx) {
      throw new ProtocolException(jaxbEx);
    }
  }
  
  /**
   * The <b>createMoney</b> method returns a Money object with the specified 
   * price. Thhis method calls the createMoney method with a default currency 
   * of "USD".
   * @param price The numeric component of a monetary value.
   */
  protected Money createMoney(double price) throws ProtocolException {
    return createMoney(price, "USD");
  }

  /**
   * The <b>createMoney</b> method returns a Money object with the specified 
   * price and currency. At this time, the only currency that Google Checkout 
   * supports is USD.
   * @param price The numeric component of a monetary value.
   * @param currency An ISO-4217 3-letter currency code (USD, JPY, EUR)
   * @return A Money object with the specified price and currency
   */
  protected Money createMoney(double price, String currency)
      throws ProtocolException {
    if (price < 0.0d) {
      throw new ProtocolException("price cannot be negative");
    }
    
    try {
      Money money = _objectFact.createMoney();
      BigDecimal bigNum = new BigDecimal(price);
      bigNum = bigNum.setScale(2, BigDecimal.ROUND_HALF_EVEN);
      money.setValue(bigNum);
      if (!StringUtil.isEmpty(currency)) {
        money.setCurrency(currency);
      } else {
        money.setCurrency("USD");
      }
      return money;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
}