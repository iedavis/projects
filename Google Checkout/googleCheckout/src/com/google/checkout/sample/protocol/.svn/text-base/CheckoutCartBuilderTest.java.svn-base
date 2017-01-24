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

import com.google.checkout.schema._2.Item;
import com.google.checkout.schema._2.ShoppingCart;
import com.google.checkout.schema._2.USStateArea;
import com.google.checkout.schema._2.ShippingRestrictions.AllowedAreasType;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * The <b>CheckoutCartBuilderTest</b> class contains unit tests for the
 * methods in the <b>CheckoutCartBuilder</b> class.
 * @version 1.0 beta
 */
public class CheckoutCartBuilderTest extends TestCase {
  private DocumentBuilder _docBuilder;
  
  protected void setUp() throws Exception {
    super.setUp();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    _docBuilder = factory.newDocumentBuilder();
  }
  
  /*
   * Test method for 'com.google.checkout.sample.protocol.CheckoutCartBuilder.createShoppingItem(String, String, int, float, String, Element)'
   */
  public void testCreateShoppingItem() throws Exception {
    CheckoutCartBuilder pBuilder = CheckoutCartBuilder.getInstance();
    String itemName = "Apple iPod Video 30G";
    String itemDesc = "Mp3 player";
    int quantity = 10;
    float unitPrice = 4999.9925449F;
    String currency = "USD";
    
    Document doc = _docBuilder.newDocument();
    Element elem = doc.createElement("item-notes");
    Text textValue = doc.createTextNode("SalesID# 3445");
    elem.appendChild(textValue);
    
    Item item = pBuilder.createShoppingItem(itemName, itemDesc, quantity,
      unitPrice, null, currency, elem);
    
    assertEquals(itemName, item.getItemName());
    assertEquals(itemDesc, item.getItemDescription());
    assertEquals(quantity, item.getQuantity());
    assertEquals(4999.99F, item.getUnitPrice().getValue().floatValue(), 0);
    assertEquals(currency, item.getUnitPrice().getCurrency());
    assertEquals(elem, item.getMerchantPrivateItemData().getAny());
  }

  private List makeCartItems() throws Exception {
    CheckoutCartBuilder pBuilder = CheckoutCartBuilder.getInstance();
    String[] itemNames = new String[] {"Apple iPod 30Gb", "TWiT subscription"};
    String[] itemDescs = new String[] {"mp3 player", "podcast subscription"};
    int[] quantities = new int[] {2, 1};
    float[] prices = new float[] {249.99F, 3.99F};
    List itemList = new ArrayList();
    for (int i = 0; i < itemNames.length; i++) {
      Item cartItem = pBuilder.createShoppingItem(itemNames[i], itemDescs[i], 
        quantities[i], prices[i], null, null, null);
      itemList.add(cartItem);
    }
    
    return itemList;
  }
  
  /*
   * Test method for 'com.google.checkout.sample.protocol.CheckoutCartBuilder.createShoppingCart(List<Item>, Date)'
   */
  public void testCreateShoppingCart() throws Exception {
    CheckoutCartBuilder pBuilder = CheckoutCartBuilder.getInstance();
    List cartItems = makeCartItems();
    int cartSize = cartItems.size();
    Date cartExpiry = new Date(System.currentTimeMillis() + 3600000L); 
    ShoppingCart cart = pBuilder.createShoppingCart(cartItems, cartExpiry, null);
    
    assertNotNull(cart);
    assertEquals(cartSize, cart.getItems().getItem().size());
    assertNull(cart.getMerchantPrivateData());
  }

  private List makeUSState() throws Exception {
    CheckoutCartBuilder pBuilder = CheckoutCartBuilder.getInstance();
    USStateArea caState = pBuilder.createStateArea("ca");
    USStateArea nyState = pBuilder.createStateArea("ny");
    List stateAreasList = new ArrayList();
    stateAreasList.add(caState);
    stateAreasList.add(nyState);
    return stateAreasList;
  }
  
  /*
   * Test method for 'com.google.checkout.sample.protocol.CheckoutCartBuilder.createAllowedAreasByStates(List<USStateArea>)'
   */
  public void testCreateAllowedAreasByStates() throws Exception {
    CheckoutCartBuilder pBuilder = CheckoutCartBuilder.getInstance();
    List usStateAreas = makeUSState();
    AllowedAreasType allowed = pBuilder.createAllowedAreasByStates(usStateAreas);
    List testList = allowed.getUsStateAreaOrUsZipAreaOrUsCountryArea();
    
    assertNotNull(testList);
    assertEquals(usStateAreas.size(), testList.size());
  }
}