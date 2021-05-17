<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>JSP - Hello World</title>
    <script>

        function statusChangeCallback(response) {
            console.log('statusChangeCallback');
            console.log(response);
            console.log(response.authResponse.accessToken);
            //alert(response.authResponse.accessToken);
            if (response.status === 'connected') {
                console.log('connected');
            } else {

            }
        }
        window.onload = function(){
            FB.getLoginStatus(function(response) {
                statusChangeCallback(response);
            });
        }

        window.fbAsyncInit = function() {
            FB.init({
                appId : '463509174943899',
                cookie : true, // enable cookies to allow the server to access
                // the session
                xfbml : true, // parse social plugins on this page
                version : 'v3.2' // use graph api version 2.8
            });
            FB.getLoginStatus(function(response) {
                statusChangeCallback(response);
            });
        };
        // Load the SDK asynchronously
        (function(d, s, id) {
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) return;
            js = d.createElement(s); js.id = id;
            js.src = "https://connect.facebook.net/en_US/sdk.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));

    </script>
</head>
<body>

<br/>
<p><a href="<s:url action="RU" />">Registar Pessoa</a></p>
<p><a href="<s:url action="CE" />">Criar Eleicao</a></p>
<p><a href="<s:url action="LU" />">Listar Pessoas</a></p>
<p><a href="<s:url action="LLU" />">Listar Utilizadores</a></p>
<p><a href="<s:url action="LE" />">Listar Eleicoes</a></p>
<p><a href="<s:url action="LM" />">Listar Mesas</a></p>

<p><a href="<s:url action="Login" />">Login</a></p>


</body>
</html>