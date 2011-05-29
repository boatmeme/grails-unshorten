/**
*
*   Copyright 2011 Jonathan Griggs
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
**/
package com.memetix.unshorten

import grails.test.*
import org.apache.log4j.*
import grails.converters.JSON
import grails.converters.XML

class UnshortenControllerTests extends ControllerUnitTestCase {
    def controller
    def unshortenService
    def log
    
    def tcoShortUrl
    def tcoFullUrl
    def bitlyShortUrl
    def bitlyFullUrl
    
    def twitterFullUrl
    
    def tweet1Short
    def tweet1Expanded
    def tweet2Short
    def tweet2Expanded
    
    def statusCodeOk = "200"
    def statusCodeServerError = "500"
    
    def statusTextOk = "OK"
    def statusTextMissingParameter = "MISSING_PARAMETER"
    
    def urlType = "url"
    def textType = "text"
    
    def jsonFormat = "json"
    def xmlFormat = "xml"
    
    def missingParameterError = "Please provide one or more 'shortUrl' or 'shortText' parameters"
    
    protected void setupUrls() {
        tcoShortUrl = "http://t.co/8lrqrZf"
        tcoFullUrl =  "http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/" 
        
        bitlyShortUrl = "http://bit.ly/mtXafs"
        bitlyFullUrl =  "http://www.careerrocketeer.com/2011/05/are-you-promotable.html"
        
        twitterFullUrl = "https://twitter.com"
    }
    
    protected void setupTexts() {
        tweet1Short = "On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://ff.im/-DlkAq http://www.the-grotto.com/tits.net http://minu.me/4fmw hahaha"
        tweet1Expanded = "On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://www.twitcaps.com/search?q=basket# http://www.the-grotto.com/tits.net http://www.twitcaps.com/search?q=dsk hahaha"
            
        tweet2Short = "This is a tweet, http://bit.ly/mtXafs 1 http://bit.ly/mtXafs 2 http://bit.ly/mtXafs 3 http://bit.ly/mtXafs 4"
        tweet2Expanded = "This is a tweet, http://www.careerrocketeer.com/2011/05/are-you-promotable.html 1 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 2 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 3 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 4"    
    }
    
    protected void setUp() {
        super.setUp()
        setupLogger()
        setupUrls()
        setupTexts()
        unshortenService = new UnshortenService()
        unshortenService.afterPropertiesSet()
        controller = new UnshortenController()
        controller.unshortenService = unshortenService
        controller.response.setCharacterEncoding("UTF-8") 
    }
    
    private setupLogger() {
        // build a logger...
        BasicConfigurator.configure() 
        LogManager.rootLogger.level = Level.DEBUG
        log = LogManager.getLogger("UnshortenService")

        // use groovy metaClass to put the log into your class
        UnshortenService.class.metaClass.getLog << {-> log}
    }

    protected void tearDown() {
        super.tearDown()
    }
    
    private parseJSONToMap(jsonResponse) {
        def map = new HashMap()
        for(int i=0;i<jsonResponse.data.size();i++) {
            def jsonObj = jsonResponse.data[i];
            if(jsonObj.type==urlType) {
               map.put(jsonObj.shortUrl.toString(), jsonObj) 
            } else if(jsonObj.type==textType) {
                map.put(jsonObj.shortText.toString(), jsonObj)
            }
        }
        return map
    }
    
    private parseXMLToMap(xmlResponse) {
        def map = new HashMap()
        for(int i=0;i<xmlResponse?.data?.entry?.size();i++) {
            def xmlObj = xmlResponse?.data?.entry[i];
            if(xmlObj?.type?.toString()==urlType) {
               map.put(xmlObj?.shortUrl?.toString(), xmlObj) 
            } else if(xmlObj?.type?.toString()==textType) {
                map.put(xmlObj?.shortText?.toString(), xmlObj)
            }
        }
        return map
    }
    
    private assertCommonResponseParams(response) {
        assertNotNull   response
        assertNotNull   response.status_code
        assertNotNull   response.status_text
        assertNotNull   response.data
        assertNotNull   response.errors
        assertNotNull   response.elapsedTime
    }

    void testIndex_No_Params() {
        def model = controller.index()
        assertNotNull   model
        assertNull      model.fullLink
    }
    
    void testIndex_Params() {
        controller.params.shortUrl = tcoShortUrl
        def model = controller.index()
        assertNotNull   model
        assertNotNull   model.fullUrl
        assertEquals    tcoFullUrl,model.fullUrl
    }
    
    void testAjax_JSON_URL_Single() {
        controller.params.shortUrl = tcoShortUrl
        controller.ajax()    
        
        def jsonResponse = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(jsonResponse)
        
        assertEquals statusCodeOk,  jsonResponse.status_code.toString()
        assertEquals statusTextOk,  jsonResponse.status_text.toString()
        assertEquals 1,             jsonResponse.data.size()
        
        def map = parseJSONToMap(jsonResponse)
        
        assertEquals tcoFullUrl,    map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,   map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,       map?.get(tcoShortUrl)   ?.type     ?.toString()
    }
    
    void testAjax_JSON_URL_List() {
        controller.params.shortUrl = [tcoShortUrl,bitlyShortUrl,twitterFullUrl]
        controller.ajax()
      
        def jsonResponse = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(jsonResponse)
        
        assertEquals statusCodeOk, jsonResponse.status_code.toString()
        assertEquals statusTextOk, jsonResponse.status_text.toString()
        assertEquals 3,            jsonResponse.data.size()
        
        def map = parseJSONToMap(jsonResponse)
        
        assertEquals tcoFullUrl,    map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,   map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,       map?.get(tcoShortUrl)   ?.type     ?.toString()
        
        assertEquals bitlyFullUrl,  map?.get(bitlyShortUrl) ?.fullUrl  ?.toString()
        assertEquals bitlyShortUrl, map?.get(bitlyShortUrl)?.shortUrl  ?.toString()
        assertEquals urlType,       map?.get(bitlyShortUrl) ?.type     ?.toString()
        
        assertEquals twitterFullUrl,map?.get(twitterFullUrl)?.fullUrl  ?.toString()
        assertEquals twitterFullUrl,map?.get(twitterFullUrl)?.shortUrl ?.toString()
        assertEquals urlType,       map?.get(twitterFullUrl)?.type     ?.toString()
    }
    
    void testAjax_JSON_URL_MultipleSame() {
        controller.params.shortUrl = [tcoShortUrl,tcoShortUrl,tcoShortUrl]
        controller.ajax()
      
        def jsonResponse = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(jsonResponse)
        
        assertEquals statusCodeOk, jsonResponse.status_code.toString()
        assertEquals statusTextOk, jsonResponse.status_text.toString()
        assertEquals 1,            jsonResponse.data.size()
        
        def map = parseJSONToMap(jsonResponse)
        
        assertEquals tcoFullUrl,    map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,   map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,       map?.get(tcoShortUrl)   ?.type     ?.toString()
    }
    
    void testAjax_JSON_URL_Null_500() {
        controller.params.shortUrl = []
        controller.ajax()
        
        def jsonResponse = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(jsonResponse)
    
        assertEquals statusCodeServerError,     jsonResponse?.status_code?.toString()
        assertEquals statusTextMissingParameter,jsonResponse?.status_text.toString()
        
        assertEquals 1,                         jsonResponse.errors.size()
        assertEquals missingParameterError,     jsonResponse.errors[0]?.toString()
        assertEquals 0,                         jsonResponse.data.size()
    }
    
    void testAjax_JSON_Text_Single() {        
        controller.params.shortText = tweet1Short
        controller.ajax()    
        
        def jsonResponse = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(jsonResponse)

        assertEquals statusCodeOk,  jsonResponse.status_code.toString()
        assertEquals statusTextOk,  jsonResponse.status_text.toString()
        assertEquals 1,             jsonResponse.data.size()
        
        def map = parseJSONToMap(jsonResponse)
        
        assertEquals tweet1Expanded,    map?.get(tweet1Short)   ?.fullText  ?.toString()
        assertEquals tweet1Short,       map?.get(tweet1Short)   ?.shortText  ?.toString()
        assertEquals textType,          map?.get(tweet1Short)   ?.type      ?.toString()
    }
    
    void testAjax_JSON_Text_Multiple() {
        def tweetList = [tweet1Short,tweet2Short]
        
        controller.params.shortText = tweetList
        controller.ajax()    
        
        def jsonResponse = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(jsonResponse)
        
        assertEquals statusCodeOk,  jsonResponse.status_code.toString()
        assertEquals statusTextOk,  jsonResponse.status_text.toString()
        assertEquals 2,             jsonResponse.data.size()
        
        def map = parseJSONToMap(jsonResponse)
        
        assertEquals tweet1Expanded,    map?.get(tweet1Short)   ?.fullText  ?.toString()
        assertEquals tweet1Short,       map?.get(tweet1Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet1Short)   ?.type      ?.toString()
        
        assertEquals tweet2Expanded,    map?.get(tweet2Short)   ?.fullText  ?.toString()
        assertEquals tweet2Short,       map?.get(tweet2Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet2Short)   ?.type      ?.toString()
    }
    
    void testAjax_JSON_TEXT_Mixed() {
        def tweetList = [tweet1Short,tweet2Short]    
        def urlList = [tcoShortUrl,bitlyShortUrl]
        
        controller.params.shortUrl = urlList
        controller.params.shortText = tweetList
        
        controller.ajax()    
        
        def response = JSON.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
        
        assertEquals statusCodeOk,  response.status_code.toString()
        assertEquals statusTextOk,  response.status_text.toString()
        assertEquals 4,             response.data.size()
        
        def map = parseJSONToMap(response)
        
        assertEquals tweet1Expanded,    map?.get(tweet1Short)   ?.fullText  ?.toString()
        assertEquals tweet1Short,       map?.get(tweet1Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet1Short)   ?.type      ?.toString()
        
        assertEquals tweet2Expanded,    map?.get(tweet2Short)   ?.fullText  ?.toString()
        assertEquals tweet2Short,       map?.get(tweet2Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet2Short)   ?.type      ?.toString()
        
        assertEquals tcoFullUrl,        map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,       map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,           map?.get(tcoShortUrl)   ?.type     ?.toString()
        assertFalse                     map?.get(tcoShortUrl)   ?.cached   ?.toBoolean()  
        
        assertEquals bitlyFullUrl,      map?.get(bitlyShortUrl) ?.fullUrl  ?.toString()
        assertEquals bitlyShortUrl,     map?.get(bitlyShortUrl) ?.shortUrl ?.toString()
        assertEquals urlType,           map?.get(bitlyShortUrl) ?.type     ?.toString()
        assertFalse                     map?.get(tcoShortUrl)   ?.cached   ?.toBoolean()
    }
    
    void testAjax_XML_URL_Single() {
        controller.params.shortUrl = tcoShortUrl
        controller.params.format = xmlFormat
        controller.ajax()    
        
        def response = XML.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
        
        assertEquals statusCodeOk,  response    ?.status_code   ?.toString()
        assertEquals statusTextOk,  response    ?.status_text   ?.toString()
        assertEquals 1,             response    ?.data.entry    ?.size()
        
        def map = parseXMLToMap(response)

        assertEquals tcoFullUrl,    map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,   map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,       map?.get(tcoShortUrl)   ?.type     ?.toString()
    }
    
    void testAjax_XML_URL_List() {
        controller.params.shortUrl = [tcoShortUrl,bitlyShortUrl]
        controller.params.format = xmlFormat
        controller.ajax()    
        
        def response = XML.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
        
        assertEquals statusCodeOk,  response    ?.status_code   ?.toString()
        assertEquals statusTextOk,  response    ?.status_text   ?.toString()
        assertEquals 2,             response    ?.data.entry    ?.size()
        
        def map = parseXMLToMap(response)

        assertEquals tcoFullUrl,    map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,   map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,       map?.get(tcoShortUrl)   ?.type     ?.toString()
        
        assertEquals bitlyFullUrl,      map?.get(bitlyShortUrl)   ?.fullUrl  ?.toString()
        assertEquals bitlyShortUrl,     map?.get(bitlyShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,           map?.get(bitlyShortUrl)   ?.type     ?.toString()
    }
    
    void testAjax_XML_URL_Null_500() {
        controller.params.shortUrl = []
        controller.params.format = xmlFormat
        controller.ajax()
        
        println controller.response.contentAsString
        def response = XML.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
    
        assertEquals statusCodeServerError,     response    ?.status_code   ?.toString()
        assertEquals statusTextMissingParameter,response    ?.status_text   ?.toString()
        
        assertEquals 1,                         response    ?.errors?.error     ?.size()
        assertEquals missingParameterError,     response    ?.errors?.error[0]  ?.toString()
        assertEquals 0,                         response    ?.data.entry        ?.size()
    }
    
    void testAjax_XML_TEXT_Single() {
        controller.params.shortText = tweet1Short
        controller.params.format = xmlFormat
        controller.ajax()    
        
        def response = XML.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
        
        assertEquals statusCodeOk,  response    ?.status_code   ?.toString()
        assertEquals statusTextOk,  response    ?.status_text   ?.toString()
        assertEquals 1,             response    ?.data.entry    ?.size()
        
        def map = parseXMLToMap(response)

        assertEquals tweet1Expanded,    map?.get(tweet1Short)   ?.fullText  ?.toString()
        assertEquals tweet1Short,       map?.get(tweet1Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet1Short)   ?.type      ?.toString()
    }
    
    void testAjax_XML_TEXT_List() {
        def tweetList = [tweet1Short,tweet2Short]    
        controller.params.shortText = tweetList
        controller.params.format = xmlFormat
        controller.ajax()    
        
        def response = XML.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
        
        assertEquals statusCodeOk,  response.status_code.toString()
        assertEquals statusTextOk,  response.status_text.toString()
        assertEquals 2,             response.data.entry.size()
        
        def map = parseXMLToMap(response)
        
        assertEquals tweet1Expanded,    map?.get(tweet1Short)   ?.fullText  ?.toString()
        assertEquals tweet1Short,       map?.get(tweet1Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet1Short)   ?.type      ?.toString()
        
        assertEquals tweet2Expanded,    map?.get(tweet2Short)   ?.fullText  ?.toString()
        assertEquals tweet2Short,       map?.get(tweet2Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet2Short)   ?.type      ?.toString()
    }
    
    void testAjax_XML_TEXT_Mixed() {
        def tweetList = [tweet1Short,tweet2Short]    
        def urlList = [tcoShortUrl,bitlyShortUrl]
        
        controller.params.shortUrl = urlList
        controller.params.shortText = tweetList
        
        controller.params.format = xmlFormat
        controller.ajax()    
        
        def response = XML.parse(controller.response.contentAsString)
        assertCommonResponseParams(response)
        
        assertEquals statusCodeOk,  response.status_code.toString()
        assertEquals statusTextOk,  response.status_text.toString()
        assertEquals 4,             response.data.entry.size()
        
        def map = parseXMLToMap(response)
        
        assertEquals tweet1Expanded,    map?.get(tweet1Short)   ?.fullText  ?.toString()
        assertEquals tweet1Short,       map?.get(tweet1Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet1Short)   ?.type      ?.toString()
        
        assertEquals tweet2Expanded,    map?.get(tweet2Short)   ?.fullText  ?.toString()
        assertEquals tweet2Short,       map?.get(tweet2Short)   ?.shortText ?.toString()
        assertEquals textType,          map?.get(tweet2Short)   ?.type      ?.toString()
        
        assertEquals tcoFullUrl,        map?.get(tcoShortUrl)   ?.fullUrl  ?.toString()
        assertEquals tcoShortUrl,       map?.get(tcoShortUrl)   ?.shortUrl ?.toString()
        assertEquals urlType,           map?.get(tcoShortUrl)   ?.type     ?.toString()
        assertFalse                     map?.get(tcoShortUrl)   ?.cached   ?.toBoolean()  
        
        assertEquals bitlyFullUrl,      map?.get(bitlyShortUrl) ?.fullUrl  ?.toString()
        assertEquals bitlyShortUrl,     map?.get(bitlyShortUrl) ?.shortUrl ?.toString()
        assertEquals urlType,           map?.get(bitlyShortUrl) ?.type     ?.toString()
        assertFalse                     map?.get(tcoShortUrl)   ?.cached   ?.toBoolean()
    }
}
