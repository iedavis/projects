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

import java.util.HashMap;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.PipelineConstants;
import atg.commerce.states.OrderStates;
import atg.nucleus.GenericService;
import atg.service.pipeline.RunProcessException;

import com.google.checkout.schema._2.OrderStateChangeNotification;

/**
 * @author ssingh
 *
 */
public class GoogleOrderStateChangeNotificationManager extends GenericService {

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


	OrderStates orderStates;

	/**
	 * @return the orderStates
	 */
	public OrderStates getOrderStates() {
		return orderStates;
	}

	/**
	 * @param orderStates the orderStates to set
	 */
	public void setOrderStates(OrderStates orderStates) {
		this.orderStates = orderStates;
	}
	
	
	/**
	 * processes Google order state changed notifications.
	 * @param notification
	 * @throws GoogleCheckoutException 
	 */
	public void processOrderStateChangeNote(OrderStateChangeNotification notification) throws GoogleCheckoutException {
		String newFinState=notification.getNewFinancialOrderState();
		String newFulfillmentState=notification.getNewFulfillmentOrderState();
		String prevFinState=notification.getPreviousFinancialOrderState();
		String prevFulfillmentState=notification.getPreviousFulfillmentOrderState();
		String googleOrderId=notification.getGoogleOrderNumber();

		if(isLoggingDebug()){
			logDebug("newFinState "+newFinState);
			logDebug("newFulfillmentState "+newFulfillmentState);
			logDebug("prevFinState "+prevFinState);
			logDebug("prevFulfillmentState "+prevFulfillmentState);
			logDebug("googleOrderId "+googleOrderId);
		}
		 
		
		Order order =getGoogleCheckoutManager().getGoogleOrder(googleOrderId);
		if(order==null){
			throw new GoogleCheckoutException(Constants.NULL_GOOGLE_ORDER);
		}
		
		if(newFinState.equals(Constants.GOOGLE_ORDER_FINANCIAL_STATE_CHARGEABLE))
			processOrderCharged(order,notification);
		
		if(newFinState.equals(Constants.GOOGLE_ORDER_FINANCIAL_CANCELLED_BY_GOOGLE))
			processOrderCancel(order,notification);
		
	}

	/**
	 * processes cancel order notification, fires off a ORder removed event
	 * @param order
	 * @param notification
	 */
	void processOrderCancel(Order order, OrderStateChangeNotification notification) {
		
		
		
	}

	/**
	 * processes order chargable notification.
	 * @param order
	 * @param notification
	 * @throws GoogleCheckoutException 
	 */
	void processOrderCharged(Order order, OrderStateChangeNotification notification) 
		throws GoogleCheckoutException {
		
		HashMap param = new HashMap();
				
		Boolean riskInfoFlag=(Boolean) ((OrderImpl)order).getRepositoryItem().getPropertyValue(Constants.PROPERTY_RISK_INFORMATION_RECEIVED);
		if(isLoggingDebug())
			logDebug(" ORder with id "+order.getId()+" has risk Information received ="+riskInfoFlag.booleanValue());
		// if riskInfoFlag is not set, do not submit the order, jsut update the state to CHARGED
		// else submit the order
		if(!(riskInfoFlag.booleanValue())){
			param.put(Constants.GOOGLE_STATE,Constants.GOOGLE_CHARGEABLE_NOTIFICATION_STATE);
			int subMittedInt= getOrderStates().getStateFromString(Constants.GOOGLE_CHARGEABLE_NOTIFICATION_STATE);
			param.put(Constants.GOOGLE_STATE_INT, new Integer(Constants.GOOGLE_CHARGEABLE_NOTIFICATION_STATE_INT));
			param.put(PipelineConstants.ORDER, order);
			try {
				getGoogleCheckoutManager().getPipelineManager().runProcess(Constants.UPDATE_GOOGLE_ORDER_STATE_PIPELINE_CHAIN, param);
			} catch (RunProcessException e) {
				throw new GoogleCheckoutException(e);
			}
		}
		else{
			param.put(Constants.GOOGLE_STATE,OrderStates.SUBMITTED);			
			param.put(Constants.GOOGLE_STATE_INT, new Integer(1));
			try {
				getGoogleCheckoutManager().getPipelineManager().runProcess(Constants.UPDATE_GOOGLE_ORDER_STATE_PIPELINE_CHAIN, param);			
				getGoogleCheckoutManager().getOrderManager().updateOrder(order);
			} catch (CommerceException e) {
				if(isLoggingError())
					logError(e);
				// TODO Auto-generated catch block
				throw new GoogleCheckoutException(e);			
			} catch (RunProcessException e) {
				throw new GoogleCheckoutException(e);
			}
			getGoogleCheckoutManager().runProcessGoogleOrderPipelineChain(order, param);
			
		}
	}
	
	
	
}
