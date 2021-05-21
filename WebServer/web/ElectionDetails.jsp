<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Title</title>
    <link rel="stylesheet" type="text/css" href="style.css">
    <script type="text/javascript">
        var websocket = null;

        window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
            connect('ws://' + window.location.host + '/WebServer/ws');
        }

        function connect(host) { // connect to the host websocket
            if ('WebSocket' in window)
                websocket = new WebSocket(host);
            else if ('MozWebSocket' in window)
                websocket = new MozWebSocket(host);
            else {
                console.log("erro");
                updateDetails('Get a real browser which supports WebSocket.');
                return;
            }

            websocket.onopen    = onOpen; // set the 4 event listeners below
            websocket.onclose   = onClose;
            websocket.onmessage = onMessage;
            websocket.onerror   = onError;
        }

        function onOpen(event) {
            var element = document.getElementById("titulo").innerHTML;
            var text = element.split(": ")[1];
            websocket.send(text);
        }

        function onClose(event) {

        }

        function onMessage(message) { // print the received message
            console.log("blablabla");
            console.log(message.data);
            updateDetails(message.data);
        }

        function onError(event) {
            updateDetails('WebSocket error.');
        }

        function updateDetails(text) {
            var info = text.split("|");
            var id = info[0];
            console.log(id);
            console.log(info[1]);
            var element = document.getElementById(id);
            console.log(element.innerHTML);

            switch (id){
                case "titulo":
                    element.innerHTML = "Titulo: "+ info[1];
                    break;
                case "descricao":
                    element.innerHTML = "Descricao: "+ info[1];
                    break;
                case "tipo":
                    element.innerHTML = "Tipo: "+ info[1];
                    break;
                case "listas":
                    if(info.length == 2) {
                        var line = document.createElement('p');
                        line.id = info[1].split(" - ")[0];
                        line.style.wordWrap = 'break-word';
                        line.innerHTML = "    " + info[1];
                        element.appendChild(line);
                        element.scrollTop = element.scrollHeight;
                    }
                    else{
                        var l = document.getElementById(info[2]);
                        l.id = info[1].split(" - ")[0];
                        l.innerHTML = "    " + info[1];
                    }
                    break;
                case "inicio":
                    element.innerHTML = "Inicio: "+ info[1];
                    break;
                case "fim":
                    element.innerHTML = "Fim: "+ info[1];
                    break;
                case "eleitores":
                    var line = document.createElement('p');
                    line.style.wordWrap = 'break-word';
                    line.innerHTML = "    " + info[1];
                    element.appendChild(line);
                    element.scrollTop = element.scrollHeight;
                    break;
                case "resultado":
                    var line = document.createElement('p');
                    line.style.wordWrap = 'break-word';
                    line.innerHTML = "    " + info[1];
                    element.appendChild(line);
                    element.scrollTop = element.scrollHeight;
                    break;
                default:

                    break;
            }
        }

    </script>
</head>
<body>

    <p id="titulo">Titulo: <s:property value="AdminConsoleBean.titulo"/></p>
    <p id="descricao">Descricao: </p>
    <p id="tipo">Tipo: </p>
    <p>Listas: </p>
    <div id="container1"><div id="listas"></div></div>
    <p id="inicio">Inicio: </p>
    <p id="fim">Fim: </p>
    <p>Eleitores: </p>
    <div id="container2"><div id="eleitores"></div></div>
    <p>Resultado: </p>
    <div id="container3"><div id="resultado"></div></div>

    <br><br>
    <s: url action="GoEditElection" var="link">
	<s: param name="election"> <c: out value="Edit ${AdminConsoleBeam.titulo}" \> </s:param>
    </s:url>
    <p><a href="${link}">
	<c:out value="Edit ${AdminConsoleBeam.titulo}" />
    </a><br></p>
 
  <c:forEach items="${AdminConsoleBean.electionDetails}" var="value">
      <c:out value="${value}" /><br>
  </c:forEach>
</body>
</html>
