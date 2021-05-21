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

  <p>Inscritos na lista:</p>
  <c:forEach items="${AdminConsoleBean.pessoasList}" var="value">
      <c:out value="${value}"/><br>
  </c:forEach>
  <s:form action="ListaEdit" method="post">
    <s:text name="Remover: "/>
    <s:textfield name="remover" /><br>
    <s:text name="Adicionar: "/>
    <s:textfield name="adicionar" /><br>
  <s:submit />
 </s:form>
</c:when>
  <c:otherwise>
    <p> Ligacao perdida! </p><br>
  </c:otherwise>
 </c:choose>

</body>
</html>
