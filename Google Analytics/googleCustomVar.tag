<%--  AUTHOR: Ian Davis   --%>
<%--  DATE: June '09       --%>
<%--  VERSION: Originally created on Blueprint 2007.1   --%>
<%--
  This Tag records an Custom Variable
--%>
<%@ tag language="java" %>
<%@ include file="/includes/prelude.jspf"%>

<%@ attribute name="slot" %>
<%@ attribute name="variable" %>
<%@ attribute name="value" %>
<%@ attribute name="scope" %>

<c:if test="${!empty variable and !empty value}">

  <c:choose>
    <c:when test="${scope == 'visitor'}" >
      <c:set var="scope" value="1" />
    </c:when> 
    <c:when test="${scope == 'session'}" >
      <c:set var="scope" value="2" />
    </c:when> 
    <c:otherwise>
      <c:set var="scope" value="3" />
	</c:otherwise>
  </c:choose>

  <c:if test="${empty slot}"> <c:set var="slot" value="1" /> </c:if>
  <c:if test="${empty value}"> <c:set var="value" value=" " /> </c:if>
  
  <c:set var="googCustomVarList" value="${googCustomVarList}¦${slot};${variable};${value};${scope}" scope="request"/>
</c:if>
