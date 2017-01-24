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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The <b>AllTests</b> class provides a method that creates a JUnit test
 * suite, which manages unit tests for regression testing the 
 * CheckoutCartBuilder, the OrderTransactionBuilder and the CallBackParser.
 * @version 1.0 beta
 */
public class AllTests {

  public static void main(String[] args) {
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(
      "Test for com.google.checkout.sample.protocol");
    //$JUnit-BEGIN$
    suite.addTestSuite(CheckoutCartBuilderTest.class);
    suite.addTestSuite(OrderTransactionBuilderTest.class);
    suite.addTestSuite(CallBackParserTest.class);
    
    //$JUnit-END$
    return suite;
  }

}
