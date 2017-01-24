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

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PipelineConstants;
import atg.google.checkout.Constants;
import atg.nucleus.logging.ApplicationLoggingImpl;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

/**
 * @author ssingh
 *
 */
public class UpdateOrderState extends ApplicationLoggingImpl implements
		PipelineProcessor {

	  private final int SUCCESS = 1;
	  
//	-----------------------------------------------
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

	
	String orderState;

	/**
	 * @return the orderState
	 */
	public String getOrderState() {
		return orderState;
	}

	int orderStateInt;

	/**
	 * @return the orderStateInt
	 */
	public int getOrderStateInt() {
		return orderStateInt;
	}

	/**
	 * @param orderStateInt the orderStateInt to set
	 */
	public void setOrderStateInt(int orderStateInt) {
		this.orderStateInt = orderStateInt;
	}
	
	/**
	 * @param orderState the orderState to set
	 */
	public void setOrderState(String orderState) {
		this.orderState = orderState;
	}


	/* (non-Javadoc)
	 * @see atg.service.pipeline.PipelineProcessor#runProcess(java.lang.Object, atg.service.pipeline.PipelineResult)
	 */
	public int runProcess(Object pParam, PipelineResult result) throws Exception {
		
		
		HashMap map = (HashMap) pParam;
	    Order order = (Order) map.get(PipelineConstants.ORDER);	    
	    String googleState=(String) map.get(Constants.GOOGLE_STATE);
	    Integer googleStateInt =(Integer) map.get(Constants.GOOGLE_STATE_INT);
	    if(googleState==null || googleStateInt==null){
	    	logError(new Exception("NO state passed here"));
	    	return STOP_CHAIN_EXECUTION_AND_ROLLBACK;
	    }
	    if(isLoggingDebug()){
	    	logDebug(" ORder is "+order.getId());
	    	logDebug(" ORder State is "+googleState);
	    	logDebug(" ORder Int State is "+googleStateInt.intValue());
	    	
	    }
	    if(order==null)
	    	return SUCCESS;
		
		order.setStateDetail(googleState);
		order.setState(googleStateInt.intValue());
	    return SUCCESS;
		
	}
	
}
