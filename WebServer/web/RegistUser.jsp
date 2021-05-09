<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Title</title>
</head>
<body>
  <s:form action="RegistUser" method="post">
    <s:text name="Tipo:" />
    <s:radio key="tipo" list="#{'Aluno':'Aluno', 'Docente':'Docente', 'Funcionario':'Funcionario'}" required="true"/><br>
    <s:text name="Nome:" />
    <s:textfield name="nome" required="true"/><br>
    <s:text name="Username:" />
    <s:textfield name="username" required="true"/><br>
    <s:text name="Password:" />
    <s:textfield name="password" required="true"/><br>
    <s:text name="Departamento:" />
    <s:textfield name="departamento" required="true"/><br>
    <s:text name="Contacto:" />
    <s:textfield name="contacto" required="true"/><br>
    <s:text name="Morada:" />
    <s:textfield name="morada" required="true"/><br>
    <s:text name="Numero de CC:" />
    <s:textfield name="numero_CC" required="true"/><br>
    <s:text name="Validade do CC:" />
    <s:select name="dia" list="daysList" required="true"/><s:text name="/" />
    <s:select name="mes" list="monthsList" required="true"/><s:text name="/" />
    <s:select name="ano" list="yearsList" required="true"/><br>

    <s:submit />
  </s:form>
</body>
</html>
