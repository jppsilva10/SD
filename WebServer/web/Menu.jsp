<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>

<br/>
<c:choose>
<c:when test="${UserBean.test}">
<p><a href="<s:url action="LE2" />">Listar Eleicoes</a></p>
<p><a href="<s:url action="Connect" />">Conectar com o Facebook</a></p>
</c:when>
    <c:otherwise>
        <p> Ligacao perdida! </p><br>
    </c:otherwise>
</c:choose>

</body>
</html>