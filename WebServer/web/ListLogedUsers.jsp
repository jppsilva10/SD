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
            connect('ws://' + window.location.host + '/WebServer/wsu');
        }

        function connect(host) { // connect to the host websocket
            if ('WebSocket' in window)
                websocket = new WebSocket(host);
            else if ('MozWebSocket' in window)
                websocket = new MozWebSocket(host);
            else {
                updateDetails('Get a real browser which supports WebSocket.');
                return;
            }

            websocket.onopen    = onOpen; // set the 4 event listeners below
            websocket.onclose   = onClose;
            websocket.onmessage = onMessage;
            websocket.onerror   = onError;
        }

        function onOpen(event) {
        }

        function onClose(event) {

        }

        function onMessage(message) { // print the received message
            updateDetails(message.data);
        }

        function onError(event) {
            updateDetails('WebSocket error.');
        }

        function updateDetails(text) {
            var element = document.getElementById("utilizadores");

            switch (text){
                case "":
                    element.innerHTML = '';
                default:
                    var line = document.createElement('p');
                    line.style.wordWrap = 'break-word';
                    line.innerHTML = "    " + text;
                    element.appendChild(line);
                    element.scrollTop = element.scrollHeight;
                    break;
            }
        }

    </script>
</head>
<body>

    <p>Utilizadores: </p>
    <div id="container"><div id="utilizadores"></div></div>

</body>
</html>
