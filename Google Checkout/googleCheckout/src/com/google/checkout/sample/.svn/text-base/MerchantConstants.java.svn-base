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
package com.google.checkout.sample;

import com.google.checkout.sample.crypto.CryptUtil;

/**
 * The <b>MerchantConstants</b> class stores global variables, such as your 
 * merchant ID and merchant key, that are required to send API requests 
 * to Google Checkout. In addition to the merchant ID and merchant key, 
 * this class also sets variables for the various URLs to which you can 
 * post API requests.
 * <br><br>
 * To incorporate this class in your own implementation of Google Checkout,
 * you <b>must</b> set the values of the <code>merchantId</code> and
 * <code>merchantKey</code> variables to your own Google Checkout merchantId
 * and merchantKey, respectively. Also note that you will need to use a
 * different merchant ID and merchant key in your sandbox and production
 * environments.
 * <br><br>
 * Also note that the URL to which you send an API request depends on the type
 * of request you are sending:
 * 
 * 1. If your website contains a form that submits a Checkout API request
 * directly to Google Checkout, call the 
 * {@see #getCheckoutUrl()} 
 * method to retrieve the URL to which the form should post the request.<br>
 * 2. If you submit a server-to-server Checkout API request or you are
 * submitting an Order Processing API request, call the
 * {@see #getRequestUrl()} 
 * method to retrieve the URL to which you should post the request.<br>
 * 3. You may also send a diagnostic request to the Google Checkout server
 * to verify that your request contains valid XML. Call the 
 * {@see #getCheckoutDiagnoseUrl()}
 * and
 * {@see #getRequestDiagnoseUrl()}
 * to get the URLs to which you should send diagnostic requests.
 * @version 1.0 beta
 */
public class MerchantConstants {

  /**
   * +++ CHANGE ME +++
   * You need to set the value of the <code>merchantId</code> and 
   * <code>merchantKey</code> variables before using this code in your
   * Google Checkout implementation. 
   */
  private static String merchantId = "232515116726596";
  private static String merchantKey = "fOv1wHB7Di0uN7MMSz8AFQ";

  /**
   * +++ CHANGE ME +++
   * In your production environment, you need to submit API requests to
   * http://checkout.google.com rather than http://sandbox.google.com.
   * As such, you will need to modify this code so that you can submit
   * development requests to one server and production requests to another
   * server.
   */
  private static String basePostUrl = 
    "http://sandbox.google.com/cws/v2/Merchant/";
  private static String checkout = "/checkout";
  private static String request = "/request";
  private static String diagnose = "/diagnose";
  
  private MerchantConstants() {
  }
  
  /**
   * The getMerchantID method returns your merchantId.
   */
  public static String getMerchantID() {
    return merchantId;
  }
  
  /**
   * The getMerchantID method returns your merchantKey.
   */
  public static String getMerchantKey() {
    return merchantKey;
  }
  
  /**
   * The getCheckoutUrl method returns the URL to use when submitting a
   * Checkout API request from a form in your online store directly to
   * Google Checkout.
   */
  public static String getCheckoutUrl() {
    return basePostUrl + merchantId + checkout;
  }

  /**
   * The getRequestUrl method returns the URL to use when submitting a
   * Order Processing API request.
   */
  public static String getRequestUrl() {
    return basePostUrl + merchantId + request;
  }

  /**
   * The getCheckoutDiagnoseUrl method returns the URL to use when submitting 
   * a request to verify that the XML in a Checkout API request is valid.
   */
  public static String getCheckoutDiagnoseUrl() {
    return basePostUrl + merchantId + checkout + diagnose;
  }

  /**
   * The getRequestDiagnoseUrl method returns the URL to use when submitting 
   * a request to verify that the XML in an Order Processing API request 
   * is valid.
   */
  public static String getRequestDiagnoseUrl() {
    return basePostUrl + merchantId + request + diagnose;
  }

  /**
   * The getHttpAuth method returns a base64-encoded string that you will
   * set in your HTTP header to perform HTTP Basic Authentication of an
   * Order Processing API request.
   */
  public static String getHttpAuth() {
    String combinedKey = merchantId + ":" + merchantKey;
    return CryptUtil.base64Encode(combinedKey);
  }
}
