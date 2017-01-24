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
package atg.google.checkout.fulfillment.messaging;

import java.util.List;

import atg.commerce.fulfillment.Modification;
import atg.commerce.fulfillment.ModifyOrder;
import atg.commerce.fulfillment.ModifyOrderNotification;
import atg.commerce.messaging.SourceSinkTemplate;
import atg.dms.patchbay.MessageSource;
import atg.dms.patchbay.MessageSourceContext;
import atg.nucleus.GenericService;

/**
 * @author ssingh
 *
 */
public class FulfillmentMessageSource extends SourceSinkTemplate implements
MessageSource {


//	-------------------------------------
	  /**
	   * <p> This method will send an ModifyOrderNotification message </p>
	   *
	   * @param pOrderId The order that was modified
	   * @param pModificationList The modifications that were made
	   * @param pModifyOrder The request that initiated these modifications.  This is null
	   *                     if these changes were not requested by an incoming message.
	   * @param pMessageSource The source of the message.
	   * @param pMessagePort the port to send the message out on
	   * @param pOriginalMessage if this message is being forwarded, this is the original
	   *                         message that was received.  If method call is creating the
	   *                         original message, this parameter should be null
	   **/
	  public void sendModifyOrderNotification(String pOrderId,
	                                          List pModificationList,
	                                          ModifyOrder pModifyOrder,
	                                          String pMessagePort,
	                                          String id,
	                                          ModifyOrderNotification pOriginalMessage)
	    throws javax.jms.JMSException
	  {
	    // if modifications have not been made, no need to send message
	    if (pModificationList.size() > 0)
	    {
	      // we are assuming the modifications have already been made.  We just
	      // need to construct the message with the appropriate Modification []
	      Modification[] mods = (Modification[])
	        pModificationList.toArray(new Modification[pModificationList.size()]);
	      
	      ModifyOrderNotification mon = new ModifyOrderNotification();
	      mon.setOrderId(pOrderId);
	      mon.setModifications(mods);
	      mon.setSource(this.getMessageSourceName());
	      mon.setId(id);

	      if(pModifyOrder != null) {
	        mon.setModifyOrderId(pModifyOrder.getId());
	        mon.setModifyOrderSource(pModifyOrder.getSource());
	      }
	      
	      // no user
	      mon.setUserId(null);

	      if(pOriginalMessage == null) {
	        mon.setOriginalSource(this.getMessageSourceName());
	        mon.setOriginalId(mon.getId());
	        mon.setOriginalUserId(mon.getUserId());      
	      } else {
	        mon.setOriginalSource(pOriginalMessage.getOriginalSource());
	        mon.setOriginalId(pOriginalMessage.getOriginalId());
	        mon.setOriginalUserId(pOriginalMessage.getOriginalUserId());      
	      }

	      this.sendCommerceMessage(mon, pMessagePort);

	    } // else ignore

	  } // sendModif


}
