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
import com.google.checkout.schema._2.AlternateTaxRule;
import com.google.checkout.schema._2.AlternateTaxTable;
import com.google.checkout.schema._2.AnyType;
import com.google.checkout.schema._2.CartExpiration;
import com.google.checkout.schema._2.CheckoutShoppingCart;
import com.google.checkout.schema._2.DefaultTaxRule;
import com.google.checkout.schema._2.DefaultTaxTable;
import com.google.checkout.schema._2.FlatRateShipping;
import com.google.checkout.schema._2.Item;
import com.google.checkout.schema._2.MerchantCalculatedShipping;
import com.google.checkout.schema._2.MerchantCalculations;
import com.google.checkout.schema._2.MerchantCheckoutFlowSupport;
import com.google.checkout.schema._2.Money;
import com.google.checkout.schema._2.Pickup;
import com.google.checkout.schema._2.ShippingRestrictions;
import com.google.checkout.schema._2.ShoppingCart;
import com.google.checkout.schema._2.TaxTables;
import com.google.checkout.schema._2.USCountryArea;
import com.google.checkout.schema._2.USStateArea;
import com.google.checkout.schema._2.USZipArea;
import com.google.checkout.schema._2.AlternateTaxTable.AlternateTaxRulesType;
import com.google.checkout.schema._2.CheckoutShoppingCart.CheckoutFlowSupportType;
import com.google.checkout.schema._2.DefaultTaxTable.TaxRulesType;
import com.google.checkout.schema._2.MerchantCheckoutFlowSupport.ShippingMethodsType;
import com.google.checkout.schema._2.ShippingRestrictions.AllowedAreasType;
import com.google.checkout.schema._2.ShippingRestrictions.ExcludedAreasType;
import com.google.checkout.schema._2.ShoppingCart.ItemsType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBException;

/**
 * The <b>CheckoutCartBuilder</b> class contains methods that create the 
 * JAXB objects needed to construct Checkout API requests.
 * @version 1.0 beta
 */
public class CheckoutCartBuilder extends AbstractProtocolBuilder {
  private static CheckoutCartBuilder _builder;
  
  public static final String USAREA_CONTINENTAL_48 = "CONTINENTAL_48";
  
  public static final String USAREA_FULL_50_STATES = "FULL_50_STATES";
  
  public static final String USAREA_ALL = "ALL";
  
  private CheckoutCartBuilder() throws JAXBException, ProtocolException {
  }

  public synchronized static CheckoutCartBuilder getInstance() 
      throws ProtocolException {
    try {
      if (_builder == null) {
        _builder = new CheckoutCartBuilder();
      }
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx);
    }
    
    return _builder;
  }
  
  /**
   * The <b>createShoppingItem</b> method creates an Item object, which contains
   * the information that would be contained within a single &lt;item&gt; XML 
   * block in a Checkout API request.
   * @param itemName Required. The name of the item.
   * @param itemDesc Required. A description of the item.
   * @param quantity Required. The number of units of the item included in 
   * the order.
   * @param unitPrice Required. The numeric cost of the item.
   * @param currency Optional. The three-letter ISO 4217 currency code 
   * associated with the numeric cost of the item.  At this time, Google 
   * Checkout only supports U.S. dollars (USD). 
   * @param taxTableSelector Optional. The name of the alternate tax table 
   * that should be used to calculate tax for the item. 
   * @param privateItemData Optional. Proprietary information about an item 
   * in an order. The value must be well formed XML with a single root tag.
   * @return {@see Item} object
   */
  public Item createShoppingItem(String itemName, String itemDesc, 
      int quantity, float unitPrice, String currency, 
      String taxTableSelector, Element privateItemData)
      throws ProtocolException {
    
    try {
      Money money = createMoney(unitPrice, currency); 
      Item item = _objectFact.createItem();
      item.setItemName(itemName);
      item.setItemDescription(itemDesc);
      item.setQuantity(quantity);
      item.setUnitPrice(money);
  
      if (taxTableSelector != null) {
        item.setTaxTableSelector(taxTableSelector);
      }
      
      if (privateItemData != null) {
        AnyType anyType = _objectFact.createAnyType();
        anyType.setAny(privateItemData);
        item.setMerchantPrivateItemData(anyType);
      }
      
      return item;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createShoppingCart</b> method constructs a ShoppingCart object, 
   * which is comprised of a list of {@see Item} objects as well as a 
   * {@see CartExpiration} date. This method must be called after you have 
   * called the 
   * {@see #createShoppingItem(String, String, int, float, String, String, Element)}
   * method for each item in the order.
   * @param itemList Required. A list of all of the items in the order.
   * @param cartExpire Optional. The date that the shopping cart expires. If 
   * you specify a value for this parameter, Google Checkout will reject the 
   * shopping cart if the user submits the cart after the specified date. If 
   * you do not specify a value for this parameter, the cart will not expire.
   * @param merchantPrivateData Optional. Proprietary information about an 
   * order. The parameter value must be well formed XML with a single root tag.
   * @return ShoppingCart object
   * @throws ProtocolException if itemList is null 
   */
  public ShoppingCart createShoppingCart(List itemList, Date cartExpire,
    Element merchantPrivateData) 
      throws ProtocolException {
    if (itemList == null) {
      throw new ProtocolException("itemList cannot be null");
    }
    
    try {
      // Create the <items> tag and add each item in the list to the cart
      ShoppingCart cart = _objectFact.createShoppingCart();
      ItemsType items = _objectFact.createShoppingCartItemsType();
      List item_list = items.getItem();
      for (int i = 0; i < itemList.size(); i++) {
        item_list.add(itemList.get(i));
      }
      cart.setItems(items);
      
      // Add the expiration date if one is provided
      if (cartExpire != null) {
        GregorianCalendar gCal = new GregorianCalendar();
        gCal.setTime(cartExpire);
        CartExpiration cartExpiry = _objectFact.createCartExpiration();
        cartExpiry.setGoodUntilDate(gCal);
        cart.setCartExpiration(cartExpiry);
      }
  
      // Add merchantPrivateData if it is provided
      if (merchantPrivateData != null) {
        AnyType anyType = _objectFact.createAnyType();
        anyType.setAny(merchantPrivateData);
        cart.setMerchantPrivateData(anyType);
      }
      
      return cart;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createCheckoutShoppingCart</b> method constructs and returns a DOM 
   * object containing all of the items and checkout-related information for an 
   * order.
   * @param cart Required. A ShoppingCart object. Also see:
   * {@see #createShoppingCart(List, Date)}
   * @param checkoutFlowSupport - Required. Information used in the checkout
   * process, including tax tables and available shipping methods. Also see 
   * the <code>createMerchantSupport</code> method
   * @throws ProtocolException if cart is null or checkoutFlowSupport is null
   */
  public Document createCheckoutCart(ShoppingCart cart, 
      MerchantCheckoutFlowSupport checkoutFlowSupport) 
      throws ProtocolException {

    if (cart == null) {
      throw new ProtocolException("both cart and flow support cannot be null");
    }
    
    try {
      CheckoutFlowSupportType flowSupport
          = _objectFact.createCheckoutShoppingCartCheckoutFlowSupportType();
      flowSupport.setMerchantCheckoutFlowSupport(checkoutFlowSupport);
      CheckoutShoppingCart chkoutCart = _objectFact.createCheckoutShoppingCartElement();
      chkoutCart.setShoppingCart(cart);
      chkoutCart.setCheckoutFlowSupport(flowSupport);
          
      return convertToDOM(chkoutCart);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createMerchantSupport</b> method constructs a 
   * MerchantCheckoutFlowSupport object, which contains the information that 
   * would be contained within the &lt;merchant-checkout-flow-support&gt; 
   * XML block in a Checkout API request.
   * @param methods Optional. A list of options for shipping the order. These 
   * options can be either flat-rate shipping options, merchant-calculated 
   * shipping options or in-store pickup. Also see the 
   * <code>createFlatShippingMethod</code>, 
   * <code>createMerchantCalculatedMethod</code> and 
   * <code>createPickupMethod</code> methods.
   *<br>
   * @param defaultTaxTable Required. This parameter contains a DefaultTaxTable
   * object, which specifies the default tax rules to apply to the order.
   * Also see the <code>createDefaultTaxTable</code> methods in this class
   * for creating tax tables for country, state and zip code areas.
   *<br>
   * @param alterTaxTableList Optional. This parameter contains a list of 
   * AlternateTaxTable objects, which specify alternate tax rules to apply to
   * certain types of items. Also see the <code>createAlternateTaxTable</code>
   * methods in this class creating alternate tax tables for country, state and
   * zip code areas.
   *<br>
   * @param couponInfo Optional. This parameter contains a MerchantCalculations
   * object, which contains the information that would be contained within the
   * &lt;merchant-calculations&gt; XML block of a Checkout API request. This 
   * information includes an indication of whether the merchant accepts coupons
   * and/or gift certificates and, if so, the URL to which Google Checkout
   * should send the &lt;merchant-calculation-callback&gt; request. Also see
   * {@see #createMerchantCalculations(boolean, boolean, String)}
   *<br>
   * @param editCartUrl Optional. A URL that the customer could click to 
   * edit her shopping cart.
   *<br>
   * @param continueShoppingUrl Optional. A URL that the customer could 
   * click to postpone the checkout process and continue shopping.
   */
  public MerchantCheckoutFlowSupport createMerchantSupport(
      ShippingMethodsType methods, DefaultTaxTable defaultTaxTable, 
      List alterTaxTableList, MerchantCalculations couponInfo, 
      String editCartUrl, String continueShoppingUrl) throws ProtocolException {

    try {
      TaxTables.AlternateTaxTablesType aTables 
          = _objectFact.createTaxTablesAlternateTaxTablesType();
      List aTaxList = aTables.getAlternateTaxTable();
      if (alterTaxTableList != null) {
        for (int i = 0; i < alterTaxTableList.size(); i++) {
          aTaxList.add(alterTaxTableList.get(i));
        }
      }
      
      TaxTables taxTables = _objectFact.createTaxTables();
      taxTables.setDefaultTaxTable(defaultTaxTable);
      taxTables.setAlternateTaxTables(aTables);
      
      MerchantCheckoutFlowSupport mChkoutSupport
          = _objectFact.createMerchantCheckoutFlowSupport();
      mChkoutSupport.setContinueShoppingUrl(continueShoppingUrl);
      mChkoutSupport.setEditCartUrl(editCartUrl);
      mChkoutSupport.setMerchantCalculations(couponInfo);
      mChkoutSupport.setShippingMethods(methods);
      mChkoutSupport.setTaxTables(taxTables);
      return mChkoutSupport;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createAllowedAreasByStates</b> method is a wrapper method that calls 
   * the <code>createAllowedAreaByList</code> private method to set the list of 
   * states where a shipping method is available. This list will be incorporated
   * into the shipping restrictions for a shipping method. Also see the
   * <code>createShippingRestrictions</code> method.
   * @param states Required. This parameter contains a list of two-letter U.S. 
   * state abbreviations. Also see {@see #createStateArea(String)}
   * @throws ProtocolException if state list given is null
   */
  public AllowedAreasType createAllowedAreasByStates(List states) 
      throws ProtocolException {
    if (states == null) {
      throw new ProtocolException("states list cannot be null");
    }
    return createAllowedAreaByList(states);
  }
  
  /**
   * The <b>createAllowedAreasByZips</b> method is a wrapper method that 
   * calls the <code>createAllowedAreaByList)</code> private method to set the 
   * list of zip codes where a shipping method is available. This list will be 
   * incorporated into the shipping restrictions for a shipping method. Also 
   * see the <code>createShippingRestrictions</code> method.
   * @param zips Required. This parameter contains a list of U.S. zip codes 
   * or zip code patterns. Also see {@see #createZipArea(String)}
   * @throws ProtocolException if zips list is null
   */
  public AllowedAreasType createAllowedAreasByZips(List zips) 
      throws ProtocolException {
    if (zips == null) {
      throw new ProtocolException("zip list cannot be null");
    }
    return createAllowedAreaByList(zips);
  }
  
  /**
   * The <b>createAllowedAreasByList</b> method sets the list of states or zip 
   * codes where a shipping method is available. This list will be incorporated 
   * into the shipping restrictions for a shipping method. This method is 
   * called by either the createAllowedAreasByStates() or the 
   * createAllowedAreasByZips() method.
   * @param List Required. This parameter contains a list of U.S. two-letter
   * state abbreviations, U.S. zip codes or zip code patterns. 
   * @return AllowedAreas object
   */
  private AllowedAreasType createAllowedAreaByList(List areas) 
      throws ProtocolException {
    try {
      AllowedAreasType allowedAreas 
          = _objectFact.createShippingRestrictionsAllowedAreasType();
      List allowedList = allowedAreas.getUsStateAreaOrUsZipAreaOrUsCountryArea();
      for (int i = 0; i < areas.size(); i++) {
        allowedList.add(areas.get(i));
      }
      
      return allowedAreas;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createAllowedAreasByCountryArea</b> method adds a U.S. country area 
   * to an AllowedAreasType object, which identifies country regions, states 
   * and zip codes where a shipping method is available.
   * @param biggerArea Required. This parameter identifies the U.S. country
   * area where a shipping option is available. Valid values for this parameter
   * are CONTINENTAL_48, FULL_50_STATES and ALL.
   * @throws ProtocolException if biggerArea is null
   */
  public AllowedAreasType createAllowedAreasByCountryArea(String biggerArea) 
      throws ProtocolException {
    if (biggerArea == null) {
      throw new ProtocolException("biggerArea cannot be null");
    } else {
      if (!biggerArea.equals(USAREA_ALL)
          || !biggerArea.equals(USAREA_CONTINENTAL_48)
          || !biggerArea.equals(USAREA_FULL_50_STATES)) {
        throw new ProtocolException("biggerArea not valid; Must be one of " +
            "CONTINENTAL_48, FULL_50_STATES and ALL");
      }
    }
    
    try {
      AllowedAreasType allowedAreas 
          = _objectFact.createShippingRestrictionsAllowedAreasType();
      List allowedList = allowedAreas.getUsStateAreaOrUsZipAreaOrUsCountryArea();
      USCountryArea countryArea = _objectFact.createUSCountryArea();
      countryArea.setCountryArea(biggerArea);
      allowedList.add(countryArea);
      return allowedAreas;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createExcludedAreasByStates</b> method is a wrapper method that calls
   * the <code>createExcludedAreaByList</code> private method to set the list 
   * of states where a shipping method is not available. This list will be 
   * incorporated into the shipping restrictions for a shipping method. Also 
   * see the <code>createShippingRestrictions</code> method.
   * @param states Required. A list of two-letter U.S. state abbreviations. 
   * Also see {@see #createStateArea(String)}
   * @throws ProtocolException if state list given is null
   */
  public ExcludedAreasType createExcludedAreasByStates(List states) 
      throws ProtocolException {
    if (states == null) {
      throw new ProtocolException("states cannot be null");
    }
    return createExcludedAreaByList(states);
  }

  /**
   * The <b>createExcludedAreasByZips</b> method is a wrapper method that calls
   * the <code>createExcludedAreaByList</code> private method to set the list 
   * of zip codes where a shipping method is not available. This list will 
   * be incorporated into the shipping restrictions for a shipping method. 
   * Also see the <code>createShippingRestriction</code> method.
   * @param zips Required. This parameter contains a list of U.S. zip codes 
   * or zip code patterns. Also see {@see #createZipArea(String)}
   * @throws ProtocolException if zips list is null
   */
  public ExcludedAreasType createExcludedAreasByZips(List zips) 
      throws ProtocolException {
    if (zips == null) {
      throw new ProtocolException("zips cannot be null");
    }
    return createExcludedAreaByList(zips);
  }
  
  /**
   * The <b>createExcludedAreasByList</b> method sets the list of states or zip 
   * codes where a shipping method is not available. This list will be 
   * incorporated into the shipping restrictions for a shipping method. This 
   * method is called by either the createExcludedAreasByStates() or the 
   * createExcludedAreasByZips() method.
   * @param List Required. This parameter contains a list of U.S. two-letter
   * state abbreviations, U.S. zip codes or zip code patterns. 
   * @return ExcludedAreasType object
   */
  private ExcludedAreasType createExcludedAreaByList(List areas) 
      throws ProtocolException {
    try {
      ExcludedAreasType excludedAreas
          = _objectFact.createShippingRestrictionsExcludedAreasType();
      List excludedList 
          = excludedAreas.getUsStateAreaOrUsZipAreaOrUsCountryArea();
      for (int i = 0; i < areas.size(); i++) {
        excludedList.add(areas.get(i));
      }
      return excludedAreas;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createExcludedAreasByCountryArea</b> method adds a U.S. country area 
   * to an ExcludedAreasType object, which identifies country regions, states and 
   * zip codes where a shipping method is not available.
   * @param biggerArea Required. This parameter identifies the U.S. country
   * area where a shipping option is not available. Valid values for this 
   * parameter are CONTINENTAL_48, FULL_50_STATES and ALL. 
   * @throws ProtocolException if biggerArea is null
   */
  public ExcludedAreasType createExcludedAreasByCountryArea(String biggerArea) 
      throws ProtocolException {
    if (biggerArea == null) {
      throw new ProtocolException("biggerArea cannot be null");
    } else {
      if (!biggerArea.equals(USAREA_ALL)
          || !biggerArea.equals(USAREA_CONTINENTAL_48)
          || !biggerArea.equals(USAREA_FULL_50_STATES)) {
        throw new ProtocolException("biggerArea not valid; Must be one of " +
            "CONTINENTAL_48, FULL_50_STATES and ALL");
      }
    }
   
    try {
      ExcludedAreasType excludedAreas
          = _objectFact.createShippingRestrictionsExcludedAreasType();
      List allowedList = excludedAreas.getUsStateAreaOrUsZipAreaOrUsCountryArea();
      USCountryArea countryArea = _objectFact.createUSCountryArea();
      countryArea.setCountryArea(biggerArea);
      allowedList.add(countryArea);
      return excludedAreas;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createShippingRestrictions</b> method creates a 
   * ShippingRestrictions object, which is comprised of an AllowedAreasType 
   * object and an ExcludedAreasType object. The ShippingRestrictions object 
   * is then used to create either a FlatRateShipping object or a 
   * MerchantCalculatedShipping object.
   *
   * @param allowedAreas Optional. An AllowedAreasType object.
   * @param excludedAreas Optional. An ExcludedAreasType object.
   */
  public ShippingRestrictions createShippingRestrictions(
      AllowedAreasType allowedAreas, ExcludedAreasType excludedAreas) 
      throws ProtocolException {
    try {
      ShippingRestrictions restrict = _objectFact.createShippingRestrictions();
      restrict.setAllowedAreas(allowedAreas);
      restrict.setExcludedAreas(excludedAreas);
      return restrict;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>addShippingMethod</b> method adds a FlatRateShipping, 
   * MerchantCalculatedShipping or Pickup object to the list of
   * shipping methods available for an order and then returns that list.
   * @param shippingMethod Required. An object that represents the
   * shipping method to be added.
   * @return list of {@see FlatRateShipping}, {@see MerchantCalculatedShipping},
   * {@see Pickup}
   */
  private ShippingMethodsType addShippingMethod(Object shippingMethod)
      throws JAXBException {
    ShippingMethodsType methods 
        = _objectFact.createMerchantCheckoutFlowSupportShippingMethodsType();
    List methodList 
        = methods.getFlatRateShippingOrMerchantCalculatedShippingOrPickup();
    methodList.add(shippingMethod);
    return methods;
  }

  /**
   * The <b>createFlatRateShipping</b> method creates a {@see FlatRateShipping} 
   * object, which contains information about a shipping method that is 
   * available for the order, and adds that object to the list of the order's 
   * shipping options.
   * @param shippingName Required. A string that can be used to identify a 
   * particular shipping option, such as &quot;UPS Ground&quot;
   * @param shippingCost Required. The numeric cost of the shipping option.
   * @param shipRestrictions Optional. A {@see ShippingRestrictions} object, 
   * which identifies the areas where a shipping option is either available 
   * or unavailable.
   * @return ShippingMethodsType object
   */
  public ShippingMethodsType createFlatRateShipping(String shippingName,
      float shippingCost, ShippingRestrictions shipRestrictions)
      throws ProtocolException {
    return createFlatRateShipping(shippingName, shippingCost, null, 
        shipRestrictions);
  }
  
  /**
   * The <b>createFlatRateShipping</b> method creates a {@see FlatRateShipping}
   * object, which contains information about a shipping method that is 
   * available for the order, and adds that object to the list of the order's 
   * shipping options.
   * @param shippingName Required. A string that can be used to identify a 
   * particular shipping option, such as &quot;UPS Ground&quot;
   * @param shippingCost Required. The numeric cost of the shipping option.
   * @param currency A three-letter ISO 4217 currency code.
   * @param shipRestrictions Optional. A ShippingRestrictions object, which 
   * identifies the areas where a shipping option is either available or 
   * unavailable.
   * @return ShippingMethodsType object
   */
  public ShippingMethodsType createFlatRateShipping(String shippingName,
      float shippingCost, String currency, ShippingRestrictions shipRestrictions)
      throws ProtocolException {
    try {
      FlatRateShipping flatRate = _objectFact.createFlatRateShipping();
      flatRate.setName(shippingName);
      flatRate.setPrice(createMoney(shippingCost, currency));
      flatRate.setShippingRestrictions(shipRestrictions);
      return addShippingMethod(flatRate);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createMerchantCalculatedShipping</b> method creates a 
   * {@see MerchantCalculatedShipping} object, which contains information about 
   * a shipping method that is available for the order, and adds that object 
   * to the list of the order's shipping options.
   * @param shippingName Required. A string that can be used to identify a 
   * particular shipping option, such as &quot;UPS Ground&quot;
   * @param shippingCost Required. The numeric cost of the shipping option. 
   * This value will only be used if the &lt;merchant-calculation-callback&gt;
   * request fails.
   * @param shipRestrictions Optional. A ShippingRestrictions object, which 
   * identifies the areas where a shipping option is either available or 
   * unavailable.
   * @return ShippingMethodsType object
   */
  public ShippingMethodsType createMerchantShipping(String shippingName,
      float shippingCost, ShippingRestrictions shipRestrictions)
      throws ProtocolException {
    return createMerchantShipping(shippingName, shippingCost, null, 
        shipRestrictions);
  }

  /**
   * The <b>createMerchantCalculatedShipping</b> method creates a 
   * {@see MerchantCalculatedShipping} object, which contains information about 
   * a shipping method that is available for the order, and adds that object 
   * to the list of the order's shipping options.
   * @param shippingName Required. A string that can be used to identify a 
   * particular shipping option, such as &quot;UPS Ground&quot;
   * @param shippingCost Required. The numeric cost of the shipping option. 
   * This value will only be used if the &lt;merchant-calculation-callback&gt; 
   * request fails.
   * @param currency A three-letter ISO 4217 currency code.
   * @param shipRestrictions Optional. A ShippingRestrictions object, which 
   * identifies the areas where a shipping option is either available or 
   * unavailable.
   * @return ShippingMethodsType object
   */
  public ShippingMethodsType createMerchantShipping(String shippingName,
      float shippingCost, String currency, ShippingRestrictions shipRestrictions)
      throws ProtocolException {
    try {
      MerchantCalculatedShipping merchantCal 
          = _objectFact.createMerchantCalculatedShipping();
      merchantCal.setName(shippingName);
      merchantCal.setPrice(createMoney(shippingCost, currency));
      merchantCal.setShippingRestrictions(shipRestrictions);
      return addShippingMethod(merchantCal);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }  

  /**
   * The <b>createPickup</b> method creates a {@see Pickup} object, which 
   * contains information about a shipping method that is available for the 
   * order, and adds that object to the list of the order's shipping options.
   * @param shippingName Required. A string that can be used to identify the 
   * shipping option, such as &quot;Instore Pickup&quot;
   * @param shippingCost Required. The numeric cost of the shipping/handling 
   * option. 
   * @return ShippingMethodsType object
   */
  public ShippingMethodsType createPickup(String shippingName, float shippingCost)
      throws ProtocolException {
    return createPickup(shippingName, shippingCost, null);
  }
  
  /**
   * The <b>createPickup</b> method creates a {@see Pickup} object, which 
   * contains information about a shipping method that is available for the 
   * order, and adds that object to the list of the order's shipping options.
   * @param shippingName Required. A string that can be used to identify the 
   * shipping option, such as &quot;Instore Pickup&quot;
   * @param shippingCost Required. The numeric cost of the shipping/handling 
   * option. 
   * @param currency A three-letter ISO 4217 currency code.
   * @return ShippingMethodsType object
   */
  public ShippingMethodsType createPickup(String shippingName, 
      float shippingCost, String currency) throws ProtocolException {
    try {
      Pickup pickup = _objectFact.createPickup();
      pickup.setName(shippingName);
      pickup.setPrice(createMoney(shippingCost, currency));
      return addShippingMethod(pickup);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /** 
   * The <b>createStateArea</b> method creates a {@see USStateArea} object
   * @param state Required. A two-letter U.S. state abbreviation
   * @throws ProtocolException if no state is provided
   * @return {@see USStateArea} object
   */
  public USStateArea createStateArea(String state) 
      throws ProtocolException {
    if (StringUtil.isEmpty(state)) {
      throw new ProtocolException("invalid state: " + state);
    }
    
    try {
      USStateArea stateArea = _objectFact.createUSStateArea();
      stateArea.setState(state.toUpperCase());
      return stateArea;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createZipArea</b> method creates a {@see USZipArea} object
   * @param zip Required. A five-digit U.S. zip code or a zip code pattern
   * @throws ProtocolException if no zip code or zip code pattern is provided
   * @return {@see USZipArea} object
   */
  public USZipArea createZipArea(String zip) 
      throws ProtocolException {
    if (StringUtil.isEmpty(zip)) {
      throw new ProtocolException("invalid zip: " + zip);
    }
    
    try {
      USZipArea zipArea = _objectFact.createUSZipArea();
      zipArea.setZipPattern(zip);
      return zipArea;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createCountryArea</b> method creates a {@see USCountryArea} object
   * @param usArea Required. A U.S. country area. Valid values are
   * CONTINENTAL_48, FULL_50_STATES, and ALL.
   * @return {@see USCountryArea} object
   * @throws ProtocolException if no USArea is provided
   */
  public USCountryArea createCountryArea(String usArea)
      throws ProtocolException {
    if (usArea == null) {
      throw new ProtocolException("invalid USArea: " + usArea);
    } else {
      if (!usArea.equals(USAREA_ALL)
          || !usArea.equals(USAREA_CONTINENTAL_48)
          || !usArea.equals(USAREA_FULL_50_STATES)) {
        throw new ProtocolException("biggerArea not valid; Must be one of " +
            "CONTINENTAL_48, FULL_50_STATES and ALL");
      }
    }
    
    try {
      USCountryArea countryArea = _objectFact.createUSCountryArea();
      countryArea.setCountryArea(usArea);
      return countryArea;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createDefaultTaxRule</b> method constructs a DefaultTaxRule object, 
   * which contains information about a default rule that will be used to
   * calculate taxes for an order. This method creates a default tax rule
   * for a U.S. state. After creating all of the default tax rules for an 
   * order, call the createDefaultTaxTable method.
   * @param taxState Required. This parameter identifies the state where
   * a tax rule should be applied. Also see {@see #createStateArea(String)}
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @param isShippingTaxable Required. This parameter contains a Boolean
   * value indicating whether tax is applied to the shipping cost
   * @return {@see DefaultTaxRule} object
   */
  public DefaultTaxRule createDefaultTaxRule(USStateArea taxState, 
      float taxRate, boolean isShippingTaxable) throws ProtocolException {
    try {
      DefaultTaxRule.TaxAreaType taxArea
          = _objectFact.createDefaultTaxRuleTaxAreaType();
      taxArea.setUsStateArea(taxState);
      return createDefaultTaxRule(taxArea, taxRate, isShippingTaxable);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createDefaultTaxRule</b> method constructs a DefaultTaxRule object, 
   * which contains information about a default rule that will be used to
   * calculate taxes for an order. This method creates a default tax rule
   * for a U.S. zip code or zip code pattern. After creating all of the 
   * default tax rules for an order, call the createDefaultTaxTable method.
   * @param taxZip Required. This parameter identifies the zip code or set of
   * zip codes (using a zip code pattern) where a tax rule should be applied. 
   * Also see {@see #createZipArea(String)}
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @param isShippingTaxable Required. This parameter contains a Boolean
   * value indicating whether tax is applied to the shipping cost
   * @return {@see DefaultTaxRule} object
   */
  public DefaultTaxRule createDefaultTaxRule(USZipArea taxZip, float taxRate,
      boolean isShippingTaxable) throws ProtocolException {
    try {
      DefaultTaxRule.TaxAreaType taxArea 
          = _objectFact.createDefaultTaxRuleTaxAreaType();
      taxArea.setUsZipArea(taxZip);
      return createDefaultTaxRule(taxArea, taxRate, isShippingTaxable);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createDefaultTaxRule</b> method constructs a DefaultTaxRule object, 
   * which contains information about a default rule that will be used to
   * calculate taxes for an order. This method creates a default tax rule
   * for a U.S. country area. After creating all of the default tax rules 
   * for an order, call the createDefaultTaxTable method.
   * @param taxCountryArea Required. This parameter identifies the country
   * area where a tax rule should be applied. Also see 
   * {@see #createCountryArea(String)}
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @param isShippingTaxable Required. This parameter contains a Boolean
   * value indicating whether tax is applied to the shipping cost
   * @return {@see DefaultTaxRule} object
   */
  public DefaultTaxRule createDefaultTaxRule(USCountryArea taxCountryArea, 
      float taxRate, boolean isShippingTaxable) throws ProtocolException {
    
    try { 
      DefaultTaxRule.TaxAreaType taxArea 
          = _objectFact.createDefaultTaxRuleTaxAreaType();
      taxArea.setUsCountryArea(taxCountryArea);
      return createDefaultTaxRule(taxArea, taxRate, isShippingTaxable);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * This method creates and returns a DefaultTaxRule object to one of the 
   * other three createDefaultTaxRule() methods. This method handles all
   * tax areas (country area, state, zip).
   * @param DefaultTaxRule.TaxArea Required. This parameter identifies the 
   * country area, state or zip code where a tax rule should be applied. 
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @param isShippingTaxable Required. This parameter contains a Boolean
   * value indicating whether tax is applied to the shipping cost
   * @return {@see DefaultTaxRule} object
   */
  private DefaultTaxRule createDefaultTaxRule(DefaultTaxRule.TaxAreaType taxArea, 
      float taxRate, boolean isShippingTaxable) throws JAXBException {
    DefaultTaxRule taxRule = _objectFact.createDefaultTaxRule();
    taxRule.setRate(taxRate);
    taxRule.setShippingTaxed(isShippingTaxable);
    taxRule.setTaxArea(taxArea);
    return taxRule;
  }

  /**
   * The <b>addDefaultTaxRule</b> method adds a {@see DefaultTaxRule} to an 
   * existing {@see DefaultTaxTable}.
   * @param taxTable This parameter contains the DefaultTaxTable to which
   * the DefaultTaxRule should be added.
   * @param rule This parameter contains the DefaultTaxRule that should be
   * added to the DefaultTaxTable
   * @return DefaultTaxTable object
   */
  public DefaultTaxTable addDefaultTaxRule(DefaultTaxTable taxTable,
      DefaultTaxRule rule) throws ProtocolException {
    DefaultTaxTable returnTaxTable = taxTable;
    
    try {
      if (returnTaxTable == null) {
        returnTaxTable = _objectFact.createDefaultTaxTable();
      }
      
      TaxRulesType taxRules = _objectFact.createDefaultTaxTableTaxRulesType();
      List taxRuleList = taxRules.getDefaultTaxRule();
      if (!taxRuleList.contains(rule) && rule != null) {
        taxRuleList.add(rule);
      }
      
      returnTaxTable.setTaxRules(taxRules);
      return returnTaxTable;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createDefaultTaxTable</b> method constructs an object that contains 
   * a list of DefaultTaxRule objects.
   * @param ruleList Required. The list of {@see DefaultTaxRule} objects that
   * apply to the order.
   * @return DefaultTaxTable object
   * @throws ProtocolException if the ruleList parameter is null.
   */
  public DefaultTaxTable createDefaultTaxTable(List ruleList)
      throws ProtocolException {
    if (ruleList == null) {
      throw new ProtocolException("ruleList cannot be null");
    }
    
    try {
      TaxRulesType taxRules = _objectFact.createDefaultTaxTableTaxRulesType();
      List taxRuleList = taxRules.getDefaultTaxRule();
      for (int i = 0; i < ruleList.size(); i++) {
        taxRuleList.add(ruleList.get(i));
      }
      
      DefaultTaxTable taxTable = _objectFact.createDefaultTaxTable();
      taxTable.setTaxRules(taxRules);
      return taxTable;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createMerchantCalculations</b> method constructs a MerchantCalculations
   * object, object, which contains the information that would be contained 
   * within the &lt;merchant-calculations&gt; XML block of a Checkout API 
   * request. This information includes an indication of whether the merchant 
   * accepts coupons and/or gift certificates and, if so, the URL to which 
   * Google Checkout should send the &lt;merchant-calculation-callback&gt;. 
   * @param acceptCoupon Required. This value should be set to true if 
   * Google Checkout should allow the customer to enter merchant coupon codes
   * @param acceptGiftCerts Required. This value should be set to true if
   * Google Checkout should allow the customer to enter gift certificate codes
   * @param merchantCalculateUrl Required. The URL to which Google Checkout 
   * should send &lt;merchant-calculation-callback&gt; requests.
   */
  public MerchantCalculations createMerchantCalculations(boolean acceptCoupon,
      boolean acceptGiftCerts, String merchantCalculateUrl) 
      throws ProtocolException {
    try {
      MerchantCalculations mCal = _objectFact.createMerchantCalculations();
      mCal.setAcceptGiftCertificates(acceptGiftCerts);
      mCal.setAcceptMerchantCoupons(acceptCoupon);
      mCal.setMerchantCalculationsUrl(merchantCalculateUrl);
      return mCal;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createAlternateTaxRule</b> method constructs an AlternateTaxRule 
   * object, which contains information about a rule that will be used to 
   * calculate taxes for particular types of items in an order. This method 
   * creates an alternate tax rule for a U.S. state. 
   * @param taxState Required. This parameter identifies the state where
   * the tax rule could be applied. Also see {@see #createStateArea(String)}
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @return {@see AlternateTaxRule} object
   */
  public AlternateTaxRule createAlternateTaxRule(USStateArea state,
      float taxRate) 
      throws ProtocolException {
    try {
      AlternateTaxRule.TaxAreaType taxArea 
          = _objectFact.createAlternateTaxRuleTaxAreaType();
      taxArea.setUsStateArea(state);
      return createAlternateTaxRule(taxArea, taxRate);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createAlternateTaxRule</b> method constructs an AlternateTaxRule 
   * object, which contains information about an alternate rule that will be 
   * used to calculate taxes for particular types of items in an order. This 
   * method creates an alternate tax rule for a U.S. zip code or zip code 
   * pattern. 
   * @param taxZip Required. This parameter identifies the zip code or set of
   * zip codes (using a zip code pattern) where a tax rule could be applied. 
   * Also see {@see #createZipArea(String)}
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @return {@see AlternateTaxRule} object
   */
  public AlternateTaxRule createAlternateTaxRule(USZipArea zip,
      float taxRate) throws ProtocolException {
    try {
      AlternateTaxRule.TaxAreaType taxArea 
          = _objectFact.createAlternateTaxRuleTaxAreaType();
      taxArea.setUsZipArea(zip);
      return createAlternateTaxRule(taxArea, taxRate);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }

  /**
   * The <b>createAlternateTaxRule</b> method constructs an AlternateTaxRule 
   * object, which contains information about an alternate rule that will be 
   * used to calculate taxes for an order. This method creates an alternate 
   * tax rule for a U.S. country area. 
   * @param taxCountryArea Required. This parameter identifies the country
   * area where a tax rule could be applied. Also see 
   * {@see #createCountryArea(String)}
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @return {@see AlternateTaxRule} object
   */
  public AlternateTaxRule createAlternateTaxRule(USCountryArea area, 
      float taxRate) throws ProtocolException {
    try {
      AlternateTaxRule.TaxAreaType taxArea 
          = _objectFact.createAlternateTaxRuleTaxAreaType();
      taxArea.setUsCountryArea(area);
      return createAlternateTaxRule(taxArea, taxRate);
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * This method creates and returns an AlternateTaxRule object to one of the 
   * other three createAlternateTaxRule() methods. This method handles all
   * tax areas (country area, state, zip).
   * @param AlternateTaxRule.TaxArea Required. This parameter identifies the 
   * country area, state or zip code where a tax rule could be applied. 
   * @param taxRate Required. This parameter identifies the tax rate that
   * should be assessed. The value should be a non-negative decimal value 
   * between 0 and 1.
   * @return {@see DefaultTaxRule} object
   */
  private AlternateTaxRule createAlternateTaxRule(
      AlternateTaxRule.TaxAreaType taxArea, float taxRate) 
      throws JAXBException {
    AlternateTaxRule aTaxRule = _objectFact.createAlternateTaxRule();
    aTaxRule.setRate(taxRate);
    aTaxRule.setTaxArea(taxArea);
    return aTaxRule;
  }
  
  /**
   * The <b>addAlternateTaxRule</b> method adds an {@see AlternateTaxRule} to an 
   * existing {@see AlternateTaxTable}.
   * @param taxTable This parameter contains the AlternateTaxTable to which
   * the AlternateTaxRule should be added.
   * @param rule This parameter contains the AlternateTaxRule that should be
   * added to the AlternateTaxTable
   * @return AlternateTaxTable object
   */
  public AlternateTaxTable addAlternateTaxRule(AlternateTaxTable taxTable,
      AlternateTaxRule rule) throws ProtocolException {
    try {
      AlternateTaxTable returnTaxTable = taxTable;
      if (returnTaxTable == null) {
        returnTaxTable = _objectFact.createAlternateTaxTable();
      }
      
      AlternateTaxRulesType aRules
          = _objectFact.createAlternateTaxTableAlternateTaxRulesType();
      List taxRuleList = aRules.getAlternateTaxRule();
      if (!taxRuleList.contains(rule) && rule != null) {
        taxRuleList.add(rule);
      }
  
      returnTaxTable.setAlternateTaxRules(aRules);
      return returnTaxTable;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
  
  /**
   * The <b>createAlternateTaxTable</b> method constructs an object that 
   * contains a list of AlternateTaxRule objects. The 
   * {@see AlternateTaxTable} is identified by a unique name, such as 
   * &quot;food&quot; or &quot;services&quot;
   * @param rules Required. The list of {@see AlternateTaxRule} objects that
   * apply to the order.
   * @param taxTableName Required. A name that identifies the set of tax rules.
   * @param isTaxRulesStandAlone Required. A Boolean value that indicates how 
   * taxes should be calculated if there is no matching {@see AlternateTaxRule}
   * for the country area, state or zip code where the customer is located.
   * @return AlternateTaxTable object
   * @throws ProtocolException if the rules parameter is null.
   */
  public AlternateTaxTable createAlternateTaxTable(List rules,
      String taxTableName, boolean isTaxRulesStandAlone)
      throws ProtocolException {
    if (rules == null) {
      throw new ProtocolException("rules cannot be null");
    }
    
    try {
      AlternateTaxRulesType taxRules 
          = _objectFact.createAlternateTaxTableAlternateTaxRulesType();
      List ruleList = taxRules.getAlternateTaxRule();
      for (int i = 0; i < rules.size(); i++) {
        ruleList.add(rules.get(i));
      }
      
      AlternateTaxTable aTaxTable = _objectFact.createAlternateTaxTable();
      aTaxTable.setAlternateTaxRules(taxRules);
      aTaxTable.setName(taxTableName);
      aTaxTable.setStandalone(isTaxRulesStandAlone);
      return aTaxTable;
    } catch (JAXBException jaxbEx) {
      throw new ProtocolException(jaxbEx.getMessage());
    }
  }
}
