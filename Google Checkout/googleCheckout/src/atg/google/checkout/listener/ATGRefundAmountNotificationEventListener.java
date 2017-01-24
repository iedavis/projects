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

import atg.commerce.CommerceException;
import atg.google.checkout.CallBackEventListener;

import com.google.checkout.sample.event.CallBackEvent;
import com.google.checkout.sample.event.RefundNotificationListener;
import com.google.checkout.sample.protocol.MerchantCalculationResultBuilder;
import com.google.checkout.sample.protocol.ProtocolException;

/**
 * Listener for Google Refund Amount Notificaions.
 * @author ssingh
 *
 */          
public class ATGRefundAmountNotificationEventListener extends
		CallBackEventListener implements RefundNotificationListener{

	
	/* 
	 * Not supported yet, would need integration with Commerce Assit
	 * (non-Javadoc)
	 * @see com.google.checkout.sample.event.CallBackListener#handleEvent(com.google.checkout.sample.event.CallBackEvent)
	 */
	public void handleEvent(CallBackEvent event) {
		if(isLoggingDebug())
			logDebug("received an event RefundAmountNotificationEvent"+event);
		try {
			returnErrorACKToGoogle();
		} catch (IOException e) {
			if(isLoggingError())
				logError(e);
		}
		return ;
	}

}
