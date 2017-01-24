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



import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.commerce.CommerceException;
import atg.commerce.order.CommerceItem;
import atg.commerce.order.CommerceItemManager;
import atg.commerce.order.Order;
import atg.commerce.order.OrderHolder;
import atg.commerce.order.OrderImpl;
import atg.commerce.order.OrderManager;
import atg.commerce.order.ShippingGroup;
import atg.commerce.pricing.PricingException;
import atg.commerce.pricing.PricingTools;
import atg.commerce.pricing.ShippingPriceInfo;
import atg.commerce.pricing.ShippingPricingEngine;
import atg.commerce.states.OrderStates;
import atg.core.util.Address;
import atg.core.util.ContactInfo;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.repository.Query;
import atg.repository.QueryBuilder;
import atg.repository.QueryExpression;
import atg.repository.Repository;
import atg.repository.RepositoryException;
import atg.repository.RepositoryItem;
import atg.repository.RepositoryView;
import atg.service.pipeline.PipelineManager;
import atg.service.pipeline.PipelineResult;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.ServletUtil;
import atg.xml.tools.XMLToolsFactory;

import com.google.checkout.sample.connect.Transmitter;
import com.google.checkout.sample.protocol.CheckoutCartBuilder;
import com.google.checkout.sample.protocol.ProtocolException;
import com.google.checkout.schema._2.AnonymousAddress;
import com.google.checkout.schema._2.AnyType;
import com.google.checkout.schema._2.DefaultTaxTable;
import com.google.checkout.schema._2.Item;
import com.google.checkout.schema._2.MerchantCalculations;
import com.google.checkout.schema._2.MerchantCheckoutFlowSupport;
import com.google.checkout.schema._2.ShoppingCart;
/**
 * @author ssingh
 *
 */
public class GoogleCheckoutManager extends GenericService {

	public void doStartService(){
		try {
			cartBuilder = CheckoutCartBuilder.getInstance();			
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
		}
	}

	OrderManager orderManager;

	public OrderManager getOrderManager() {
		return orderManager;
	}


	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}


	CheckoutCartBuilder cartBuilder ;

	public CheckoutCartBuilder getCartBuilder() {
		return cartBuilder;
	}


	public void setCartBuilder(CheckoutCartBuilder pCartBuilder) {
		this.cartBuilder = pCartBuilder;
	}

	long mCartExpirationTime=1800000; 
	public long getCartExpirationTime() {
		return mCartExpirationTime;
	}


	public void setCartExpirationTime(long cartExpirationTime) {
		mCartExpirationTime = cartExpirationTime;
	}


	String callbackURL;
	public String getCallbackURL() {
		return callbackURL;
	}


	public void setCallbackURL(String callbackURL) {
		this.callbackURL = callbackURL;
	}

	ShippingPricingEngine mShippingPricingEngine;
	public ShippingPricingEngine getShippingPricingEngine() {
		return mShippingPricingEngine;
	}


	public void setShippingPricingEngine(
			ShippingPricingEngine shippingPricingEngine) {
		mShippingPricingEngine = shippingPricingEngine;
	}

	boolean defaultShipTaxable;

	public boolean isDefaultShipTaxable() {
		return defaultShipTaxable;
	}

	public void setDefaultShipTaxable(boolean defaultShipTaxable) {
		this.defaultShipTaxable = defaultShipTaxable;
	}



	boolean acceptCoupons =true;


	public boolean isAcceptCoupons() {
		return acceptCoupons;
	}


	public void setAcceptCoupons(boolean acceptCoupons) {
		this.acceptCoupons = acceptCoupons;
	}
	boolean acceptGiftCertificates=true;

	public boolean isAcceptGiftCertificates() {
		return acceptGiftCertificates;
	}


	public void setAcceptGiftCertificates(boolean acceptGiftCertificates) {
		this.acceptGiftCertificates = acceptGiftCertificates;
	}


	Repository profileRepository;

	public Repository getProfileRepository() {
		return profileRepository;
	}


	public void setProfileRepository(Repository profileRepository) {
		this.profileRepository = profileRepository;
	}	

	XMLToolsFactory xmlToolsFactory;

	public XMLToolsFactory getXmlToolsFactory() {
		return xmlToolsFactory;
	}


	public void setXmlToolsFactory(XMLToolsFactory xmlToolsFactory) {
		this.xmlToolsFactory = xmlToolsFactory;
	}

	String httpGoogleUrl="https://sandbox.google.com/cws/v2/Merchant/472634655968449/request/";
//	String httpGoogleUrl="https://sandbox.google.com/cws/v2/Merchant/472634655968449/checkout/diagnose/";


	public String getHttpGoogleUrl() {
		return httpGoogleUrl;
	}


	public void setHttpGoogleUrl(String httpGoogleUrl) {
		this.httpGoogleUrl = httpGoogleUrl;
	}
	PricingTools pricingTools;

	/**
	 * @return the pricingTools
	 */
	public PricingTools getPricingTools() {
		return pricingTools;
	}


	/**
	 * @param pricingTools the pricingTools to set
	 */
	public void setPricingTools(PricingTools pricingTools) {
		this.pricingTools = pricingTools;
	}

	GoogleMerchantCalculationManager googleMerchantCalculationManager;

	/**
	 * @return the googleMerchantCalculationManager
	 */
	public GoogleMerchantCalculationManager getGoogleMerchantCalculationManager() {
		return googleMerchantCalculationManager;
	}


	/**
	 * @param googleMerchantCalculationManager the googleMerchantCalculationManager to set
	 */
	public void setGoogleMerchantCalculationManager(
			GoogleMerchantCalculationManager googleMerchantCalculationManager) {
		this.googleMerchantCalculationManager = googleMerchantCalculationManager;
	}


	GoogleTaxCalculator googleTaxCalculator;

	/**
	 * @return the googleTaxCalcacultor
	 */
	public GoogleTaxCalculator getGoogleTaxCalculator() {
		return googleTaxCalculator;
	}


	/**
	 * @param googleTaxCalcacultor the googleTaxCalcacultor to set
	 */
	public void setGoogleTaxCalculator(GoogleTaxCalculator googleTaxCalcacultor) {
		this.googleTaxCalculator = googleTaxCalcacultor;
	}


	GoogleNewOrderNotificationManager googleNewOrderNotificationManager;

	/**
	 * @return the googleNewOrderNotificationManager
	 */
	public GoogleNewOrderNotificationManager getGoogleNewOrderNotificationManager() {
		return googleNewOrderNotificationManager;
	}


	/**
	 * @param googleNewOrderNotificationManager the googleNewOrderNotificationManager to set
	 */
	public void setGoogleNewOrderNotificationManager(
			GoogleNewOrderNotificationManager googleNewOrderNotificationManager) {
		this.googleNewOrderNotificationManager = googleNewOrderNotificationManager;
	}

	GoogleOrderStateChangeNotificationManager googleOrderStateChangeNotificationManager;

	/**
	 * @return the googleOrderStateChangeNotificationManager
	 */
	public GoogleOrderStateChangeNotificationManager getGoogleOrderStateChangeNotificationManager() {
		return googleOrderStateChangeNotificationManager;
	}


	/**
	 * @param googleOrderStateChangeNotificationManager the googleOrderStateChangeNotificationManager to set
	 */
	public void setGoogleOrderStateChangeNotificationManager(
			GoogleOrderStateChangeNotificationManager googleOrderStateChangeNotificationManager) {
		this.googleOrderStateChangeNotificationManager = googleOrderStateChangeNotificationManager;
	}


	PipelineManager pipelineManager;
	/**
	 * @return the pipelineManager
	 */
	public PipelineManager getPipelineManager() {
		return pipelineManager;
	}

	/**
	 * @param pipelineManager the pipelineManager to set
	 */
	public void setPipelineManager(PipelineManager pipelineManager) {
		this.pipelineManager = pipelineManager;
	}

	OrderStates orderStates;

	/**
	 * @return the orderStates
	 */
	public OrderStates getOrderStates() {
		return orderStates;
	}

	/**
	 * @param orderStates the orderStates to set
	 */
	public void setOrderStates(OrderStates orderStates) {
		this.orderStates = orderStates;
	}
	
	GoogleTransactionManager googlePaymentManager;

	/**
	 * @return the googlePaymentManager
	 */
	public GoogleTransactionManager getGooglePaymentManager() {
		return googlePaymentManager;
	}


	/**
	 * @param googlePaymentManager the googlePaymentManager to set
	 */
	public void setGooglePaymentManager(GoogleTransactionManager googlePaymentManager) {
		this.googlePaymentManager = googlePaymentManager;
	}
	
	Repository productCatalog;

	/**
	 * @return the productCatalog
	 */
	public Repository getProductCatalog() {
		return productCatalog;
	}


	/**
	 * @param productCatalog the productCatalog to set
	 */
	public void setProductCatalog(Repository productCatalog) {
		this.productCatalog = productCatalog;
	}
	
	
	/**
	 * 
	 * @param pURL
	 * @param pRequest
	 * @return
	 */
	public String prepareURL(String pURL,DynamoHttpServletRequest pRequest){		
		if(pRequest==null){
			pRequest=ServletUtil.getCurrentRequest();
		}
		pURL=ServletUtil.processRedirectURL(pURL, pRequest, false);

		pURL=pURL+pRequest.getURLSessionIdSpecifier()+pRequest.getRequestedSessionId();
		return pURL;
	}

	/**
	 * @param order
	 * @param profile
	 * @return 
	 * @throws CommerceException 
	 */
	public String postOrderToGoogle(Order order,Collection pPrcingModels,Locale pLocale, 
			RepositoryItem pProfile, Map pExtraParameters,String pEditURL,String pContinueURL) throws CommerceException {

		String xmlOrder=converToGoogleCart(order, pPrcingModels, pLocale, pProfile, pExtraParameters,pEditURL,pContinueURL);		
		return postXMLMessageToGoogle(xmlOrder);

	}

	

	/**
	 * converts atg items to Google items
	 * @param pAtgItems
	 * @return
	 * @throws CommerceException
	 */
	List convertATGToGoogleItems(List pAtgItems) throws CommerceException{
		if(pAtgItems==null ||pAtgItems.size()==0){
			if(isLoggingError())
				logError("Error: No items passed for conversion, returning null");
			return null;
		}
		List googleItems = new ArrayList();
		Iterator ciItr = pAtgItems.iterator();
		CommerceItem ci =null;
		RepositoryItem productRef,skuRef=null;
		String currency,description = null,name=null;
		int qty;
		float price;

		CommerceItemManager cim;
		while(ciItr.hasNext()){
			ci=(CommerceItem)ciItr.next();
			// filter SoftgoodItems
			productRef =(RepositoryItem) ci.getAuxiliaryData().getProductRef();
			skuRef = (RepositoryItem) ci.getAuxiliaryData().getCatalogRef();
			if(productRef==null){
				if(isLoggingError())
					logError("Error: Product Data is null, cannot set name on items");
			}
			if(skuRef==null){
				if(isLoggingError())
					logError("Error: Sku Data is null, cannot set name on items");
			}			
		//	name=(String) productRef.getPropertyValue(Constants.PROPERTY_NAME);
		//	description=(String) productRef.getPropertyValue(Constants.PROPERTY_DESCRIPTION);
			qty=(int) ci.getQuantity();
			price=(float) ci.getPriceInfo().getListPrice();
			currency=ci.getPriceInfo().getCurrencyCode();
			//Element privateData=null;
			/*privateData = this.getCartBuilder().createItemPrivateData("1233","123445");
			if(isLoggingDebug()){
				logDebug(" AnyType ="+privateData);
				logDebug(" AnyType.getAny ="+privateData);
				logDebug(" AnyType.getAny.class ="+privateData.getClass());
			}		
			*/

			if(isLoggingDebug()){
				logDebug("Attempting to create a new Google Skus Id="+skuRef.getRepositoryId()+" ProductId="+productRef.getRepositoryId()+" qty="+qty+" price="+price+" currency ="+currency);
			}
			googleItems.add(convertATGToGoogleItem(skuRef.getRepositoryId(),productRef.getRepositoryId(),qty,price,currency,null,null));
		}
		return googleItems;
	}

	/**
	 * takes information about an item and creates google item for it.
	 * @param pName
	 * @param pDesciption
	 * @param pQty
	 * @param pPrice
	 * @param pCurrency
	 * @param pTaxTableSelector
	 * @param pPrivateItemData
	 * @return
	 * @throws CommerceException
	 */
	Item convertATGToGoogleItem(String pName, String pDesciption, int pQty, float pPrice, String pCurrency, String pTaxTableSelector, Element pPrivateItemData) throws CommerceException{
		Item item=null;
		try {
			item=getCartBuilder().createShoppingItem(pName, pDesciption, pQty, pPrice, pCurrency, pTaxTableSelector, pPrivateItemData);			
			if(isLoggingDebug())
				logDebug("Item Merchant Private Data ="+item.getMerchantPrivateItemData());

		} catch (ProtocolException e) {
			throw new CommerceException(e);
		}	

		return item;

	}


	/**
	 * @param order
	 * @throws CommerceException 
	 */
	String converToGoogleCart(Order pOrder,Collection pPrcingModels,Locale pLocale, 
			RepositoryItem pProfile, Map pExtraParameters, String pEditURL,String pContinueURL) throws CommerceException {
		// TODO Auto-generated method stub

		if(isLoggingDebug())
			logDebug("Convert To XML called for order ="+pOrder);
		//pOrder.getCommerceItems();

		// read from ATG order->items, description, price, qty etc
		// create Google cart

		List atgItems = pOrder.getCommerceItems();
		List googleItems=convertATGToGoogleItems(atgItems);
		Date cartExpiration = new Date(System.currentTimeMillis()+getCartExpirationTime());
		ShoppingCart cart;
		Document googleCart=null;
		try {
			Element merchantPrivateData=null;
			cart =getCartBuilder().createShoppingCart(googleItems, cartExpiration, merchantPrivateData);

			MerchantCheckoutFlowSupport checkoutFlowSupport=null;
			checkoutFlowSupport=createMerchatFlowSupport(pOrder, pPrcingModels, pLocale, pProfile, pExtraParameters,pEditURL,pContinueURL);
			googleCart=getCartBuilder().createCheckoutCart(cart, checkoutFlowSupport);
			fixItemsinCart(googleCart);
			createMerchantPrivateData(googleCart,pOrder);

			// Hack: Google code does not work, so we have to force ship methods in the document
			Map shipMethodVsCost = this.getAvailableShippingMethods(pOrder, pPrcingModels, pLocale, pProfile, pExtraParameters);
			createShipMethods(googleCart, shipMethodVsCost);
			setMerchanCalculationFlag(googleCart);

			if(googleCart==null){
				if(isLoggingError())
					logError("Failed to create cart for Google");
				return null;
			}
			String catStr=CheckoutCartBuilder.unmarshal(googleCart);
			if(isLoggingDebug())
				logDebug("Google cart:"+catStr);

			return catStr;

			//cartBuilder  to create Googgle cart
		} catch (ProtocolException e) {
			if(isLoggingError())
				logError(e);
			throw new CommerceException(e);
		}


	}


	/**
	 * @param googleCart
	 * @throws GoogleCheckoutException 
	 */
	void fixItemsinCart(Document googleCart) throws GoogleCheckoutException {
		
		NodeList items=googleCart.getElementsByTagName(Constants.ITEM_TAG);
		
		if(items==null)
			return;

		for(int i=0;i<items.getLength();i++){
			
			try {
				fixItem(googleCart,items.item(i));
			} catch (RepositoryException e) {
				if(isLoggingError())
					logError(e);
				throw new GoogleCheckoutException(e);
			}
		}
	}


	/**
	 * overwrites name, description and creates merchant-item-id tag.
	 * because of Google Bug, we have to resort to this
	 * @param googleCart
	 * @param node
	 * @throws RepositoryException 
	 */
	void fixItem(Document googleCart, Node item) throws RepositoryException {
		
		NodeList children = item.getChildNodes();
		String merchantItemId,itemName = null,itemDescr = null;
		Node itemChildNode=null;
		String skuId=null,productId=null;
		RepositoryItem sku=null,product=null;
		for (int i = 0; i < children.getLength(); i++)
		{
			itemChildNode=children.item(i);
			if(isLoggingDebug())
				logDebug("Processing node "+itemChildNode);
			if (itemChildNode.getNodeName().equals(Constants.ITEM_NAME_TAG))
			{
				skuId=itemChildNode.getFirstChild().getNodeValue();
				if(isLoggingDebug())
					logDebug("skuId "+skuId);
				sku=getProductCatalog().getItem(skuId, Constants.SKU_ITEM_DESC);
				if(sku!=null){
					itemName=(String) sku.getPropertyValue(Constants.PROPERTY_NAME);
					if(isLoggingDebug())
						logDebug("Name is "+itemName);
					itemChildNode.getFirstChild().setNodeValue(itemName);
				}
			}
			if (itemChildNode.getNodeName().equals(Constants.ITEM_DESCRIPTION_TAG))
			{
				productId=itemChildNode.getFirstChild().getNodeValue();
				if(isLoggingDebug())
					logDebug("productId "+productId);
				product=getProductCatalog().getItem(productId, Constants.PRODUCT_ITEM_DESC);
				if(product!=null){
					itemDescr=(String) product.getPropertyValue(Constants.PROPERTY_DESCRIPTION);
					if(isLoggingDebug())
						logDebug("Descr is "+itemDescr);
					itemChildNode.getFirstChild().setNodeValue(itemDescr);
				}
			}			
		}
		
		createMerchantPrivateItemTag(googleCart,skuId,productId,item);

	}


	/**
	 * @param googleCart
	 * @param skuId
	 * @param productId
	 * @param item 
	 */
	void createMerchantPrivateItemTag(Document document, String skuId, String productId, Node item) {
		if(document==null){
			if(isLoggingError())
				logError("Document is null");
			return ;
		}

		if(skuId==null){
			if(isLoggingError())
				logError("SkuId null, cannot create merchant private data, integration will fail");
			return ;
		}
		Element privateDataElement=document.createElement(Constants.MERCHANT_PRIVATE_ITEM_TAG);
		Element atgElement=document.createElement(Constants.MERCHANT_PRIVATE_ITEM_ATG_TAG);
		Element skuElement= document.createElement(Constants.MERCHANT_PRIVATE_ITEM_SKUID_TAG);		
		Element productElement=document.createElement(Constants.MERCHANT_PRIVATE_ITEM_PRODUCTID_TAG);
		
		Text skuText=document.createTextNode(skuId);
		Text productText=document.createTextNode(productId);		
		skuElement.appendChild(skuText);		
		productElement.appendChild(productText);
		
		atgElement.appendChild(productElement);
		atgElement.appendChild(skuElement);
		
		privateDataElement.appendChild(atgElement);	
		
		item.appendChild(privateDataElement);
		
	}


	/**
	 * set the merchant-calculated in default tax table tag, bug in Google API
	 * @param googleCart
	 */
	void setMerchanCalculationFlag(Document googleCart) {

		NodeList taxList=googleCart.getElementsByTagName(Constants.TAX_TABLES_TAG);
		if(taxList==null)
			return;
		Element elem=(Element) taxList.item(0);
		elem.setAttribute(Constants.MERCHANT_CALCULATED_TAG, "true");		
	}


	/**
	 * @param order
	 * @return
	 * @throws CommerceException 
	 */
	MerchantCheckoutFlowSupport createMerchatFlowSupport(Order order,Collection pPrcingModels,Locale pLocale, 
			RepositoryItem pProfile, Map pExtraParameters, String pEditURL,String pContinueURL) throws CommerceException {


		DefaultTaxTable taxTable=getGoogleTaxCalculator().calculateTaxTables(order,pProfile, null, 0, this.isDefaultShipTaxable());		 
		try {
			String callbackURL=prepareURL(getCallbackURL(), null);
			MerchantCalculations mc=this.getCartBuilder().createMerchantCalculations(this.isAcceptCoupons(),this.isAcceptGiftCertificates(), callbackURL);

			/*			  
			Set methods = shipMethods.keySet();
			Iterator methodItr = methods.iterator();
			Float cost;
			String method;
			ShippingMethodsType tempShipMethodsType=null,shipMethodsType=null;
			while(methodItr.hasNext()){
				method=(String)methodItr.next();
				cost=(Float) shipMethods.get(method);
				if(isLoggingDebug())
					logDebug("SHpi Method = "+method+" cost ="+cost);
				tempShipMethodsType=this.getCartBuilder().createMerchantShipping(method, cost.floatValue(), null);
				if(shipMethodsType==null){
					shipMethodsType=tempShipMethodsType;
				}else{
					shipMethodsType.getFlatRateShippingOrMerchantCalculatedShippingOrPickup().add(tempShipMethodsType.getFlatRateShippingOrMerchantCalculatedShippingOrPickup());
				}

			}
			 */

			MerchantCheckoutFlowSupport mfs= this.getCartBuilder().createMerchantSupport(null, taxTable, null, mc, pEditURL, pContinueURL);
			return mfs;
		} catch (ProtocolException e) {
			throw new CommerceException(e);
		}
	}


	/**
	 * Creates merchant calculated shipping options, Google code does not create the right XML so we have to do it ourselves.
	 * @param document
	 * @param shipMethodVsCost
	 */
	void createShipMethods(Document document, Map shipMethodVsCost){

		if(document==null){
			if(isLoggingError())
				logError("Document is null");
			return ;
		}
		if(shipMethodVsCost==null){
			if(isLoggingError())
				logError("shipMethodVsCost is null");
			return ;
		}
		Set methods = shipMethodVsCost.keySet();
		Iterator methodItr = methods.iterator();
		Float cost;
		String method;
		Element flowSupport =
			(Element)document.getElementsByTagName("merchant-checkout-flow-support").item(0);
		Element shippingMethods = document.createElement("shipping-methods");

		while(methodItr.hasNext()){
			method=(String)methodItr.next();
			cost=(Float) shipMethodVsCost.get(method);
			if(isLoggingDebug())
				logDebug("SHpi Method = "+method+" cost ="+cost);
			Set keys = shipMethodVsCost.keySet();

			Element merchantCalculatedShipping =
				document.createElement(Constants.MERCHANT_SHIPPING_CALCULATION_ELEMENT);
			merchantCalculatedShipping.setAttribute("name", method);
			Element price = document.createElement("price");
			price.setAttribute("currency", "USD");
			price.appendChild(document.createTextNode(cost.toString()));
			merchantCalculatedShipping.appendChild(price);
			shippingMethods.appendChild(merchantCalculatedShipping);
			flowSupport.appendChild(shippingMethods); 
		}		 
	}




	/**
	 * computes the shipping cost for the shipping method set in shipping group
	 * @param order
	 * @param sg
	 * @param shipMethods
	 * @return
	 * @throws PricingException 
	 */
	public float priceShippingMethod(Order pOrder, ShippingGroup pSg,Collection pPricingModels,
			Locale pLocale, RepositoryItem pProfile,
			Map pExtraParameters) throws PricingException {
		ShippingPriceInfo spi=this.getShippingPricingEngine().priceShippingGroup(pOrder, pSg, pPricingModels, pLocale, pProfile, pExtraParameters);
		if(spi!=null)
			return (float) spi.getAmount();
		else
			return 0;
	}

	/**
	 * returns a map of available shipping methods, key is Shippingmethod and value is cost.
	 * @param pOrder
	 * @param pPrcingModels
	 * @param pLocale
	 * @param pProfile
	 * @param pExtraParameters
	 * @return
	 * @throws CommerceException
	 */
	public Map getAvailableShippingMethods(Order pOrder,Collection pPrcingModels,Locale pLocale, 
			RepositoryItem pProfile, Map pExtraParameters) 
	throws CommerceException{
		List sgs=pOrder.getShippingGroups();
		ShippingGroup sg= (ShippingGroup) sgs.get(0);
		List atgShipMethods=null;
		Map shipMethodCostMap = new HashMap();
		try {
			atgShipMethods=getShippingPricingEngine().getAvailableMethods(sg, pPrcingModels, pLocale, pProfile, pExtraParameters);
			// price each shipping method for default price and create shipMethodsType
			if(isLoggingDebug())
				logDebug("Shipping Methods = "+atgShipMethods);
			Iterator shipItr=atgShipMethods.iterator();
			String shipMethod;
			float price;
			while(shipItr.hasNext()){

				shipMethod= (String)shipItr.next();
				sg.setShippingMethod(shipMethod);
				price=priceShippingMethod(pOrder,sg,pPrcingModels, pLocale, pProfile, pExtraParameters);
				shipMethodCostMap.put(shipMethod, new Float(price));

				if(isLoggingDebug())
					logDebug("Ship Method ="+shipMethod+" price ="+price);

			}

			return shipMethodCostMap;

		} catch (PricingException e) {
			throw new CommerceException (e);
		}

	}

	/**
	 * Creates the merchant private data, basically we need ATG Order Id and session id (maybe)
	 * in merchant private data.
	 * @param pOrder
	 * @return
	 */
	public void createMerchantPrivateData(Document document,Order pOrder){

		if(document==null){
			if(isLoggingError())
				logError("Document is null");
			return ;
		}

		if(pOrder==null){
			if(isLoggingError())
				logError("Order is not null, cannot create merchant private data, integration will fail");
			return ;
		}
		Element atgElement=document.createElement(Constants.ATG_PRIVATE_DATA_TAG);
		Element orderElement= document.createElement(Constants.ORDER_TAG);
		if(isLoggingDebug())
			logDebug(" Class for order element is "+orderElement.getClass());
				
		Element profileElement=document.createElement(Constants.PROFILE_TAG);
		Text orderText=document.createTextNode(pOrder.getId());
		Text profileText=document.createTextNode(pOrder.getProfileId());		
		orderElement.appendChild(orderText);		
		profileElement.appendChild(profileText);
		Element shoppingCartElement =
			(Element)document.getElementsByTagName(Constants.SHOPPINGCART_TAG).item(0);
		Element privateData=document.createElement(Constants.MERCHANT_PRIVATE_DATA_TAG);
		atgElement.appendChild(profileElement);
		atgElement.appendChild(orderElement);
		privateData.appendChild(atgElement);		
		shoppingCartElement.appendChild(privateData);
		

	}
	

	/**
	 * transmits the xml-cart to google
	 * @param xmlOrder
	 * @throws CommerceException 
	 */
	public String postXMLMessageToGoogle(String pXmlOrder) throws GoogleCheckoutException {

		URL url;
		String response=null;
		if(pXmlOrder==null){
			if(isLoggingError())
				logError("XML message is null?");
		}		
		try {
			if(isLoggingDebug())
				logDebug("Sending message to Google");
			url = new URL(httpGoogleUrl);
			response=Transmitter.transmit(url, pXmlOrder);
			if(isLoggingDebug())
				logDebug("Done Sending message to Google, response "+response);
		
		} catch (MalformedURLException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		} catch (IOException e) {
			if(isLoggingError())
				logError(e);			
			throw new GoogleCheckoutException(e);	
		}
		return response;

	}



	/**
	 * copies Google address to ATG address
	 * address1 and 2 remain null (Google doesn't send those to us)
	 * ATG Address city- Google Address City
	 * ATG Address state- Google Address Region
	 * ATG Address Country- Google Address CountryCode
	 * ATG Address PostalCode- Google Address PostalCode
	 * @param address
	 * @param atgAddress
	 */
	public void copyAddressToShippingGroup(AnonymousAddress address, Address atgAddress) {
		if(atgAddress==null||address==null)
			return;

		atgAddress.setAddress1(null);
		atgAddress.setAddress2(null);
		atgAddress.setCity(address.getCity());
		atgAddress.setState(address.getRegion());
		atgAddress.setCountry(address.getCountryCode());
		atgAddress.setPostalCode(address.getPostalCode());		
		if(isLoggingDebug()){
			logDebug("ATG Shipping Address is "+atgAddress.getCity()+","+atgAddress.getState()+","+atgAddress.getPostalCode()+","+atgAddress.getCountry());
		}
	}



	/**
	 * copies Google address to ATG address
	 * address1 and 2 remain null (Google doesn't send those to us)
	 * ATG Address city- Google Address City
	 * ATG Address state- Google Address Region
	 * ATG Address Country- Google Address CountryCode
	 * ATG Address PostalCode- Google Address PostalCode
	 * @param address
	 * @param atgAddress
	 */
	public void copyAddressToATGAddress(com.google.checkout.schema._2.Address address, atg.core.util.Address atgAddress) {
		if(atgAddress==null||address==null)
			return;


		if(atgAddress instanceof ContactInfo)
			((ContactInfo)atgAddress).setEmail(address.getEmail());
		String[] name=StringUtils.splitStringAtCharacter( address.getContactName(), ' ');

		atgAddress.setFirstName(name[0]);
		atgAddress.setLastName(name[1]);

		atgAddress.setAddress1(address.getAddress1());
		atgAddress.setAddress2(address.getAddress2());
		atgAddress.setCity(address.getCity());
		atgAddress.setState(address.getRegion());
		atgAddress.setCountry(address.getCountryCode());
		atgAddress.setPostalCode(address.getPostalCode());		
		if(isLoggingDebug()){
			logDebug("ATG Shipping Address is "+atgAddress.getCity()+","+atgAddress.getState()+","+atgAddress.getPostalCode()+","+atgAddress.getCountry());
		}
	}



	/**
	 * executes google process order pipeline chain
	 * @param order
	 * @throws GoogleCheckoutException 
	 */
	void runProcessGoogleOrderPipelineChain(Order order,HashMap extraParameters) throws GoogleCheckoutException {

		try {
			if(isLoggingDebug())
				logDebug("Invoking pipeline");
			PipelineResult result =getOrderManager().processOrder(order,Constants.PROCESS_GOOGLE_ORDER,extraParameters);
			if(isLoggingDebug())
				logDebug("Done: Invoking pipeline");
			if(result!=null){
				Object[] keys=result.getErrorKeys();
				Object[] errors=result.getErrors();
				if(keys!=null){
					for(int i=0;i<keys.length;i++){
						if(isLoggingDebug())
							logDebug("Error key is"+keys[i]+" error ="+errors[i]);
					}
				}
			}

		} catch (CommerceException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}

	}

	/**
	 * @param googleOrderId
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	public Order getGoogleOrder(String googleOrderId) throws GoogleCheckoutException {

		try {
			Repository rep = getOrderManager().getOrderTools().getOrderRepository();
			RepositoryView view = rep.getView(Constants.ORDER_ITEM_DESCRIPTOR);
			QueryBuilder qb=view.getQueryBuilder();
			QueryExpression qe1=qb.createConstantQueryExpression(googleOrderId);

			QueryExpression qe2=qb.createPropertyQueryExpression(Constants.PROPERTY_GOOGLE_ORDER_ID);
			Query query=qb.createComparisonQuery(qe1, qe2,QueryBuilder.EQUALS);
			RepositoryItem [] items=view.executeQuery(query);
			if(items==null || (items.length==0)){
				if(isLoggingDebug())
						logDebug(" Order does not already exists with id ="+googleOrderId);
				return null;
			}
			else {
				if(isLoggingDebug())
					logDebug(" Order does already exists with id ="+googleOrderId);
				String orderId =(String) items[0].getPropertyValue(Constants.PROPERTY_ORDER_ID);
				return getOrderManager().loadOrder(orderId);				
			}
		} catch (CommerceException e) {
			throw new GoogleCheckoutException(e);
		} catch (RepositoryException e) {
			throw new GoogleCheckoutException(e);
		}
	}


	/**
	 * @param order
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	public String getGoogleOrderIdFromOrder(Order order) throws GoogleCheckoutException {
		RepositoryItem rep = ((OrderImpl)order).getRepositoryItem();
		String googleOrderId=null;
		try {
			googleOrderId = (String) DynamicBeans.getPropertyValue(rep,Constants.PROPERTY_GOOGLE_ORDER_ID);
		} catch (PropertyNotFoundException e) {
			if(isLoggingError())
				logError(e);
			throw new GoogleCheckoutException(e);
		}		
		return googleOrderId;
	}
	
	
	/**
	 * extracts the orderId from the Merchant Private data,without orderId we have no idea which order to price.
	 * @param type
	 * @return
	 * @throws GoogleCheckoutException 
	 */
	public Map findATGOrderAndProfileIdFromEventPrivateData(AnyType type) throws GoogleCheckoutException{
		
		if(type==null){
			if(isLoggingError())
				logError(" Private Data is null from Google !! ERROR");
			return null;
		}				
		Object obj=type.getAny();
		if(obj==null){
			if(isLoggingDebug())
				logDebug("What's inside private data =?"+obj);			
			return null;
		}		
		StringReader stringReader = new StringReader((String) obj);
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
    	
	    NodeList atgProfile = doc.getElementsByTagName("atgProfile");
	    NodeList atgOrder = doc.getElementsByTagName("atgOrder");
	    Node atgProfileNode=atgProfile.item(0);
	    NodeList profileNodes=atgProfileNode.getChildNodes();
	    Node atgOrdereNode=atgOrder.item(0);
	    NodeList orderNodes=atgOrdereNode.getChildNodes();
	    
	    Map result = new HashMap();
	  
	    result.put(Constants.PROFILE_ID,profileNodes.item(0).getNodeValue());
	    result.put(Constants.ORDER_ID,orderNodes.item(0).getNodeValue());
	    if(isLoggingDebug())
	    	logDebug("Returning results from private data "+result);
		
	    return result;
	}
	
	/**
	 * finds order from the Shopping Cart, cart is part of the event.
	 * Uses Merchant private data to find order information.
	 * @param cart
	 * @return
	 * @throws CommerceException
	 */
	public Order findOrderFromEvent(ShoppingCart cart) throws CommerceException{
		if(cart==null)
			return null;
		Map privateData=findATGOrderAndProfileIdFromEventPrivateData(cart.getMerchantPrivateData());
		if(isLoggingDebug())
			logDebug(" Private Data from Google ="+privateData);
		String orderId=(String) privateData.get(Constants.ORDER_ID);
		String profileId=(String) privateData.get(Constants.PROFILE_ID);
		
		DynamoHttpServletRequest request=ServletUtil.getCurrentRequest();
		OrderHolder orderHolder=(OrderHolder) request.resolveName(Constants.SHOPPING_CART_PATH);
		Order order = orderHolder.getCurrent();
		if(isLoggingDebug())
			logDebug(" Order Holder ="+orderHolder+" current ="+order.getId());
		if(!order.getId().equals(orderId)){
			if(isLoggingError())
				logError("ORder Id does not match with the session Order, cannot process this order, please place the order again");
			// GOOGLE is not returning the merchant data this weekend, so don't throw exception, see how far we can go
			//throw new CommerceException(" ORder Id does not match with the session Order, cannot process this order, please place the order again");
		}
		return order;
	}

	
}
