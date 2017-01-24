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
package atg.google.checkout;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.TransactionManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderHolder;
import atg.nucleus.GenericService;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.ServletUtil;

import com.google.checkout.schema._2.AnyType;
import com.google.checkout.schema._2.ShoppingCart;

/**
 * @author ssingh
 *
 */
public abstract class CallBackEventListener  extends GenericService {

	GoogleCheckoutManager googleCheckoutManager;

	public GoogleCheckoutManager getGoogleCheckoutManager() {
		return googleCheckoutManager;
	}

	public void setGoogleCheckoutManager(GoogleCheckoutManager googleCheckoutManager) {
		this.googleCheckoutManager = googleCheckoutManager;
	}
	
	TransactionManager transactionManager;

	/**
	 * @return the transactionManager
	 */
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	/**
	 * @param transactionManager the transactionManager to set
	 */
	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	/**
	 * Loads an order given the id. Use OrderManager to load order.
	 * @param orderId
	 * @return
	 * @throws CommerceException
	 */
	public Order loadOrder(String orderId) throws CommerceException{
		if(isLoggingDebug())
			logDebug("received notification to process order with id-"+orderId);
		if(orderId==null)
			return null;
		return this.getGoogleCheckoutManager().getOrderManager().loadOrder(orderId);		
	}
	
	/**
	 * extracts the orderId from the Merchant Private data,without orderId we have no idea which order to price.
	 * @param type
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	public Map findATGOrderAndProfileIdFromEventPrivateData(AnyType type) throws GoogleCheckoutException{

		return getGoogleCheckoutManager().findATGOrderAndProfileIdFromEventPrivateData(type);
	}
	
	/**
	 * finds order from the Shopping Cart, cart is part of the event.
	 * Uses Merchant private data to find order information.
	 * @param cart
	 * @return
	 * @throws CommerceException
	 */
	public Order findOrderFromEvent(ShoppingCart cart) throws CommerceException{
		return getGoogleCheckoutManager().findOrderFromEvent(cart);
	}
	
	
	public void returnSuccessACKToGoogle() throws IOException{
		DynamoHttpServletRequest request=ServletUtil.getCurrentRequest();
		DynamoHttpServletResponse response=request.getResponse();
		response.setStatus(HttpServletResponse.SC_OK );
		PrintWriter writer=response.getWriter();
		writer.print(Constants.NOTIFICATION_SUCCESS_ACK);
		response.flushBuffer();
		
	}
	
	public void returnErrorACKToGoogle() throws IOException{
		DynamoHttpServletRequest request=ServletUtil.getCurrentRequest();
		DynamoHttpServletResponse response=request.getResponse();
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );		
		response.flushBuffer();
		
	}
	
	
	
	
}
