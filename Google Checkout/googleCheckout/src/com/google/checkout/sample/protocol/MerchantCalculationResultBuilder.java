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
package com.google.checkout.sample.protocol;

import com.google.checkout.sample.util.StringUtil;
import com.google.checkout.schema._2.CouponResult;
import com.google.checkout.schema._2.GiftCertificateResult;
import com.google.checkout.schema._2.MerchantCalculationResults;
import com.google.checkout.schema._2.Result;
import com.google.checkout.schema._2.MerchantCalculationResults.ResultsType;
import com.google.checkout.schema._2.Result.MerchantCodeResultsType;

import org.w3c.dom.Document;

import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * The <b>MerchantCalculationResultBuilder</b> class contains methods that 
 * create the JAXB objects needed to construct Merchant Calculations API 
 * responses.
 * @version 1.0 beta
 */
public class MerchantCalculationResultBuilder 
    extends AbstractProtocolBuilder {

  private static MerchantCalculationResultBuilder _builder; 
  
  private MerchantCalculationResultBuilder() throws ProtocolException, 
      JAXBException {
  }
  
  /**
   * Default constructor 
   */
  public static MerchantCalculationResultBuilder getInstance() 
      throws ProtocolException {
    try {
      if (_builder == null) {
        _builder = new MerchantCalculationResultBuilder();
      }
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx);
    }
    
    return _builder;    
  }
  
  /**
   * The <b>createCouponResult</b> method constructs a 
   * &lt;coupon-calculation&gt; result. Also see: {@see CouponResult}
   * @param isCodeValid Optional. Boolean flag that is true if coupon code 
   * is valid.
   * @param couponValue Optional. The value of the coupon.
   * @param couponCode Required. The coupon code.
   * @param additionalMsg Optional. A message to send to the buyer that will 
   * display next to the coupon code.
   * @throws ProtocolException if coupon code is empty
   */
  public CouponResult createCouponResult(boolean isCodeValid, 
      float couponValue, String couponCode, String additionalMsg) 
      throws ProtocolException {
    if (StringUtil.isEmpty(couponCode)) {
      throw new ProtocolException("coupon code cannot be empty");
    }
    
    try {
    //  CouponResult cResult = _objectFact.createCouponResult();
      CouponResult cResult =_objectFact.createResultMerchantCodeResultsTypeCouponResult();    
      cResult.setCalculatedAmount(createMoney(couponValue));
      cResult.setCode(couponCode);
      cResult.setValid(isCodeValid);
      cResult.setMessage(additionalMsg);
      return cResult;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createGiftCertResult</b> method constructs a 
   * &lt;gift-certificate-calculation&gt; result. Also see 
   * {@see GiftCertificateResult}
   * @param isCodeValid Optional. Boolean flag that is true if gift certificate
   * code is valid
   * @param certificateValue Optional. The value of the gift certificate.
   * @param certificateCode Required. The gift certificate code.
   * @param additionalMsg Optional. A message for the buyer that will 
   * display next to the gift certificate code.
   * @throws ProtocolException if gift certificate code is empty
   */
  public GiftCertificateResult createGiftCertResult(boolean isCodeValid,
      float certificateValue, String certificateCode, String additionalMsg) 
      throws ProtocolException {
    if (StringUtil.isEmpty(certificateCode)) {
      throw new ProtocolException();
    }
    
    try {
   //   GiftCertificateResult gResult = _objectFact.createGiftCertificateResult();
   	  GiftCertificateResult gResult = _objectFact.createResultMerchantCodeResultsTypeGiftCertificateResult(); 
      gResult.setCalculatedAmount(createMoney(certificateValue));
      gResult.setCode(certificateCode);
      gResult.setValid(isCodeValid);
      gResult.setMessage(additionalMsg);
      return gResult;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createMerchantCalResultsByGiftCert</b> method creates a 
   * &lt;merchant-calculation-results&gt; response with a list of 
   * gift certificates but no coupons.
   * @param giftResults A list of GiftCertificateResult objects
   * @param totalTaxAmount The total tax amount to assess for the order
   * @param shippingRate The cost to ship the order.
   * @param isShippable A Boolean flag indicating whether the order can
   * be shipping to the specified address using the requested shipping method.
   * @param addressId An ID for the shipping address.
   */
  public MerchantCalculationResults createMerchantCalResultsByGiftCert(
      List giftResults, float totalTaxAmount, 
      float shippingRate, boolean isShippable, String addressId)
      throws ProtocolException {
    return createMerchantCalResults(null, giftResults, totalTaxAmount,
      shippingRate, isShippable, addressId);
  }
  
  /**
   * The <b>createMerchantCalResultsByCoupon</b> method creates a 
   * &lt;merchant-calculation-results&gt; response with a list of coupons
   * but no gift certificates.
   * @param couponResults A list of CouponResult objects
   * @param totalTaxAmount The total tax amount to assess for the order
   * @param shippingRate The cost to ship the order.
   * @param isShippable A Boolean flag indicating whether the order can
   * be shipping to the specified address using the requested shipping method.
   * @param addressId An ID for the shipping address.
   */
  public MerchantCalculationResults createMerchantCalResultsByCoupon(
      List couponResults, float totalTaxAmount, 
      float shippingRate, boolean isShippable, String addressId)
      throws ProtocolException {
    return createMerchantCalResults(couponResults, null, totalTaxAmount,
      shippingRate, isShippable, addressId);
  }
  
  /**
   * The <b>createMerchantCalResults</b> method creates a 
   * &lt;merchant-calculation-results&gt; response with a list of coupons
   * and a list of gift certificates. The <code>createMerchantCalResultsByCoupon</b>
   * and <code>createMerchantCalResultsByGiftCert</code> methods both call
   * this method to create a &lt;merchant-calculation-results&gt; response.
   * @param couponList A list of CouponResult objects
   * @param giftCertList A list of GiftCertificateResult objects
   * @param totalTaxAmount The total tax amount to assess for the order
   * @param shippingRate The cost to ship the order.
   * @param isShippable A Boolean flag indicating whether the order can
   * be shipping to the specified address using the requested shipping method.
   * @param addressId An ID for the shipping address.
   */
  public MerchantCalculationResults createMerchantCalResults(
      List couponList, List giftCertList,
      float totalTaxAmount, float shippingRate, boolean isShippable,
      String addressId) throws ProtocolException {

    try {
      MerchantCodeResultsType merchantCodeResults
          = _objectFact.createResultMerchantCodeResultsType();
      List list = merchantCodeResults.getCouponResultOrGiftCertificateResult();
      if (couponList != null) {
        for (int i = 0; i < couponList.size(); i++) {
          list.add(couponList.get(i));
        }
      }
  
      if (giftCertList !=null) {
        for (int i = 0; i < giftCertList.size(); i++) {
          list.add(giftCertList.get(i));
        }
      }
      
      Result individualResult = _objectFact.createResult();
      individualResult.setAddressId(addressId);
      individualResult.setShippable(isShippable);
      individualResult.setShippingRate(createMoney(shippingRate));
      individualResult.setTotalTax(createMoney(totalTaxAmount));
      individualResult.setMerchantCodeResults(merchantCodeResults);
      
      ResultsType results 
          = _objectFact.createMerchantCalculationResultsResultsType();
      results.getResult().add(individualResult);
      MerchantCalculationResults mResults 
          = _objectFact.createMerchantCalculationResults();
      mResults.setResults(results);
      return mResults;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createMerchantCalResultsDOM</b> converts a 
   * MerchantCalculationResults DOM object to a 
   * &lt;merchant-calculation-results&gt; XML response
   */
  public Document createMerchantCalResultsDOM(MerchantCalculationResults results) 
      throws ProtocolException {
    if (results == null) {
      throw new ProtocolException("merchant calculation results cannot be null");
    }
    return convertToDOM(results);
  }
  
public MerchantCalculationResults createMerchantCalculationResultsElement(ResultsType results) throws ProtocolException{
	  
	  try {
		  MerchantCalculationResults mcr= _objectFact.createMerchantCalculationResults();
		  mcr.setResults(results);
		  return mcr;
	} catch (JAXBException e) {
		 throw new ProtocolException(e.getMessage());
	}
  }
  
  public MerchantCalculationResults createMerchantCalculationResults(ResultsType results) throws ProtocolException{
	  
	  try {
		  MerchantCalculationResults mcr= _objectFact.createMerchantCalculationResultsElement();
		  mcr.setResults(results);
		  return mcr;
	} catch (JAXBException e) {
		 throw new ProtocolException(e.getMessage());
	}
  }
  
  public Result createIndividualResult( List couponList, List giftCertList,
	      float totalTaxAmount, float shippingRate, boolean isShippable,
	      String addressId) throws ProtocolException{
	  
	   try {
		      MerchantCodeResultsType merchantCodeResults
		          = _objectFact.createResultMerchantCodeResultsType();
		      List list = merchantCodeResults.getCouponResultOrGiftCertificateResult();
		      if (couponList != null) {
		        for (int i = 0; i < couponList.size(); i++) {
		          list.add(couponList.get(i));
		        }
		      }
		  
		      if (giftCertList !=null) {
		        for (int i = 0; i < giftCertList.size(); i++) {
		          list.add(giftCertList.get(i));
		        }
		      }
		      
		      Result individualResult = _objectFact.createResult();
		      individualResult.setAddressId(addressId);
		      individualResult.setShippable(isShippable);
		      individualResult.setShippingRate(createMoney(shippingRate));
		      individualResult.setTotalTax(createMoney(totalTaxAmount));
		      individualResult.setMerchantCodeResults(merchantCodeResults);
		      
		      return individualResult;
	} catch (JAXBException e) {
		 throw new ProtocolException(e.getMessage());
	}
  }
  
 public ResultsType createMerchantCalculationResultsResultsType() throws ProtocolException{
	  
	  try {
		return _objectFact.createMerchantCalculationResultsResultsType();
	} catch (JAXBException e) {
		 throw new ProtocolException(e.getMessage());
	}
 }
  
}
