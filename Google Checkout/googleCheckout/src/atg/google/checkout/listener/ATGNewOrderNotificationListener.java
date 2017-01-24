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
import java.util.Map;

import javax.transaction.TransactionManager;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.google.checkout.CallBackEventListener;
import atg.google.checkout.Constants;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.NewOrderNotificationEvent;
import com.google.checkout.sample.event.NewOrderNotificationListener;
import com.google.checkout.schema._2.NewOrderNotification;

/**
 * @author ssingh
 *
 */
public class ATGNewOrderNotificationListener extends CallBackEventListener implements NewOrderNotificationListener {


	/* (non-Javadoc)
	 * @see com.google.checkout.sample.event.CallBackListener#handleEvent(com.google.checkout.sample.event.CallBackEvent)
	 */
	public void handleEvent(CallBackEvent event) {
		TransactionManager tm = getTransactionManager();
		TransactionDemarcation td = new TransactionDemarcation ();
		try {
			try {
				td.begin (tm, td.REQUIRED);

				try {
					if(isLoggingDebug())
						logDebug("-------------->>>>>>>  received an event NewOrderNotification"+event);
					if((event==null)||(!(event instanceof NewOrderNotificationEvent))){			
						if(isLoggingError())
							logError("Event received is not instance of NewOrderNotificationEvent calss is "+event.getClass());
						returnErrorACKToGoogle();
					}

					NewOrderNotificationEvent orderNotification;
					orderNotification=(NewOrderNotificationEvent) event;

					NewOrderNotification orderNote = orderNotification.getNewOrderNote();		
					Order order;
					try {
						order = getGoogleCheckoutManager().getGoogleNewOrderNotificationManager().createNewGoolgleOrder(orderNote);
						if(order!=null){
							if(isLoggingDebug())
								logDebug("Created a new order with id ="+order.getId());
						}
						returnSuccessACKToGoogle();

					} catch (CommerceException e) {
						if(isLoggingError())
							logError(e);
						returnErrorACKToGoogle();
					}
					
					catch (Exception e) {
						if(isLoggingError())
							logError(e);
						returnErrorACKToGoogle();
					}

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

		return;

	}

}
