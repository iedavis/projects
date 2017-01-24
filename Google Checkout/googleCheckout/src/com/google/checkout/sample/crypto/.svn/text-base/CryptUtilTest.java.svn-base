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
package com.google.checkout.sample.crypto;

import junit.framework.TestCase;

/**
 * The <b>CryptUtilTest</b> class contains unit tests for the methods in
 * the <code>CryptUtil</b> class.
 * @version 1.0 beta
 */
public class CryptUtilTest extends TestCase {
  private String clearTxt = "<shopping-cart><items>" +
      "<item><item-name>BlackBerry 8700c</item-name>" +
      "<item-description>personal communication device</item-description>" +
      "<unit-price currency=\"USD\">249.99</unit-price><quantity>1</quantity>" +
      "</item><item><item-name>Samsung MP3 Player</item-name>" +
      "<item-description>Mp3 player</item-description>" +
      "<unit-price currency=\"USD\">399.00</unit-price>" +
      "<quantity>1</quantity></item></items></shopping-cart>";

  private String base64Txt = "PHNob3BwaW5nLWNhcnQ+PGl0ZW1zPjxpdGVtPjxpdGVt" +
      "LW5hbWU+QmxhY2tCZXJyeSA4NzAwYzwvaXRlbS1uYW1lPjxpdGVtLWRlc2NyaXB0aW9u" +
      "PnBlcnNvbmFsIGNvbW11bmljYXRpb24gZGV2aWNlPC9pdGVtLWRlc2NyaXB0aW9uPjx1b" +
      "ml0LXByaWNlIGN1cnJlbmN5PSJVU0QiPjI0OS45OTwvdW5pdC1wcmljZT48cXVhbnRpdHk" +
      "+MTwvcXVhbnRpdHk+PC9pdGVtPjxpdGVtPjxpdGVtLW5hbWU+U2Ftc3VuZyBNUDMgUG" +
      "xheWVyPC9pdGVtLW5hbWU+PGl0ZW0tZGVzY3JpcHRpb24+TXAzIHBsYXllcjwvaXRlbS" +
      "1kZXNjcmlwdGlvbj48dW5pdC1wcmljZSBjdXJyZW5jeT0iVVNEIj4zOTkuMDA8L3VuaXQ" +
      "tcHJpY2U+PHF1YW50aXR5PjE8L3F1YW50aXR5PjwvaXRlbT48L2l0ZW1zPjwvc2hvcHBpb" +
      "mctY2FydD4=";
  
  private String testSig = "wHRu8Kzol4L/UzMfHElEC0cEHE8=";
  
  private String testKey = "fOv1wHB7Di0uN7MMSz8AFQ";
  
  private String testSH1Auth = "cce9eeb853efe23d88740f9bdd72a3edc37a1179";
  
  private String testEmail = "noreply@google.com";
  
  /*
   * Test method for 'com.google.checkout.sample.crypto.CryptUtil.generateSignature(String, String)'
   */
  public void testGenerateSignature() {
    String sig = CryptUtil.generateSignature(testKey, clearTxt);
    assertEquals(sig, testSig);
  }

  /*
   * Test method for 'com.google.checkout.sample.crypto.CryptUtil.base64Encode(byte[])'
   */
  public void testBase64EncodeByteArray() {
    byte[] clearByteArray = clearTxt.getBytes();
    char[] base64CharArray = Base64Coder.encode(clearByteArray);
    assertEquals(new String(base64CharArray), base64Txt);
  }

  /*
   * Test method for 'com.google.checkout.sample.crypto.CryptUtil.base64Encode(String)'
   */
  public void testBase64EncodeString() {
    String base64TestStr = Base64Coder.encode(clearTxt);
    assertEquals(base64TestStr, base64Txt);
  }

  /*
   * Test method for 'com.google.checkout.sample.crypto.CryptUtil.base64Decode(String)'
   */
  public void testBase64Decode() {
    String clearTestStr = Base64Coder.decode(base64Txt);
    assertEquals(clearTestStr, clearTxt);
  }
}
