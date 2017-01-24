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

import com.google.checkout.sample.util.StringUtil;
import com.google.checkout.schema._2.AddMerchantOrderNumberElement;
import com.google.checkout.schema._2.AddTrackingDataElement;
import com.google.checkout.schema._2.ArchiveOrderElement;
import com.google.checkout.schema._2.CancelOrderElement;
import com.google.checkout.schema._2.ChargeOrderElement;
import com.google.checkout.schema._2.DeliverOrderElement;
import com.google.checkout.schema._2.Money;
import com.google.checkout.schema._2.ProcessOrderElement;
import com.google.checkout.schema._2.RefundOrderElement;
import com.google.checkout.schema._2.SendBuyerMessageElement;
import com.google.checkout.schema._2.TrackingData;
import com.google.checkout.schema._2.UnarchiveOrderElement;

import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;

/**
 * The <b>OrderTransactionBuilder</b> class contains methods that create the
 * JAXB objects needed to construct Order Processing API requests.
 * @version 1.0 beta
 */
public class OrderTransactionBuilder extends AbstractProtocolBuilder {
  private static OrderTransactionBuilder _builder;
  
  private OrderTransactionBuilder() throws JAXBException, ProtocolException {
    super();
  }
  
  public synchronized static OrderTransactionBuilder getInstance() 
      throws ProtocolException {
    try {
      if (_builder == null) {
        _builder = new OrderTransactionBuilder();
      }
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx);
    }
    
    return _builder;
  }
  
  /**
   * This <b>chargeOrder</b> method calls the 
   * <code>chargeOrder (String, float, String)</code> method to create a
   * DOM object for charging an order. This method uses "USD" as the
   * default currency.
   * @param orderNumber Google Checkout-assigned order number
   * @param amount Numeric amount to charge the customer. 
   */
  public Document chargeOrder(String orderNumber, float amount) 
      throws ProtocolException {
    return chargeOrder(orderNumber, amount, "USD");
  }
  
  /**
   * This <b>chargeOrder</b> method creates the XML request for a
   * &lt;charge-order&gt; Order Processing API request.
   * @param orderNumber Google Checkout-assigned order number
   * @param amount Numeric amount to charge the customer.
   * @param currency 3-letter ISO-4217 currency code: USD, JPY, GBP
   */
  public Document chargeOrder(String orderNumber, float amount, String currency) 
      throws ProtocolException {
    try {
      Money money = createMoney(amount, currency);
      ChargeOrderElement chargeReq = _objectFact.createChargeOrderElement();
      chargeReq.setGoogleOrderNumber(orderNumber);
      chargeReq.setAmount(money);
      return convertToDOM(chargeReq);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>cancelOrder</b> method creates the XML request for a
   * &lt;cancel-order&gt; Order Processing API request.
   * @param orderNumber Google Checkout-assigned order number
   * @param reason to cancel an order
   * @param comment Additional comments from the merchant regarding the
   * order cancellation
   */
  public Document cancelOrder(String orderNumber, String reason, String comment)
      throws ProtocolException {
    try {
      CancelOrderElement cancelRequest = _objectFact.createCancelOrderElement();
      cancelRequest.setGoogleOrderNumber(orderNumber);
      cancelRequest.setReason(reason);
      cancelRequest.setComment(comment);
      return convertToDOM(cancelRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
    
  
  /**
   * The <b>processOrder</b> method creates the XML request for a
   * &lt;process-order&gt; Order Processing API request, which adds
   * a note the customer's inbox indicating that you are processing the 
   * order. This command also updates the order's fulfillment-order-state
   * to "PROCESSING".
   * @param orderNumber Google Checkout-assigned order number
   */
  public Document processOrder(String orderNumber)
      throws ProtocolException {
    try {
      ProcessOrderElement processRequest 
          = _objectFact.createProcessOrderElement();
      processRequest.setGoogleOrderNumber(orderNumber);
      return convertToDOM(processRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * This <b>refundOrder</b> method creates the XML request for a
   * &lt;refund-order&gt; Order Processing API request. This method
   * instructs Google Checkout to refund the full amount order amount.
   * The currency defaults to "USD".
   * @param orderNumber Google Checkout-assigned order number
   * @param reason Reason to refund the order
   */
  public Document refundOrder(String orderNumber, String reason)
      throws ProtocolException {
    return refundOrder(orderNumber, reason, -1.0F, null);
  }

  /**
   * This <b>refundOrder</b> creates a DOM object for issuing a
   * &lt;refund-order&gt; Order Processing API request for either the full
   * amount of an order or a partial amount of the order total.
   * @param orderNumber Google Checkout-assigned order number
   * @param reason Reason to refund the order
   * @param amount Numeric amount to refund to the customer.
   * @param currency 3-letter ISO 4217 currency code: USD, JPY, GBP
   */
  public Document refundOrder(String orderNumber, String reason, float amount,
      String currency) throws ProtocolException {
    if (StringUtil.isEmpty(reason)) {
      throw new ProtocolException("Refunding order: reason is missing!");
    }
    
    try {
      RefundOrderElement refundRequest = _objectFact.createRefundOrderElement();
      refundRequest.setGoogleOrderNumber(orderNumber);
      refundRequest.setReason(reason);
      if (amount > 0.0F && !StringUtil.isEmpty(currency)) {
        Money refundAmount = createMoney(amount, currency);
        refundRequest.setAmount(refundAmount);
      }
      return convertToDOM(refundRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * Creates a DOM for submitting the tracking info
   * @param orderNumber Google Checkout-assigned order number
   * @param trackNumber Tracking number that the courier has assigned
   * to the shipment
   * @param courier Name of the shipment carrier
   */
  public Document addTrackingData(String orderNumber, String trackNumber, 
      String courier) throws ProtocolException {
    try {
      AddTrackingDataElement addTrackingDataRequest 
          = _objectFact.createAddTrackingDataElement();
      TrackingData trackData = _objectFact.createTrackingData();
      trackData.setTrackingNumber(trackNumber);
      trackData.setCarrier(courier);
      addTrackingDataRequest.setTrackingData(trackData);
      addTrackingDataRequest.setGoogleOrderNumber(orderNumber);
      return convertToDOM(addTrackingDataRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * This <b>trackOrder</b> method calls the 
   * <code>trackOrder (String, String, String, boolean)</code> method to 
   * create a DOM object for a &lt;deliver-order&gt; Order Processing
   * API request. This request includes shipment tracking information
   * and instructs Google Checkout not to email the customer when the 
   * command is processed.
   * @param orderNumber Google Checkout-assigned order number
   * @param trackNumber Tracking number that the courier has assigned
   * to the shipment
   * @param courier Name of the shipment carrier
   */
  public Document trackOrder(String orderNumber, String trackNumber, 
      String courier) throws ProtocolException {
    return trackOrder(orderNumber, trackNumber, courier, false);
  }
  
  /**
   * This <b>trackOrder</b> method creates a DOM request for a
   * &lt;deliver-order&gt; Order Processing API request. This request 
   * includes shipment tracking information and may also instruct
   * Google Checkout to email the customer when the command is
   * processed.
   * @param orderNumber Google Checkout-assigned order number
   * @param trackNumber Tracking number that the courier has assigned
   * to the shipment
   * @param courier Name of the shipment carrier
   * @param toEmail Boolean flag that indicates whether Google Checkout
   * should email the customer when the command is processed.
   */
  public Document trackOrder(String orderNumber, String trackNumber, 
      String courier, boolean toEmail) throws ProtocolException {
    try {
      DeliverOrderElement deliveryRequest 
          = createOrderDelivery(orderNumber, toEmail);
      TrackingData trackData = _objectFact.createTrackingData();
      trackData.setTrackingNumber(trackNumber);
      trackData.setCarrier(courier);
      deliveryRequest.setTrackingData(trackData);
      return convertToDOM(deliveryRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createOrderDelivery</b> method creates a DeliverOrderElement
   * object, which is used to construct a &lt;deliver-order&gt; 
   * Order Processing API request. This method sets the value of the
   * google-order-number and the send-email flag in the request.
   * @param orderNumber Google Checkout-assigned order number
   * @param toEmail Boolean flag that indicates whether Google Checkout should
   * send an email confirmation to the customer
   */
  private DeliverOrderElement createOrderDelivery(String orderNumber, 
      boolean toEmail) throws ProtocolException {
    try {
      DeliverOrderElement deliveryRequest 
          = _objectFact.createDeliverOrderElement();
      deliveryRequest.setGoogleOrderNumber(orderNumber);
      deliveryRequest.setSendEmail(toEmail);
      return deliveryRequest;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * This <b>markOrderDelivered</b> method creates a DOM object for issuing
   * a &lt;deliver-order&gt; Order Processing API request, which changes
   * an order's fulfillment-order-state to DELIVERED. This method does not
   * include shipment tracking information for the order and instructs 
   * Google Checkout not to email the customer when the command is processed.
   * @param orderNumber Google Checkout-assigned order number
   */
  public Document markOrderDelivered(String orderNumber) 
      throws ProtocolException {
    return markOrderDelivered(orderNumber, false);
  }
  
  /**
   * This <b>markOrderDelivered</b> method creates a DOM object for issuing
   * a &lt;deliver-order&gt; Order Processing API request, which changes
   * an order's fulfillment-order-state to DELIVERED. This method does not
   * include shipment tracking information for the order but it may instruct 
   * Google Checkout to email the customer when the command is processed.
   * @param orderNumber Google Checkout-assigned order number
   * @param toEmail Boolean flag that indicates whether Google Checkout
   * should email the customer when the command is processed.
   */
  public Document markOrderDelivered(String orderNumber, boolean toEmail) 
      throws ProtocolException {
    DeliverOrderElement deliveryRequest 
        = createOrderDelivery(orderNumber, toEmail);
    return convertToDOM(deliveryRequest);
  }
  
  /**
   * The <b>archiveOrder</b> method creates the XML request for an
   * &lt;archive-order&gt; Order Processing API request, instructing
   * Google Checkout to remove the order from the merchant's Inbox 
   * in the Google Checkout Merchant Center. You may want to archive an
   * order after the customer has been charged and the order has been
   * delivered.
   * @param orderNumber Google Checkout-assigned order number
   */
  public Document archiveOrder(String orderNumber) throws ProtocolException {
    try {
      ArchiveOrderElement archiveRequest 
          = _objectFact.createArchiveOrderElement();
      archiveRequest.setGoogleOrderNumber(orderNumber);
      return convertToDOM(archiveRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>unarchiveOrder</b> method creates the XML request for an
   * &lt;unarchive-order&gt; Order Processing API request, instructing
   * Google Checkout to move the order back into the merchant's Inbox
   * in the Google Checkout Merchant Center.
   * @param orderNumber Google Checkout-assigned order number
   */
  public Document unarchiveOrder(String orderNumber) throws ProtocolException {
    try {
      UnarchiveOrderElement unArchiveReqeust 
          = _objectFact.createUnarchiveOrderElement();
      unArchiveReqeust.setGoogleOrderNumber(orderNumber);
      return convertToDOM(unArchiveReqeust);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>addMerchantOrderNumber</b> method creates the XML request for
   * an &lt;add-merchant-order-number&gt; command. This command allows you
   * to attach the value that you assign to uniquely identify an order to
   * that order in Google Checkout.
   * @param googleOrderNumber Google Checkout-assigned order number
   * @param merchantOrderNumber Value that the merchant uses to uniquely
   * identify the order identified by the googleOrderNumber.
   * @throws ProtocolException if either method argument is empty
   */
  public Document addMerchantOrderNumber(String googleOrderNumber, 
      String merchantOrderNumber) throws ProtocolException {
    if (StringUtil.isEmpty(googleOrderNumber) 
        || StringUtil.isEmpty(merchantOrderNumber)) {
      throw new ProtocolException("google order number " +
          "and merchant order number must not be empty");
    }

    try {
      AddMerchantOrderNumberElement addOrderNumberRequest
          = _objectFact.createAddMerchantOrderNumberElement();
      addOrderNumberRequest.setGoogleOrderNumber(googleOrderNumber);
      if (!StringUtil.isEmpty(merchantOrderNumber)) {
        addOrderNumberRequest.setMerchantOrderNumber(merchantOrderNumber);
      }
      return convertToDOM(addOrderNumberRequest);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>sendBuyerMessage</b> method creates an XML request containing a 
   * message that you would like to communicate to the customer.
   * @param googleOrderNumber Google Checkout-assigned order number
   * @param message The content of the message
   * @param toEmail Boolean flag whether to send an email to the buyer in 
   * addition to posting the message in the buyer's Google Checkout account
   */
  public Document sendBuyerMessage(String googleOrderNumber, String message,
      boolean toEmail) throws ProtocolException {
    if (StringUtil.isEmpty(googleOrderNumber)) {
      throw new ProtocolException("google order number must not be empty");
    }

    try {
      SendBuyerMessageElement buyerMsg = _objectFact.createSendBuyerMessageElement();
      buyerMsg.setGoogleOrderNumber(googleOrderNumber);
      buyerMsg.setMessage(message);
      buyerMsg.setSendEmail(toEmail);
      return convertToDOM(buyerMsg);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
}