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

/**
 * @author ssingh
 *
 */
public interface ErrorCodes {

	String GENERIC_SYSTEM_ERROR = "genericSystemError";
	String EMPTY_TAX_TABLE = "emptyErrorCodes";
	String InvalidParameters = "invalidParameters";
	String NULL_ORDER = "nullOrder";
	String NULL_SHOPPING_CART = "nullShoppingCart";
	String NULL_ITEM_PRIVATE_DATA = "nullItemPrivateData";
	String INVALID_SHIPPING_GROUP_TYPE = "invalidShippingGroup";
	String INVALID_PAYMENT_GROUP_TYPE = "invalidPaymentGroup";
	String ORDER_ALREADY_CREATED = "orderAlreadyCreated";

}
