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

import com.google.checkout.sample.MerchantConstants;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
/**
 * The <b>CryptUtil</b> class provides methods for creating the HMAC-SHA1 
 * signatures required to submit Checkout API requests to Google Checkout.
 * @version 1.0 beta
 */
public class CryptUtil {
  private static final String MAC_ALG = "HmacSHA1";
  private static final String DIGEST_ALG = "SHA-1";
  
  /**
   * The <b>generateSignature</b> method has one required parameter, which
   * is the XML document containing the customer's shopping cart data.
   * This method retrieves your merchantKey from the 
   * {@see MerchantConstants} 
   * class and then combines that key with the XML shopping cart to 
   * generate the HMAC-SHA1 signature.
   * @param cart The XML document containing the customer's shopping cart data.
   * @return Base64-encoded HMAC-SHA1 signature
   */
  public static String generateSignature(String merchantKey, String cart) {
    byte[] bKey = merchantKey.getBytes();
    byte[] data = cart.getBytes();
    Mac mac = null;

    try {
      mac = Mac.getInstance(MAC_ALG);
      mac.init(new SecretKeySpec(bKey, MAC_ALG));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // sign the shopping cart
    byte[] sig = mac.doFinal(data);

    // base64 encode the signature
    return String.valueOf(Base64Coder.encode(sig));
  }

  /**
   * The base64Encode(byte[] data)</b> method is a wrapper method that calls 
   * {@see Base64Coder#encode(byte[])}
   * and converts the return value from that method to a string.
   */
  public static String base64Encode(byte[] data) {
    return String.valueOf(Base64Coder.encode(data));
  }
  
  /**
   * The base64Encode(String data)</b> method is a wrapper method that calls 
   * {@see #base64Encode(byte[])}
   * and returns the return value from that method.
   */
  public static String base64Encode(String data) {
    return base64Encode(data.getBytes());
  }

  /**
   * The base64Encode(String data)</b> method is a wrapper method that calls 
   * {@see Base64Coder#base64Decode(String)}
   * and returns the return value from that method.
   */
  public static String base64Decode(String data) {
    return Base64Coder.decode(data);
  }
}
