package atg.google.checkout.fulfillment.messaging;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import atg.commerce.fulfillment.ModifyOrderNotification;
import atg.dms.patchbay.MessageSink;
import atg.google.checkout.GoogleOrderModificationNotifcationHandler;
import atg.nucleus.GenericService;

/**
 * A class to listen for Fulfillment messages. This will listen for activities happening on the fulfillment side and
 * update Google order at relevant times. 
 * Tried to use OrderFulfillerModificationHandler, fits almost perfectly here but OrderFulfillerModificationHandler is closely 
 * tied to fulfillment code, we expect this class to run at non-fulfillment servers, makes things a little complicated.
 * So, pretty much writing another handler;
 * @author ssingh
 *
 */
public class FulfillmentMessageSink extends GenericService implements
		MessageSink {


	GoogleOrderModificationNotifcationHandler googleOrderModificationNotifcationHandler;

	/**
	 * @return the googleOrderModificationNotifcationHandler
	 */
	public GoogleOrderModificationNotifcationHandler getGoogleOrderModificationNotifcationHandler() {
		return googleOrderModificationNotifcationHandler;
	}

	/**
	 * @param googleOrderModificationNotifcationHandler the googleOrderModificationNotifcationHandler to set
	 */
	public void setGoogleOrderModificationNotifcationHandler(
			GoogleOrderModificationNotifcationHandler googleOrderModificationNotifcationHandler) {
		this.googleOrderModificationNotifcationHandler = googleOrderModificationNotifcationHandler;
	}

	
	/* (non-Javadoc)
	 * @see atg.dms.patchbay.MessageSink#receiveMessage(java.lang.String, javax.jms.Message)
	 */
	public void receiveMessage(String portName, Message message) throws JMSException {
		if(isLoggingDebug()){
			logDebug("Received a message "+message+" on Port "+portName);
			logDebug(" received message class"+message.getClass());
		}
			Object obj=((ObjectMessage)message).getObject();
			if(obj!=null)
				if(isLoggingDebug())
					logDebug("Class of obj is "+obj.getClass());
			
			if(obj instanceof ModifyOrderNotification){
				getGoogleOrderModificationNotifcationHandler().handleModifyOrderNotification(portName,(ObjectMessage) message);
			}
		

	}
	


}
