<div id="ajaxResponse">
  <div id="elapsedTime">Request Time: ${unshortenResponse.elapsedTime}ms</div>
  <div id="status_code">Status Code: ${unshortenResponse.status_code}</div>
  <div id="status_text">Status Text: ${unshortenResponse.status_text}<div>
  <g:each var="error" in="${unshortenResponse?.errors}">
    <div id="error">${error}</div>
  </g:each>   
    <g:each var="entry" in="${unshortenResponse?.data}">
       <div id="entry"> 
          <div id="type"><b>Type</b>: ${entry.type}</div>
          <g:if test="${entry.type=='url'}">
            <div id="shortUrl"><b>Short Url</b>: ${entry.shortUrl}</div>
            <div id="fullUrl"><b>Full Url</b>: ${entry.fullUrl}</div>
            <div id="status"><b>Status</b>: ${entry.status}</div>
            <div id="cached"><b>Cached</b>: ${entry.cached}</div>
          </g:if>
          <g:if test="${entry.type=='text'}">
            <div id="shortText"><b>Short Text</b>: ${entry.shortText}</div>
            <div id="fullText"><b>Full Text</b>: ${entry.fullText}</div>
          </g:if>
       </div>
    </g:each>
</div>
