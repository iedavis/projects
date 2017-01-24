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

import atg.nucleus.naming.ComponentName;

/**
 * @author ssingh
 *
 */
public class Constants {

	public static final String PROPERTY_DESCRIPTION = "description";
	public static final String PROPERTY_NAME = "displayName";
	public static final String MERCHANT_PRIVATE_DATA_TAG = "merchant-private-data";
	public static final String ATG_PRIVATE_DATA_TAG = "atgPrivateData";
	public static final String ORDER_TAG = "atgOrder";
	public static final String ORDER_ID = "order_id";
	public static final String SHOPPINGCART_TAG = "shopping-cart";
	public static final String SHOPPING_CART_PATH = "/atg/commerce/ShoppingCart";
	public static final String MERCHANT_CALCULATIONS_RESULTS = "merchant-calculation-results";
	public static final String RESULTS = "results";
	public static final String PROFILE_PATH = "/atg/userprofiling/Profile";
	public static final String CART_MODIFIER_FORMHANDLER_PATH = "/atg/commerce/order/purchase/CartModifierFormHandler";
	public static final Object COUPON_KEY = "COUPONS";
	public static final Object GIFT_CERT_KEY = "GIFTCERTIFICATES";
	public static final String PROMOTION_PROPERTY = "promotion";
	public static final String MERCHANT_SHIPPING_CALCULATION_ELEMENT = "merchant-calculated-shipping";
	public static final String GOOGLE_ITEM_ADJUSTMENT_DECRIPTION = "Item level Google Adjustment";
	public static final String GOOGLE_SHIPPPING_ADJUSTMENT = "Shipping Google Adjustment";
	public static final String PROCESS_GOOGLE_ORDER = "processGoogleOrder";
	public static final String PROFILE_TAG = "atgProfile";
	public static final String PROFILE_ID = "profile_id";
	public static final String DUMMY_CC_NUMBER = "4111111111111111";
	public static final String DUMMY_CC_TYPE = "VISA";
	public static final String DUMMY_CC_EXPIRATION_MONTH = "12";
	public static final String DUMMY_CC_EXPIRATION_DAY = "12";
	public static final String DUMMY_CC_EXPIRATION_YEAR = "2050";
	public static final String GOOGLE_ORDER_TOTAL = "GOOGLE ORDER TOTAL";
	public static final int GOOGLE_NEW_ORDER_NOTIFICATION_STATE_INT = 5000;
	public static final int GOOGLE_CHARGEABLE_NOTIFICATION_STATE_INT = 5001;
	public static final String GOOGLE_NEW_ORDER_NOTIFICATION_STATE="google_new_order_notification";
	public static final String GOOGLE_CHARGEABLE_NOTIFICATION_STATE="google_charge_order_notification";
	public static final String PROPERTY_COUPON_MESSAGE = "displayName";
	public static final String PROPERTY_PROMOTION = "promotion";

	public static final Object NOTIFICATION_SUCCESS_ACK = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><notification-acknowledgment xmlns=\"http://checkout.google.com/schema/2\"/>";
	public static final Object GOOGLE_STATE = "GOOGLE_STATE";
	public static final Object GOOGLE_STATE_INT = "GOOGLE_STATE_INT";
	public static final String PROPERTY_GOOGLE_ORDER_ID = "googleOrderId";
	public static final String TAX_TABLES_TAG = "tax-tables";
	public static final String MERCHANT_CALCULATED_TAG = "merchant-calculated";
	public static final String ORDER_ITEM_DESCRIPTOR = "order";
	public static final String PROPERTY_ORDER_ID = "id";
	public static final String NULL_GOOGLE_ORDER = "nullGoogleOrder";
	public static final String PROPERTY_RISK_INFORMATION_RECEIVED = "riskInformationReceived";
	public static final String SUBMIT_GOOGLE_ORDER_PIPELINE_CHAIN = "submitGoogleOrderChain";
	public static final String UPDATE_GOOGLE_ORDER_STATE_PIPELINE_CHAIN = "updateGoogleOrdeStatePipline";
	
	
	// Google order states
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_REVIEWING = "REVIEWING";
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_CHARGEABLE = "CHARGEABLE";
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_CHARGING = "CHARGING";
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_CHARGED = "CHARGED";
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_PAYMENT_DECLINED = "PAYMENT_DECLINED";
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_CANCELLED = "CANCELLED";
	public static final String GOOGLE_ORDER_FINANCIAL_STATE_CANCELLED_BY_GOOGLE = "CANCELLED_BY_GOOGLE";
	public static final String PROPERTY_AMOUNT_DEBITED = "amountDebited";
	public static final String AWAITING_GOOGLE_CHARGE = "AWAITING_GOOGLE_CHARGE";
	public static final int AWAITING_GOOGLE_CHARGE_INT = 1000;
	public static final String GOOGLE_SETTLE_ERROR = "Error Setting with Google";
	public static final String ITEM_TAG = "item";
	public static final Object ITEM_NAME_TAG = "item-name";
	public static final String MERCHANT_ITEM_ID_TAG = "merchant-item-id";
	public static final String DELIMITER = "||";
	public static final String ITEM_DESCRIPTION_TAG = "item-description";
	public static final String MERCHANT_PRIVATE_ITEM_TAG = "merchant-private-item-data";
	public static final String MERCHANT_PRIVATE_ITEM_SKUID_TAG = "skuId";
	public static final String MERCHANT_PRIVATE_ITEM_PRODUCTID_TAG = "productId";
	
	public static final String SKU_ITEM_DESC = "sku";
	public static final String PRODUCT_ITEM_DESC = "product";
	public static final String MERCHANT_PRIVATE_ITEM_ATG_TAG = "atgPrivateItemData";
	public static final String PROPERTY_GIFT_AMOUNT_REMAINING = "amountRemaining";
	public static final String GIFT_CERTIFICATE_PAYMENT_GROUP = "giftCertificate";
	public static final Object PROPERTY_STATE = "state";
	public static final String DEFAULT_CANCEL_REASON = "ATG CANCELLED";
	public static final String DEFAULT_CANCEL_COMMENT = "ATG MERCHANT CANCELLED";
	public static final Object GOOGLE_ORDER_FINANCIAL_CANCELLED_BY_GOOGLE = "CANCELLED_BY_GOOGLE";
	public static final String PROPERTY_GIFT_AMOUNT_AUTHORIZED = "amountAuthorized";
	
	
	
	
	
	
}
