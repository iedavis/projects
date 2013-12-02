<%--  AUTHOR: Ian Davis   --%>
<%--  DATE: June '09       --%>
<%--  VERSION: Originally created on Blueprint 2007.1   --%>
<%--
  This Tag records an Event which can be communicated to Google Analytics
--%>
<%@ tag language="java" %>
<%@ include file="/includes/taglibs.jspf"%>

<%@ attribute name="category" %>
<%@ attribute name="action" %>
<%@ attribute name="label" %>
<%@ attribute name="value" %>

<c:if test="${!empty category and !empty action}">

  <c:catch>
    <fmt:formatNumber value="${value}" type="number" maxFractionDigits="0" var="valueInteger" />
  </c:catch>

  <c:if test="${!empty valueInteger and empty label}"> <c:set var="label" value=" " /> </c:if>

  <c:set var="googEventList" value="${googEventList}¦${category};${action};${label};${valueInteger}" scope="request"/>
</c:if>
