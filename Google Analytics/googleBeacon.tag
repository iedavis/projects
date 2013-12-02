<%--  AUTHOR: Ian Davis  --%>
<%--  DATE: July '09     --%>
<%--  VERSION: Originally created on Blueprint 2007.1   --%>
<%--
      This Tag renders JavaScript for Google Analytics
--%>
<%@ tag language="java" %>
<%@ include file="/includes/prelude.jspf"%>

<%@ attribute name="accountNumOverride" %>
<%@ attribute name="trackedQueryParams" %>
<%@ attribute name="searchTermParam" %>
<%@ attribute name="orderAffiliation" %>
<%@ attribute name="skuDisplayProperties" %>

<dsp:importbean bean="/atg/dynamo/droplet/ComponentExists"/>
<dsp:importbean bean="/atg/dynamo/droplet/RQLQueryForEach"/>
<dsp:importbean bean="/atg/dynamo/droplet/ForEach"/>
<dsp:importbean bean="/atg/userprofiling/Profile"/>
<dsp:importbean bean="/atg/commerce/ShoppingCart"/>
<dsp:importbean bean="/atg/commerce/catalog/ProductLookup"/>
<dsp:importbean bean="/atg/commerce/catalog/CategoryLookup"/>

<dsp:droplet name="ComponentExists">
  <dsp:param name="path" value="/atg/b2cblueprint/StoreConfiguration"/>
  <dsp:oparam name="true">
    <dsp:importbean bean="/atg/b2cblueprint/StoreConfiguration" />
  </dsp:oparam>
</dsp:droplet>

<dsp:droplet name="ComponentExists">
  <dsp:param name="path" value="/atg/store/StoreConfiguration"/>
  <dsp:oparam name="true">
    <dsp:importbean bean="/atg/store/StoreConfiguration" />
  </dsp:oparam>
</dsp:droplet>

<c:set var="googleFolder" value="Google Analytics"/>
<c:set var="googleSegFolder" value="Segment Mapping"/>

<dsp:getvalueof var="thisStoreId" vartype="java.lang.String" bean="Profile.storeid"/>

<dsp:getvalueof var="thisPage" vartype="java.lang.String" bean="/OriginatingRequest.servletPath"/>
<dsp:getvalueof var="thisRequestURI" vartype="java.lang.String" bean="/OriginatingRequest.requestURI"/>
<dsp:getvalueof var="thisQueryString" vartype="java.lang.String" bean="/OriginatingRequest.queryString"/>

<dsp:getvalueof var="productId" param="productId"/>
<dsp:getvalueof var="categoryId" param="categoryId"/>
<dsp:getvalueof var="thisProfileId" bean="Profile.id"/>
<dsp:getvalueof var="rootCat" bean="Profile.catalog.rootNavigationCategory.id"/>
<dsp:getvalueof var="atgVersion" vartype="java.lang.String" bean="/atg/dynamo/Configuration.version"/>

<c:set var="skuDisplayProperties" value="${(!empty skuDisplayProperties) ? skuDisplayProperties : 'Color,Size'}" />
<c:set var="skuDisplayProperties" value="${(skuDisplayProperties == 'null') ? '' : skuDisplayProperties}" />

<c:set var="trackedQueryParams" value="productId,categoryId,${trackedQueryParams}" />

<c:set var="googTrackPageview" value="${thisRequestURI}" />

<%--  NOTE THAT WE ARE STORING THE IMPORTANT VARIABLES IN A SESSION SCOPE  --%>
<%--  SO THAT WE DON'T NEED TO RE-QUERY ON EVERY PAGE REQUEST.             --%>

<c:if test="${empty googAccountNum or empty googEnabled}" >

  <dsp:droplet name="/atg/dynamo/droplet/RQLQueryForEach">
    <dsp:param name="googleFolder" value="${googleFolder}"/>
    <dsp:param name="queryRQL" value="parentfolder.name = :googleFolder"/>
    <dsp:param name="repository"  value="/atg/commerce/catalog/ProductCatalog" />
    <dsp:param name="itemDescriptor" value="media-internal-text"/>
    <dsp:oparam name="output">
      <dsp:getvalueof var="foundElement" vartype="java.lang.String" param="element.name"/>
      <c:set var="storeSpecificAccountName" value="Account (${(!empty thisStoreId) ? thisStoreId : 'store_us'})" />

      <c:choose>
        <c:when test="${foundElement == storeSpecificAccountName}">
          <dsp:getvalueof var="googAccountNum" vartype="java.lang.String" param="element.data" scope="session"/>
        </c:when>
        <c:when test="${foundElement == 'Account'}">
          <dsp:getvalueof var="googAccountNum" vartype="java.lang.String" param="element.data" scope="session"/>
        </c:when>
        <c:when test="${foundElement == 'Enabled'}">
          <dsp:getvalueof var="googEnabled" vartype="java.lang.String" param="element.data" scope="session"/>
        </c:when>
        <c:when test="${foundElement == 'Domain Name'}">
          <dsp:getvalueof var="googDomainName" vartype="java.lang.String" param="element.data" scope="session"/>
        </c:when>
        <c:when test="${foundElement == 'AdWords Code Snippet'}">
          <dsp:getvalueof var="googAdWordsSnippet" vartype="java.lang.String" param="element.data" scope="session"/>
        </c:when>
        <c:when test="${foundElement == 'Ignore Pages'}">
          <dsp:getvalueof var="googIgnorePages" vartype="java.lang.String" param="element.data" scope="session"/>
        </c:when>
      </c:choose>

    </dsp:oparam>
    <dsp:oparam name="empty" />
    <dsp:oparam name="error" />
  </dsp:droplet>


  <%-- BUILD AN 'ATG PROFILE GROUPS' TO 'GOOGLE SEGMENTS' MAP  --%>
  <dsp:droplet name="/atg/dynamo/droplet/RQLQueryForEach">
    <dsp:param name="googleFolder" value="${googleFolder}"/>
    <dsp:param name="googleSegFolder" value="${googleSegFolder}"/>
    <dsp:param name="queryRQL" value="parentfolder.name = :googleSegFolder and parentfolder.parentfolder.name = :googleFolder "/>
    <dsp:param name="repository"  value="/atg/commerce/catalog/ProductCatalog" />
    <dsp:param name="itemDescriptor" value="media-internal-text"/>
    <dsp:oparam name="output">
      <dsp:getvalueof var="thisGoogSegment" param="element.name"/>

      <dsp:getvalueof var="theseATGsegments" param="element.data" />

      <c:forEach items="${fn:split(theseATGsegments , ',')}" var="seg">
        <c:set var="thisATGseg" value="${fn:trim(seg)}" />
        <%-- VALIDATE THAT A PROFILE GROUP EXISTS WITH THIS NAME  --%>
        <dsp:droplet name="ComponentExists">
          <dsp:param name="path" value="/atg/registry/RepositoryGroups/UserProfiles/${thisATGseg}"/>
          <dsp:oparam name="true">
            <dsp:getvalueof var="googSegmentMap" vartype="java.lang.String" value="${googSegmentMap}¦${thisATGseg};${thisGoogSegment}" scope="session"/>
          </dsp:oparam>
        </dsp:droplet>
      </c:forEach>

    </dsp:oparam><dsp:oparam name="empty" /><dsp:oparam name="error" />
  </dsp:droplet>
</c:if>


<c:choose>
  <c:when test="${empty googEnabled and empty accountNumOverride}" >
  <!-- No Google 'Enabled' entry was found nor hard-coded Account Number passed-->
  </c:when>

  <c:when test="${fn:toLowerCase(googEnabled) != 'true' and empty accountNumOverride}" >
  <!-- Google Analytics is not enabled -->
  </c:when>

  <c:when test="${empty googAccountNum and empty accountNumOverride}" >
  <!-- No Google Analytics Account Number entry was found -->
  </c:when>

  <c:when test="${fn:contains(fn:toLowerCase(googIgnorePages) , fn:toLowerCase(thisPage))}" >
  <!-- Google Analytics disabled for this particular page -->
  </c:when>

  <c:otherwise>

    <c:if test="${!empty accountNumOverride}" >
      <c:set var="googAccountNum" value="${accountNumOverride}" scope="session"/>
    </c:if>

    <%--  FILTER THE QUERY PARAMETERS  --%>
    <c:set var="thisQueryString" value="&${thisQueryString}" />
    <c:set var="thisQueryStringArray" value="${fn:split(thisQueryString , '&')}" />

    <c:forEach items="${thisQueryStringArray}" var="queryToken" >
      <c:set var="queryTokenParam" value="${fn:substringBefore(queryToken,'=')}" />
      <c:set var="queryTokenParamValue" value="${fn:substringAfter(queryToken,'=')}" />
      <c:set var="foundWanted" value="false" />

      <c:forEach items="${fn:split(trackedQueryParams , ',')}" var="wantedParam">
        <c:if test="${queryTokenParam == fn:trim(wantedParam)}" >
          <c:set var="foundWanted" value="true" />
        </c:if>
      </c:forEach>

      <c:if test="${foundWanted == 'true' and !empty queryTokenParamValue}" >
        <c:set var="filteredThisQueryString" value="${filteredThisQueryString}&${queryToken}" />
      </c:if>
    </c:forEach>

    <c:if test="${fn:startsWith(filteredThisQueryString,'&')}" >
      <c:set var="filteredThisQueryString" value="${fn:substringAfter(filteredThisQueryString,'&')}" />
    </c:if>

    <c:set var="thisQueryString" value="${filteredThisQueryString}" />

    <c:if test="${!empty thisQueryString}" >
      <c:set var="googTrackPageview" value="${thisRequestURI}?${thisQueryString}" />
    </c:if>

    <%--  GET THE SEARCH TERMS, IF THIS IS A SEARCH RESULTS PAGE  --%>
    <c:set var="atgSearchInstalled" value="false"/>
    <c:catch> <dsp:getvalueof var="atgSearchInstalled" bean="StoreConfiguration.atgSearchInstalled"/> </c:catch>

    <c:choose>

      <c:when test='${!empty searchTermParam}' >
        <dsp:getvalueof var="queryTerm" param="${searchTermParam}"/>
      </c:when>

      <c:when test='${atgSearchInstalled == "true"}' >
        <%-- THIS IS USING ATG SEARCH, SO GET THE STUFF TERM FROM THE FORM HANDLER BEANS  --%>
        <c:choose>
          <c:when test="${fn:contains(atgVersion ,'ATGPlatform/2007')}" >
            <c:catch>
              <dsp:getvalueof var="queryTerm" bean="/atg/commerce/search/ProductCatalogQueryFormHandler.question"/>
              <dsp:getvalueof var="resultSetSize" bean="/atg/commerce/search/ProductCatalogQueryFormHandler.facetSearchResponse.totalResults"/>
            </c:catch>
          </c:when>
          <c:when test="${fn:contains(atgVersion ,'ATGPlatform/9')}" >
            <c:catch>
              <dsp:getvalueof var="queryTerm" bean="/atg/commerce/search/catalog/QueryFormHandler.searchResponse.question"/>
              <dsp:getvalueof var="resultSetSize" bean="/atg/commerce/search/catalog/QueryFormHandler.searchResponse.groupCount"/>
            </c:catch>
          </c:when>
        </c:choose>

      </c:when>

      <c:when test='${atgSearchInstalled == "false" and param.mode == "simple"}' >
        <%-- THIS IS PROBABLY A BASIC SEARCH IMPLEMENTATION  --%>
        <dsp:getvalueof var="queryTerm" bean="/atg/commerce/catalog/ProductSearch.searchInput"/>
        <dsp:getvalueof var="resultSetSize" bean="/atg/commerce/catalog/ProductSearch.resultSetSize"/>
      </c:when>

      <c:when test='${atgSearchInstalled == "false" and param.mode == "adv"}' >
        <%-- THIS IS PROBABLY A BASIC SEARCH IMPLEMENTATION  --%>
        <dsp:getvalueof var="queryTerm" bean="/atg/commerce/catalog/AdvProductSearch.searchInput"/>
        <dsp:getvalueof var="resultSetSize" bean="/atg/commerce/catalog/AdvProductSearch.resultSetSize"/>
        <dsp:getvalueof var="searchCatId" bean="/atg/commerce/catalog/AdvProductSearch.hierarchicalCategoryId"/>

        <%-- IF THERE IS ONE, LOOKUP THE FILTER CATEGORY NAME  --%>
        <c:if test="${!empty searchCatId}" >
          <dsp:droplet name="CategoryLookup">
            <dsp:param name="id" value="${searchCatId}" /> 
            <dsp:oparam name="output">
              <dsp:getvalueof var="queryCat" param="element.displayName"/>
            </dsp:oparam>
            <dsp:oparam name="empty"/>
          </dsp:droplet>
        </c:if>
      </c:when>
    </c:choose>

    <fmt:message key="common.keyword" var="xkeyword"/>
    <c:if test="${fn:contains(queryTerm, xkeyword)}" >
      <%-- USER HAS JUST CLICKED ON THE SEARCH BUTTON WITHOUT ENTERING  --%>
      <%-- ANYTHING, SO NOT WORTH TRACKING AS A 'REAL' SEARCH ATTEMPT.  --%>
      <c:remove var="queryTerm" />
    </c:if>

    <c:if test="${!empty queryTerm or !empty queryCat}" >
      <%-- THIS IS A SEARCH RESULT PAGE, SO ADD SOME PARAMS ONTO THE TRACKED PAGE FOR GOOGLE TO EXAMINE  --%>

      <c:set var="queryTerm" value="${fn:toLowerCase(queryTerm)}"/>
      <c:set var="queryCat" value="${fn:toLowerCase(queryCat)}"/>

      <c:set var="charString" value="&¦ ¦,¦.¦_¦;¦:"/>
      <c:forEach items="${fn:split(charString , '¦')}" var="token">
        <c:set var="queryTerm" value="${fn:replace(queryTerm, token , '+')}"/>
        <c:set var="queryCat" value="${fn:replace(queryCat, token , '+')}"/>
      </c:forEach>

      <c:set var="charString" value="(¦)¦{¦}¦'¦\"¦[¦]"/>
      <c:forEach items="${fn:split(charString , '¦')}" var="token">
        <c:set var="queryTerm" value="${fn:replace(queryTerm, token , '')}"/>
        <c:set var="queryCat" value="${fn:replace(queryCat, token , '')}"/>
      </c:forEach>
      
      <c:set var="queryTerm" value="${fn:replace(queryTerm, '+++', '+')}"/>
      <c:set var="queryTerm" value="${fn:replace(queryTerm, '++', '+')}"/>
      <c:set var="queryCat" value="${fn:replace(queryCat, '+++', '+')}"/>
      <c:set var="queryCat" value="${fn:replace(queryCat, '++', '+')}"/>

      <c:set var="queryParamNameString" value="${(empty searchTermParam) ? 'qTerm' : searchTermParam}" />

      <c:if test="${queryTerm != lastQueryTerm or queryCat != lastQueryCat}" >
          <c:if test="${!empty queryTerm and empty queryCat}" >
          <c:set var="thisQueryString" value="${queryParamNameString}=${queryTerm}" />
          <c:set var="eventSearchString" value="${queryTerm}" />
        </c:if>
          <c:if test="${!empty queryCat}" >
          <c:if test="${empty queryTerm}" > <c:set var="queryTerm" value="all"/> </c:if>
          <c:set var="thisQueryString" value="${queryParamNameString}=${queryTerm}&qCat=${queryCat}" />
          <c:set var="eventSearchString" value="${queryTerm} (in ${queryCat} category)" />
        </c:if>

        <c:set var="googTrackPageview" value="${thisRequestURI}?${thisQueryString}" />

        <c:set var="resultSetSize" value="${(!empty resultSetSize) ? resultSetSize : 0 }" />
 
        <c:if test="${!empty eventSearchString and resultSetSize gt 0}" >
          <c:set var="googEventList" value="${googEventList}¦Search Hits;${eventSearchString}; ;${resultSetSize}" scope="request"/>
        </c:if>
        <c:if test="${!empty eventSearchString and resultSetSize == 0}" >
          <c:set var="googEventList" value="${googEventList}¦Null Search;${eventSearchString};" scope="request"/>
        </c:if>
        <c:set var="lastQueryTerm" value="${queryTerm}" scope="session"/>
        <c:set var="lastQueryCat" value="${queryCat}" scope="session"/>
      </c:if>
    </c:if>

    <%-- INSERT THE CATEGORY NAME FOR EASIER VIEWING IN REPORTS  --%>
    <c:if test="${!empty categoryId and empty productId}" >
      <dsp:droplet name="CategoryLookup">
        <dsp:param name="id" param="categoryId" /> 
        <dsp:oparam name="output">
          <dsp:getvalueof var="catName" vartype="java.lang.String" param="element.displayName"/>
          <c:set var="catName" value="${fn:replace(catName, '&' , '+')}"/>
          <c:set var="catName" value="${fn:replace(catName, '\"' , '')}"/>
          <c:set var="catName" value="${fn:replace(catName, ';' , ':')}"/>
          <c:set var="googTrackPageview" value="${thisRequestURI}?categoryName=${catName}&${thisQueryString}" />
          <c:set var="googEventList" value="${googEventList}¦Category View;(${categoryId}) ${catName}" scope="request"/>
        </dsp:oparam> <dsp:oparam name="empty" /> <dsp:oparam name="error" />
      </dsp:droplet>
    </c:if>

    <%-- INSERT THE PRODUCT NAME FOR EASIER VIEWING IN REPORTS  --%>
    <c:if test="${!empty productId}" >
      <dsp:droplet name="ProductLookup">
        <dsp:param name="id" param="productId"/>
        <dsp:oparam name="output">
          <dsp:getvalueof var="prodName" vartype="java.lang.String" param="element.displayName"/>
          <dsp:getvalueof var="prodCatName" vartype="java.lang.String" param="element.parentCategory.displayName"/>
          <c:set var="prodName" value="${fn:replace(prodName, '&' , '+')}"/>
          <c:set var="prodName" value="${fn:replace(prodName, '\"' , '')}"/>
          <c:set var="prodName" value="${fn:replace(prodName, ';' , ':')}"/>
          <c:set var="googTrackPageview" value="${thisRequestURI}?productName=${prodName}&${thisQueryString}" />

          <c:set var="googEventList" value="${googEventList}¦Product View;(${productId}) ${prodName} (${prodCatName})" scope="request"/>
          <c:if test="${!empty lastProdViewedString and not fn:contains(lastProdViewedString, productId)}" >
            <c:set var="googEventList" value="${googEventList}¦Prev Product Viewed;(${productId}) ${prodName} (${prodCatName});${lastProdViewedString}" scope="request"/>
            <c:set var="googEventList" value="${googEventList}¦Next Product Viewed;${lastProdViewedString};(${productId}) ${prodName} (${prodCatName})" scope="request"/>
          </c:if>

          <c:set var="lastProdViewedString" value="(${productId}) ${prodName} (${prodCatName})" scope="session"/>
        </dsp:oparam> <dsp:oparam name="empty" /> <dsp:oparam name="error" />
      </dsp:droplet>
    </c:if>

    <!-- START Google Analytics beacon -->
    <script type="text/javascript">
      var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
      document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
    </script>

    <script type="text/javascript">
      try {
        var pageTracker = _gat._getTracker("${fn:trim(googAccountNum)}");
        pageTracker._initData();

    <%--  ENABLE CODE FOR DOMAIN AND SUB-DOMAINS, IF ONE HAS BEEN SET  --%>
        <c:if test="${!empty googDomainName}" >
          pageTracker._setDomainName("${fn:trim(googDomainName)}");
          pageTracker._setAllowHash(false);
        </c:if>

    <%--  PROCESS ANY GOOGLE SEGMENTS  --%>
    <c:choose>
      <%-- DETERMINE IF THERE IS A CODED SEGMENT THAT HAS BEEN SET  --%>
      <c:when test="${!empty segmentOverride}" >
        pageTracker._setVar("${segmentOverride}");
        pageTracker._setCustomVar( 1 , "ATG_Mapped_Segment" , "${segmentOverride}" , 2 );
      </c:when>

      <%-- DETERMINE IF THERE IS A MAPPED SEGMENT THAT IS RELEVANT  --%>
      <c:otherwise>
        <c:forEach items="${fn:split(googSegmentMap , '¦')}" var="segmentPair">
          <c:set var="thisATGseg" value="${fn:substringBefore(segmentPair , ';')}" />
          <c:set var="thisGoogSeg" value="${fn:substringAfter(segmentPair , ';')}" />
          <dsp:getvalueof var="isSegMember" vartype="java.lang.String" bean="Profile.${thisATGseg}"/>
          <c:if test="${empty segmentMatch and isSegMember == 'true'}" >
            <c:set var="segmentMatch" value="${thisGoogSeg}" />
        pageTracker._setVar("${segmentMatch}");
        pageTracker._setCustomVar( 1 , "ATG_Mapped_Segment" , "${segmentMatch}" , 2 );
          </c:if>
        </c:forEach>
      </c:otherwise>
    </c:choose>

    <%--  PROCESS ANY GOOGLE CUSTOM VARIABLES FOUND IN THE REQUEST-SCOPED VARIABLE  --%>
    <c:if test="${!empty googCustomVarList}" >
      <c:forEach items="${fn:split(googCustomVarList , '¦')}" var="varListMember">
        <c:set var="varSetMemberArray" value="${fn:split(varListMember, ';')}" />
        pageTracker._setCustomVar( ${varSetMemberArray[0]} , "${varSetMemberArray[1]}" , "${varSetMemberArray[2]}" , ${varSetMemberArray[3]} );
      </c:forEach>
    </c:if>
    <c:remove var="googCustomVarList" scope="request"/>

        pageTracker._trackPageview("${googTrackPageview}");

		<%--  PROCESS ANY GOOGLE EVENTS FOUND IN THE REQUEST-SCOPED VARIABLE  --%>
    <c:if test="${!empty googEventList}" >
      <c:forEach items="${fn:split(googEventList , '¦')}" var="eventListMember">
        <c:set var="eventSetMemberArray" value="${fn:split(eventListMember, ';')}" />
        pageTracker._trackEvent("${eventSetMemberArray[0]}" , "${eventSetMemberArray[1]}" , "${eventSetMemberArray[2]}" <c:if test="${!empty eventSetMemberArray[3]}" > , ${eventSetMemberArray[3]} </c:if> );
      </c:forEach>
    </c:if>
    <c:remove var="googEventList" scope="request"/>

		} catch(err) {}
    </script>
    <!-- END Google Analytics beacon -->

    <%--  OK. LETS SEE IF WE NEED TO PROCESS A NEW ORDER  --%>
    <dsp:setvalue param="submittedOrder" beanvalue="/atg/commerce/ShoppingCart.last"/>
    <dsp:getvalueof var="submittedOrderID" vartype="java.lang.String" param="submittedOrder.omsOrderId"/>

    <c:if test="${!empty submittedOrderID and submittedOrderID != googLastSentOrderID}" >
      <%-- IF ShoppingCart.last HAS A VALUE, AND IT'S DIFFERENT TO THE ONE LAST SUBMITTED TO GOOGLE --%>
      <c:set var="isNewOrderSubmit" value="true" />

      <%--  STORE THE SUBMITTED ORDER ID IN A SESSION SCOPE SO WE CAN LATER RECOGNIZE A NEW ORDER  --%>
      <c:set var="googLastSentOrderID" value="${submittedOrderID}" scope="session" />
    </c:if>


    <c:if test="${isNewOrderSubmit == 'true'}" >

<%--  <dsp:getvalueof var="orderTotal" vartype="java.lang.String" param="submittedOrder.priceInfo.total"/>   --%>
      <dsp:getvalueof var="orderTotal" vartype="java.lang.String" param="submittedOrder.priceInfo.amount"/>

      <dsp:getvalueof var="orderTax" vartype="java.lang.String" param="submittedOrder.priceInfo.tax"/>
      <dsp:getvalueof var="orderShipping" vartype="java.lang.String" param="submittedOrder.priceInfo.shipping"/>

      <dsp:droplet name="/atg/dynamo/droplet/ForEach">
        <dsp:param name="array" param="submittedOrder.shippingGroups"/>
        <dsp:param name="elementName" value="thisShippingGroup"/>
        <dsp:oparam name="output">
          <dsp:getvalueof var="shippingGroupType" vartype="java.lang.String" param="thisShippingGroup.shippingGroupClassType"/>
          <c:if test="${shippingGroupType == 'hardgoodShippingGroup' and empty orderCity}" >
            <dsp:getvalueof var="orderCity" vartype="java.lang.String" param="thisShippingGroup.shippingAddress.city"/>
            <dsp:getvalueof var="orderState" vartype="java.lang.String" param="thisShippingGroup.shippingAddress.state"/>
            <dsp:getvalueof var="orderCountry" vartype="java.lang.String" param="thisShippingGroup.shippingAddress.country"/>
          </c:if>
        </dsp:oparam>
      </dsp:droplet>   <%-- Close ForEach droplet --%>

      <c:set var="orderCity" value="${(!empty orderCity) ? orderCity : 'N/A'}"/>
      <c:set var="orderState" value="${(!empty orderState) ? orderState : 'N/A'}"/>
      <c:set var="orderCountry" value="${(!empty orderCountry) ? orderCountry : 'N/A'}"/>

      <dsp:getvalueof var="thisOrderDate" param="submittedOrder.submittedDate"/>
      <c:catch> <fmt:formatDate value="${thisOrderDate}" type="both" pattern="dd-MMM-yy HH:mm" var="thisOrderDate" /> </c:catch>
      <c:set var="orderCity" value="${fn:replace(orderCity, '\"' , '')}"/>
      <c:set var="orderState" value="${fn:replace(orderState, '\"' , '')}"/>
      <c:set var="orderCountry" value="${fn:replace(orderCountry, '\"' , '')}"/>

      <dsp:getvalueof var="orderAffiliation" vartype="java.lang.String" value="${(!empty orderAffiliation) ? orderAffiliation : 'None'}"/>

      <c:set var="orderString" value="${submittedOrderID} (Cust# ${thisProfileId} - ${thisOrderDate})" />

    <!-- START Google Analytics - eCommerce Order -->
    <script type="text/javascript">
      try {

        pageTracker._addTrans( "${orderString}", "${orderAffiliation}", "${orderTotal}", "${orderTax}", "${orderShipping}", "${orderCity}", "${orderState}", "${orderCountry}" );

      <dsp:droplet name="/atg/dynamo/droplet/ForEach">
        <dsp:param name="array" param="submittedOrder.commerceItems"/>
        <dsp:param name="elementName" value="thisOrderItem"/>
        <dsp:oparam name="output">

          <dsp:getvalueof var="itemID" vartype="java.lang.String" param="thisOrderItem.catalogRefId"/>
          <dsp:getvalueof var="itemName" vartype="java.lang.String" param="thisOrderItem.auxiliaryData.catalogRef.displayName"/>
          <c:if test="${empty itemName}" > <dsp:getvalueof var="itemName" vartype="java.lang.String" param="thisOrderItem.auxiliaryData.productRef.displayName"/> </c:if>
          <dsp:getvalueof var="itemCategoryName" vartype="java.lang.String" param="thisOrderItem.auxiliaryData.productRef.parentCategory.displayName"/>
          <dsp:getvalueof var="itemParentCategoryName" vartype="java.lang.String" param="thisOrderItem.auxiliaryData.productRef.parentCategory.parentCategory.displayName"/>
          <dsp:getvalueof var="itemParentCategoryId" vartype="java.lang.String" param="thisOrderItem.auxiliaryData.productRef.parentCategory.parentCategory.id"/>
          <dsp:getvalueof var="itemQuantity" vartype="java.lang.String" param="thisOrderItem.quantity"/>

          <dsp:getvalueof var="onSale" vartype="java.lang.String" param="thisOrderItem.priceInfo.onSale"/>
          <c:choose>
            <c:when test="${onSale == 'true'}" > <dsp:getvalueof var="itemUnitPrice" vartype="java.lang.String" param="thisOrderItem.priceInfo.salePrice"/> </c:when>
            <c:otherwise> <dsp:getvalueof var="itemUnitPrice" vartype="java.lang.String" param="thisOrderItem.priceInfo.listPrice"/> </c:otherwise>
          </c:choose>

          <c:set var="categoryString" value="${itemParentCategoryName} ${itemCategoryName}" />
          <c:if test="${itemParentCategoryId == rootCat or empty itemParentCategoryName}" >
            <c:set var="categoryString" value="${itemCategoryName}" />
          </c:if>

          <%--  STRIP OUT UNICODE CHARS FROM PROD NAME, SINCE GOOGLE DOESN'T RECOGNIZE THEM   --%>
          <c:set var="itemName" value="${fn:replace(itemName, '&nbsp;' , ' ')}" />
          <c:if test="${fn:contains(itemName, '&') and fn:contains(itemName, ';')}">
            <c:set var="newName"  value="" />
            <c:forEach items="${fn:split(itemName , '&')}" var="chars">
              <c:set var="substr" value="${chars}" />
              <c:if test="${fn:contains(substr, ';')}" >
                <c:set var="substr" value="${fn:substringAfter(substr,';')}" />
              </c:if>
              <c:set var="newName" value="${newName}${substr}" />
            </c:forEach>
            <c:set var="itemName" value="${newName}" /> 
          </c:if>

          <%--  STRIP OUT ANY OBVIOUS HTML TAGS IN THE PRODUCT NAME   --%>

          <c:set var="charString" value="\"¦[¦]¦<br>¦<BR>¦<p>¦<P>¦<b>¦<B>¦<i>¦<I>"/>
          <c:forEach items="${fn:split(charString , '¦')}" var="token">
            <c:set var="itemName" value="${fn:replace(itemName, token , '')}"/>
          </c:forEach>

          <c:set var="productString" value="${itemName} (${categoryString})" />
          <c:set var="skuString" value="${itemID} ${itemName}" />

          <c:forEach items="${fn:split(skuDisplayProperties , ',')}" var="skuProp">
            <c:remove var="skuPropValue" />
            <dsp:getvalueof var="skuPropValue" param="thisOrderItem.auxiliaryData.catalogRef.${fn:trim(skuProp)}"/>
            <c:if test="${!empty skuPropValue}" >
              <c:set var="skuString" value="${skuString} ,  ${skuProp}:${skuPropValue}" />
            </c:if>
          </c:forEach>

          <c:set var="skuString" value="${fn:replace(skuString, '\"' , '#')}"/>
          <c:set var="productString" value="${fn:replace(productString, '\"' , '#')}"/>
          <c:set var="categoryString" value="${fn:replace(categoryString, '\"' , '#')}"/>

        pageTracker._addItem( "${orderString}", "${skuString}", "${productString}", "${categoryString}", "${itemUnitPrice}", "${itemQuantity}" );

        </dsp:oparam>
      </dsp:droplet>   <%-- Close ForEach droplet --%>

        pageTracker._trackTrans();

      } catch(err) {}
    </script>
    <!-- END Google Analytics - eCommerce Order -->

    </c:if>

    <%--  IF THIS IS A NEW ORDER, RENDER THE ADWORDS CONVERSION CODE SNIPPET   --%>
    <c:out value="${(isNewOrderSubmit == 'true' and !empty googAdWordsSnippet) ? googAdWordsSnippet : ''}" />

  </c:otherwise>
</c:choose>