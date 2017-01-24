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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;

import atg.commerce.CommerceException;
import atg.commerce.claimable.ClaimableException;
import atg.commerce.claimable.ClaimableManager;
import atg.commerce.claimable.InsufficientFundsException;
import atg.commerce.fulfillment.PaymentGroupUpdate;
import atg.commerce.order.CreditCard;
import atg.commerce.order.GiftCertificate;
import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupNotFoundException;
import atg.commerce.states.PaymentGroupStates;
import atg.google.checkout.CallBackEventListener;
import atg.google.checkout.GoogleCheckoutException;
import atg.google.checkout.fulfillment.messaging.FulfillmentMessageSource;
import atg.payment.creditcard.CreditCardStatusImpl;
import atg.payment.giftcertificate.GiftCertificateStatusImpl;
import atg.repository.RepositoryException;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.ChargeNotificationEvent;
import com.google.checkout.sample.event.ChargeNotificationListener;
import com.google.checkout.schema._2.ChargeAmountNotification;
import com.google.checkout.schema._2.Money;

/**
 * Charge Notifcation Listener for Google Events
 * @author ssingh
 *
 */
public class ATGChargeNotificationListener extends CallBackEventListener  implements ChargeNotificationListener {


	FulfillmentMessageSource fulfillmentMessageSource;

	/**
	 * @return the fulfillmentMessageSource
	 */
	public FulfillmentMessageSource getFulfillmentMessageSource() {
		return fulfillmentMessageSource;
	}

	/**
	 * @param fulfillmentMessageSource the fulfillmentMessageSource to set
	 */
	public void setFulfillmentMessageSource(
			FulfillmentMessageSource fulfillmentMessageSource) {
		this.fulfillmentMessageSource = fulfillmentMessageSource;
	}
	
	
	/* 
	 * handle charge amount
	 * (non-Javadoc)
	 * @see com.google.checkout.sample.event.CallBackListener#handleEvent(com.google.checkout.sample.event.CallBackEvent)
	 */
	public void handleEvent(CallBackEvent event) {
		if(isLoggingDebug())
			logDebug("-------------->>>>>>> ChargeAmountNotification event received ");
		if(event!=null){
			if(isLoggingDebug())
				logDebug(" Event calss is "+event.getClass());
		}
		ChargeAmountNotification amtNotification=((ChargeNotificationEvent)event).getChargeAmountNote();

		try {
			try {
				processChargeOrderNotification(amtNotification);
			} catch (GoogleCheckoutException e) {
				if(isLoggingError())
					logError(e);
				returnErrorACKToGoogle();
			}
			catch (Exception e) {
				if(isLoggingError())
					logError(e);
				returnErrorACKToGoogle();
			}
			returnSuccessACKToGoogle();
		} catch (IOException e) {
			if(isLoggingError())
				logError(e);
		}
		return;

	}

	/**
	 * updates CC to debited and fires off a message CC settled
	 * Fulfillement system then marks the order as delivered
	 * @param amtNotification
	 * @throws GoogleCheckoutException 
	 */
	void processChargeOrderNotification(ChargeAmountNotification amtNotification) throws GoogleCheckoutException {
		String googleOrderId=amtNotification.getGoogleOrderNumber() ;
		Money charge=amtNotification.getLatestChargeAmount();
		Money totalCharge=amtNotification.getTotalChargeAmount();
		Order order=getGoogleCheckoutManager().getGoogleOrder(googleOrderId);

		if(isLoggingDebug()){
			logDebug(" Received google order id charge notification"+googleOrderId);
			logDebug(" Latest Charge Amt "+charge.getValue());
			logDebug(" Total Charge"+totalCharge.getValue());			
		}

		String ccId=findCreditCartId(order);
		if(ccId==null)
			return;
		debitGiftCertificates(order);
		CreditCard cc;
		try {
			cc = (CreditCard) order.getPaymentGroup(ccId);
			markCCAsDebit(cc, amtNotification, order);
		} catch (PaymentGroupNotFoundException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (InvalidParameterException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		
		try {
			getGoogleCheckoutManager().getOrderManager().updateOrder(order);
		} catch (CommerceException e1) {
			if(isLoggingError())
				logError(e1);
			throw new GoogleCheckoutException(e1);
		}
	
	}


	/**
	 * debits any applicable gift certificates
	 * @param order
	 * @throws GoogleCheckoutException 
	 */
	void debitGiftCertificates(Order order) throws GoogleCheckoutException {
	
		if(order==null)
			return;
		
		List pgs=order.getPaymentGroups();
		PaymentGroup pg=null;
		Iterator itr =pgs.iterator();
		while(itr.hasNext()){
			pg=(PaymentGroup) itr.next();
			if(pg instanceof GiftCertificate){
				debitGiftCertificate((GiftCertificate)pg);
			}
		}
		
	}

	/**
	 * debits the Gift certificate
	 * @param pg
	 * @throws GoogleCheckoutException 
	 */
	void debitGiftCertificate(GiftCertificate pg) throws GoogleCheckoutException {
		ClaimableManager manager = getGoogleCheckoutManager().getGoogleMerchantCalculationManager().getClaimableManager();
		double amt=pg.getAmountAuthorized();
		if(isLoggingDebug())
			logDebug(" Debit Amt"+amt+" from Gift Certificate "+pg.getGiftCertificateNumber());
		try {
			manager.debitClaimableGiftCertificate(pg.getGiftCertificateNumber(), amt);
			pg.setAmountDebited(amt);
			
		} catch (atg.commerce.claimable.InvalidParameterException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (ClaimableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InsufficientFundsException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (RepositoryException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		
	}

	/**
	 * marks cc as debited
	 * @param cc
	 * @param amtNotification
	 * @throws GoogleCheckoutException 
	 */void markCCAsDebit(CreditCard cc,ChargeAmountNotification amtNotification,Order order) throws GoogleCheckoutException{
		Money totalCharge=amtNotification.getTotalChargeAmount();
		cc.setAmountDebited(totalCharge.getValue().doubleValue());
		CreditCardStatusImpl ccStatus = new CreditCardStatusImpl();
		ccStatus.setAmount(amtNotification.getLatestChargeAmount().getValue().doubleValue());
		ccStatus.setTransactionTimestamp(amtNotification.getTimestamp().getTime());
		ccStatus.setTransactionId(amtNotification.getSerialNumber());
		cc.addDebitStatus(ccStatus);
		cc.setState(2);
		cc.setStateDetail(PaymentGroupStates.SETTLED);
		
		
		String[] creditCardIds=new String[1];
		creditCardIds[0]=cc.getId();
		PaymentGroupUpdate pgUpdate = new PaymentGroupUpdate();
		pgUpdate.setPaymentGroupIds(creditCardIds);
		pgUpdate.setModificationId(cc.getId());
		pgUpdate.setModificationType(pgUpdate.PAYMENT_GROUP_UPDATE);
	
		//Modification payModification=orderFulfillmentTools.createPayUpdateModification(Constants.PROPERTY_AMOUNT_DEBITED,new Double(prevAmt) , new Double(totalCharge.getValue().doubleValue()), ccId);
		List modList = new ArrayList();
		modList.add(pgUpdate);
		try {
			getFulfillmentMessageSource().sendModifyOrderNotification(order.getId(),
					modList,
			        null,               
			        "ModifyNotificationPort",
			        amtNotification.getSerialNumber(),
			        null);
		} catch (JMSException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		double prevAmt=cc.getAmountDebited();
		if(isLoggingDebug())
			logDebug(" Updating payment groups, prev Amt ="+prevAmt+" new Charge ="+totalCharge.getValue().doubleValue());
	

	}
	
	/**
	 * finds the Credit Card ID from the order
	 * @param order
	 * @return
	 */
	String findCreditCartId(Order order) {
		List pgs=order.getPaymentGroups();
		Iterator itr =pgs.iterator();
		PaymentGroup pg;
		// only 1 CC supported, find it and use it
		while(itr.hasNext()){
			pg=(PaymentGroup) itr.next();
			if(pg instanceof CreditCard){
				return pg.getId();			
			}
		}
		return null;

	}
	


}
