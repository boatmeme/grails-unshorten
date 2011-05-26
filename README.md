
# Unshorten - URL Expander Plugin for Grails


## Description

The Unshorten plugin provides a means for your Grails application to expand Shortened URLs (http://bit.ly/jkD0Qr and http://tinyurl.com/3vy9xga, for example)
into their Original, Unshortened form (http://amazon.com or http://grails.org, for example) without the need for calling a Third-Party API.

Provides a Grails Service, TagLib, and Controller to your application.

Backed by a configurable Least-Recently-Used (LRU) Cache, the Unshorten plugin optimizes performance by making a minimal number of HTTP HEAD method calls.

Contains service calls and controller actions to dynamically handle single or multiple URLs as well as GSP Tags to unshorten URLs within blocks of text, and to create links

## Installation

Enter your application directory and run the following from the command line: 

    grails install-plugin unshorten

## Configuration

The UnshortenPlugin may be configured with several parameters, all specified in your application's */grails-app/conf/Config.groovy*


    unshorten.cache.maxSize        = 10000   // Default 10000
    unshorten.http.connectTimeout  = 1000    // in millis, default 1000
    unshorten.http.readTimeout     = 1000    // in millis, default 1000
    unshorten.ajax.forward.html    = [controller:'myController', action:'myHtmlAction']
    unshorten.ajax.forward.json    = [controller:'myController', action:'myJsonAction']
    unshorten.ajax.forward.xml     = [controller:'myController', action:'myXmlAction']

***
### unshorten.cache.maxSize

This is the maximum number of Unshortened URLs that will be stored in the LRU Cache at any given time. When this number is exceeded, the Least-Recently-Used entry in the Cache will be evicted.

_Defaults to 10000 entries_

***
### unshorten.http.connectTimeout

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for a response during the TCP Handshake stage of the HTTP connection

It sets the [connectionTimeout](http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setConnectTimeout(\)) property of the URLConnection 

_Defaults to 1000 milliseconds_

>_If you're returning many Unshortened urls with a status of 'TIMED\_OUT' you may try increasing this setting_

***
### unshorten.http.readTimeout

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for reading from the input stream of an established connection.

It sets the [readTimeout](http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setReadTimeout(\)) property of the URLConnection 

_Defaults to 1000 milliseconds_

>_If you're returning many Unshortened urls with a status of 'TIMED\_OUT' you may try increasing this setting_

***
### unshorten.ajax.forward.html 
Optional map containing an `action` and `controller` to forward an AJAX request after the plugin's `/unshorten/ajax?format=html` action has finished processing. 

Setting this property allows you to override the default HTML display. You will receive a java.util.Map assigned to the `request.unshortenResponse` variable to the response containing the data you may use in the response.

Example action in your own MyCustomController.groovy:

    def ajaxHtmlTemplate = {
        def results = request.unshortenResponse

        for(e in results.data)
            e.enrichedData = "ENRICHED_FOR_HTML"

        [unshortenResponse: results]
    }

***
### unshorten.ajax.forward.json
Optional map containing an `action` and `controller` to forward an AJAX request after the plugin's `/unshorten/ajax?format=json` action has finished processing. 

Setting this property allows you to augment/enrich the JSON data before it is returned to the caller in the response. 

You will receive a String assigned to the `request.unshortenResponse` variable containing the JSON data you may use in the response. If you wish to work with it as JSON, you must parse it first. 

Example action in your own MyCustomController.groovy:

    def ajaxJsonTemplate = {
        def results = JSON.parse(request.unshortenResponse)
        
        for(e in results.data)
            e.enrichedData = "ENRICHED_FOR_JSON"
        
        render results as JSON
    }

*** 
### unshorten.ajax.forward.xml
Optional map containing an `action` and `controller` to forward an AJAX request after the plugin's `/unshorten/ajax?format=xml` action has finished processing. 

Setting this property allows you to augment/enrich the XML data before it is returned to the caller in the response. 

You will receive a String assigned to the `request.unshortenResponse` variable containing the XML data you may use in the response. If you wish to work with it as XML, you must parse it first. 

Example action in your own MyCustomController.groovy:

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
# UnshortenService

## Services
***
### unshorten()

Takes a single String and returns a Map representing the Unshortened URL and ancillary data regarding its HTTP and caching statuses

_Parameters_

* a String - the URL to unshorten

_Returns_

* A Map

The values in the `returnMap` are as follows:

* **returnMap.shortUrl** - the original URL
* **returnMap.fullUrl** - the unshortened URL
* **returnMap.cached** - A boolean that is true if the shortenUrl was retrieved from the cache, and false if HTTP rigamarole was required
* **returnMap.status** - A String, the state of the URL and one of the following possible values:
     * `UNSHORTENED` - successfully unshortened
     * `NOT_SHORTENED` - URL was already expanded, i.e. no redirect required
     * `REDIRECTED` - URL was redirected internal to the TLD of the original URL
     * `NOT_FOUND` - No document at this URL, returned 404 Error
     * `TIMED_OUT` - Destination server or Shortener API did not respond within the timeouts specified by Config.groovy
     * `INVALID` - Poorly formed, or non-existent URL
     * `UNKNOWN` - Something else happened

> _**Note**_: A URL that has TIMED_OUT is not cached, the reason being that sometimes Third-party Shortening services may be temporarily unavailable now, but responsive next time we try. 

>_Considering addressing configuration of this behavior in future releases._

Example:

    def shortUrl = "http://bit.ly/jkD0Qr"
    unshortenService?.unshorten(shortUrl)

_returns_

    [fullUrl:http://www.cbsnews.com/8301-503543_162-20063168-503543.html, status:UNSHORTENED, shortUrl:http://bit.ly/jkD0Qr, cached:false]

***
### unshortenAll()

Takes a single String OR List of Strings representing 1 - n Shortened URLs and returns a Map of Maps with their Unshortened versions, keyed by the ShortURL(s) passed into the method

_Parameters_

* a String - a single URL to unshorten

_OR_

* a List of Strings - 1 or more URLs to Unshorten

_Returns_

 * a Map of Maps - The returnMap's values match those from the single unshorten() method, but keyed by the corresponding originalUrl passed in as a parameter.

Example:

    def shortUrls = ["http://bit.ly/jkD0Qr","http://minu.me/4fmw"]
    unshortenService?.unshortenAll(shortUrls)

_returns_

    [http://bit.ly/jkD0Qr:[fullUrl:http://www.cbsnews.com/8301-503543_162-20063168-503543.html, status:UNSHORTENED, shortUrl:http://bit.ly/jkD0Qr, cached:false],http://minu.me/4fmw:[fullUrl:http://www.twitcaps.com/search?q=dsk, status:UNSHORTENED, shortUrl:http://minu.me/4fmw, cached:false]]

***
### expandUrlsInText()

Takes a String representing a block of text and replaces all URL occurrences with their Unshortened versions

_Parameters_

* a String - a block of text containing 1 - n potentially shortened URLs

_Returns_

* a String - the block of text with all URLs replaced by their Unshortened counterparts

    def shortUrl = "Check out these links! http://bit.ly/jkD0Qr and http://minu.me/4fmw"
    unshortenService?.unshorten(shortUrl)

_returns_

     Check out these links! http://www.cbsnews.com/8301-503543_162-20063168-503543.html and http://www.twitcaps.com/search?q=dsk

***
###expandUrlsInTextAll()

Takes a single String or List of Strings representing a block of text and replaces all URL occurrences with their Unshortened versions returning a Map of Maps with the fullText keyed by the shortText

_Parameters_

* a String - a block of text containing 1 - n potentially shortened URLs

_OR_

* a List of Strings - 1 or more blocks of text containing 1 - n potentially shortened URLs

_Returns_

 * a Map of Maps - The returnMap's values match those from the single expandUrlsInTextAll() method, but keyed by the corresponding originalTextBlock passed in as a parameter.

[TODO: Provide Sample Data]

# UnshortenTagLib

## Tags
***
### expandUrls

    <unshorten:expandUrls>
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
    </unshorten:expandUrls>


_results in_

    I just tweeted this URL so you could see it http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
    and also this one http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
***
### expandAndLinkUrls


    <unshorten:expandAndLinkUrls linkClass="myLinkClass">
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
    </unshorten:expandAndLinkUrls>

_results in_

    I just tweeted this URL so you could see it 
    <a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
        http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
    </a>
    and also this one 
    <a class="myLinkClass" href="http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/">
       http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
    </a>
***
### unshortenUrl


    <unshorten:unshortenUrl url='http://bit.ly/jkD0Qr'/>

_results in_


    http://www.cbsnews.com/8301-503543_162-20063168-503543.html

***
### unshortenAndLinkUrl


    <unshorten:unshortenAndLinkUrl class="myLinkClass" url='http://bit.ly/jkD0Qr'/>


_results in_


    <a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
        http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
    </a>


# UnshortenController

## Actions

***
### /unshorten/

Provides a test form for validating the functionality of the Unshorten plugin and testing individual URLs. May serve as a template for your own application.
***
### /unshorten/ajax

 AJAX Action, accepts params and returns an HTML fragment, JSON, or XML

_Parameters_

* **shortUrl** - one or more URLs
* **shortText** - one or more blocks of text that may contain shortened links (i.e. Tweets)
* **format** - `json`, `xml`, or `html`. Determines the format of the response. Defaults to `json`.

> _At least 1 shortUrl OR shortText must be supplied, or the response will return a 500 status\_code_

For example, doing an HTTP GET on this URL:

    app-context/unshorten/ajax?shortUrl=http://bit.ly/jkD0Qr&shortUrl=http://t.co/8lrqrZf&shortText=Tweet!%20http://bit.ly/11Da1f

might return the following JSON:


    {
      "status_code":"200",
      "status_text":"OK",
      "elapsedTime":13,
      "errors":[],
       "data":
           [
               {
                "cached":false,
                "fullUrl":"http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look books/",
                "status":"UNSHORTENED",
                "shortUrl":"http://t.co/8lrqrZf"
                "type":"url"
               },
               {
                 "cached":false,
                 "fullUrl":"http://www.cbsnews.com/8301-503543_162-20063168-503543.html",
                 "status":"UNSHORTENED",
                 "shortUrl":"http://bit.ly/jkD0Qr"
                 "type":"url"
                },
                {
                 "fullText":"Tweet! http://twitcaps.com",
                 "shortText":"Tweet! http://bit.ly/11Da1f"
                 "type":"text"
                }
            ]
    }


OR the following XML:

 
    <response>
        <status_code>200</status_code>
        <status_text>OK</status_text>
        <errors />
        <data>
            <entry>
                <type>url</type>
                <shortUrl>http://t.co/8lrqrZf</shortUrl>
                <fullUrl>
                     http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
                </fullUrl>
                <status>UNSHORTENED</status>
                <cached>false</cached>
            </entry>
            <entry>
                <type>url</type>
                <shortUrl>http://bit.ly/jkD0Qr</shortUrl>
                <fullUrl>
                    http://www.cbsnews.com/8301-503543_162-20063168-503543.html
                 </fullUrl>
                <status>UNSHORTENED</status>
                <cached>false</cached>
            </entry>
            <entry>
                <type>text</type>
                <shortText>Tweet! http://bit.ly/11Da1f</shortText>
                <fullText>Tweet! http://twitcaps.com/</fullText>
            </entry>
        </data>
        <elapsedTime>91</elapsedTime>
    </response>


## Other plugins

[urlreversi: Revert your shortened URLs](http://grails.org/plugin/urlreversi)

The urlreversi plugin has been around for quite a while longer than Unshorten and provides the basic functionality of Unshortening (shortUrl-in / fullUrl-out) in a Service as well as a TagLib for convenience.

While it does not feature a Caching implementation as far as I can tell, it should not be too difficult to implement your own cache around its functionality.

## Source Code @ GitHub

The source code is available on GitHub at [https://github.com/boatmeme/grails-unshorten](https://github.com/boatmeme/grails-unshorten). 

Find a bug? Fork it. Fix it. Issue a pull request.

Contributions welcome!

## Issue Tracking @ GitHub

Issue tracking is also on GitHub at [https://github.com/boatmeme/grails-unshorten/issues](https://github.com/boatmeme/grails-unshorten/issues).

Bug reports, Feature requests, and general inquiries welcome.

## Contact

Feel free to contact me by email (jonathan.griggs at gmail.com) or follow me on GitHub at [https://github.com/boatmeme](https://github.com/boatmeme).

# Change Log

## v1.0.4 - 2011.05.26

* `unshorten.http.readTimeout` property was incorrectly named
* Cosmetic changes on the test view, `app_context/translate``

## v1.0.3 - 2011.05.22

* Fixed bug where URL Status was being set to UNKNOWN when it should be set to TIMED_OUT
* AJAX response can now return HTML
* AJAX format parameter supports 'html' value
* Support for 3 new configuration options:


           unshorten.ajax.forward.html   = [controller:'myController', action:'myAction']
           unshorten.ajax.forward.json   = [controller:'myController', action:'myAction']
           unshorten.ajax.forward.xml    = [controller:'myController', action:'myAction']

These can be (optionally) set to a map with the 'controller' and 'action' in your application to forward the results of the Unshorten AJAX action. By specifying these options you can process or style the data before returning it to the browser.

## v1.0.2 - 2011.05.20

* Added UnshortenService.expandUrlsInTextAll() to take a list of 1 - n text blocks and return the results of expanding all of them
* AJAX action now supports ‘shortText’ parameter which operates on blocks of text instead of individual urls
* AJAX response ‘data’ object now returns ‘type’ property. This can be either ‘url’ or ‘text’
* AJAX response now returns ‘elapsedTime’ property (time of call in milliseconds)
* AJAX response can now return XML
* AJAX action now supports ‘format’ parameter which can be either ‘json’ or ‘xml’. Defaults to ‘json’
* Added UrlStatus Enum to UnshortenService

## v1.0.1 - 2011.05.19 

* Added support for redirects via HTTP 302 and 303 (bad shortener!)
* Added support for chaining redirects
* Fixed bug with relative redirects
* Added status for "redirect"

## v1.0 - 2011.05.17

* Initial release