<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Title</title>
</head>
<>
<c:choose>
    <c:when test="${AdminConsoleBean.test}">
        <p> Eleicoes: </p><br>
        <c:forEach items="${AdminConsoleBean.electionsList}" var="value">
            <s:url action="ElectionDetails" var="link">
                <s:param name="titulo"> <c:out value="${value}" /> </s:param>
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
<>
</body>
</html>
