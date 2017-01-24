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
import java.io.StringReader;

import javax.servlet.ServletException;

import org.xml.sax.InputSource;

import atg.commerce.CommerceException;
import atg.commerce.order.purchase.CartModifierFormHandler;
import atg.droplet.DropletException;
import atg.service.pipeline.PipelineResult;
import atg.service.pipeline.RunProcessException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

import com.google.checkout.sample.protocol.ProtocolException;
import com.google.checkout.schema._2.CheckoutRedirect;
import com.google.checkout.schema._2.impl.ErrorElementImpl;

/**
 * @author ssingh
 *
 */
public class GoogleCheckoutFormHandler extends CartModifierFormHandler {

	
	String postGoogleCartSuccessURL;
	String postGoogleCartErrorURL;
	
	

	public String getPostGoogleCartErrorURL() {
		return postGoogleCartErrorURL;
	}

	public void setPostGoogleCartErrorURL(String postGoogleCartErrorURL) {
		this.postGoogleCartErrorURL = postGoogleCartErrorURL;
	}

	public String getPostGoogleCartSuccessURL() {
		return postGoogleCartSuccessURL;
	}
	public void setPostGoogleCartSuccessURL(String postGoogleCartSuccessURL) {
		this.postGoogleCartSuccessURL = postGoogleCartSuccessURL;
	}
	

	String editURL;
	String continueURL;



	/**
	 * @return the continueURL
	 */
	public String getContinueURL() {
		return continueURL;
	}

	/**
	 * @param continueURL the continueURL to set
	 */
	public void setContinueURL(String continueURL) {
		this.continueURL = continueURL;
	}

	/**
	 * @return the editURL
	 */
	public String getEditURL() {
		return editURL;
	}

	/**
	 * @param editURL the editURL to set
	 */
	public void setEditURL(String editURL) {
		this.editURL = editURL;
	}
	
	
	GoogleCheckoutManager googleCheckoutManager;
	
	public GoogleCheckoutManager getGoogleCheckoutManager() {
		return googleCheckoutManager;
	}

	public void setGoogleCheckoutManager(GoogleCheckoutManager googleCheckoutManager) {
		this.googleCheckoutManager = googleCheckoutManager;
	}
	String validateGoogleOrderPipelineChain ;



	/**
	 * @return the validateGoogleOrderPipelineChain
	 */
	public String getValidateGoogleOrderPipelineChain() {
		return validateGoogleOrderPipelineChain;
	}

	/**
	 * @param validateGoogleOrderPipelineChain the validateGoogleOrderPipelineChain to set
	 */
	public void setValidateGoogleOrderPipelineChain(
			String validateGoogleOrderPipelineChain) {
		this.validateGoogleOrderPipelineChain = validateGoogleOrderPipelineChain;
	}
	
	/**
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @throws CommerceException 
	 */
	void prePostGoogleCart(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse ) throws CommerceException{
	}
	

	public String getRedirectURL (String response) throws GoogleCheckoutException{
		StringReader strReader = new StringReader(response);
		InputSource ipDataSrc= new InputSource(strReader);

		CheckoutRedirect result;
		Object obj;
		ErrorElementImpl error;
		try {
			obj = getGoogleCheckoutManager().getCartBuilder().parseToJAXB(ipDataSrc);
			if(obj instanceof ErrorElementImpl){
				error=(ErrorElementImpl)obj;
				addFormException(new DropletException(error.getErrorMessage()));
				if(isLoggingError())
					logError(error.getErrorMessage());
			}else if(obj instanceof CheckoutRedirect){				
				result=(CheckoutRedirect)obj;
				if(isLoggingDebug())
					logDebug("Redirect URL="+result.getRedirectUrl());
				return result.getRedirectUrl();
			}
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException (e);
		}

		
		return "";
	}
	
	/**
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return
	 * @throws CommerceException
	 */
	String postGoogleCart(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse ) throws CommerceException{
		
		
		String editURL=this.getGoogleCheckoutManager().prepareURL(pRequest.getRequestURI(),pRequest);		
		String continueURL=this.getGoogleCheckoutManager().prepareURL("/ondemand/index.jsp",pRequest);		

		if(isLoggingDebug())
			logDebug("Edit URL is"+editURL+" continue URL="+continueURL);
		
		return this.getGoogleCheckoutManager().postOrderToGoogle(getOrder(), this.getUserPricingModels().getShippingPricingModels(), this.getUserLocale(), this.getProfile(), null,editURL,continueURL);
		
	}
	
	/**
	 * 
	 * @param pRequest
	 * @param pResponse
	 */
	void postPostGoogleCart(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse ){
		
	}
	
	
	
	/**
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean handlePostGoogleCart(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse )
	throws IOException, ServletException{
		String redirectURL =null;
		try {
			this.prePostGoogleCart(pRequest,pResponse );
			runValidateGoogleOrderPipelinChain( pRequest,  pResponse);	
			if(this.getFormError()){				
				return checkFormRedirect(null ,this.getPostGoogleCartErrorURL(),pRequest,pResponse);
			}
			String response = postGoogleCart( pRequest,  pResponse );
			redirectURL=getRedirectURL(response);
			postPostGoogleCart(pRequest,pResponse);
		} catch (CommerceException e) {
			addFormException(new DropletException(e.getMessage()));
		} catch (RunProcessException e) {
			if(isLoggingError())
				logError(e);
			addFormException(new DropletException(ErrorCodes.GENERIC_SYSTEM_ERROR));
		}
		
		return checkFormRedirect(redirectURL ,this.getPostGoogleCartErrorURL(),pRequest,pResponse);
	}

	
	
	/**
	 * @param order
	 * @throws IOException 
	 * @throws ServletException 
	 * @throws RunProcessException 
	 */
	void runValidateGoogleOrderPipelinChain(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse)
		throws RunProcessException, ServletException, IOException {
	
		 PipelineResult result=runProcess(getValidateGoogleOrderPipelineChain(), getOrder(),  this.getUserPricingModels(),
                  this.getUserLocale(pRequest, pResponse),  getProfile(),  null,null);
		 if(isLoggingDebug())
			 logDebug("Pipeline Results ="+result);
		 this.processPipelineErrors(result);
		
	}

	
	/**
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	/*
	public boolean handleTestMerchantCodes(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse )
	throws IOException, ServletException{
		try{
			testPrepareOrder();
			getGoogleCheckoutManager().getGoogleMerchantCalculationManager().testMerchantCodes();
			Map arrangedMerchantCodes=getGoogleCheckoutManager().getGoogleMerchantCalculationManager().arrangeMerchantCodes(getGoogleCheckoutManager().getGoogleMerchantCalculationManager().getTestMerchantCodes());
			List coponCodes = (List) arrangedMerchantCodes.get(Constants.COUPON_KEY);
			
			getGoogleCheckoutManager().getGoogleMerchantCalculationManager().processCoupons(getProfile().getRepositoryId(),coponCodes);
			if(isLoggingDebug())
				logDebug(" ----- Pricing Order after Promotions "+" ORder ID ="+getOrder().getId());
			this.getGoogleCheckoutManager().getPricingTools().priceOrderSubtotal(getOrder(), getDefaultLocale(), getProfile(), null);
			this.getOrderManager().updateOrder(getOrder());
			this.getGoogleCheckoutManager().getGoogleMerchantCalculationManager().debugPrices((OrderImpl) getOrder());
			Map couponMap=getGoogleCheckoutManager().getGoogleMerchantCalculationManager().findAppliedCoupons(getOrder(), coponCodes);
			if(isLoggingDebug())
				logDebug(" Coupon Map applied to order ="+couponMap);
		} catch (Exception e) {
			logError(e);
		}
		
		return false;
	}

	*/

	
}
