<div id="ajaxResponse">
  <div id="elapsedTime">Request Time: ${unshortenResponse?.elapsedTime}ms</div>
  <div id="status_code">Status Code: ${unshortenResponse?.status_code}</div>
  <div id="status_text">Status Text: ${unshortenResponse?.status_text}<div>
  <g:each var="error" in="${unshortenResponse?.errors}">
    <div id="error">${error}</div>
  </g:each>   
    <g:each var="entry" in="${unshortenResponse?.data}">
       <div id="entry"> 
          <div id="type"><b>Type</b>: ${entry.type}</div>
          <div id="type"><b>Type</b>: ${entry.enrichedData}</div>
          <g:if test="${entry.type=='url'}">
            
            <div id="fullUrl"><b>Full Url</b>: ${entry.fullUrl}</div>
            
          </g:if>
          <g:if test="${entry.type=='text'}">
            
            <div id="fullText"><b>Full Text</b>: ${entry.fullText}</div>
          </g:if>
       </div>
    </g:each>
</div>
