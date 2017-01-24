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
import java.util.Locale;
import java.util.ResourceBundle;

import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.commerce.order.PipelineConstants;
import atg.core.util.ResourceUtils;
import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

/**
 * @author ssingh
 *
 */
public class ValidateSingleShippingGroup extends GenericService implements
		PipelineProcessor {

	
	public static final int SUCCESS = 1;
	public static final int ERROR = -1;

    static final String USER_MSGS_RES_NAME = "atg.commerce.order.UserMessages";
	static final String RESOURCE_NAME = "atg.commerce.order.OrderResources";
	static final Object ERROR_ONLY_ONE_SG_ALLOWED = "oneShippingGroupAllowed";

	  /** Resource Bundle **/
	private static ResourceBundle sResourceBundle =
	      ResourceBundle.getBundle(RESOURCE_NAME, atg.service.dynamo.LangLicense.getLicensedDefault());
	private static ResourceBundle sUserResourceBundle =
	      ResourceBundle.getBundle(USER_MSGS_RES_NAME, Locale.getDefault());
	
	int [] returnCodes= {SUCCESS,ERROR};
	
	/* (non-Javadoc)
	 * @see atg.service.pipeline.PipelineProcessor#getRetCodes()
	 */
	public int[] getRetCodes() {
		// TODO Auto-generated method stub
		return returnCodes;
	}

	/* (non-Javadoc)
	 * @see atg.service.pipeline.PipelineProcessor#runProcess(java.lang.Object, atg.service.pipeline.PipelineResult)
	 */
	public int runProcess(Object pParam, PipelineResult pResult) throws Exception {
		 HashMap map = (HashMap) pParam;
		 Order order = (Order) map.get(PipelineConstants.ORDER);
		 
		 if (order == null)
		      throw new InvalidParameterException(
		        ResourceUtils.getMsgResource("InvalidOrderParameter", RESOURCE_NAME, sResourceBundle));

		 if(order.getShippingGroupCount()>1){
		 
			 if(isLoggingDebug())
				 logDebug("Number of Shipping Groups is greater than 1, cannot proceed");
			 pResult.addError(ERROR_ONLY_ONE_SG_ALLOWED, "only 1 shipping group allowed");
			 return this.STOP_CHAIN_EXECUTION_AND_ROLLBACK ;
		 }
			
		
		return SUCCESS; 
		 
		
	}

}
