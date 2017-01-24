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

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;

import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.DynamoServlet;

/**
 * @author ssingh
 *
 */
public class GoogleCallbackDroplet extends DynamoServlet {

	GoogleMessageDispatcher googleMessageDispatcher;

	public GoogleMessageDispatcher getGoogleMessageDispatcher() {
		return googleMessageDispatcher;
	}

	public void setGoogleMessageDispatcher(
			GoogleMessageDispatcher googleMessageDispatcher) {
		this.googleMessageDispatcher = googleMessageDispatcher;
	}
	
	public void service (DynamoHttpServletRequest request, DynamoHttpServletResponse response)
		throws IOException, ServletException
	
	{
		if(isLoggingDebug()){
			logDebug("Request received from Google");
		}
		if(this.getGoogleMessageDispatcher()!=null){
			getGoogleMessageDispatcher().handleMessage(request, response);
		}
		if(isLoggingDebug())
			logDebug("Done Processing Request  from Google");
		
	}
	
	
	
}

