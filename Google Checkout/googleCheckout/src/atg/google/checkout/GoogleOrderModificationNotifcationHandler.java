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

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import atg.commerce.CommerceException;
import atg.commerce.fulfillment.GenericUpdate;
import atg.commerce.fulfillment.Modification;
import atg.commerce.fulfillment.ModificationHandler;
import atg.commerce.fulfillment.ModifyOrderNotification;
import atg.commerce.order.Order;
import atg.commerce.states.OrderStates;
import atg.nucleus.GenericService;

/**
 * @author ssingh
 *
 */
public class GoogleOrderModificationNotifcationHandler extends GenericService
		implements ModificationHandler {


	GoogleCheckoutManager googleCheckoutManager;
	GoogleTransactionManager googleTransactionManager;
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
	 * @return the googleTransactionManager
	 */
	public GoogleTransactionManager getGoogleTransactionManager() {
		return googleTransactionManager;
	}

	/**
	 * @param googleTransactionManager the googleTransactionManager to set
	 */
	public void setGoogleTransactionManager(
			GoogleTransactionManager googleTransactionManager) {
		this.googleTransactionManager = googleTransactionManager;
	}
	
	
	/* (non-Javadoc)
	 * @see atg.commerce.fulfillment.ModificationHandler#handleModifyOrder(java.lang.String, javax.jms.ObjectMessage)
	 */
	public void handleModifyOrder(String pPortName, ObjectMessage pMessage)
			throws JMSException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see atg.commerce.fulfillment.ModificationHandler#handleModifyOrderNotification(java.lang.String, javax.jms.ObjectMessage)
	 */
	public void handleModifyOrderNotification(String pPortName,
			ObjectMessage pMessage) throws JMSException {
		
		ModifyOrderNotification obj=(ModifyOrderNotification) pMessage.getObject();
		if(obj!=null)
			if(isLoggingDebug())
				logDebug("Class of obj is "+obj.getClass());
		
		try {
			processModifyOrderNotification(obj);
		} catch (GoogleCheckoutException e) {
			if(isLoggingError())
				logError(e);
		}

	}
	
	/**
	 * process an incoming modification message, upon some actions send updates to Google
	 * @param obj
	 * @throws GoogleCheckoutException 
	 */
	void processModifyOrderNotification(ModifyOrderNotification message) throws GoogleCheckoutException {
		if(message==null){
			if(isLoggingError())
				logError("ModificationOrderNotification is null");
			return;
		}
		
		String id = message.getId();
		Modification[] modifications = message.getModifications();
		String orderId = message.getOrderId();
		Order order = null;
		try {
			order = getGoogleCheckoutManager().getOrderManager().loadOrder(orderId);
		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
		}
		// if not a Google order, ignore ignore
		if(getGoogleCheckoutManager().getGoogleOrderIdFromOrder(order)==null)
			return;
		
		String type = message.getType();
		if(isLoggingDebug())
			logDebug("received a message with id "+id+" orderId"+orderId+" type="+type);
		if(modifications==null){
			if(isLoggingDebug())
				logDebug("No Modifications");
		}
		Modification mod=null;
		for(int i=0;i<modifications.length;i++){
			mod=modifications[i];
			processModification(order,mod);
				
		}
	}

	/**
	 * process the incoming message, we process following event at this type
	 * update shipping group state to AWAITING GOOGLE CHARGE-> debit request to Google
	 * Remove -> send cancel request to Google
	 * MarkDelivered -> send mark delivered to Google
	 * @param order 
	 * @param mod
	 * @throws GoogleCheckoutException 
	 */
	void processModification(Order order, Modification mod) throws GoogleCheckoutException {
		if(mod==null)
			return;
		 
		if(isLoggingDebug()){
			logDebug("Modifation Class"+mod.getClass());
			logDebug("Modifation Type"+mod.getModificationType());
			logDebug(" Id"+mod.getModificationId());
		}	
		GenericUpdate gu=null;
		if(mod instanceof GenericUpdate){
			gu=(GenericUpdate) mod;
			logDebug("Generic Update event target type"+gu.getTargetType());
			logDebug("Generic Update event target getTargetId"+gu.getTargetId());
			logDebug("Generic Update event target getPropertyName"+gu.getPropertyName());
			logDebug("Generic Update event target getNewValue"+gu.getNewValue());
		}
		if(gu==null)
			return;
		String targetId=gu.getTargetId();
		int targetType=gu.getTargetType();
		
		if(targetType==Modification.TARGET_SHIPPING_GROUP){
			processShippingGroupChange(order,gu);
		}
		if(targetType==Modification.TARGET_ORDER){
			processOrderChange(order,gu);
		}
		
	}

	/**
	 * if order state is NO_PENDING_ACTION, mark delivered
	 * @param order
	 * @param gu
	 * @throws GoogleCheckoutException 
	 */
	void processOrderChange(Order order, GenericUpdate gu) throws GoogleCheckoutException {
		String propertyName=gu.getPropertyName();
		OrderStates os =getGoogleCheckoutManager().getOrderStates();
		if(propertyName.equals(Constants.PROPERTY_STATE)){
			Integer newValue=(Integer) gu.getNewValue();
			if(newValue.intValue()==os.getStateValue(OrderStates.NO_PENDING_ACTION)){
				if(isLoggingDebug()){
					logDebug("Debug received for ORder NO PENDING ACTION! breakthough----! DELIVERED NOW");
				}
				getGoogleTransactionManager().markDelivered(order);
			}
			else if (newValue.intValue()==os.getStateValue(OrderStates.REMOVED)){
				getGoogleTransactionManager().cancelOrder(order,Constants.DEFAULT_CANCEL_REASON,Constants.DEFAULT_CANCEL_COMMENT);
			}
			
			
		}
		
	}

	/**
	 * if message is Shipping Group update and state is AWAITING GOOGLE CHARGE, send debit request to Google
	 * @param order
	 * @param gu
	 * @throws GoogleCheckoutException 
	 */
	void processShippingGroupChange(Order order, GenericUpdate gu) throws GoogleCheckoutException {
		
		String propertyName=gu.getPropertyName();
		if(propertyName.equals(Constants.PROPERTY_STATE)){
			Integer newValue=(Integer) gu.getNewValue();
			if(newValue.intValue()==Constants.AWAITING_GOOGLE_CHARGE_INT){
				if(isLoggingDebug()){
					logDebug("Debug received for Debit! breakthough----! debit NOW");
				}
				getGoogleTransactionManager().debit(order);
			}
		}
		
	}

}
