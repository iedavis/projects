<dsp:page>
<?xml version="1.0" encoding="UTF-8"?>

<%--  ***********************************************  --%>
<%--  OPTIONAL USER INPUT QUERY PARAMS TO SITEMAP.JSP  --%>
<dsp:getvalueof var="stores" vartype="java.lang.String" param="stores"/>
<%--  ***********************************************  --%>

<%--  *********************************  --%>
<%--  YOU MAY WANT TO EDIT THESE VALUES  --%>
<c:set var="catalogRepository" value="/atg/commerce/catalog/ProductCatalog" />
<c:set var="mapBatchSize" value="10000" />
<c:set var="changeFreq" value="weekly" />
<c:set var="staticSitemapMediaFolder" value="Sitemap" />
<c:set var="storeIdentifierParam" value="storeId" />
<%--  *********************************  --%>

<dsp:importbean bean="/atg/userprofiling/Profile"/>
<dsp:importbean bean="/atg/dynamo/droplet/RQLQueryRange"/>
<dsp:importbean bean="/atg/dynamo/droplet/RQLQueryForEach"/>
<dsp:importbean bean="/atg/dynamo/droplet/ComponentExists"/>
<dsp:importbean bean="/atg/repository/seo/CatalogItemLink"/>

<dsp:getvalueof var="thisServerName" vartype="java.lang.String" bean="/OriginatingRequest.serverName"/>
<dsp:getvalueof var="thisServerPort" vartype="java.lang.String" bean="/OriginatingRequest.serverPort"/>
<dsp:getvalueof var="thisContextRoot" vartype="java.lang.String" bean="/OriginatingRequest.contextPath" />
<dsp:getvalueof var="thisPageName" vartype="java.lang.String" bean="/OriginatingRequest.servletPath"/>

<dsp:getvalueof var="rootCat" bean="Profile.catalog.rootNavigationCategory.id"/>
<c:if test="${empty stores}"> <dsp:getvalueof var="stores" vartype="java.lang.String" bean="Profile.storeId"/> </c:if>
<c:if test="${empty stores}"> <c:set var="stores" value="null" /> </c:if>

<c:set var="catalogQuery" value="id >= :startId AND ancestorCategories INCLUDES :rootCat AND (endDate IS NULL OR endDate > :today)" />
<%-- <c:set var="catalogQuery" value="id >= :startId AND ancestorCategories INCLUDES :rootCat AND endDate > :today" /> --%>

<c:set var="serverLocationString" value="http://${thisServerName}${thisContextRoot}" />
<c:if test="${thisServerPort != '80'}"> <c:set var="serverLocationString" value="http://${thisServerName}:${thisServerPort}${thisContextRoot}" /> </c:if>

<dsp:droplet name="ComponentExists">
<dsp:param name="path" value="${catalogRepository}"/>
  <dsp:oparam name="false">
    <c:set var="catalogRepositoryExists" value="false" />
  </dsp:oparam>
</dsp:droplet>

<c:choose>
  <c:when test="${catalogRepositoryExists == 'false'}">
    <errorMessage>Catalog Repository "${catalogRepository}" is not found. Change in the code.</errorMessage>
  </c:when>

  <c:when test="${empty param.map}">

<sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
    <dsp:droplet name="RQLQueryRange">
      <dsp:param name="sitemapFolder" value="${staticSitemapMediaFolder}"/>
      <dsp:param name="queryRQL" value="parentfolder.name = :sitemapFolder"/>
      <dsp:param name="repository"  value="${catalogRepository}" />
      <dsp:param name="howMany" value="1"/>
      <dsp:param name="itemDescriptor" value="media-internal-text"/>
      <dsp:oparam name="output">

        <c:forEach items="${fn:split(stores , ',')}" var="thisStore">
  <sitemap><loc>
          <c:if test="${thisStore != 'null'}" > <c:out value="${serverLocationString}${thisPageName}?map=static&${storeIdentifierParam}=${thisStore}" /> </c:if>
          <c:if test="${thisStore == 'null'}" > <c:out value="${serverLocationString}${thisPageName}?map=static" /> </c:if>
  </loc></sitemap>
        </c:forEach>

      </dsp:oparam>
      <dsp:oparam name="empty" /><dsp:oparam name="error" />
    </dsp:droplet>

    <c:forEach items="${fn:split('category,product' , ',')}" var="itemDesc">
      <dsp:droplet name="RQLQueryForEach">
      <dsp:param name="rootCat" value="${rootCat}"/>
      <dsp:param name="today" bean="/atg/dynamo/service/CurrentDate.timeAsTimestamp"/>
      <dsp:param name="startId" value=""/>
      <dsp:param name="queryRQL" value="${catalogQuery}"/>
      <dsp:param name="itemDescriptor" value="${itemDesc}"/>
      <dsp:param name="repository"  value="${catalogRepository}" />
      <dsp:param name="sortProperties" value="+id"/>
      <dsp:param name="elementName" value="thisCatalogItem"/>
        <dsp:oparam name="output">
          <dsp:getvalueof var="thisIndex" vartype="java.lang.String" param="index"/>
          <dsp:getvalueof var="thisId" vartype="java.lang.String" param="thisCatalogItem.id"/>
          <c:if test="${thisIndex mod mapBatchSize == 0}" >

            <c:forEach items="${fn:split(stores , ',')}" var="thisStore">
  <sitemap><loc>
              <c:if test="${thisStore != 'null'}" > <c:out value="${serverLocationString}${thisPageName}?map=${itemDesc}&startId=${thisId}&${storeIdentifierParam}=${thisStore}" /> </c:if>
              <c:if test="${thisStore == 'null'}" > <c:out value="${serverLocationString}${thisPageName}?map=${itemDesc}&startId=${thisId}" /> </c:if>
  </loc></sitemap>
            </c:forEach>

          </c:if>      
        </dsp:oparam>
        <dsp:oparam name="empty" /><dsp:oparam name="error" />
      </dsp:droplet>
    </c:forEach>
</sitemapindex>
  </c:when>

  <c:when test="${param.map == 'static'}">

    <dsp:droplet name="RQLQueryRange">
      <dsp:param name="sitemapFolder" value="${staticSitemapMediaFolder}"/>
      <dsp:param name="queryRQL" value="parentfolder.name = :sitemapFolder"/>
      <dsp:param name="repository" value="${catalogRepository}" />
      <dsp:param name="itemDescriptor" value="media-internal-text"/>
      <dsp:param name="howMany" value="${mapBatchSize}"/>
      <dsp:oparam name="outputStart"> <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"> </dsp:oparam>
      <dsp:oparam name="output">
        <dsp:getvalueof var="thisPageURL" vartype="java.lang.String" param="element.name"/>
        <c:if test="${fn:startsWith(thisPageURL,'/')}" >
          <dsp:getvalueof var="thisPagePriority" vartype="java.lang.String" param="element.data"/>
          <c:if test="${empty thisPagePriority}" > <c:set var="thisPagePriority" value="0.5" /> </c:if>
          <c:set var="locURL" value="${serverLocationString}${thisPageURL}" />
          <c:if test="${!empty param.storeId}" > <c:set var="locURL" value="${locURL}?${storeIdentifierParam}=${param.storeId}" /> </c:if>
   <url>
      <loc><c:out value="${locURL}" /></loc>
      <changefreq>${changeFreq}</changefreq>
      <priority>${thisPagePriority}</priority>
   </url>
        </c:if>
      </dsp:oparam>
      <dsp:oparam name="outputEnd"> </urlset> </dsp:oparam>
      <dsp:oparam name="empty" /><dsp:oparam name="error" />
    </dsp:droplet>
  </c:when>

  <c:when test="${param.map == 'category' or param.map == 'product'}">

    <dsp:getvalueof var="startId" vartype="java.lang.String" param="startId"/>
  
    <dsp:droplet name="ComponentExists">
    <dsp:param name="path" value="/atg/repository/seo/CanonicalItemLink"/>
      <dsp:oparam name="true">
        <c:set var="useCanonicalDroplet" value="true" />
      </dsp:oparam>
      <dsp:oparam name="false">
        <c:set var="useCanonicalDroplet" value="false" />
      </dsp:oparam>
    </dsp:droplet>
	
    <c:if test="${empty startId}" >
      <dsp:droplet name="RQLQueryRange">
        <dsp:param name="queryRQL" value="ALL"/>
        <dsp:param name="repository" value="${catalogRepository}" />
        <dsp:param name="itemDescriptor" value="${param.map}"/>
        <dsp:param name="sortProperties" value="+id"/>
        <dsp:param name="howMany" value="1"/>
        <dsp:oparam name="output">
          <dsp:getvalueof var="startId" vartype="java.lang.String" param="element.id"/>
        </dsp:oparam>
        <dsp:oparam name="empty" /><dsp:oparam name="error" />
      </dsp:droplet>
	</c:if>
	
    <dsp:droplet name="RQLQueryRange">
      <dsp:param name="startId" value="${startId}"/>
      <dsp:param name="rootCat" value="${rootCat}"/>
      <dsp:param name="today" bean="/atg/dynamo/service/CurrentDate.timeAsTimestamp"/>
      <dsp:param name="queryRQL" value="${catalogQuery}"/>
      <dsp:param name="repository"  value="${catalogRepository}" />
      <dsp:param name="itemDescriptor" value="${param.map}"/>
      <dsp:param name="howMany" value="${mapBatchSize}"/>
      <dsp:param name="sortProperties" value="+id"/>
      <dsp:param name="elementName" value="thisCatalogItem"/>
      <dsp:oparam name="outputStart"> <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"> </dsp:oparam>
      <dsp:oparam name="output">
		
		<c:choose>
		  <c:when test="${useCanonicalDroplet == 'true'}" >
            <dsp:droplet name="/atg/repository/seo/CanonicalItemLink">
              <dsp:param name="item" param="thisCatalogItem"/>
              <dsp:param name="itemDescriptorName" value="${param.map}"/>
              <dsp:oparam name="output">
                <dsp:getvalueof var="pageURL" param="url" vartype="java.lang.String"/>
              </dsp:oparam>
            </dsp:droplet>
		  </c:when>
		  <c:otherwise>
            <dsp:droplet name="/atg/repository/seo/CatalogItemLink">
              <dsp:param name="item" param="thisCatalogItem"/>
              <dsp:oparam name="output">
                <dsp:getvalueof var="pageURL" vartype="java.lang.String" param="url"/>
              </dsp:oparam>
            </dsp:droplet>
		  </c:otherwise>
	    </c:choose>

        <c:if test="${!empty pageURL}" >
          <c:set var="pageURL" value="${fn:replace(pageURL, ' ', '-')}" />
          <c:set var="pageURL" value="${fn:replace(pageURL, '+', '-')}" />
		  <c:set var="separator" value="${(fn:contains(pageURL, '?')) ? '&' : '?' }" />
          <c:set var="locURL" value="${serverLocationString}${pageURL}" />
          <c:if test="${!empty param.storeId}" > <c:set var="locURL" value="${locURL}${separator}${storeIdentifierParam}=${param.storeId}" /> </c:if>
  <url>
    <loc><c:out value="${locURL}" /></loc>
    <changefreq>${changeFreq}</changefreq>
  </url>
        </c:if>

      </dsp:oparam>
      <dsp:oparam name="outputEnd"> </urlset> </dsp:oparam>
      <dsp:oparam name="empty" /><dsp:oparam name="error" />
    </dsp:droplet>
  </c:when>
  <c:otherwise>
    <errorMessage>'map' parameter value is not valid</errorMessage>
  </c:otherwise>
</c:choose>
</dsp:page>