<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Title</title>
</head>
<body>

 <c:choose>
  <c:when test="${AdminConsoleBean.test}">

  <s:form action="CreateEdit" method="post">
    <s:text name="Titulo:"/><
    <s:textfield name="novoTitulo" required="true" /><br>
  <s:submit />
 </s:form>
</c:when>
  <c:otherwise>
    <p> Ligacao perdida! </p><br>
  </c:otherwise>
 </c:choose>

</body>
</html>
