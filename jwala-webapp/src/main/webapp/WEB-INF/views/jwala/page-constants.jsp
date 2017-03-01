<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

var jwalaVars = {};
<c:forEach var="javaScriptVariable" items="${javaScriptVariables}">
    jwalaVars["${javaScriptVariable.variableName}"] = ${javaScriptVariable.variableValue};
</c:forEach>
jwalaVars["rootContextName"]="<%= request.getContextPath() %>";