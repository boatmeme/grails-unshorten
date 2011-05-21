<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Link Unshorten</title>
  </head>
  <body>
    <g:form method="post">
      <input type="text" name="shortUrl"/>
      <input type="Submit" value="Unshorten"/>
      <g:if test="${shortUrl}">
        <p>
          shortUrl: ${shortUrl}
        </p>
        <p>
          fullUrl: ${fullUrl}
        </p>
        <p>
          status: ${status}
        </p>
        <p>
          cached: ${cached}
        </p>
        <p>
          elapsedTime: ${elapsedTime} ms
        </p>
      </g:if>
    </g:form>
  </body>
</html>
