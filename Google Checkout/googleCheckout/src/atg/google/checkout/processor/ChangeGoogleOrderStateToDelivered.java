package atg.google.checkout.processor;

import java.util.HashMap;
import java.util.List;

import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.commerce.fulfillment.OrderFulfiller;
import atg.commerce.fulfillment.OrderFulfillmentTools;
import atg.commerce.fulfillment.PipelineConstants;
import atg.commerce.order.InvalidParameterException;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.states.OrderStates;
import atg.commerce.states.PaymentGroupStates;
import atg.commerce.states.ShippingGroupStates;
import atg.core.util.ResourceUtils;
import atg.google.checkout.Constants;
import atg.google.checkout.GoogleTransactionManager;
import atg.nucleus.logging.ApplicationLoggingImpl;
import atg.repository.RepositoryItem;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

/**
 * @author ssingh
 *
 */
public class ChangeGoogleOrderStateToDelivered extends ApplicationLoggingImpl
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
		 
		// getGoogleTransactionManager().markDelivered(pOrder);
		 
		
		return SUCCESS;
	}

	GoogleTransactionManager googleTransactionManager;


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
	
}
