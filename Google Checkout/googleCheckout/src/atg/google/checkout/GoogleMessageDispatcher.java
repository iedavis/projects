/*************************************************
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*************************************************/

package atg.google.checkout;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import atg.nucleus.GenericService;
import atg.nucleus.ServiceMap;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

import com.google.checkout.sample.event.CallBackListener;
import com.google.checkout.sample.event.ChargeNotificationListener;
import com.google.checkout.sample.event.ChargebackNotificationListener;
import com.google.checkout.sample.event.MerchantCalculationCallbackListener;
import com.google.checkout.sample.event.NewOrderNotificationListener;
import com.google.checkout.sample.event.OrderStateChangeNotificationListener;
import com.google.checkout.sample.event.RefundNotificationListener;
import com.google.checkout.sample.event.RiskInformationNotificationListener;
import com.google.checkout.sample.protocol.CallBackParser;
import com.google.checkout.sample.protocol.ProtocolException;

/**
 * @author ssingh
 *
 */
public class GoogleMessageDispatcher extends GenericService {

	
	CallBackParser callbackParser;

	public CallBackParser getCallbackParser() {
		return callbackParser;
	}

	public void setCallbackParser(CallBackParser callbackParser) {
		this.callbackParser = callbackParser;
	}	
	
	ServiceMap eventHandlers;
	public ServiceMap getEventHandlers() {
		return eventHandlers;
	}

	public void setEventHandlers(ServiceMap eventHandlers) {
		this.eventHandlers = eventHandlers;
	}
	
	public void doStartService(){
		try {
			setCallbackParser(CallBackParser.getInstance());
			initHandlers();
			
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
		}
		
	}
	
	/**
	 * 
	 */
	private void initHandlers() {
		if(this.eventHandlers==null)
		{
			if(isLoggingError())
			logError("Event handlers are not configued");
			return;
		}
		Set keys = eventHandlers.keySet();
		Iterator itr = keys.iterator();
		String eventName;
		CallBackListener listener;
		Class eventClass;
		
		while(itr.hasNext()){
			eventName=(String)itr.next();
			listener=(CallBackListener) eventHandlers.get(eventName);
			if(listener==null){
				if(isLoggingError())
					logError("Event Handler is not configured for event name"+eventName);
				continue;
			}
			// have to hardcode it, google has different methods for each listener, pretty lame
			// should have kept it generic, easy to extend
			if(eventName.equals("com.google.checkout.sample.event.ChargeAmountNotification")){
				 getCallbackParser().addCallBackListener((ChargeNotificationListener)listener);
			}
			else if(eventName.equals("com.google.checkout.sample.event.ChargebackAmountNotification")){
				 getCallbackParser().addCallBackListener((ChargebackNotificationListener)listener);
			}
			else if(eventName.equals("com.google.checkout.sample.event.NewOrderNotification")){
				 getCallbackParser().addCallBackListener((NewOrderNotificationListener)listener);
			}
			else if(eventName.equals("com.google.checkout.sample.event.OrderStateChangeNotification")){
				 getCallbackParser().addCallBackListener((OrderStateChangeNotificationListener)listener);
			}
			else if(eventName.equals("com.google.checkout.sample.event.RefundAmountNotification")){
				 getCallbackParser().addCallBackListener((RefundNotificationListener)listener);
			}
			else if(eventName.equals("com.google.checkout.sample.event.RiskInformationNotification")){
				 getCallbackParser().addCallBackListener((RiskInformationNotificationListener)listener);
			}
			else if(eventName.equals("com.google.checkout.sample.event.MerchantCalculationCallback")){
				 getCallbackParser().addCallBackListener((MerchantCalculationCallbackListener)listener);
			}else{
				if(isLoggingError())
					logError(" Cannot add handler for "+eventName+" No Such event");
			}
			
			
		}
		
		
	}

	
	public void handleMessage(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse )
		throws IOException, ServletException
	{

		if(isLoggingDebug())
			logDebug("RECEIVED message from Google, start processing");
	    try {
	      CallBackParser handler = this.getCallbackParser();

	      if(handler==null){
	    	  if(isLoggingError())
	    		  logError("Handler is null");
	    	  pResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	      }
	    	  
		  if(isLoggingDebug())
				logDebug("RECEIVED message from Google, delegating to handler"+handler.getClass());
	      // Retrieve the request body as an InputStream
	      InputStream in = pRequest.getInputStream();

	      // Pass the request body to the CallBackParser#parseCallBack method
	      handler.parseCallBack(in);	      
	    } catch (ProtocolException protocolEx) {
	      if(isLoggingError())
	    	  logError(protocolEx);
	      pResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    }
		if(isLoggingDebug())
			logDebug("RECEIVED message from Google, done processing");
		
	}



}
