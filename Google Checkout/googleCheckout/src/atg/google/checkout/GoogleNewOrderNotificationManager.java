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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atg.commerce.CommerceException;
import atg.commerce.claimable.ClaimableException;
import atg.commerce.claimable.ClaimableManager;
import atg.commerce.claimable.InvalidParameterException;
import atg.commerce.order.CommerceItem;
import atg.commerce.order.CommerceItemManager;
import atg.commerce.order.CreditCard;
import atg.commerce.order.GiftCertificate;
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.OrderManager;
import atg.commerce.order.PaymentGroup;
import atg.commerce.order.PaymentGroupManager;
import atg.commerce.order.ShippingGroup;
import atg.commerce.pricing.ItemPriceInfo;
import atg.commerce.pricing.OrderPriceInfo;
import atg.commerce.pricing.PricingAdjustment;
import atg.commerce.pricing.ShippingPriceInfo;
import atg.commerce.pricing.TaxPriceInfo;
import atg.nucleus.GenericService;
import atg.payment.creditcard.CreditCardStatusImpl;
import atg.payment.giftcertificate.GiftCertificateStatusImpl;
import atg.repository.RepositoryException;

import com.google.checkout.schema._2.Address;
import com.google.checkout.schema._2.AnyType;
import com.google.checkout.schema._2.BuyerMarketingPreferences;
import com.google.checkout.schema._2.CouponAdjustment;
import com.google.checkout.schema._2.GiftCertificateAdjustment;
import com.google.checkout.schema._2.Item;
import com.google.checkout.schema._2.Money;
import com.google.checkout.schema._2.NewOrderNotification;
import com.google.checkout.schema._2.OrderAdjustment;
import com.google.checkout.schema._2.ShoppingCart;
import com.google.checkout.schema._2.OrderAdjustment.MerchantCodesType;
import com.google.checkout.schema._2.OrderAdjustment.ShippingType;
import com.google.checkout.schema._2.Result.MerchantCodeResultsType.CouponResult;
import com.google.checkout.schema._2.Result.MerchantCodeResultsType.GiftCertificateResult;
import com.google.checkout.schema._2.ShoppingCart.ItemsType;
import com.google.checkout.schema._2.impl.ItemImpl;

/**
 * @author ssingh
 *
 */
public class GoogleNewOrderNotificationManager extends GenericService{


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

	
	/**
	 * creates new order and copies all info from Google order to ATG order,
	 * no pricing is done, all prices are frozen
	 * @return
	 * @throws CommerceException 
	 */
	public Order createNewGoolgleOrder(NewOrderNotification orderNote) throws CommerceException {

		if(getGoogleCheckoutManager()==null)
		{
			if(isLoggingError())
				logError("Google Checkout Manager is null");
			return null;
		}

		com.google.checkout.schema._2.Address billAddress=orderNote.getBuyerBillingAddress();
		com.google.checkout.schema._2.Address shipAddress=orderNote.getBuyerShippingAddress();
		String googleOrderId=orderNote.getGoogleOrderNumber();
		Order prevOrder=getGoogleCheckoutManager().getGoogleOrder(googleOrderId);
		if(prevOrder!=null)
			return prevOrder;
		
		long buyerId = orderNote.getBuyerId();
		OrderAdjustment adjustment=orderNote.getOrderAdjustment();
		BuyerMarketingPreferences buyerMarketingPreference = orderNote.getBuyerMarketingPreferences();
		ShoppingCart cart =orderNote.getShoppingCart();
		AnyType privateData=cart.getMerchantPrivateData();
		Map orderProfileMap=getGoogleCheckoutManager().findATGOrderAndProfileIdFromEventPrivateData(privateData);
		String profileId=(String) orderProfileMap.get(Constants.PROFILE_ID);
		String orderId=(String) orderProfileMap.get(Constants.ORDER_ID);
		
		if(isLoggingDebug()){
			logDebug(" Google Order Id is:"+googleOrderId);
			logDebug(" Buyer id is"+ buyerId);
			logDebug(" Order adjustment is"+adjustment.getAdjustmentTotal());
			logDebug(" Private Data is "+privateData);
			logDebug(" orderId is"+orderId);
			logDebug(" profileId is"+profileId);
		}

		Order order = this.getGoogleCheckoutManager().getOrderManager().createOrder(profileId);
		((OrderImpl)order).getRepositoryItem().setPropertyValue(Constants.PROPERTY_GOOGLE_ORDER_ID, googleOrderId);
		copyCommerceItems(order,cart);
		copyPriceInformation(order,orderNote,adjustment);
		setShippingInformation(order,shipAddress,orderNote.getOrderAdjustment().getShipping());
		double giftCertAmt=applyMerchantCodes(order,orderNote);
		double remainingAmt=order.getPriceInfo().getTotal()-giftCertAmt;
		if(remainingAmt<=0)
			remainingAmt=0;
		if(isLoggingDebug())
			logDebug("Amt Applied towards Gift Certf ="+giftCertAmt+" Remaining for CC ="+remainingAmt);
		
		// since GC are enough to cover order total, skip Credit Card
		if(remainingAmt>0)
			setBillingInformation(order,orderNote,remainingAmt);
		
		HashMap param=new HashMap();
		param.put(Constants.GOOGLE_STATE, Constants.GOOGLE_NEW_ORDER_NOTIFICATION_STATE);
		param.put(Constants.GOOGLE_STATE_INT, new Integer(Constants.GOOGLE_NEW_ORDER_NOTIFICATION_STATE_INT));
		getGoogleCheckoutManager().runProcessGoogleOrderPipelineChain(order,param);
		try {
			getGoogleCheckoutManager().getOrderManager().updateOrder(order);
		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		if(isLoggingDebug())
			logDebug(" Order state is "+order.getState()+" Order state detail ="+order.getStateDetail());
		return order;
	}

	

	/**
	 * @param order
	 * @param orderNote
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	double applyMerchantCodes(Order order, NewOrderNotification orderNote) throws GoogleCheckoutException {
		
		if(order==null|| orderNote==null)
			return 0;
		
		OrderAdjustment adjustment=orderNote.getOrderAdjustment();
		MerchantCodesType merchantCodes = adjustment.getMerchantCodes();
		List giftCertificateAdjustmentOrCouponAdjustment = merchantCodes.getGiftCertificateAdjustmentOrCouponAdjustment();
		if(giftCertificateAdjustmentOrCouponAdjustment==null)
			return 0;
		MerchantCodesType codeType=null;
		CouponResult coupon=null;
		GiftCertificateResult giftCertf=null;
		Object temp=null;
		double giftAmt=0;
		Iterator itr = giftCertificateAdjustmentOrCouponAdjustment.iterator();
		while(itr.hasNext()){
			// dunno what class it is going to be
			temp=itr.next();
			if(isLoggingDebug())
					logDebug("Class of returned Merchant code is "+temp.getClass()+" value ="+temp);
			if(temp instanceof CouponAdjustment){
				applyCouponCodes(order,orderNote,(CouponAdjustment)temp);
				
			}
			else if(temp instanceof GiftCertificateAdjustment){
				giftAmt+=applyGiftCertificate(order,orderNote,(GiftCertificateAdjustment)temp);
			}
		
		}
		if(isLoggingDebug())
			logDebug(" Total Gift Certificate Amt applied to order ="+giftAmt);

		return giftAmt;
	}

	/**
	 * Gift certificates need to be added as Payment Groups
	 * @param order
	 * @param orderNote
	 * @param giftAmtAlreadyApplied 
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	double applyGiftCertificate(Order order, NewOrderNotification orderNote, GiftCertificateAdjustment giftCertAdj) throws GoogleCheckoutException {
		
		Money calcAmount = giftCertAdj.getCalculatedAmount();
		String code = giftCertAdj.getCode();
		String message = giftCertAdj.getMessage();
		Money appliedAmount = giftCertAdj.getAppliedAmount();
		
		if(isLoggingDebug()){
			logDebug("Received a Gift Certificate "+code+" for amt "+appliedAmount+" Calculated Amt ="+calcAmount);
		}		
		ClaimableManager claimableManager=getGoogleCheckoutManager().getGoogleMerchantCalculationManager().getClaimableManager();
		OrderManager orderManager=getGoogleCheckoutManager().getOrderManager();
		PaymentGroupManager pgManager=orderManager.getPaymentGroupManager();
		try {
			claimableManager.authorizeClaimableGiftCertificate(code,appliedAmount.getValue().doubleValue());
			GiftCertificate gc = (GiftCertificate) pgManager.createPaymentGroup(Constants.GIFT_CERTIFICATE_PAYMENT_GROUP);
			gc.setAmount(appliedAmount.getValue().doubleValue());
			gc.setAmountAuthorized(appliedAmount.getValue().doubleValue());
			gc.setCurrencyCode(appliedAmount.getCurrency());
			gc.setGiftCertificateNumber(code);			
			GiftCertificateStatusImpl authStatus=new GiftCertificateStatusImpl();
			
			authStatus.setAmount(getLogListenerCount());
			authStatus.setTransactionId(orderNote.getSerialNumber());
			gc.getAuthorizationStatus().add(authStatus);
			
			pgManager.addPaymentGroupToOrder(order, gc);
			orderManager.addOrderAmountToPaymentGroup(order, gc.getId(),appliedAmount.getValue().doubleValue() );
			return appliedAmount.getValue().doubleValue();
			
		} catch (InvalidParameterException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (ClaimableException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (RepositoryException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
	}

	/**
	 * Coupons can only be applied at order level, since we don't know what items will get affected.
	 * This is ok because it is only for auditing purposes, Google sends us the prices and we copy 
	 * them over and freeze them.
	 * @param order
	 * @param orderNote
	 */
	void applyCouponCodes(Order order, NewOrderNotification orderNote,CouponAdjustment couponAdj) {
		
		Money appliedAmount = couponAdj.getAppliedAmount();
		String code = couponAdj.getCode();
		String message = couponAdj.getMessage();
		
		OrderPriceInfo opi=order.getPriceInfo();
		
		List adjustments=opi.getAdjustments();
		PricingAdjustment adjustment=new PricingAdjustment(); 
		
		if(isLoggingDebug())
			logDebug("Adding coupon code"+code+" as and adjustment to order for Amt"+appliedAmount.getValue().doubleValue());
		adjustment.setQuantityAdjusted(1); // frankly, we dont know the qty, so just hardcode it
		adjustment.setTotalAdjustment(appliedAmount.getValue().doubleValue());
		adjustment.setAdjustmentDescription(code);
		adjustments.add(adjustment);
	}

	/**
	 * @param order
	 * @param orderNote
	 * @throws GoogleCheckoutException 
	 */
	void setBillingInformation(Order order, NewOrderNotification orderNote,double amt) throws GoogleCheckoutException {
		if(order==null || orderNote==null){
			return;
		}
		
		setBillingAddress(order,orderNote.getBuyerBillingAddress());
		List pgs=order.getPaymentGroups();
		Iterator pgItr=pgs.iterator();
		PaymentGroup pg=null;
		CreditCard cc=null;
		while(pgItr.hasNext()){
			pg=(PaymentGroup) pgItr.next();
			if(pg instanceof CreditCard){
				cc=(CreditCard) pg;
			}
		}
	
		if(cc==null){
			if(isLoggingError())
				logError("Payment Group  is null");
		}
	
		OrderAdjustment adjustment=orderNote.getOrderAdjustment();
		double total=orderNote.getOrderTotal().getValue().doubleValue();		
		String currency=orderNote.getOrderTotal().getCurrency();
		double shipCost=adjustment.getShipping().getMerchantCalculatedShippingAdjustment().getShippingCost().getValue().doubleValue();
		double tax=adjustment.getTotalTax().getValue().doubleValue();
		
		cc.setCreditCardNumber(Constants.DUMMY_CC_NUMBER);
		cc.setCreditCardType(Constants.DUMMY_CC_TYPE);
		cc.setExpirationMonth(Constants.DUMMY_CC_EXPIRATION_MONTH);
		cc.setExpirationDayOfMonth(Constants.DUMMY_CC_EXPIRATION_DAY);
		cc.setExpirationYear(Constants.DUMMY_CC_EXPIRATION_YEAR);
		cc.setCurrencyCode(currency);
		
		cc.setAmountAuthorized(amt);
		cc.setAmount(amt);
		CreditCardStatusImpl authStatus = new CreditCardStatusImpl();
		authStatus.setAmount(amt);
		cc.getAuthorizationStatus().add(authStatus);
		
		OrderManager om =getGoogleCheckoutManager().getOrderManager();
		try {
			om.addRemainingOrderAmountToPaymentGroup(order, cc.getId());
		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		
		if(isLoggingDebug())
			logDebug(" CC Amt is "+order.getPriceInfo().getTotal());
		
	}



	/**
	 * @param order
	 * @param shipAddress
	 * @param shipping
	 * @throws GoogleCheckoutException 
	 */
	void setShippingInformation(Order order, Address shipAddress, ShippingType shipping) throws GoogleCheckoutException {
		ShippingGroup sg=(ShippingGroup) order.getShippingGroups().get(0);
		if(sg==null){
			if(isLoggingError())
				logError("Shipping Group is null");
		}
		if(!(sg instanceof HardgoodShippingGroup)){
			throw new GoogleCheckoutException(ErrorCodes.INVALID_SHIPPING_GROUP_TYPE);
		}
		String shipMethod=shipping.getMerchantCalculatedShippingAdjustment().getShippingName();
		String shipCurrency=shipping.getMerchantCalculatedShippingAdjustment().getShippingCost().getCurrency();
		double shipCost=shipping.getMerchantCalculatedShippingAdjustment().getShippingCost().getValue().doubleValue();
		
		((HardgoodShippingGroup)sg).setShippingMethod(shipMethod);
		ShippingPriceInfo priceInfo = ((HardgoodShippingGroup)sg).getPriceInfo();
		if(priceInfo==null){
			if(isLoggingError())
				logError("Shipping price info is null");			
		}
		priceInfo.getAdjustments();
		PricingAdjustment pa = new PricingAdjustment();
		pa.setAdjustmentDescription(Constants.GOOGLE_SHIPPPING_ADJUSTMENT);
		pa.setTotalAdjustment(shipCost);
		priceInfo.getAdjustments().add(pa);
		priceInfo.setAmount(shipCost);
		priceInfo.setCurrencyCode(shipCurrency);
		setShippingAddress(order,shipAddress);
	}

	/**
	 * @param order
	 * @param adjustment
	 */
	void copyPriceInformation(Order order, NewOrderNotification orderNote, OrderAdjustment adjustment) {
		if(order==null || adjustment==null)
			return;
		double total=orderNote.getOrderTotal().getValue().doubleValue();
		String currency=orderNote.getOrderTotal().getCurrency();
		double shipCost=adjustment.getShipping().getMerchantCalculatedShippingAdjustment().getShippingCost().getValue().doubleValue();
		double tax=adjustment.getTotalTax().getValue().doubleValue();
		
		OrderPriceInfo priceInfo=order.getPriceInfo();
		double amt=total-shipCost-tax;
		priceInfo.setAmount(amt);
		priceInfo.setShipping(shipCost);
		priceInfo.setTax(tax);
		priceInfo.setCurrencyCode(currency);
		setTax(order,tax,currency);
		
		List adjustments=priceInfo.getAdjustments();
		if(adjustments!=null){
			PricingAdjustment pa = new PricingAdjustment();
			pa.setTotalAdjustment(total);
			pa.setAdjustmentDescription(Constants.GOOGLE_ORDER_TOTAL);
			adjustments.add(pa);
			setMerchantCodes(adjustments,adjustment.getMerchantCodes());
			
		}
		priceInfo.markAsFinal();
		if(isLoggingDebug())
		{
			logDebug("ORder. total ="+priceInfo.getAmount());
			logDebug("ORder. setShipping ="+priceInfo.getShipping());
			logDebug("ORder. total ="+priceInfo.getTax());
			logDebug("ORder. total ="+priceInfo.getTotal());
			
		}
	}

	/**
	 * @param order
	 * @param tax
	 * @param currency
	 */
	void setTax(Order order, double tax, String currency) {
		TaxPriceInfo tpi=order.getTaxPriceInfo();
		tpi.setAmount(tax);
		tpi.setCurrencyCode(currency);
		tpi.setStateTax(tax);
		tpi.markAsFinal();
	}

	/**
	 * @param merchantCodes
	 */
	void setMerchantCodes(List adjustments,MerchantCodesType merchantCodes) {
		
		if(isLoggingDebug()){
			logDebug("Add an order level adjustment for each merchan Code");
		}
		
	}

	/**
	 * @param order
	 * @param cart
	 * @throws GoogleCheckoutException 
	 */
	void copyCommerceItems(Order order, ShoppingCart cart) throws GoogleCheckoutException {

		if(order==null){
			if(isLoggingError())
				logError("Order is null");
			throw new GoogleCheckoutException(ErrorCodes.NULL_ORDER);
		}
		if(cart==null){
			if(isLoggingError())
				logError("Cart is null");
			throw new GoogleCheckoutException(ErrorCodes.NULL_SHOPPING_CART);
		}

		ItemsType googleItemsType = cart.getItems();		
		List googleItems = googleItemsType.getItem();
		Iterator itr =googleItems.iterator();
		Item item=null;
		CommerceItem ci=null;
		
		try {
			while(itr.hasNext()){
				item=(Item) itr.next();
				ci=createATGCommerceItem((ItemImpl) item);

				getGoogleCheckoutManager().getOrderManager().getCommerceItemManager().addItemToOrder(order, ci);
/*
				ShippingGroup sg=(ShippingGroup) order.getShippingGroups().get(0);
				if(sg==null){
					if(isLoggingError())
						logError("Shipping Group is null");
				}
			//	getGoogleCheckoutManager().getOrderManager().getCommerceItemManager().addRemainingItemQuantityToShippingGroup(order, ci.getId(), sg.getId());		
				PaymentGroup pg = (PaymentGroup) order.getPaymentGroups().get(0);
				if(pg==null){
					if(isLoggingError())
						logError("Shipping Group is null");
				}
			//	getGoogleCheckoutManager().getOrderManager().getCommerceItemManager().addRemainingItemAmountToPaymentGroup(order, ci.getId(), pg.getId());
*/
			}
		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
	}

	/**
	 * @param item
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	CommerceItem createATGCommerceItem(ItemImpl item) throws GoogleCheckoutException {

		int qty=item.getQuantity();
		Money price=item.getUnitPrice();
		AnyType privateDataType=item.getMerchantPrivateItemData();
		if(privateDataType==null){
			return null ;// for promotions, just ignore them			
		}
		String privateData=(String) privateDataType.getAny();
		Map itemInfo=fetchItemInformation(privateData);
		Set products=itemInfo.keySet();
		Iterator itr =products.iterator();
		String productId=null, skuId=null;
		if(itr.hasNext())
			productId=(String) itr.next();
		skuId=(String) itemInfo.get(productId);
		CommerceItemManager cim =this.getGoogleCheckoutManager().getOrderManager().getCommerceItemManager();
		CommerceItem atgItem;
		try {
			atgItem = cim.createCommerceItem(skuId,productId, qty);
			
		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
		if(atgItem!=null){
			setATGItemPrices(atgItem,price,qty);
		}
		return atgItem;
	}

	/**
	 * @param atgItem
	 * @param price
	 */
	void setATGItemPrices(CommerceItem atgItem, Money price,int qty) {

		if(atgItem==null || price==null)
			return;
		ItemPriceInfo amtInfo = atgItem.getPriceInfo();
		if(amtInfo==null){
			if(isLoggingError())
				logError("Amount Info is null");
			return;
		}
		PricingAdjustment adjustment = new PricingAdjustment() ;		
		adjustment.setQuantityAdjusted(qty);
		adjustment.setTotalAdjustment(qty*price.getValue().doubleValue());
		adjustment.setAdjustmentDescription(Constants.GOOGLE_ITEM_ADJUSTMENT_DECRIPTION);

		amtInfo.getAdjustments().add(adjustment);
		amtInfo.setListPrice(price.getValue().doubleValue());
		amtInfo.setAmount(qty*price.getValue().doubleValue());
		amtInfo.setAmountIsFinal(true);
		amtInfo.setCurrencyCode(price.getCurrency());
		if(isLoggingDebug()){
			logDebug("Set the amount on CommerceItem ci"+atgItem.getCatalogRefId()+" to"+amtInfo.getAmount());
		}
	}

	/**
	 * gets skuid and privateId from privateData, returns a map of productId and skuId
	 * @param privateData
	 * @throws GoogleCheckoutException 
	 */
	Map fetchItemInformation(String privateData) throws GoogleCheckoutException {
		Map itemInfomation=new HashMap();
		//itemInfomation.put("prod150009","sku50002");
		if(privateData==null)
			return itemInfomation;
		
		StringReader stringReader = new StringReader(privateData);
    	InputSource stringSource = new InputSource(stringReader);
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder _docBuilder = null;
		try {
			_docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}
    	Document doc = null;
		try {
			doc = _docBuilder.parse(stringSource);
		} catch (SAXException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (IOException e) {
			if(isLoggingError())
				logError(e);			
			throw new GoogleCheckoutException(e);			
		}
		
		NodeList skuNodes=doc.getElementsByTagName(Constants.MERCHANT_PRIVATE_ITEM_SKUID_TAG);
		NodeList productNodes=doc.getElementsByTagName(Constants.MERCHANT_PRIVATE_ITEM_PRODUCTID_TAG);
		
		if(skuNodes==null || productNodes==null)
			return null;
		
		String skuId=skuNodes.item(0).getChildNodes().item(0).getNodeValue();
		String productId=productNodes.item(0).getChildNodes().item(0).getNodeValue();
		
		if(isLoggingDebug())
			logDebug("Fetched SKUID ="+skuId+" productId "+productId);
		itemInfomation.put(productId,skuId);
		
		return itemInfomation;
	}

	/**
	 * @param order
	 * @param billAddress
	 * @throws GoogleCheckoutException 
	 */
	void setBillingAddress(Order order, Address billAddress) throws GoogleCheckoutException {
		PaymentGroup pg=(PaymentGroup) order.getPaymentGroups().get(0);
		if(pg==null){
			if(isLoggingError())
				logError("Payment Group  is null");
		}
		if(!(pg instanceof CreditCard)){
			throw new GoogleCheckoutException(ErrorCodes.INVALID_PAYMENT_GROUP_TYPE);
		}
		
		atg.core.util.Address address=((CreditCard)pg).getBillingAddress();
		this.getGoogleCheckoutManager().copyAddressToATGAddress(billAddress, address);
	}

	/**
	 * @param order
	 * @param shipAddress
	 * @throws GoogleCheckoutException 
	 */
	void setShippingAddress(Order order, Address shipAddress) throws GoogleCheckoutException {
		ShippingGroup sg=(ShippingGroup) order.getShippingGroups().get(0);
		if(sg==null){
			if(isLoggingError())
				logError("Shipping Group is null");
		}
		if(!(sg instanceof HardgoodShippingGroup)){
			throw new GoogleCheckoutException(ErrorCodes.INVALID_SHIPPING_GROUP_TYPE);
		}
		atg.core.util.Address address=((HardgoodShippingGroup)sg).getShippingAddress();
		this.getGoogleCheckoutManager().copyAddressToATGAddress(shipAddress, address);
	}

	

}
