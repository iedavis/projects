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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import atg.commerce.CommerceException;
import atg.commerce.claimable.ClaimableManager;
import atg.commerce.order.CommerceItem;
import atg.commerce.order.HardgoodShippingGroup;
import atg.commerce.order.Order;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.ShippingGroup;
import atg.commerce.pricing.AmountInfo;
import atg.commerce.pricing.OrderPriceInfo;
import atg.commerce.pricing.PricingAdjustment;
import atg.commerce.pricing.PricingException;
import atg.core.util.Address;
import atg.nucleus.GenericService;
import atg.repository.Repository;
import atg.repository.RepositoryException;
import atg.repository.RepositoryItem;

import com.google.checkout.sample.protocol.MerchantCalculationResultBuilder;
import com.google.checkout.sample.protocol.ProtocolException;
import com.google.checkout.schema._2.AnonymousAddress;
import com.google.checkout.schema._2.CouponResult;
import com.google.checkout.schema._2.GiftCertificateResult;
import com.google.checkout.schema._2.MerchantCalculationResults;
import com.google.checkout.schema._2.MerchantCodeString;
import com.google.checkout.schema._2.Method;
import com.google.checkout.schema._2.Result;

/**
 * @author ssingh
 *
 */
public class GoogleMerchantCalculationManager extends GenericService {

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

	ClaimableManager claimableManager;
	/**
	 * @return the claimableManager
	 */
	public ClaimableManager getClaimableManager() {
		return claimableManager;
	}

	/**
	 * @param claimableManager the claimableManager to set
	 */
	public void setClaimableManager(ClaimableManager claimableManager) {
		this.claimableManager = claimableManager;
	}

	/**
	 * @param order
	 * @param codes list of repository Items of coupons
	 */
	public Map findAppliedCoupons(Order order, List codes) {

		if(codes==null)
			return null;		
		Map couponMap = new HashMap();
		Map promotionMap=findAllAppliedPromotions(order);
		if(promotionMap==null)
			return null;
		Iterator itr =codes.iterator();
		RepositoryItem couponItem=null,promotionItem=null;
		Double amt=null;
		while(itr.hasNext()){
			couponItem=(RepositoryItem) itr.next();
			promotionItem=(RepositoryItem) couponItem.getPropertyValue(Constants.PROMOTION_PROPERTY);
			amt=(Double) promotionMap.get(promotionItem.getRepositoryId());
			if(amt!=null)
				couponMap.put(couponItem, amt);
		}
		if(isLoggingDebug())
			logDebug(" Coupon Code Map ="+couponMap);
		return couponMap;
	}


	/**
	 * Do we price order or just shipping, price everything because promotions can be tied to address? weird but ya.
	 * @param order
	 * @param shipMethod
	 * @param address
	 * @param codes
	 * @throws CommerceException 
	 */
	public Result performSingleMerchantResultCalculation(MerchantCalculationResultBuilder resultBuilder,Order order,RepositoryItem pProfile, 
			Method shipMethod, AnonymousAddress address, List couponCodes, List pGiftCertf) throws CommerceException {


		if(order==null || pProfile==null || shipMethod==null)
			throw new GoogleCheckoutException (ErrorCodes.InvalidParameters);

		if(isLoggingDebug()){
			logDebug("Order "+order.getId());
			logDebug("Profile "+pProfile.getRepositoryId());
			logDebug("shipMethod "+shipMethod.getName());
			logDebug("address "+address.getId());
			logDebug("couponCodes "+couponCodes);
			logDebug("pGiftCertf "+pGiftCertf);
		}

		// TODO Auto-generated method stub
		ShippingGroup sg=null;
		Address atgAddress=null;
		sg=(ShippingGroup) order.getShippingGroups().get(0);
		atgAddress=((HardgoodShippingGroup)sg).getShippingAddress();

		getGoogleCheckoutManager().copyAddressToShippingGroup(address, atgAddress);
		sg.setShippingMethod(shipMethod.getName());
		Map appliedCoupons=null;
		try {
			getGoogleCheckoutManager().getPricingTools().priceOrderTotal(order);
			appliedCoupons=findAppliedCoupons(order,couponCodes);
			List couponList=createAppliedCouponsResult(resultBuilder,appliedCoupons);
			List giftCertList=createGiftCertificateResult(resultBuilder,pGiftCertf,order);

			if(isLoggingDebug())
				debugPrices((OrderImpl)order);
			MerchantCalculationResults mcr;
			try {
				//			return resultBuilder.createIndividualResult(couponCodes, pGiftCertf, (float) 15.00,(float) order.getPriceInfo().getShipping(),  true, address.getId());
				return resultBuilder.createIndividualResult(couponList, giftCertList, (float) order.getPriceInfo().getTax(),(float) order.getPriceInfo().getShipping(),  true, address.getId());


			} catch (ProtocolException e) {
				throw new CommerceException(e);
			}

		} catch (PricingException e) {
			throw new CommerceException(e);
		}



	}

	/**
	 * computes the amt that should be chaged on gift certificate.
	 * @param resultBuilder
	 * @param giftCertf
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	public List createGiftCertificateResult(MerchantCalculationResultBuilder resultBuilder, List giftCertiticates,Order order) throws GoogleCheckoutException {
		if(resultBuilder==null || giftCertiticates==null || order==null)
			return null;
		List results = new ArrayList();
		RepositoryItem giftCertf=null;
		OrderPriceInfo price=order.getPriceInfo();
		RepositoryItem pomotionItem=null;
		String message="Gift Certificate";
		GiftCertificateResult giftResult;
		// start with order total
		double total=price.getTotal();
		Double giftAmt=null,amtAuth=null;
		
		double amtToApply=0;
		Iterator itr=giftCertiticates.iterator();
		
		while(itr.hasNext()){
			giftCertf=(RepositoryItem)itr.next();			
			giftAmt=(Double) giftCertf.getPropertyValue(Constants.PROPERTY_GIFT_AMOUNT_REMAINING);
			amtAuth=(Double) giftCertf.getPropertyValue(Constants.PROPERTY_GIFT_AMOUNT_AUTHORIZED);
			double amtRemaining=giftAmt.doubleValue()-amtAuth.doubleValue();
			
			if(isLoggingDebug()){
				logDebug("Gift certificate Cocde"+giftCertf.getRepositoryId()+" Amt Remaining on Gift Certf "+giftAmt+" Auth is "+amtAuth+" Order total is"+total);
			}
			// if order is already $0, do not apply any Gift Certificates
			if(total<=0){
				amtToApply=0;
				// we already reached 0 so get out
				break;
			}
			// if total is less than gift Amt, only apply order amt
			else if(total<=amtRemaining){
				amtToApply=total;
				total=0;
			}
			// if gift certf is less than giftAmt, apply whole amt and adjust remaining total for other Gift certf or CC
			else if(total>amtRemaining){
				amtToApply=amtRemaining;
				total=total-amtRemaining;
			}
			
			if(isLoggingDebug()){
				logDebug("Gift certificate code "+giftCertf.getRepositoryId()+" Amt ="+amtToApply+" Message "+message);
			}
			
			try {
				giftResult=resultBuilder.createGiftCertResult(true, (float)amtToApply, giftCertf.getRepositoryId(), message);
			} catch (ProtocolException e) {
				if(isLoggingError())
					logError(e);
				throw new GoogleCheckoutException(e);
			}
			results.add(giftResult);
		}
		
		return results;
		
	}


	/**
	 * @param appliedCoupons
	 * @param couponCodes
	 * @return
	 */
	public List createAppliedCouponsResult(MerchantCalculationResultBuilder resultBuilder,Map appliedCoupons) {
		if(appliedCoupons==null)
			return null;
		Set appliedCouponSet=appliedCoupons.keySet();
		Iterator appliedCouponItr=appliedCouponSet.iterator();

		//MerchantCodeString code=null;
		String codeStr=null,message=null;
		
		Double amt=new Double(0);
		CouponResult result=null;
		RepositoryItem pomotionItem=null,code=null;
		List couponList = new ArrayList();
		while(appliedCouponItr.hasNext()){
			code=(RepositoryItem) appliedCouponItr.next();
			codeStr=code.getRepositoryId();
			amt=(Double) appliedCoupons.get(code);
			pomotionItem=(RepositoryItem) code.getPropertyValue(Constants.PROPERTY_PROMOTION);
			message=(String) pomotionItem.getPropertyValue(Constants.PROPERTY_COUPON_MESSAGE);

			if(isLoggingDebug())
				logDebug("Creating a coupon result with code="+codeStr+" Amt"+amt+" message"+message);

			try{
				result = resultBuilder.createCouponResult(true,-1*amt.floatValue(), codeStr, message);				
				if(isLoggingDebug())
					logDebug("Created a coupon result ="+result);
				couponList.add(result);
			} catch (ProtocolException e) {
				if(isLoggingError())
					logError(e);
			}

		}
		return couponList;
	}


	/**
	 * from the merchant Codes find which are coupon codes and which are gift certificates 
	 * and then return a map where key is couponCodes or merchantCodes and values is a list of corresponding repository items.
	 * @param merchantCodes
	 * @return
	 */
	public Map arrangeMerchantCodes(List merchantCodes) {
		List couponCodes=new ArrayList();
		List giftCertCodes= new ArrayList();

		if(merchantCodes==null)
			return null;

		if(getClaimableManager()==null){
			if(isLoggingError())
				logError("Claimable Manager not configured");
			return null;
		}		

		String giftCertItemDesc=getClaimableManager().getClaimableTools().getGiftCertificateItemDescriptorName();
		RepositoryItem claimableItem=null;

		Iterator itr=merchantCodes.iterator();
		MerchantCodeString code=null;
		String codeStr=null;
		while(itr.hasNext()){
			code=(MerchantCodeString)itr.next();
			if(code==null)
				continue;
			codeStr=code.getCode();
			claimableItem=checkAsCoupon(codeStr);
			if(claimableItem!=null){
				if(isLoggingDebug())
					logDebug(" Claimable Itme with code "+codeStr+" is a Coupon");
				couponCodes.add(claimableItem);
				continue;
			}
			claimableItem=checkAsGiftCertificate(codeStr);
			if(claimableItem!=null){
				if(isLoggingDebug())
					logDebug(" Claimable Itme with code "+codeStr+" is a Gift Certificate");
				if(checkIfValid(claimableItem))
					giftCertCodes.add(claimableItem);
				continue;
			}
			if(claimableItem==null){
				if(isLoggingDebug())
					logDebug(" Claimable Itme with code did not match any Coupn or gift certificate codes");			
			}

		}


		Map codeMap=new HashMap();
		codeMap.put(Constants.COUPON_KEY, couponCodes);
		codeMap.put(Constants.GIFT_CERT_KEY, giftCertCodes);
		if(isLoggingDebug())
			logDebug("Arranged Merchan codes="+codeMap);
		return codeMap;
	}


	/**
	 * checks if Gift cert has already expired
	 * @param claimableItem
	 * @return
	 */
	boolean checkIfValid(RepositoryItem claimableItem) {
		
		if(claimableItem==null)
			return false;
		String expirationPropertyName = getClaimableManager().getClaimableTools().getExpirePropertyName();		
		Date expireDate=(Date) claimableItem.getPropertyValue(expirationPropertyName);
		if(expireDate==null)
			return true; // never expires
		// if expiration date is after now, valid GC
		if(isLoggingDebug())
			logDebug(" Expire date"+expireDate.getTime()+" Now is "+System.currentTimeMillis());
		if(expireDate.getTime()>System.currentTimeMillis())
			return true;
		
		return false;
	}


	/**
	 * Try to see if the code is a Coupon,return the item if it is
	 * @param merchantCodes
	 * @return
	 */
	public RepositoryItem checkAsCoupon(String code) {

		Repository claimableRepository=getClaimableManager().getClaimableTools().getClaimableRepository();
		String couponItemDesc=getClaimableManager().getClaimableTools().getCouponItemDescriptorName();
		RepositoryItem claimableItem=null;

		try {
			claimableItem=claimableRepository.getItem(code,couponItemDesc );
		} catch (RepositoryException e) {
			if(isLoggingDebug())
				logDebug(" Claimable Itme with code "+code+" is NOT a Coupon");
		}
		return claimableItem;

	}


	/**
	 * Try to see if the code is a gift Certificate,return the item if it is
	 * @param merchantCodes
	 * @return
	 */
	public RepositoryItem checkAsGiftCertificate(String code) {
		Repository claimableRepository=getClaimableManager().getClaimableTools().getClaimableRepository();
		String giftCertItemDesc=getClaimableManager().getClaimableTools().getGiftCertificateItemDescriptorName();
		RepositoryItem claimableItem=null;

		try {
			claimableItem=claimableRepository.getItem(code,giftCertItemDesc );
			if(isLoggingDebug())
				logDebug(" Claimable Itme with code "+code+" is a Gift Certificate");
		} catch (RepositoryException e) {
			// ignore error, maybe a Gift certf
		}
		return claimableItem;
	}

	/**
	 * claims coupons for the user. The claim method adds the corresponding promotion to profile
	 * @param order
	 * @param profile
	 * @param coponCodes
	 * @throws CommerceException 
	 */
	public void applyCoupons(String pProfileId, List pCouponCodes) throws CommerceException {

		if(pProfileId==null){
			throw new CommerceException("Profile is null");
		}
		if(pCouponCodes==null || pCouponCodes.size()<=0)
			return;

		Iterator itr = pCouponCodes.iterator();
		RepositoryItem coupon=null;
		while(itr.hasNext()){
			coupon=(RepositoryItem) itr.next();
			if(coupon==null)
				continue;
			if(isLoggingDebug())
				logDebug("Processing coupon code "+coupon);
			getClaimableManager().claimCoupon(pProfileId, coupon.getRepositoryId());
		}

	}


	/**
	 * @param order
	 */
	void debugPrices(OrderImpl order) {

		logDebug("Order.getAmount ="+order.getPriceInfo().getAmount());
		logDebug("Order.Shipping Cost ="+order.getPriceInfo().getShipping());
		logDebug("Order. Tax ="+order.getPriceInfo().getTax());
		logDebug("Order. Total ="+order.getPriceInfo().getTotal());
		debugItemsPrices( order);
	}

	/**
	 * @param order
	 */
	void debugItemsPrices(OrderImpl order) {

		if(order==null)
			return;

		List items = order.getCommerceItems();
		Iterator itr =items.iterator();
		CommerceItem ci=null;
		AmountInfo ai=null;		

		while(itr.hasNext()){
			ci=(CommerceItem) itr.next();
			ai=ci.getPriceInfo();
			logDebug(" Amount of Commerce Item " +ci.getCatalogRefId()+" Amount ="+ai.getAmount());
			debugAdjustments(ai.getAdjustments());

		}
	}

	void debugAdjustments(List adjustments){
		if(adjustments==null)
			return;
		Iterator adjItr=adjustments.iterator();
		PricingAdjustment pa=null;
		RepositoryItem promo;
		while(adjItr.hasNext()){			 
			pa=(PricingAdjustment) adjItr.next();
			promo=pa.getPricingModel();
			logDebug(" List Price ="+pa.getAdjustmentDescription() +" descr ="+pa.getAdjustmentDescription()+" promo? "+(promo==null?null:promo.getRepositoryId()));

		}		 

	}


	/**
	 * finds all the applied promotions at the order level
	 * @param pOrder
	 * @return
	 */
	Map findAllAppliedPromotions(Order pOrder){
		Map promotionMap = new HashMap();

		findOrderLevelAppliedPromotions(pOrder, promotionMap);
		findShippingGroupLevelAppliedPromotions(pOrder, promotionMap);
		findItemLevelAppledPromotions(pOrder, promotionMap);
		if(isLoggingDebug())
			logDebug(" Promotion Map is "+promotionMap);
		return promotionMap;
	}

	/**
	 *  find all applied promotions at Order level  and return the map of promotionId vs adjustment
	 * @param order
	 * @param codes
	 * @return
	 */
	public Map findOrderLevelAppliedPromotions(Order pOrder,Map pPromotionMap) {
		if(pOrder==null || pPromotionMap==null)
			return pPromotionMap;
		List orderAdj=pOrder.getPriceInfo().getAdjustments();
		findAnyAppliedPromotions(orderAdj, pPromotionMap);
		return pPromotionMap;
	}

	/**
	 *  find all applied promotions at Shipping Group level and return the map of promotionId vs adjustment
	 * @param order
	 * @param codes
	 * @return
	 */
	public Map findShippingGroupLevelAppliedPromotions(Order pOrder,Map pPromotionMap) {

		if(pOrder==null || pPromotionMap==null)
			return pPromotionMap;
		List sgs= pOrder.getShippingGroups();
		if(sgs==null)
			return pPromotionMap;
		Iterator itr =sgs.iterator();
		ShippingGroup sg=null;
		AmountInfo pi=null;
		while(itr.hasNext()){
			sg=(ShippingGroup) itr.next();
			pi=sg.getPriceInfo();
			findAnyAppliedPromotions(pi.getAdjustments(),pPromotionMap);
		}


		return pPromotionMap;
	}

	/**
	 * find all applied promotions at Commerce Item leve and return the map of promotionId vs adjustment
	 * @param order
	 * @param codes
	 * @return
	 */
	public Map findItemLevelAppledPromotions(Order pOrder,Map pPromotionMap) {
		if(pOrder==null || pPromotionMap==null)
			return pPromotionMap;
		List items= pOrder.getCommerceItems();
		if(items==null)
			return pPromotionMap;
		Iterator itr =items.iterator();
		CommerceItem ci=null;
		AmountInfo pi=null;
		while(itr.hasNext()){
			ci=(CommerceItem) itr.next();
			pi=ci.getPriceInfo();
			findAnyAppliedPromotions(pi.getAdjustments(),pPromotionMap);
		}
		return pPromotionMap;
	}

	/**
	 * finds the applied promotions from a given adjustmets.
	 * If promotion is found in the map updates the price with new amount 
	 * if promotion is not found in the map adds to the map, key is promotion Id and price is the adjustment
	 * @param pAdjustments
	 * @param promotionMap
	 * @return
	 */
	Map findAnyAppliedPromotions(List pAdjustments, Map promotionMap){
		if(pAdjustments==null)
			return null;		
		Iterator adjItr=pAdjustments.iterator();
		PricingAdjustment pa=null;
		RepositoryItem promo;
		Double currentPrice=null;
		double newPrice;
		while(adjItr.hasNext()){			 
			pa=(PricingAdjustment) adjItr.next();
			promo=pa.getPricingModel();
			if(promo!=null){
				if(isLoggingDebug())
					logDebug("Found a promotion, updating the Map"+promo.getRepositoryId());
				if(promotionMap.containsKey(promo.getRepositoryId())){

					currentPrice=(Double) promotionMap.get(promo.getRepositoryId());					
					if(isLoggingDebug())
						logDebug(" FOund a promotion already in the Map, with current Amt"+currentPrice);
					newPrice=currentPrice.doubleValue()+pa.getAdjustment();
					if(isLoggingDebug())
						logDebug(" FOund a promotion already in the Map, with new Amt"+newPrice);
					promotionMap.remove(promo.getRepositoryId());
					promotionMap.put(promo.getRepositoryId(), new Double(newPrice));
				}else{
					if(isLoggingDebug())
						logDebug(" FOund a new promotion in the Map, with current Amt"+pa.getAdjustment());
					promotionMap.put(promo.getRepositoryId(),new Double(pa.getAdjustment()));
				}
			}

		}		 
		return promotionMap;
	}




}