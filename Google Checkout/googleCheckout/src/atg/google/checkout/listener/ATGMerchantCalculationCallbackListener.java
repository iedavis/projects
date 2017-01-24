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
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.w3c.dom.Document;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.google.checkout.CallBackEventListener;
import atg.google.checkout.Constants;
import atg.repository.RepositoryItem;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.ServletUtil;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.MerchantCalculationCallbackEvent;
import com.google.checkout.sample.event.MerchantCalculationCallbackListener;
import com.google.checkout.sample.protocol.AbstractProtocolBuilder;
import com.google.checkout.sample.protocol.MerchantCalculationResultBuilder;
import com.google.checkout.sample.protocol.ProtocolException;
import com.google.checkout.schema._2.AnonymousAddress;
import com.google.checkout.schema._2.Calculate;
import com.google.checkout.schema._2.MerchantCalculationCallback;
import com.google.checkout.schema._2.MerchantCalculationResults;
import com.google.checkout.schema._2.Method;
import com.google.checkout.schema._2.Result;
import com.google.checkout.schema._2.Calculate.AddressesType;
import com.google.checkout.schema._2.Calculate.MerchantCodeStringsType;
import com.google.checkout.schema._2.Calculate.ShippingType;
import com.google.checkout.schema._2.MerchantCalculationResults.ResultsType;

/**
 * @author ssingh
 *
 */
public class ATGMerchantCalculationCallbackListener extends CallBackEventListener implements MerchantCalculationCallbackListener{


	public void doStartService(){
		this.registerResultBuilder();
	}

	AbstractProtocolBuilder resultBuilder;

	public AbstractProtocolBuilder getResultBuilder() {
		return resultBuilder;
	}

	public void setResultBuilder(AbstractProtocolBuilder resultBuilder) {
		this.resultBuilder = resultBuilder;
	}

	/**
	 * This method is called by the super doStartService.
	 * This registers a ResultBuilder, resultBuilders are needed to prepare result to be sent back to Google
	 * @see atg.google.checkout.CallBackEventListener#registerResultBuilder()
	 */	
	protected void registerResultBuilder() {
		try {
			this.setResultBuilder(MerchantCalculationResultBuilder.getInstance());
		} catch (ProtocolException e) {
			new CommerceException(e);
		}

	}

	/* (non-Javadoc)
	 * @see com.google.checkout.sample.event.CallBackListener#handleEvent(com.google.checkout.sample.event.CallBackEvent)
	 */
	public void handleEvent(CallBackEvent event) {

		TransactionManager tm = getTransactionManager();
		TransactionDemarcation td = new TransactionDemarcation ();
		try {
			try {
				td.begin (tm, td.REQUIRED);


				if(isLoggingDebug()){
					logDebug("-------------->>>>>>> Received an event MerchantCalculationCallback"+event );
					DynamoHttpServletRequest request=ServletUtil.getCurrentRequest();
					logDebug("Request ="+request.getRequestURIWithQueryString()+" session id ="+request.getSession().getId());
					if(event!=null)
						logDebug("Class for event ="+event.getClass().getName());
				}
				if(event==null)
					return ;//TODO: error msg?
				if(!(event instanceof MerchantCalculationCallbackEvent)){
					if(isLoggingError())
						logError("Event is not instance of MerchantCalculationCallback, invalid listener, check the Dispatcher configs");
				}
				DynamoHttpServletRequest request = ServletUtil.getCurrentRequest();
				RepositoryItem profile=(RepositoryItem) request.resolveName(Constants.PROFILE_PATH);
				MerchantCalculationCallbackEvent merchantEvent=(MerchantCalculationCallbackEvent)event;
				MerchantCalculationCallback merchantCalcEvent=merchantEvent.getMerchantCalculationNote();
				Order order =null;
				try {
					order= super.findOrderFromEvent(merchantCalcEvent.getShoppingCart());
					if(order==null){
						if(isLoggingError())
							logError("No Order could be found for processing");
						return ; // error response? Google doesn't care?
					}
					validateOrder(order,profile);

					Calculate calc=merchantCalcEvent.getCalculate();
					MerchantCalculationResults result=calculateMerchantResults(order, calc,profile);
					Document doc= this.getResultBuilder().convertToDOM(result);			
					String resultStr=getGoogleCheckoutManager().getCartBuilder().unmarshal(doc);
					DynamoHttpServletResponse response=request.getResponse();
					PrintWriter writer =response.getWriter();
					writer.print(resultStr);


				} catch (CommerceException e) {
					if(isLoggingError())
						logError(e);
				}
				// find atg order from privateData
				// load atg order
				//price as requested and create return result using MerchantCalculationResultBuilder
				catch (ProtocolException e) {
					if(isLoggingError())
						logError(e);
				} catch (IOException e) {
					if(isLoggingError())
						logError(e);
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
	 * validates order, makes sure state is INCOMPLETE, order belongs to right session etc
	 * 1 shippping group, 1 payment group, no softgoods etc
	 * @param profile 
	 * @param order
	 * @throws CommerceException 
	 */
	void validateOrder(Order pOrder, RepositoryItem profile) throws CommerceException {
		// TODO Auto-generated method stub

		if(!checkProfile(pOrder, profile))
			throw new CommerceException("Profile Id in Order does not match Id in Session");


	}

	/**
	 * performs merchant calculations.<br>
	 * For each address and each shipping method compute shipping costs.<br>
	 * @param calc
	 * @param profile 
	 * @throws CommerceException 
	 * @Order pOrder
	 */
	MerchantCalculationResults calculateMerchantResults(Order pOrder,Calculate calc, RepositoryItem profile) throws CommerceException {

		if(calc==null)
			return null;
		if(pOrder==null||profile==null)
			return null;


		AddressesType addresses=calc.getAddresses();
		MerchantCodeStringsType merchantCodes=calc.getMerchantCodeStrings();
		List codes=merchantCodes.getMerchantCodeString();
		ShippingType shipMethods=calc.getShipping();

		Map arrangedMerchantCodes=getGoogleCheckoutManager().getGoogleMerchantCalculationManager().arrangeMerchantCodes(codes);
		List couponCodes = (List) arrangedMerchantCodes.get(Constants.COUPON_KEY);
		getGoogleCheckoutManager().getGoogleMerchantCalculationManager().applyCoupons(profile.getRepositoryId(),couponCodes);
	
		List giftCertificates=(List) arrangedMerchantCodes.get(Constants.GIFT_CERT_KEY);;

		// for each shippping method and each address compute costs
		List anonAddressList=addresses.getAnonymousAddress();
		Iterator addressItr=anonAddressList.iterator();
		List shipMethodsList=shipMethods.getMethod();
		Iterator shipMethodItr=shipMethodsList.iterator();
		AnonymousAddress address=null;
		Method shipMethod=null;
		Result individualResult;
		ResultsType resultType;
		MerchantCalculationResults mcr=null;
		try {
			resultType = ((MerchantCalculationResultBuilder)getResultBuilder()).createMerchantCalculationResultsResultsType();

		} catch (ProtocolException e1) {
			if(isLoggingError())
				logError(e1);
			throw new CommerceException(e1);
		}


		while(addressItr.hasNext()){
			address=(AnonymousAddress) addressItr.next();
			while(shipMethodItr.hasNext()){
				shipMethod=(Method) shipMethodItr.next();
				individualResult =getGoogleCheckoutManager().getGoogleMerchantCalculationManager().performSingleMerchantResultCalculation((MerchantCalculationResultBuilder) getResultBuilder(),pOrder,profile,shipMethod,address,couponCodes,giftCertificates);
				individualResult.setShippingName(shipMethod.getName());
				resultType.getResult().add(individualResult);
			}

		}
		try {
			mcr=((MerchantCalculationResultBuilder)getResultBuilder()).createMerchantCalculationResults(resultType);
		} catch (ProtocolException e1) {
			if(isLoggingError())
				logError(e1);
			throw new CommerceException(e1);
		}
		if(isLoggingDebug())
			try {
				logDebug(" Merchant Calculation Result ="+this.getGoogleCheckoutManager().getCartBuilder().unmarshal(this.getResultBuilder().convertToDOM(mcr)));
			} catch (ProtocolException e) {
				if(isLoggingError())
					logError(e);
			}

			return mcr;
	}


	/**
	 * checks if profile from order and in sessoin match, sanity check
	 * @param order
	 * @return 
	 */
	boolean checkProfile(Order order,RepositoryItem profile) {
		if(isLoggingDebug())
			logDebug("Profile id from order ="+order.getProfileId());

		String profileId=profile.getRepositoryId();
		if(isLoggingDebug())
			logDebug("Profile id from session "+profileId);
		if(profileId!=order.getProfileId()){
			if(isLoggingError())
				logError("Profile Id from order does not match id from Session");
			return false;
		}
		return true;
	}



}
