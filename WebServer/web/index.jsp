<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>JSP - Hello World</title>
</head>
<body>

<br/>
<p><a href="<s:url action="RU" />">Registar Pessoa</a></p>
<p><a href="<s:url action="CE" />">Criar Eleicao</a></p>
<p><a href="<s:url action="LU" />">Listar Pessoas</a></p>
<p><a href="<s:url action="LE" />">Listar Eleicoes</a></p>
<p><a href="<s:url action="LM" />">Listar Mesas</a></p>
</body>
</html>