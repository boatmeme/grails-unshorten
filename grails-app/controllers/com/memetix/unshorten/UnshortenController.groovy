package com.memetix.unshorten

import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.web.json.JSONObject
import groovy.xml.MarkupBuilder

/**
 * UnshortenController provides an example / test implementation of the Unshorten plugin
 * 
 * /index action takes a single parameter (params.shortLink) and passes it to unshortenService.unshorten()
 * Then, it hands the resulting Map of URL properties back to the index view and displays it.
 * 
 * /ajax action accepts the following params: 
 * 
 *      shortUrl     : 1 - n URLs to Unshorten
 *      shortText    : 1 - n text blobs to parse for urls and unshorten
 *      format       : 'json' or 'xml'
 * 
 * 
 * Returns JSON: 
 *    {
 *       "status_code": 200, 
 *       "status_text": "OK",
 *       "data": [{
 *               "type": "url"
 *               "shortUrl": "http://shortUrl",
 *               "fullUrl": "http://fullUrl",
 *               "status": "url_status_value"
 *               "cached": "true"
 *           }]
 *     }
 * 
 * @author Jonathan Griggs  <twitcaps.developer @ gmail.com>
 * @version     1.0.3   2011.05.20                              
 * @since       1.0     2011.05.17                         
 */
class UnshortenController {
    def unshortenService
    def grailsApplication
    
    def ajaxXmlForward = grailsApplication?.config?.unshorten?.ajax?.forward?.xml
    def ajaxJsonForward = grailsApplication?.config?.unshorten?.ajax?.forward?.json
    def ajaxHtmlForward = grailsApplication?.config?.unshorten?.ajax?.forward?.html
    
    /*
    * Index action
    * 
    * @version     1.0.1   2011.05.18                              
    * @since       1.0     2011.05.17
    */
    def index = { 
        def responseMap
        if(params.shortUrl) {
            def startTime = System.currentTimeMillis()
            responseMap = unshortenService.unshorten(params.shortUrl)
            responseMap.elapsedTime = System.currentTimeMillis()-startTime
            return responseMap
        } 
        return [:]
    }
    
    /*
    * Ajax action
    * 
    * @version     1.0.3   2011.05.20                              
    * @since       1.0     2011.05.17
    */
    def ajax = {
        def startTime = System.currentTimeMillis()
        def responseObj
        
        def format = params?.format?.toLowerCase() ?: 'json'
        
        if(!params?.shortUrl
            &&!params?.shortText) {
            responseObj = ["status_code":"500","status_text":"MISSING_PARAMETER","errors":["Please provide one or more 'shortUrl' or 'shortText' parameters"],"data":[]]
        } else {
            def expandedMap
            responseObj = ["status_code":"200","status_text":"OK","data":[],"errors":[]]

            if(params?.shortUrl) {
                try {
                    expandedMap = unshortenService.unshortenAll(params?.shortUrl)

                    responseObj."data".addAll(expandedMap.collect {key,value ->
                        value.type = 'url'
                        return value
                    })
                } catch (Exception e) {
                    responseObj."status_code" = 500
                    responseObj."status_text" = "ERROR"
                    responseObj."errors".add(e?.getMessage())
                }
            } 

            if(params?.shortText) {
                try {
                    expandedMap = unshortenService.expandUrlsInTextAll(params?.shortText)

                    responseObj."data".addAll(expandedMap.collect {key,value ->
                        return [type:"text", shortText: key, fullText: value]
                    })
                } catch (Exception e) {
                    responseObj."status_code" = 500
                    responseObj."status_text" = "ERROR"
                    responseObj."errors".add(e?.getMessage())
                }
            }
        }
        
        if(format=='json') {
            responseObj."elapsedTime" = System.currentTimeMillis()-startTime
            if(!ajaxJsonForward?.action||!ajaxJsonForward?.controller) {
                render responseObj as JSON
            } else {
                forward(action:ajaxJsonForward.action,controller:ajaxJsonForward.controller,model:[unshortenResponse:new JSON(responseObj).toString()])
            }
        } else if(format=='html') {
            responseObj."elapsedTime" = System.currentTimeMillis()-startTime
            if(!ajaxHtmlForward?.action||!ajaxHtmlForward?.controller) {
                render(view:'ajax',model:[unshortenResponse:responseObj])
            } else {
                forward(action:ajaxHtmlForward.action,controller:ajaxHtmlForward.controller,model:[unshortenResponse:responseObj])
            }
        } else {
            def b = new groovy.xml.StreamingMarkupBuilder()
            b.encoding = "UTF-8"
            def xml = b.bind {
                    mkp.xmlDeclaration()
                    response {
                        status_code(responseObj."status_code")
                        status_text(responseObj."status_text")
                        errors {
                            for(e in responseObj."errors") {
                                error(e)
                            }
                        }
                        data {
                            for(datum in responseObj."data") {
                                entry {
                                    type(datum."type")
                                    if(datum."type"=="url") {
                                        shortUrl(datum."shortUrl")
                                        fullUrl(datum."fullUrl")
                                        status(datum."status")
                                        cached(datum."cached")
                                    } else if(datum."type"=="text") {
                                        shortText(datum."shortText")
                                        fullText(datum."fullText")
                                    }
                                }
                            }
                        }
                        elapsedTime(System.currentTimeMillis()-startTime)
                    }
            }
            if(!ajaxXmlForward?.action||!ajaxXmlForward?.controller) {
                render(text: xml.toString(),contentType:'text/xml') 
            } else {
                forward(action:ajaxXmlForward.action,controller:ajaxXmlForward.controller,model:[unshortenResponse:writer.toString()])
            }
        }
    }    
}
