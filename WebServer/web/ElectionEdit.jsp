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
  <c: set var="tituloAnterior" scope="session" value="${AdminConsoleBean.titulo}"/>
   <s:form action="EditElection" method="post">

    <p>Titulo:</p>
    <s:textfield name="titulo" required="true" value="${tituloAnterior}"/><br>

    <p>Descricao</p>
    <s:textfield name="descricao" required="true" value="${AdminConsoleBean.descricao}"/><br>

    <p>Tipo:</p>
    <s:textfield name="tipo" required="true" value="${AdminConsoleBean.tipo}/><br>

    <p>Inicio:</p>
    <s:select name="dia_i" list="daysList" required="true"/><s:text name="/" />
    <s:select name="mes_i" list="monthsList" required="true"/><s:text name="/" />
    <s:select name="ano_i" list="yearsList" required="true"/><br>

    <p>Fim:<p/>
    <s:select name="dia_f" list="daysList" required="true"/><s:text name="/" />
    <s:select name="mes_f" list="monthsList" required="true"/><s:text name="/" />
    <s:select name="ano_f" list="yearsList" required="true"/><br>
    <s:submit />
   </s:form>

    <s:text name="Listas"/>Listas:</s:text>
    <c:forEach items="${AdminConsoleBean.listasList}" var="value">
	<s:url action="ListaEdit" var="link">	
	    <s:param name="tituloLista"> <c:out value="${value}" /> </s:param>
	</s:url>
	<p><a href="${link}">
                <c:out value="${value}" />
            </a><br></p>
        </c:forEach>
  </c:when>
  <c:otherwise>
    <p> Ligacao perdida! </p><br>
  </c:otherwise>
 </c:choose>


</body>
</html>
