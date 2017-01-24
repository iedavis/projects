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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import atg.nucleus.Nucleus;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.ServletUtil;

/**
 * @author ssingh
 *
 */
public class GoogleCallbackServlet extends javax.servlet.http.HttpServlet 
implements javax.servlet.Servlet{
	
	final String CALL_BACK_DISPATCHER="/com/google/checkout/GoogleMessageDispatcher"; 
	 /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public GoogleCallbackServlet() {
		super();
    
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
	  doPost(request, response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws ServletException, IOException {
System.out.println("Message received, delegating to processor"+"request="+request);
		
//		Get the Dynamo Request/Response Pair
		DynamoHttpServletRequest dRequest =
			ServletUtil.getDynamoRequest(request);
		if(request==null){
			System.out.println("Request is null");
		}
				
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
		
		/*
    try {
      CallBackParser handler = CallBackParser.getInstance();

      // Retrieve the request body as an InputStream
      InputStream in = request.getInputStream();

      // Pass the request body to the CallBackParser#parseCallBack method
      handler.parseCallBack(in);
      PrintWriter out = response.getWriter();
      out.print("call back accepted");
    } catch (ProtocolException protocolEx) {
      
    }*/
	}   	  	    
}
