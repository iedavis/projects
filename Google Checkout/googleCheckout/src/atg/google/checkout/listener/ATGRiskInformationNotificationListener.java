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
package atg.google.checkout.listener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.transaction.TransactionManager;

import atg.commerce.CommerceException;
import atg.commerce.order.CreditCard;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PipelineConstants;
import atg.commerce.states.OrderStates;
import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.google.checkout.CallBackEventListener;
import atg.google.checkout.Constants;
import atg.google.checkout.GoogleCheckoutException;
import atg.service.pipeline.RunProcessException;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.RiskInformationNotificationEvent;
import com.google.checkout.sample.event.RiskInformationNotificationListener;
import com.google.checkout.schema._2.Address;
import com.google.checkout.schema._2.RiskInformation;
import com.google.checkout.schema._2.RiskInformationNotification;

/**
 * @author ssingh
 *
 */
public class ATGRiskInformationNotificationListener extends CallBackEventListener implements RiskInformationNotificationListener {



	/* (non-Javadoc)
	 * @see com.google.checkout.sample.event.CallBackListener#handleEvent(com.google.checkout.sample.event.CallBackEvent)
	 */
	public void handleEvent(CallBackEvent event) {
		TransactionManager tm = getTransactionManager();
		TransactionDemarcation td = new TransactionDemarcation ();
		try {
			try {
				td.begin (tm, td.REQUIRED);

				if(isLoggingDebug())
					logDebug("-------------->>>>>>> received an event RiskInformationNotification");
				if(event!=null){
					if(isLoggingDebug())
						logDebug(" Event calss is "+event.getClass());
				}

				// update Google Order State to Risk information verified

				RiskInformationNotificationEvent riskEvent = (RiskInformationNotificationEvent)event;

				try {
					processRiskEvent(riskEvent.getRiskNote());
				} catch (GoogleCheckoutException e) {
					try {
						returnErrorACKToGoogle();
					} catch (IOException e1) {
						if(isLoggingError())
							logError(e);
					}
				}
			}
			finally {
				td.end ();
			}
		}
		catch (TransactionDemarcationException exc) {
			if(isLoggingError())
				logError(exc);
		}

		return ;
	}

	/**
	 * @param riskEvent
	 * @throws GoogleCheckoutException 
	 */
	void processRiskEvent(RiskInformationNotification riskEvent) throws GoogleCheckoutException {
		if(riskEvent==null){
			if(isLoggingError())
				logError("Risk event is null");
		}

		String googleOrderId = riskEvent.getGoogleOrderNumber();
		RiskInformation riskInfo=riskEvent.getRiskInformation();
		Address billingAddress=riskInfo.getBillingAddress();

		Order order=getGoogleCheckoutManager().getGoogleOrder(googleOrderId);
		if(order==null){
			throw new GoogleCheckoutException(Constants.NULL_GOOGLE_ORDER);
		}		
		List pgs = order.getPaymentGroups();
		Iterator itr = pgs.iterator();
		PaymentGroup pg=null;
		atg.core.util.Address atgAddress=null;
		while(itr.hasNext()){
			pg=(PaymentGroup)itr.next();
			if(pg instanceof CreditCard){
				atgAddress=((CreditCard)pg).getBillingAddress();
				getGoogleCheckoutManager().copyAddressToATGAddress(billingAddress, atgAddress);
			}
		}
		((OrderImpl)order).getRepositoryItem().setPropertyValue(Constants.PROPERTY_RISK_INFORMATION_RECEIVED, new Boolean(true));

		if(order.getStateDetail()==Constants.GOOGLE_CHARGEABLE_NOTIFICATION_STATE){
			HashMap param = new HashMap();
			param.put(Constants.GOOGLE_STATE,OrderStates.SUBMITTED);			
			param.put(Constants.GOOGLE_STATE_INT, new Integer(1));
			param.put(PipelineConstants.ORDER, order);
			try {
				getGoogleCheckoutManager().getPipelineManager().runProcess(Constants.UPDATE_GOOGLE_ORDER_STATE_PIPELINE_CHAIN, param);			
				getGoogleCheckoutManager().getOrderManager().updateOrder(order);

			} catch (RunProcessException e) {
				if(isLoggingError())
					logError(e);
				throw new GoogleCheckoutException(e);
			} catch (CommerceException e) {
				if(isLoggingError())
					logError(e);
				throw new GoogleCheckoutException(e);
			}
			if(isLoggingDebug()){
				logDebug("After Update Order state is "+order.getState());
				logDebug("After Update Order state detail is "+order.getStateDetail());
			}
			try {
				getGoogleCheckoutManager().getPipelineManager().runProcess(Constants.SUBMIT_GOOGLE_ORDER_PIPELINE_CHAIN, param);
			} catch (RunProcessException e) {
				throw new GoogleCheckoutException(e);
			}
		}
		if(isLoggingDebug()){
			logDebug("After Update Order state is "+order.getState());
			logDebug("After Update Order state detail is "+order.getStateDetail());
		}

		try {
			getGoogleCheckoutManager().getOrderManager().updateOrder(order);
		} catch (CommerceException e) {
			throw new GoogleCheckoutException(e);
		}
	}

}
