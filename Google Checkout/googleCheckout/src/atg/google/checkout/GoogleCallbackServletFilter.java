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
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.ServletUtil;
import atg.userprofiling.Profile;

/**
 * @author ssingh
 *
 */
public class GoogleCallbackServletFilter implements Filter {

	final String CALL_BACK_DISPATCHER="/com/google/checkout/GoogleMessageDispatcher"; 
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println("init filter");
	}

	public void doFilter(ServletRequest request,
			ServletResponse response,
			FilterChain chain)
	throws IOException, ServletException
	{
		System.out.println("Message received, delegating to processor"+"request="+request);
		
//		Get the Dynamo Request/Response Pair
		DynamoHttpServletRequest dRequest =
			ServletUtil.getDynamoRequest(request);
		
		DynamoHttpServletResponse dResponse= dRequest.getResponse();

//		Resolve the Profile object
		GoogleMessageDispatcher gsd =
			(GoogleMessageDispatcher)dRequest.resolveName(CALL_BACK_DISPATCHER);

		if(gsd!=null){
			gsd.handleMessage(dRequest, dResponse);
		}else{
			System.out.println("Dispathcer is not configued");
		}
		System.out.println("Done processing");
		chain.doFilter(request,response);
		PrintWriter out = response.getWriter();
		out.print("call back accepted");
		return;
	}


}
