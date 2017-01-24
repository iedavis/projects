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
package atg.google.checkout.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.commerce.CommerceException;
import atg.commerce.fulfillment.OrderFulfiller;
import atg.commerce.fulfillment.OrderFulfillmentTools;
import atg.commerce.fulfillment.PipelineConstants;
import atg.commerce.order.CreditCard;
import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.ShippingGroup;
import atg.commerce.payment.PaymentException;
import atg.commerce.payment.PaymentManager;
import atg.commerce.states.OrderStates;
import atg.commerce.states.PaymentGroupStates;
import atg.commerce.states.ShippingGroupStates;
import atg.core.util.ResourceUtils;
import atg.google.checkout.Constants;
import atg.google.checkout.GoogleShippingGroupStates;
import atg.google.checkout.GoogleTransactionManager;
import atg.nucleus.logging.ApplicationLoggingImpl;
import atg.payment.PaymentStatus;
import atg.repository.RepositoryItem;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

/**
 * @author ssingh
 *
 */
public class SendGoogleOrderChargeMessageProcessor extends ApplicationLoggingImpl
implements PipelineProcessor {

	private final int SUCCESS = 1;


	static final String MY_RESOURCE_NAME = "atg.commerce.fulfillment.FulfillmentResources";

	/** Resource Bundle **/
	private static java.util.ResourceBundle sResourceBundle = java.util.ResourceBundle.getBundle(MY_RESOURCE_NAME, atg.service.dynamo.LangLicense.getLicensedDefault());


	//-----------------------------------------------
	/**
	 * Returns the valid return codes
	 * 1 - The processor completed
	 * @return an integer array of the valid return codes.
	 */
	public int[] getRetCodes()
	{
		int[] ret = {SUCCESS};
		return ret;
	} 

	/* (non-Javadoc)
	 * @see atg.service.pipeline.PipelineProcessor#runProcess(java.lang.Object, atg.service.pipeline.PipelineResult)
	 */
	public int runProcess(Object pParam, PipelineResult result) throws Exception {

		HashMap map = (HashMap) pParam;
		Order pOrder = (Order) map.get(PipelineConstants.ORDER);
		OrderFulfiller of = (OrderFulfiller) map.get(PipelineConstants.ORDERFULFILLER);
		List performedModifications = (List) map.get(PipelineConstants.MODIFICATIONLIST);

		if (of == null)
			throw new InvalidParameterException(ResourceUtils.getMsgResource("InvalidOrderFulfillerParameter",
					MY_RESOURCE_NAME, sResourceBundle));

		if (pOrder == null) {
			if (of.isLoggingError())
				of.logError(atg.commerce.fulfillment.Constants.ORDER_IS_NULL);
			throw new InvalidParameterException(ResourceUtils.getMsgResource("InvalidOrderParameter",
					MY_RESOURCE_NAME, sResourceBundle));
		}

		OrderFulfillmentTools tools = of.getOrderFulfillmentTools();
		if(!(pOrder instanceof OrderImpl)){
			return SUCCESS;
		}
		ShippingGroupStates sgs = of.getShippingGroupStates();
		PaymentGroupStates pgs = of.getPaymentGroupStates();
		OrderStates orderStates = of.getOrderStates();
		RepositoryItem rep = ((OrderImpl)pOrder).getRepositoryItem();
		String googleOrderId=null;
		try {
			googleOrderId = (String) DynamicBeans.getPropertyValue(rep,Constants.PROPERTY_GOOGLE_ORDER_ID);
		} catch (PropertyNotFoundException e) {
			if(isLoggingError())
				logError(e);				
		}	
		
		if(googleOrderId==null){
			return SUCCESS;
		}
		if(hasCC(pOrder)){
			if(isLoggingDebug())
				logDebug("Order has CC, sending message to Google");
			
			// we fire off a modification message and our listener should take care of this.
			//	boolean googleResult=getGoogleTransactionManager().debit(pOrder);
			//assume no errors because we using messaging now
			if(true){
				// if success just mark the SG state as PROCESSING, not done yet, wait for google to come back and update CC
				ShippingGroup sg=(ShippingGroup) pOrder.getShippingGroups().get(0);
				tools.setShippingGroupState(sg, Constants.AWAITING_GOOGLE_CHARGE_INT, Constants.AWAITING_GOOGLE_CHARGE, performedModifications);
				if(isLoggingDebug())
					logDebug(" Changed Shipping Group state to AWAITING_GOOGLE_CHARGE_INT");
			}else{
				// error sending message to Google, mark order as PENDING_MERCHAN_ACTION, grab attention
				ShippingGroup sg=(ShippingGroup) pOrder.getShippingGroups().get(0);
				tools.setShippingGroupState(sg, sgs.getStateValue(sgs.PENDING_MERCHANT_ACTION), Constants.GOOGLE_SETTLE_ERROR, performedModifications);
				PaymentGroup pg = (PaymentGroup) pOrder.getPaymentGroups().get(0);
				tools.setPaymentGroupState(pg,pgs.getStateValue(pgs.SETTLE_FAILED),Constants.GOOGLE_SETTLE_ERROR, performedModifications);
				tools.setOrderState(pOrder, orderStates.getStateValue(orderStates.PENDING_MERCHANT_ACTION), Constants.GOOGLE_SETTLE_ERROR, performedModifications);
			}
			return STOP_CHAIN_EXECUTION_AND_COMMIT;
		}

	
	return SUCCESS;
}
	
	
	/**
	 * checks if order has a Credit Cart, maybe in some cases no CC
	 * @param order
	 * @return
	 */
	private boolean hasCC(Order order) {
		PaymentGroup pg;
		List pgs=order.getPaymentGroups();
		Iterator itr = pgs.iterator();
		// only 1 CC supported, find it and use it
		while(itr.hasNext()){
			pg=(PaymentGroup) itr.next();
			if(pg instanceof CreditCard){
				return true;
			}
		}
		return false;
	}
	



}
