package unshortenapp

import grails.converters.JSON
import grails.converters.XML

class ClientController {

    def index = { }

    def ajaxHtmlTemplate = {
        def results = request.unshortenResponse
        for(e in results.data)
            e.enrichedData = "ENRICHED_FOR_HTML"
        [unshortenResponse: results]
    }
    
    def ajaxJsonTemplate = {
        def results = JSON.parse(request.unshortenResponse)
        
        for(e in results.data)
            e.enrichedData = "ENRICHED_FOR_JSON"
        
        render results as JSON
    }
    
    def ajaxXmlTemplate = {
        def results = new XmlParser().parseText(request.unshortenResponse)

        results.data.entry.each { e ->
            e.appendNode("enrrichedData","ENRICHED_FOR_XML")
        }
        
        def writer = new StringWriter()
        new XmlNodePrinter(new PrintWriter(writer)).print(results)
        def result = writer.toString()
        
        render(text: result, contentType:"text/xml", encoding:"UTF-8")
    }
}
