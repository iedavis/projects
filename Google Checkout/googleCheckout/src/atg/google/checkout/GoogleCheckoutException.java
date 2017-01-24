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

import atg.commerce.CommerceException;

/**
 * @author ssingh
 *
 */
public class GoogleCheckoutException extends CommerceException {

	public GoogleCheckoutException (String msg){
		super(msg);
	}
	
	  /**
	   * Constructs a new CommerceException.
	   * @param pSourceException the initial exception which was the root
	   * cause of the problem
	   **/
	  public GoogleCheckoutException(Throwable pSourceException) {
	    super(pSourceException);
	  }
}
