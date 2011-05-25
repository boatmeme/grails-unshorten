<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>grails-unshorten Plugin</title>
    <meta name="layout" content="main" />
    <style type="text/css" media="screen">
        #container {
            margin-top:20px;
            margin-left:30px;
            padding:10px;
            background-color: #FBFCD0;
            width:600px;
            border:5px solid #EECD7E;
        }
        #title {
          color: #666666;
          font-family: 'Century Gothic', sans-serif;
          font-size:2em;
          margin:10px;
        }
        #shortUrl {
          width: 350px;
          height: 2em;
          color: #333333;
          font-family: 'Lucida Sans Unicode', 'Lucida Grande', sans-serif;
          font-size: 1.5em;
          margin:10px;
        }
        #submit {
          color: #333333;
          height:2em;
          font-family: 'Lucida Sans Unicode', 'Lucida Grande', sans-serif;
          font-size:1.5em;
          margin:10px;
        }
        #submit:hover {
          background-color: #EECD7E;
          color:white;
        }
        #unshortened {
          background-color: #FFF3A1;
          border: 4px solid #EECD7E;
          font-family: Tahoma, Geneva, sans-serif;
          font-size:1.2em;
          padding: 5px;
          margin: 10px;
        }
        #unshortened p {
          margin:5px;
        }
    </style>
  </head>
  <body>
    <g:set var="pluginManager" value="${applicationContext.getBean('pluginManager')}"></g:set>
    <div id="container">
      <div id="title">grails-unshorten v<a href="https://github.com/boatmeme/grails-unshorten/tree/v${pluginManager?.getGrailsPlugin('unshorten')?.version}">${pluginManager?.getGrailsPlugin('unshorten')?.version}</a></div>
        <g:form method="post">
          <input type="text" id="shortUrl" name="shortUrl"/>
          <input type="Submit" id="submit" value="Unshorten"/>
          <g:if test="${shortUrl}">
            <div id="unshortened">
            <p>
              shortUrl: <b>${shortUrl}</b>
            </p>
            <p>
              fullUrl: <b>${fullUrl}</b>
            </p>
            <p>
              status: <b>${status}</b>
            </p>
            <p>
              cached: <b>${cached}</b>
            </p>
            <p>
              elapsedTime: <b>${elapsedTime} ms</b>
            </p>
            </div>
          </g:if>
        </g:form>
    </div>
  </body>
</html>
