<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"  isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<head>
<title>Launch a SAML Service Provider URL</title>

<script type="text/javascript" language="javascript">
window.name="saml";
function poponload()
{
    document.forms['redirect'].submit();
}
</script>

<meta http-equiv="refresh"/>
</head>
<c:choose>
  <c:when test="${debug==true}">
     <body  style="background-color:#00003B;">
  </c:when>
  <c:otherwise>
     <body onload="poponload()">
  </c:otherwise>
</c:choose>
<font face="Arial" color="White">
<small>Redirecting to target <c:out value="${redirectUrl}"/>  ...</small>
<form name="redirect" method="post" action="<c:out value='${redirectUrl}'/>" target="saml">
  <ul>
      <c:forEach var="p" items="${paramValues}">
            <c:set var="samlidPParamKey" scope="request" value="${p.key}" />
            <c:if test="${debug==true}">
                <li><b><c:out value="${p.key}"/></b>: 
                    <c:forEach var="value" items="${p.value}">
                        <c:out value="${value}"/>
                    </c:forEach>
                </li>
            </c:if>
            <%
                String key = (String)request.getAttribute("samlidPParamKey");
                if ((key != null) && (key.length() > 4)){
                    key = key.substring(0,5);
                }
                request.setAttribute("samlidPParamKey",key);
            %>
            <c:set var="paramPrefix" scope="request" value="${samlidPParamKey}" />
            <c:if test="${(!empty p.value) && (p.key!='GSM')  && (paramPrefix!='saml_') && (p.key!='SAMLRequest') && (p.key!='saml')}">
                <c:forEach var='value' items='${p.value}'>
                    <input type="hidden" name="<c:out value='${p.key}'/>" value="<c:out value='${value}'/>"/>
                </c:forEach>
            </c:if>
      </c:forEach>
      <c:if test="${debug==true}">
          <li><input type="submit" name="mysubmit" value="Redirect to targetUrl"></input></li>
      </c:if>
  </ul>
  <input type="hidden" name="SAMLResponse" value="<c:out value='${samlToken}'/>"/>
  <c:if test="${postSamlParams==true}">
     <c:forEach var="a" items="${postParams}">
         <input type="hidden" name="<c:out value='${a.key}'/>" value="<c:out value='${a.value}'/>"/>
     </c:forEach>
     <li><input type="submit" name="saml_responseSubmit" value="Redirect to targetUrl"></input></li>
  </c:if>
</form>

<c:if test="${debug==true}">
	<form>
	<ul>
      	<li><b>SAML Assertion: </b><br>
    		<textarea cols="160" rows="30" name="comment"><c:out value="${samlxml}"/></textarea>
		</li>
      	<li><b>SAML Assertion (Base 64 encoded): </b><br>
    		<textarea cols="160" rows="30" name="comment"><c:out value="${samlToken}"/></textarea>
		</li>
	</ul>
	</form>
</c:if>

<small>Copyright Cerner Healthcare 2011</small>

</font>

</body>
</html>
