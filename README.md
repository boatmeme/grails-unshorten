
# Unshorten - URL Expander


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


### unshorten.cache.maxSize

This is the maximum number of Unshortened URLs that will be stored in the LRU Cache at any given time. When this number is exceeded, the Least-Recently-Used entry in the Cache will be evicted.

_Defaults to 10000 entries_

### unshorten.http.connectTimeout

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for a response during the TCP Handshake stage of the HTTP connection

It sets the [connectionTimeout](http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setConnectTimeout(\)) property of the URLConnection 

_Defaults to 1000 milliseconds_

>_If you're returning many Unshortened urls with a status of 'TIMED\_OUT' you may try increasing this setting_

### unshorten.http.readTimeout

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for reading from the input stream of an established connection.

It sets the [readTimeout](http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setReadTimeout(\)) property of the URLConnection 

_Defaults to 1000 milliseconds_

>_If you're returning many Unshortened urls with a status of 'TIMED\_OUT' you may try increasing this setting_

## UnshortenService

*unshorten()*

Takes a single String and returns a Map representing the Unshortened URL and ancillary data regarding its HTTP and caching statuses

The values in the returnMap are as follows:

* returnMap.shortUrl = the original URL
* returnMap.fullUrl = the unshortened URL
* returnMap.cached = A boolean that is true if the shortenUrl was retrieved from the cache, and false if HTTP rigamarole was required
* returnMap.status = A String, the state of the URL and one of the following possible values:
                              ** UNSHORTENED - successfully unshortened
                              ** NOT_SHORTENED - URL was already expanded, i.e. no redirect required
                              ** REDIRECTED - URL was redirected internal to the TLD of the original URL
                              ** NOT_FOUND - No document at this URL, returned 404 Error
                              ** TIMED_OUT - Destination server or Shortener API did not respond within the timeouts specified by Config.groovy
                              ** INVALID - Poorly formed, or non-existent URL
                              ** UNKNOWN - Something else happened

{note}A URL that has TIMED_OUT is not cached, the reason being that sometimes Shortening services may be temporarily unavailable now, but responsive next time we try. Considering addressing configuration of this behavior in future releases.{note}

*unshortenAll()*

Takes a single String OR List of Strings representing 1 - n Shortened URLs and returns a Map of Maps with their Unshortened versions, keyed by the ShortURL(s) passed into the method

[TODO: Provide Example]

*expandUrlsInText()*

Takes a String representing a block of text and replaces all URL occurrences with their Unshortened versions

[TODO: Provide Example]

*expandUrlsInTextAll()*

Takes a single String or List of Strings representing a block of text and replaces all URL occurrences with their Unshortened versions returning a Map of Maps with the fullText keyed by the shortText

[TODO: Provide Example]

h2. UnshortenTagLib

*expandUrls*

{code}
<unshorten:expandUrls>
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
</unshorten:expandUrls>
{code}

outputs

{code}
I just tweeted this URL so you could see it http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
and also this one http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
{code}

*expandAndLinkUrls*

{code}
<unshorten:expandAndLinkUrls linkClass="myLinkClass">
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
</unshorten:expandAndLinkUrls>
{code}

outputs

{code}
I just tweeted this URL so you could see it 
<a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
</a>
and also this one 
<a class="myLinkClass" href="http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/">
   http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
</a>
{code}

*unshortenUrl*

{code}
<unshorten:unshortenUrl url='http://bit.ly/jkD0Qr'/>
{code}

outputs

{code}
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
{code}

*unshortenAndLinkUrl*

{code}
<unshorten:unshortenAndLinkUrl class="myLinkClass" url='http://bit.ly/jkD0Qr'/>
{code}

outputs

{code}
<a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
</a>
{code}

h2. UnshortenController

h3. Actions

h4. /unshorten/index 

Provides a test form for validating the functionality of the Unshorten plugin and testing individual URLs. May serve as a template for your own application.

h4. /unshorten/ajax - AJAX

*Parameters*

* *shortUrl* - one or more URLs
* *shortText* - one or more blocks of text that may contain shortened links (i.e. Tweets)
* *format* - 'json' or 'xml'. Determines the format of the response. Defaults to json.

{warning}At least 1 shortUrl OR shortText must be supplied, or the response will return a 500 status_code{warning}

For example, doing an HTTP GET on this URL:

{code}app-context/unshorten/ajax?shortUrl=http://bit.ly/jkD0Qr&shortUrl=http://t.co/8lrqrZf&shortText=Tweet!%20http://bit.ly/11Da1f{code}

might return the following JSON:

{code}
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
{code}

OR the following XML:

{code}
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
{code}

h2. Other plugins

[urlreversi: Revert your shortened URLs|http://grails.org/plugin/urlreversi]

The urlreversi plugin has been around for quite a while longer than Unshorten and provides the basic functionality of Unshortening (shortUrl-in / fullUrl-out) in a Service as well as a TagLib for convenience.

While it does not feature a Caching implementation as far as I can tell, it should not be too difficult to implement your own cache around its functionality.

h2. Contact

Soon, JIRA and Source links

h1. Change Log

h2. v1.0.3 - 2011.05.22

* Fixed bug where URL Status was being set to UNKNOWN when it should be set to TIMED_OUT
* AJAX response can now return HTML
* AJAX format parameter supports 'html' value

* Support for 3 new Configuration options:

{code}unshorten.ajax.forward.html
unshorten.ajax.forward.json
unshorten.ajax.forward.xml{code}

These can be (optionally) set to a map with the 'controller' and 'action' in your application to forward the results of the Unshorten AJAX action. By specifying these options you can process or style the data before returning it to the browser.

h2. v1.0.2 - 2011.05.20

* Added UnshortenService.expandUrlsInTextAll() to take a list of 1 - n text blocks and return the results of expanding all of them
* AJAX action now supports ‘shortText’ parameter which operates on blocks of text instead of individual urls
* AJAX response ‘data’ object now returns ‘type’ property. This can be either ‘url’ or ‘text’
* AJAX response now returns ‘elapsedTime’ property (time of call in milliseconds)
* AJAX response can now return XML
* AJAX action now supports ‘format’ parameter which can be either ‘json’ or ‘xml’. Defaults to ‘json’
* Added UrlStatus Enum to UnshortenService

h2. v1.0.1 - 2011.05.19 

* Added support for redirects via HTTP 302 and 303 (bad shortener!)
* Added support for chaining redirects
* Fixed bug with relative redirects
* Added status for "redirect"

h2. v1.0 - 2011.05.17

* Initial release
h1. Unshorten URL Expander

h2. Shortened URLs

Often these days - and in particular if you are developing applications that integrate with Social Media APIs - you will encounter the incomprehensible yet concise notation of Shortened URLs. There's a whole mess of third-party applications that provide indistinguishably similar services to turn quaint, human-readable, _regular_ URLs into algorithmically-generated munchkins fit for 140 characters. A small sampling includes:

* http://bit.ly
* http://ow.ly
* http://t.co
* http://goo.gl
* http://fb.me
* http://tinyurl.com
* http://is.gd

{note}Wikipedia.org [describes URL Shortening|http://en.wikipedia.org/wiki/URL_shortening] as:

...a technique on the World Wide Web in which a Uniform Resource Locator (URL) may be made substantially shorter in length and still direct to the required page. 

This is achieved by using an HTTP Redirect on a domain name that is short, which links to the web page that has a long URL. For example, the URL {code}http://en.wikipedia.org/wiki/URL_shortening{code} can be shortened to {code}http://bit.ly/urlwiki{code} This is especially convenient for messaging technologies such as Twitter and Identi.ca, which severely limit the number of characters that may be used in a message. Short URLs allow otherwise long web addresses to be referred to in a tweet.{note}

h3. Drawbacks

All well and good. Smaller links are useful and mo' better then, right?

If you're trying to Tweet a link, the answer is almost certainly yes. In other situations, and outside of the artificial constraints of Twitter and other micro-blogging platforms, the answer may not be so clear cut. Here are some potential negative impacts to consider:

* Your browsing experience is at the mercy of the Third-Party Shorten service, for example they may choose to implement floating frames or other such UX annoyances
* Your browsing / clicking habits may be being tracked, correlated, crunched, and cross-referenced. This may be for purposes no more nefarious than marketing or hive-mind data analytics, but there is rarely, if ever, any way to opt out of this behavior.
* The original URL is obfuscated, which itself reveals a slew of problematic consequences
** The ShortURL is most often less informative than the original URL
** You could be forwarded to an unsafe web site and never know until you get there
** You could be subject to severe Rickrollings or, worse, Goat.se'd into the unemployment line

and, most importantly for the purposes of this plugin:

* There is often information within the Original, Unshortened URL that needs to be acted upon - either programmatically, or functionally - by your application

h2. Unshortened URLs

In a shocking turn of events, and right along side the recent glut of Third-Party URL Shorteners, a symbiotic ecosystem of Third-Party URL *UN*shorteners has evolved to serve precisely this niche.

Most provide some kind of API access, usually RESTful, often returning JSON and less frequently XML. For many applications - and particularly so for exclusively client-side apps - this is the quickest, easiest or only way to implement the unshortening functionality.

Still, the drawbacks of using such services are readily apparent:

* You're coupling your application to yet another third-party API, and one that - in most cases - enables only a secondary behavior
* You're at the mercy of the API's rate limits - which, admittedly, may or may not be a concern for your application
* Strangely, to my mind, some URL Unshorteners only support a certain subset of Short URL formats.
* Because of rate limits and for the sake of performance, you are usually still stuck implementing some sort of caching scheme to minimize the API calls

If your volume of Unshorten requests goes even slightly beyond the bare minimum, and you're running on a web application framework like Grails, there really is no reason not to perform the Unshortening work from within your application. You've probably already done much of the work if you're at all concerned about performance.

Indeed, the vast majority of code required for such a thing is focused around optimizing performance, caching the data, and structuring the requests and responses in the most useful and flexible ways.

Fortunately, as a Grails developer, you can just install the Unshorten plugin in your own application and enjoy these benefits immediately.

h2. Unshorten Plugin

h3. Description

The Unshorten plugin provides a means for your Grails application to expand Shortened URLs (http://bit.ly/jkD0Qr and http://tinyurl.com/3vy9xga, for example)
into their Original, Unshortened form (http://amazon.com or http://grails.org, for example) without the need for calling a Third-Party API.

Provides a Grails Service, TagLib, and Controller to your application.

Backed by a configurable Least-Recently-Used (LRU) Cache, the Unshorten plugin optimizes performance by making a minimal number of HTTP HEAD method calls.

Contains service calls and controller actions to dynamically handle single or multiple URLs as well as GSP Tags to unshorten URLs within blocks of text, and to create links

h3. Installation

Enter your application directory and run the following from the command line: 

{code}grails install-plugin unshorten{code}

h3. Configuration

The UnshortenPlugin may be configured with several parameters, all specified in your application's */grails-app/conf/Config.groovy*

{code}
unshorten.cache.maxSize        = 10000   // Default 10000
unshorten.http.connectTimeout = 1000     // millis, default 1000
unshorten.http.readTimeout      = 1000     // millis, default 1000
{code}

*unshorten.cache.maxSize*

This is the maximum number of Unshortened URLs that will be stored in the LRU Cache at any given time. When this number is exceeded, the Least-Recently-Used entry in the Cache will be evicted.

{note}Defaults to 10000 entries{note}

*unshorten.http.connectTimeout*

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for a response during the TCP Handshake stage of the HTTP connection

It sets the [connectionTimeout|http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setConnectTimeout()] property of the URLConnection 

{note}Defaults to 1000 milliseconds{note}

{warning}If you're returning many Unshortened urls with a status of 'timed_out' you may try increasing this setting{warning}

*unshorten.http.readTimeout*

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for reading from the input stream of an established connection.

It sets the [readTimeout|http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setReadTimeout()] property of the URLConnection 

{note}Defaults to 1000 milliseconds{note}

{warning}If you're returning many Unshortened urls with a status of 'timed_out' you may try increasing this setting{warning}

h2. UnshortenService

*unshorten()*

Takes a single String and returns a Map representing the Unshortened URL and ancillary data regarding its HTTP and caching statuses

The values in the returnMap are as follows:

* returnMap.shortUrl = the original URL
* returnMap.fullUrl = the unshortened URL
* returnMap.cached = A boolean that is true if the shortenUrl was retrieved from the cache, and false if HTTP rigamarole was required
* returnMap.status = A String, the state of the URL and one of the following possible values:
                              ** UNSHORTENED - successfully unshortened
                              ** NOT_SHORTENED - URL was already expanded, i.e. no redirect required
                              ** REDIRECTED - URL was redirected internal to the TLD of the original URL
                              ** NOT_FOUND - No document at this URL, returned 404 Error
                              ** TIMED_OUT - Destination server or Shortener API did not respond within the timeouts specified by Config.groovy
                              ** INVALID - Poorly formed, or non-existent URL
                              ** UNKNOWN - Something else happened

{note}A URL that has TIMED_OUT is not cached, the reason being that sometimes Shortening services may be temporarily unavailable now, but responsive next time we try. Considering addressing configuration of this behavior in future releases.{note}

*unshortenAll()*

Takes a single String OR List of Strings representing 1 - n Shortened URLs and returns a Map of Maps with their Unshortened versions, keyed by the ShortURL(s) passed into the method

[TODO: Provide Example]

*expandUrlsInText()*

Takes a String representing a block of text and replaces all URL occurrences with their Unshortened versions

[TODO: Provide Example]

*expandUrlsInTextAll()*

Takes a single String or List of Strings representing a block of text and replaces all URL occurrences with their Unshortened versions returning a Map of Maps with the fullText keyed by the shortText

[TODO: Provide Example]

h2. UnshortenTagLib

*expandUrls*

{code}
<unshorten:expandUrls>
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
</unshorten:expandUrls>
{code}

outputs

{code}
I just tweeted this URL so you could see it http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
and also this one http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
{code}

*expandAndLinkUrls*

{code}
<unshorten:expandAndLinkUrls linkClass="myLinkClass">
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
</unshorten:expandAndLinkUrls>
{code}

outputs

{code}
I just tweeted this URL so you could see it 
<a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
</a>
and also this one 
<a class="myLinkClass" href="http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/">
   http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
</a>
{code}

*unshortenUrl*

{code}
<unshorten:unshortenUrl url='http://bit.ly/jkD0Qr'/>
{code}

outputs

{code}
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
{code}

*unshortenAndLinkUrl*

{code}
<unshorten:unshortenAndLinkUrl class="myLinkClass" url='http://bit.ly/jkD0Qr'/>
{code}

outputs

{code}
<a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
</a>
{code}

h2. UnshortenController

h3. Actions

h4. /unshorten/index 

Provides a test form for validating the functionality of the Unshorten plugin and testing individual URLs. May serve as a template for your own application.

h4. /unshorten/ajax - AJAX

*Parameters*

* *shortUrl* - one or more URLs
* *shortText* - one or more blocks of text that may contain shortened links (i.e. Tweets)
* *format* - 'json' or 'xml'. Determines the format of the response. Defaults to json.

{warning}At least 1 shortUrl OR shortText must be supplied, or the response will return a 500 status_code{warning}

For example, doing an HTTP GET on this URL:

{code}app-context/unshorten/ajax?shortUrl=http://bit.ly/jkD0Qr&shortUrl=http://t.co/8lrqrZf&shortText=Tweet!%20http://bit.ly/11Da1f{code}

might return the following JSON:

{code}
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
{code}

OR the following XML:

{code}
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
{code}

h2. Other plugins

[urlreversi: Revert your shortened URLs|http://grails.org/plugin/urlreversi]

The urlreversi plugin has been around for quite a while longer than Unshorten and provides the basic functionality of Unshortening (shortUrl-in / fullUrl-out) in a Service as well as a TagLib for convenience.

While it does not feature a Caching implementation as far as I can tell, it should not be too difficult to implement your own cache around its functionality.

h2. Contact

Soon, JIRA and Source links

h1. Change Log

h2. v1.0.3 - 2011.05.22

* Fixed bug where URL Status was being set to UNKNOWN when it should be set to TIMED_OUT
* AJAX response can now return HTML
* AJAX format parameter supports 'html' value

* Support for 3 new Configuration options:

{code}unshorten.ajax.forward.html
unshorten.ajax.forward.json
unshorten.ajax.forward.xml{code}

These can be (optionally) set to a map with the 'controller' and 'action' in your application to forward the results of the Unshorten AJAX action. By specifying these options you can process or style the data before returning it to the browser.

h2. v1.0.2 - 2011.05.20

* Added UnshortenService.expandUrlsInTextAll() to take a list of 1 - n text blocks and return the results of expanding all of them
* AJAX action now supports ‘shortText’ parameter which operates on blocks of text instead of individual urls
* AJAX response ‘data’ object now returns ‘type’ property. This can be either ‘url’ or ‘text’
* AJAX response now returns ‘elapsedTime’ property (time of call in milliseconds)
* AJAX response can now return XML
* AJAX action now supports ‘format’ parameter which can be either ‘json’ or ‘xml’. Defaults to ‘json’
* Added UrlStatus Enum to UnshortenService

h2. v1.0.1 - 2011.05.19 

* Added support for redirects via HTTP 302 and 303 (bad shortener!)
* Added support for chaining redirects
* Fixed bug with relative redirects
* Added status for "redirect"

h2. v1.0 - 2011.05.17

* Initial release


The goal of this project is to provide a more simple, easy to use and less verbose API to work with mongodb using the Groovy programming language.

More information can be found here: http://blog.paulopoiati.com.

# Support

Any bug, suggestion or ... whatever.

Email: paulogpoiati@gmail.com

# Usage
	// To download GMongo on the fly and put it at classpath
	@Grab(group='com.gmongo', module='gmongo', version='0.5.1')
	import com.gmongo.GMongo
	// Instantiate a com.gmongo.GMongo object instead of com.mongodb.Mongo
	// The same constructors and methods are available here
	def mongo = new GMongo()
	
	// Get a db reference in the old fashion way
	def db = mongo.getDB("gmongo")
	
	// Collections can be accessed as a db property (like the javascript API)
	assert db.myCollection instanceof com.mongodb.DBCollection
	// They also can be accessed with array notation 
	assert db['my.collection'] instanceof com.mongodb.DBCollection
	
	// Insert a document
	db.languages.insert([name: 'Groovy'])
	// A less verbose way to do it
	db.languages.insert(name: 'Ruby')
	// Yet another way
	db.languages << [name: 'Python']
	
	// Insert a list of documents
	db.languages << [[name: 'Javascript', type: 'prototyped'], [name: 'Ioke', type: 'prototyped']]
	
	def statics = ['Java', 'C', 'VB']
	
	statics.each {
		db.languages << [name: it, type: 'static']
	}
	
	// Finding the first document
	def lang = db.languages.findOne()
	assert lang.name == 'Groovy'
	// Set a new property
	lang.site = 'http://groovy.codehaus.org/'
	// Save the new version
	db.languages.save lang
	
	assert db.languages.findOne(name: 'Groovy').site == 'http://groovy.codehaus.org/'
	
	// Counting the number of documents in the collection
	assert db.languages.find(type: 'static').count() == 3
	
	// Another way to count
	assert db.languages.count(type: 'prototyped') == 2
	
	// Updating a document using the '$set' operator
	db.languages.update([name: 'Python'], [$set: [paradigms: ['object-oriented', 'functional', 'imperative']]])
	
	assert 3 == db.languages.findOne(name: 'Python').paradigms.size()
	
	// Using upsert
	db.languages.update([name: 'Haskel'], [$set: [paradigms: ['functional']]], true)
	
	assert db.languages.findOne(name: 'Haskel')
	
	// Removing some documents
	db.languages.remove(type: 'prototyped')
	assert 0 == db.languages.count(type: 'prototyped')
	
	// Removing all documents
	db.languages.remove([:])
	assert 0 == db.languages.count()
	
	// To ensure complete consistency in a session use DB#inRequest
	// It is analogous to user DB#requestStarted and DB#requestDone
	db.inRequest {
		db.languages.insert(name: 'Objective-C')
		assert 1 == db.languages.count(name: 'Objective-C')
	}
	
## MapReduce
	@Grab(group='com.gmongo', module='gmongo', version='0.5.1')
	import com.gmongo.GMongo
	
	def mongo = new GMongo()
	def db = mongo.getDB("gmongo")
	
	def words = ['foo', 'bar', 'baz']
	def rand  = new Random()		

	1000.times { 
		db.words << [word: words[rand.nextInt(3)]]
	}
	
	assert db.words.count() == 1000
	
	def result = db.words.mapReduce(
		"""
		function map() {
			emit(this.word, {count: 1})
		}
		""",
		"""
		function reduce(key, values) {
			var count = 0
			for (var i = 0; i < values.length; i++)
				count += values[i].count
			return {count: count}
		}
		""",
		"mrresult",
		[:] // No Query
	)
	
	assert db.mrresult.count() == 3
	assert db.mrresult.find()*.value*.count.sum() == 1000

# Build
				
The project is build using gradle. Gradle can be found in: http://www.gradle.org
				
# Test
				
To run the tests start a mongo instance on localhost:27017h1. Unshorten URL Expander

h2. Shortened URLs

Often these days - and in particular if you are developing applications that integrate with Social Media APIs - you will encounter the incomprehensible yet concise notation of Shortened URLs. There's a whole mess of third-party applications that provide indistinguishably similar services to turn quaint, human-readable, _regular_ URLs into algorithmically-generated munchkins fit for 140 characters. A small sampling includes:

* http://bit.ly
* http://ow.ly
* http://t.co
* http://goo.gl
* http://fb.me
* http://tinyurl.com
* http://is.gd

{note}Wikipedia.org [describes URL Shortening|http://en.wikipedia.org/wiki/URL_shortening] as:

...a technique on the World Wide Web in which a Uniform Resource Locator (URL) may be made substantially shorter in length and still direct to the required page. 

This is achieved by using an HTTP Redirect on a domain name that is short, which links to the web page that has a long URL. For example, the URL {code}http://en.wikipedia.org/wiki/URL_shortening{code} can be shortened to {code}http://bit.ly/urlwiki{code} This is especially convenient for messaging technologies such as Twitter and Identi.ca, which severely limit the number of characters that may be used in a message. Short URLs allow otherwise long web addresses to be referred to in a tweet.{note}

h3. Drawbacks

All well and good. Smaller links are useful and mo' better then, right?

If you're trying to Tweet a link, the answer is almost certainly yes. In other situations, and outside of the artificial constraints of Twitter and other micro-blogging platforms, the answer may not be so clear cut. Here are some potential negative impacts to consider:

* Your browsing experience is at the mercy of the Third-Party Shorten service, for example they may choose to implement floating frames or other such UX annoyances
* Your browsing / clicking habits may be being tracked, correlated, crunched, and cross-referenced. This may be for purposes no more nefarious than marketing or hive-mind data analytics, but there is rarely, if ever, any way to opt out of this behavior.
* The original URL is obfuscated, which itself reveals a slew of problematic consequences
** The ShortURL is most often less informative than the original URL
** You could be forwarded to an unsafe web site and never know until you get there
** You could be subject to severe Rickrollings or, worse, Goat.se'd into the unemployment line

and, most importantly for the purposes of this plugin:

* There is often information within the Original, Unshortened URL that needs to be acted upon - either programmatically, or functionally - by your application

h2. Unshortened URLs

In a shocking turn of events, and right along side the recent glut of Third-Party URL Shorteners, a symbiotic ecosystem of Third-Party URL *UN*shorteners has evolved to serve precisely this niche.

Most provide some kind of API access, usually RESTful, often returning JSON and less frequently XML. For many applications - and particularly so for exclusively client-side apps - this is the quickest, easiest or only way to implement the unshortening functionality.

Still, the drawbacks of using such services are readily apparent:

* You're coupling your application to yet another third-party API, and one that - in most cases - enables only a secondary behavior
* You're at the mercy of the API's rate limits - which, admittedly, may or may not be a concern for your application
* Strangely, to my mind, some URL Unshorteners only support a certain subset of Short URL formats.
* Because of rate limits and for the sake of performance, you are usually still stuck implementing some sort of caching scheme to minimize the API calls

If your volume of Unshorten requests goes even slightly beyond the bare minimum, and you're running on a web application framework like Grails, there really is no reason not to perform the Unshortening work from within your application. You've probably already done much of the work if you're at all concerned about performance.

Indeed, the vast majority of code required for such a thing is focused around optimizing performance, caching the data, and structuring the requests and responses in the most useful and flexible ways.

Fortunately, as a Grails developer, you can just install the Unshorten plugin in your own application and enjoy these benefits immediately.

h2. Unshorten Plugin

h3. Description

The Unshorten plugin provides a means for your Grails application to expand Shortened URLs (http://bit.ly/jkD0Qr and http://tinyurl.com/3vy9xga, for example)
into their Original, Unshortened form (http://amazon.com or http://grails.org, for example) without the need for calling a Third-Party API.

Provides a Grails Service, TagLib, and Controller to your application.

Backed by a configurable Least-Recently-Used (LRU) Cache, the Unshorten plugin optimizes performance by making a minimal number of HTTP HEAD method calls.

Contains service calls and controller actions to dynamically handle single or multiple URLs as well as GSP Tags to unshorten URLs within blocks of text, and to create links

h3. Installation

Enter your application directory and run the following from the command line: 

{code}grails install-plugin unshorten{code}

h3. Configuration

The UnshortenPlugin may be configured with several parameters, all specified in your application's */grails-app/conf/Config.groovy*

{code}
unshorten.cache.maxSize        = 10000   // Default 10000
unshorten.http.connectTimeout = 1000     // millis, default 1000
unshorten.http.readTimeout      = 1000     // millis, default 1000
{code}

*unshorten.cache.maxSize*

This is the maximum number of Unshortened URLs that will be stored in the LRU Cache at any given time. When this number is exceeded, the Least-Recently-Used entry in the Cache will be evicted.

{note}Defaults to 10000 entries{note}

*unshorten.http.connectTimeout*

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for a response during the TCP Handshake stage of the HTTP connection

It sets the [connectionTimeout|http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setConnectTimeout()] property of the URLConnection 

{note}Defaults to 1000 milliseconds{note}

{warning}If you're returning many Unshortened urls with a status of 'timed_out' you may try increasing this setting{warning}

*unshorten.http.readTimeout*

A java.net.URLConnection is used to perform the unshorten functionality of the plugin.

This is the maximum amount of time in milliseconds that we will wait for reading from the input stream of an established connection.

It sets the [readTimeout|http://download.oracle.com/javase/6/docs/api/java/net/URLConnection.html#setReadTimeout()] property of the URLConnection 

{note}Defaults to 1000 milliseconds{note}

{warning}If you're returning many Unshortened urls with a status of 'timed_out' you may try increasing this setting{warning}

h2. UnshortenService

*unshorten()*

Takes a single String and returns a Map representing the Unshortened URL and ancillary data regarding its HTTP and caching statuses

The values in the returnMap are as follows:

* returnMap.shortUrl = the original URL
* returnMap.fullUrl = the unshortened URL
* returnMap.cached = A boolean that is true if the shortenUrl was retrieved from the cache, and false if HTTP rigamarole was required
* returnMap.status = A String, the state of the URL and one of the following possible values:
                              ** UNSHORTENED - successfully unshortened
                              ** NOT_SHORTENED - URL was already expanded, i.e. no redirect required
                              ** REDIRECTED - URL was redirected internal to the TLD of the original URL
                              ** NOT_FOUND - No document at this URL, returned 404 Error
                              ** TIMED_OUT - Destination server or Shortener API did not respond within the timeouts specified by Config.groovy
                              ** INVALID - Poorly formed, or non-existent URL
                              ** UNKNOWN - Something else happened

{note}A URL that has TIMED_OUT is not cached, the reason being that sometimes Shortening services may be temporarily unavailable now, but responsive next time we try. Considering addressing configuration of this behavior in future releases.{note}

*unshortenAll()*

Takes a single String OR List of Strings representing 1 - n Shortened URLs and returns a Map of Maps with their Unshortened versions, keyed by the ShortURL(s) passed into the method

[TODO: Provide Example]

*expandUrlsInText()*

Takes a String representing a block of text and replaces all URL occurrences with their Unshortened versions

[TODO: Provide Example]

*expandUrlsInTextAll()*

Takes a single String or List of Strings representing a block of text and replaces all URL occurrences with their Unshortened versions returning a Map of Maps with the fullText keyed by the shortText

[TODO: Provide Example]

h2. UnshortenTagLib

*expandUrls*

{code}
<unshorten:expandUrls>
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
</unshorten:expandUrls>
{code}

outputs

{code}
I just tweeted this URL so you could see it http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
and also this one http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
{code}

*expandAndLinkUrls*

{code}
<unshorten:expandAndLinkUrls linkClass="myLinkClass">
        I just tweeted this URL so you could see it http://bit.ly/jkD0Qr, 
        and also this one http://t.co/8lrqrZf
</unshorten:expandAndLinkUrls>
{code}

outputs

{code}
I just tweeted this URL so you could see it 
<a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
</a>
and also this one 
<a class="myLinkClass" href="http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/">
   http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/
</a>
{code}

*unshortenUrl*

{code}
<unshorten:unshortenUrl url='http://bit.ly/jkD0Qr'/>
{code}

outputs

{code}
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
{code}

*unshortenAndLinkUrl*

{code}
<unshorten:unshortenAndLinkUrl class="myLinkClass" url='http://bit.ly/jkD0Qr'/>
{code}

outputs

{code}
<a class="myLinkClass" href="http://www.cbsnews.com/8301-503543_162-20063168-503543.html">
http://www.cbsnews.com/8301-503543_162-20063168-503543.html 
</a>
{code}

h2. UnshortenController

h3. Actions

h4. /unshorten/index 

Provides a test form for validating the functionality of the Unshorten plugin and testing individual URLs. May serve as a template for your own application.

h4. /unshorten/ajax - AJAX

*Parameters*

* *shortUrl* - one or more URLs
* *shortText* - one or more blocks of text that may contain shortened links (i.e. Tweets)
* *format* - 'json' or 'xml'. Determines the format of the response. Defaults to json.

{warning}At least 1 shortUrl OR shortText must be supplied, or the response will return a 500 status_code{warning}

For example, doing an HTTP GET on this URL:

{code}app-context/unshorten/ajax?shortUrl=http://bit.ly/jkD0Qr&shortUrl=http://t.co/8lrqrZf&shortText=Tweet!%20http://bit.ly/11Da1f{code}

might return the following JSON:

{code}
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
{code}

OR the following XML:

{code}
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
{code}

h2. Other plugins

[urlreversi: Revert your shortened URLs|http://grails.org/plugin/urlreversi]

The urlreversi plugin has been around for quite a while longer than Unshorten and provides the basic functionality of Unshortening (shortUrl-in / fullUrl-out) in a Service as well as a TagLib for convenience.

While it does not feature a Caching implementation as far as I can tell, it should not be too difficult to implement your own cache around its functionality.

h2. Contact

Soon, JIRA and Source links

h1. Change Log

h2. v1.0.3 - 2011.05.22

* Fixed bug where URL Status was being set to UNKNOWN when it should be set to TIMED_OUT
* AJAX response can now return HTML
* AJAX format parameter supports 'html' value

* Support for 3 new Configuration options:

{code}unshorten.ajax.forward.html
unshorten.ajax.forward.json
unshorten.ajax.forward.xml{code}

These can be (optionally) set to a map with the 'controller' and 'action' in your application to forward the results of the Unshorten AJAX action. By specifying these options you can process or style the data before returning it to the browser.

h2. v1.0.2 - 2011.05.20

* Added UnshortenService.expandUrlsInTextAll() to take a list of 1 - n text blocks and return the results of expanding all of them
* AJAX action now supports ‘shortText’ parameter which operates on blocks of text instead of individual urls
* AJAX response ‘data’ object now returns ‘type’ property. This can be either ‘url’ or ‘text’
* AJAX response now returns ‘elapsedTime’ property (time of call in milliseconds)
* AJAX response can now return XML
* AJAX action now supports ‘format’ parameter which can be either ‘json’ or ‘xml’. Defaults to ‘json’
* Added UrlStatus Enum to UnshortenService

h2. v1.0.1 - 2011.05.19 

* Added support for redirects via HTTP 302 and 303 (bad shortener!)
* Added support for chaining redirects
* Fixed bug with relative redirects
* Added status for "redirect"

h2. v1.0 - 2011.05.17

* Initial release
