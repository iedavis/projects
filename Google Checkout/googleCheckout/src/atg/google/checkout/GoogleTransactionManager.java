/*<ATGCOPYRIGHT>
 * Copyright (C) 1999-2006 Art Technology Group, Inc.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Art Technology Group.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * Art Technology Group (ATG) MAKES NO REPRESENTATIONS OR WARRANTIES
 * ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ATG SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * "Dynamo" is a trademark of Art Technology Group, Inc.
 </ATGCOPYRIGHT>*/
/**
 * 
 */
package atg.google.checkout;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.commerce.order.CreditCard;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.PaymentGroup;
import atg.nucleus.GenericService;
import atg.repository.RepositoryItem;

import com.google.checkout.sample.protocol.OrderTransactionBuilder;
import com.google.checkout.sample.protocol.ProtocolException;
import com.google.checkout.schema._2.RequestReceivedResponse;
import com.google.checkout.schema._2.impl.ErrorElementImpl;
import com.google.checkout.schema._2.impl.RequestReceivedElementImpl;

/**
 * @author ssingh
 *
 */
public class GoogleTransactionManager extends GenericService {

	OrderTransactionBuilder transactionBuilder;
	
	/**
	 * @return the transactionBuilder
	 */
	public OrderTransactionBuilder getTransactionBuilder() {
		return transactionBuilder;
	}

	/**
	 * @param transactionBuilder the transactionBuilder to set
	 */
	public void setTransactionBuilder(OrderTransactionBuilder transactionBuilder) {
		this.transactionBuilder = transactionBuilder;
	}
	
	
	/**
	 * initializes the transaction builder
	 *
	 */
	public void doStartService(){
		try {
			transactionBuilder = OrderTransactionBuilder.getInstance();					
		} catch (ProtocolException e) {
			if(isLoggingError()){
				logError("-------- Transaction Builder is null ");
				logError(e);
			}
		}
		
	}
	
	
	GoogleCheckoutManager googleCheckoutManager;

	/**
	 * @return the googleCheckoutManager
	 */
	public GoogleCheckoutManager getGoogleCheckoutManager() {
		return googleCheckoutManager;
	}

	/**
	 * @param googleCheckoutManager the googleCheckoutManager to set
	 */
	public void setGoogleCheckoutManager(GoogleCheckoutManager googleCheckoutManager) {
		this.googleCheckoutManager = googleCheckoutManager;
	}
	
	
	/**
	 * sends debit message to Google
	 * @param googleOrderId
	 * @param amt
	 * @throws GoogleCheckoutException
	 */
	public boolean debit(String googleOrderId,float amt) throws GoogleCheckoutException{
		try {
			if(getTransactionBuilder()==null)
			{
				if(isLoggingError())
					logError(" Google transaction builder is not initalized");
			}
			Document dom =getTransactionBuilder().chargeOrder(googleOrderId, amt);
			String message=getTransactionBuilder().unmarshal(dom);
			String response=getGoogleCheckoutManager().postXMLMessageToGoogle(message);
			StringReader strReader = new StringReader(response);
			InputSource ipDataSrc= new InputSource(strReader);

			Object responseObj= getTransactionBuilder().parseToJAXB(ipDataSrc);
			if(isLoggingDebug()){
				logDebug("Sent request to google "+message);
				logDebug("Received response from Google ="+response);
				logDebug("Received response class ="+responseObj.getClass());				
			}
			if(responseObj instanceof ErrorElementImpl)
				return false;
			if(responseObj instanceof RequestReceivedElementImpl)
				return true;
				
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		return false;
	}

	/**
	 * debits an amount from the CC on an order
	 * @param order
	 * @param amt
	 * @throws GoogleCheckoutException
	 */
	public boolean debit(Order order) throws GoogleCheckoutException{
		
		if(order==null){
			if(isLoggingError())
				logError("ORder is null passed to debit when trying to send to Google");
			return false;
		}
		
		String googleOrderId=getGoogleCheckoutManager().getGoogleOrderIdFromOrder(order);
		float amt=0;
		List pgs=order.getPaymentGroups();
		Iterator itr =pgs.iterator();
		PaymentGroup pg;
		// only 1 CC supported, find it and use it
		while(itr.hasNext()){
			pg=(PaymentGroup) itr.next();
			if(pg instanceof CreditCard){
				amt=(float) pg.getAmountAuthorized();
				break;
			}
		}
		if(isLoggingDebug()){
			logDebug("For order with id="+order.getId()+" amount charged on CC="+amt);
		}
		
		return debit(googleOrderId,amt);
		
	}
	
	/**
	 *  sends message to Google to mark order as delivered to Google
	 * @param googleOrderId
	 * @return
	 */
	public String markDelivered(String googleOrderId){
		try {
			Document doc =getTransactionBuilder().markOrderDelivered(googleOrderId);
			String request = getTransactionBuilder().unmarshal(doc);
			if(isLoggingDebug())
				logDebug("Sending Request to Google "+request);
			String response= getGoogleCheckoutManager().postXMLMessageToGoogle(request);
			
			if(isLoggingDebug())
				logDebug("Response from Google "+response);
			return response;
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
		} catch (GoogleCheckoutException e) {
			if(isLoggingError())
				logError(e);
		}
		return "error";
	}
	

	/**
	 * marks order as delivered to Google
	 * @param pOrder
	 * @return
	 * @throws GoogleCheckoutException
	 */
	public String markDelivered(Order pOrder) throws GoogleCheckoutException{
		if(pOrder==null)
			return null;
		String googleOrderId=getGoogleCheckoutManager().getGoogleOrderIdFromOrder(pOrder);
		return markDelivered(googleOrderId);
	}

	/**
	 * sends tracking data to Google, not supported till now, nothing to send
	 * @param googleOrderId
	 * @param trackingNumber
	 * @param courier
	 * @return
	 */
	public String addTracking(String googleOrderId,String trackingNumber,String courier){
		try {
			Document doc =getTransactionBuilder().addTrackingData(trackingNumber, googleOrderId, courier);
			String request = getTransactionBuilder().unmarshal(doc);
			if(isLoggingDebug())
				logDebug("Sending Request to Google "+request);
			String response= getGoogleCheckoutManager().postXMLMessageToGoogle(request);
			
			if(isLoggingDebug())
				logDebug("Response from Google "+response);
			return response;
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
		} catch (GoogleCheckoutException e) {
			if(isLoggingError())
				logError(e);
		}
		return "error";
	}
	

	/**
	 * sends tracking data to Google, not supported till now, nothing to send
	 * @param pOrder
	 * @param trackingNumber
	 * @param courier
	 * @return
	 * @throws GoogleCheckoutException
	 */
	public String addTracking(Order pOrder,String trackingNumber,String courier) throws GoogleCheckoutException{
		if(pOrder==null)
			return null;
		String googleOrderId=getGoogleCheckoutManager().getGoogleOrderIdFromOrder(pOrder);
		return addTracking(googleOrderId,trackingNumber,courier);
	}

	/**
	 * sends cancel order message to Google
	 * @param order
	 * @throws GoogleCheckoutException 
	 */
	public String cancelOrder(Order pOrder,String reason,String comment) throws GoogleCheckoutException {
		if(pOrder==null)
			return null;
		String googleOrderId=getGoogleCheckoutManager().getGoogleOrderIdFromOrder(pOrder);
		return cancelOrder(googleOrderId,reason,comment);
		
	}
	
	/**
	 * sends cancel order message to Google
	 * @param order
	 */
	public String cancelOrder(String googleOrderId,String reason,String comment) {
		try {
			Document doc =getTransactionBuilder().cancelOrder(googleOrderId, reason, comment);
			String request = getTransactionBuilder().unmarshal(doc);
			if(isLoggingDebug())
				logDebug("Sending Cancel Request to Google "+request);
			String response= getGoogleCheckoutManager().postXMLMessageToGoogle(request);
			
			if(isLoggingDebug())
				logDebug("Response from Google "+response);
			return response;
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
		} catch (GoogleCheckoutException e) {
			if(isLoggingError())
				logError(e);
		}
		return "error";
		
	}
	
	
}
