<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Title</title>
    <meta charset="UTF-8">
    <script>

        /*
      var w;
      window.onload = function() {
        console.log(document.getElementById("url").href);
        var url = document.getElementById("url").href;
        w = window.open(url);
        w.onload = function(){
          console.log(w.document.getElementsByTagName("BODY")[0].textContent);
            console.log(w.document.getElementsByTagName("BODY")[0].innerHTML);
            console.log(w.location.href);
        }
      }

         */
        window.onload = function() {
            window.location.href = document.getElementById("url").href;
        }
      //console.log(document.getElementById("url").innerHTML);
      //var url = document.getElementById("url").href;
      //var w = window.open(url);
      //var code = w.document.getElementById("").innerHTML;

    </script>

</head>
<body>
<c:choose>
  <c:when test="${UserBean.test}">

    <p><a id="url" href="${UserBean.link}"/><br></p>

      <p>A obeter autorizacao ...</p>

  </c:when>
  <c:otherwise>
    <p> Ligacao perdida! </p><br>
  </c:otherwise>
</c:choose>
</body>
</html>
