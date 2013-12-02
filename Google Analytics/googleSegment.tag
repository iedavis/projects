<%--  AUTHOR: Ian Davis   --%>
<%--  DATE: Jan '09       --%>
<%--  VERSION: Originally created on Blueprint 2007.1   --%>
<%--
  This Tag will set the Segment to be communicated to Google Analytics
--%>
<%@ tag language="java" %>
<%@ include file="/includes/taglibs.jspf"%>

<%@ attribute name="segment" %>

<c:if test="${!empty segment}">
  <c:set var="segmentOverride" value="${segment}" scope="session"/>
</c:if>