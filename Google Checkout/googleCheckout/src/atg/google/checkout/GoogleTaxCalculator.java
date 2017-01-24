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
package atg.google.checkout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import atg.commerce.CommerceException;
import atg.commerce.order.Order;
import atg.nucleus.GenericService;
import atg.repository.RepositoryItem;

import com.google.checkout.sample.protocol.ProtocolException;
import com.google.checkout.schema._2.DefaultTaxRule;
import com.google.checkout.schema._2.DefaultTaxTable;
import com.google.checkout.schema._2.USStateArea;

/**
 * @author ssingh
 *
 */
public class GoogleTaxCalculator extends GenericService {
	

	GoogleCheckoutManager googleCheckoutManager;

	/**
	 * @return the googleCheckoutManager
	 */
	public GoogleCheckoutManager getGoogleCheckoutManager() {
		return googleCheckoutManager;
	}


	/**
	 * @param googleCheckoutManager the googleCheckoutManager to set
	 */
	public void setGoogleCheckoutManager(GoogleCheckoutManager googleCheckoutManager) {
		this.googleCheckoutManager = googleCheckoutManager;
	}
	
	Map defaultTaxTable;

	/**
	 * @return the defaultTaxTable
	 */
	public Map getDefaultTaxTable() {
		return defaultTaxTable;
	}


	/**
	 * @param defaultTaxTable the defaultTaxTable to set
	 */
	public void setDefaultTaxTable(Map defaultTaxTable) {
		this.defaultTaxTable = defaultTaxTable;
	}


	/**
	 * returns the default tax table, goes thru the defaultTaxTable property and creates a tax rule for each given state.
	 * @param order
	 * @param profile
	 * @return 
	 * @throws CommerceException 
	 */
	DefaultTaxTable calculateTaxTables(Order order, RepositoryItem profile,String state, float taxRate,boolean shipTaxable) 
			throws GoogleCheckoutException {
		//TODO compute taxes from some table
		try {
			List ruleList=new ArrayList();		
			if((getDefaultTaxTable()==null)||(getDefaultTaxTable().size()==0)){
				if(isLoggingError())
					logError(" Default Tax Table is not computed");
				throw new GoogleCheckoutException(ErrorCodes.EMPTY_TAX_TABLE);
			}
			DefaultTaxRule taxRule=null;
			DefaultTaxTable taxTable=null;
			USStateArea usState=null;
			String stateKey=null,cost=null;
			Iterator itr= getDefaultTaxTable().keySet().iterator();
			float rate=0;
			while(itr.hasNext()){
				stateKey=(String) itr.next();
				cost=(String) getDefaultTaxTable().get(stateKey);
				usState=getGoogleCheckoutManager().getCartBuilder().createStateArea(stateKey);
				
				if(isLoggingDebug()){
					logDebug("Default tax rate: state="+stateKey+" cost="+cost);
				}
				rate=Float.parseFloat(cost);
				rate=(float) (rate*0.001);
				taxRule=getGoogleCheckoutManager().getCartBuilder().createDefaultTaxRule(usState,rate, shipTaxable);			
				ruleList.add(taxRule);
				
			}
			
			taxTable=getGoogleCheckoutManager().getCartBuilder().createDefaultTaxTable(ruleList);
			return taxTable;

		} catch (ProtocolException e) {
			throw new GoogleCheckoutException(e);
		}

	}
	

}
