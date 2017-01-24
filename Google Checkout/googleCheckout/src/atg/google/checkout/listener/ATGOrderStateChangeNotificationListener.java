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

import javax.transaction.TransactionManager;

import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.google.checkout.CallBackEventListener;
import atg.google.checkout.GoogleCheckoutException;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.OrderStateChangeNotificationEvent;
import com.google.checkout.sample.event.OrderStateChangeNotificationListener;
import com.google.checkout.schema._2.OrderStateChangeNotification;

/**
 * Class to receive and process Order State Change Notifications
 * @author ssingh
 *
 */
public class ATGOrderStateChangeNotificationListener extends CallBackEventListener implements OrderStateChangeNotificationListener{


	/* 
	 * Handlers order state change events.
	 * We only care about Chargable and Cancelled orders.
	 * Other events are ignored.
	 * (non-Javadoc)
	 * @see com.google.checkout.sample.event.CallBackListener#handleEvent(com.google.checkout.sample.event.CallBackEvent)
	 */
	public void handleEvent(CallBackEvent event) {

		TransactionManager tm = getTransactionManager();
		TransactionDemarcation td = new TransactionDemarcation ();
		try {
			try {
				td.begin (tm, td.REQUIRED);

				if(isLoggingDebug()){
					logDebug("received an event OrderStateChangeNotification");
				}
				if(event==null)
					return;
				if(isLoggingDebug())
					logDebug("Event class is "+event.getClass());
				try {
					OrderStateChangeNotificationEvent orderStateChangeEvent=(OrderStateChangeNotificationEvent)event;
					OrderStateChangeNotification notification= orderStateChangeEvent.getOrderStateChangeNote();

					getGoogleCheckoutManager().getGoogleOrderStateChangeNotificationManager().processOrderStateChangeNote(notification);
				} catch (GoogleCheckoutException e) {
					try {
						returnErrorACKToGoogle();
						if(isLoggingError())
							logError(e);
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

}
