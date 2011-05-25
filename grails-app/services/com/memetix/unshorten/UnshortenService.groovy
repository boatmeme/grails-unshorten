package com.memetix.unshorten

import org.codehaus.groovy.grails.commons.ConfigurationHolder

enum UrlStatus {UNSHORTENED,NOT_FOUND,REDIRECTED,TIMED_OUT,INVALID,NOT_SHORTENED,UNKNOWN}
/**
 * UnshortenService provides services that take shortened URLs as input: 
 * 
 *      [http://t.co/8lrqrZf, http://tinyurl.com/3vy9xga, http://bit.ly/jkD0Qr] for example 
 *  
 * And returns the corresponding expanded, original URLs
 * 
 * There is a service unshorten(), for single links that returns a single String representing the expanded URL
 * There is a service unshortenAll(), which is used for lists of shortened URLs. It returns a Map with expanded URL values keyed by the shortened url
 * 
 * The UnshortenService is backed by a Thread-safe LRU Cache with a configurable maxSize that defaults to 10000. Unshortened/Expanded Links 
 * are stored as Strings in the cache, keyed by the Shortened version of the URL
 * 
 * In config.groovy, you may configure the following parameters:
 * 
 *      unshorten.cache.maxSize (default: 10000) : an integer that defines the maximum number of entries in the link cache
 *      unshorten.http.connectTimeout (default 1000) : time in milliseconds defining the maximum time to wait before giving up connecting to a URL (HTTP Connect Timeout)
 *      unshorten.http.readTimeout (default 1000) : time in milliseconds defining the maximum time to wait before giving up on socket reads during unshortening a URL (HTTP Read Timeout)
 * 
 * @author Jonathan Griggs  <twitcaps.developer @ gmail.com>
 * @version     1.0.3   2011.05.20                              
 * @since       1.0     2011.05.17                            
 */

class UnshortenService {
    static transactional = false
    static urlRegex = /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:\/~\+#]*[\w\-\@?^=%&amp;\/~\+#])/
    def maxCacheSize = ConfigurationHolder?.config?.unshorten?.cache?.maxSize ?: 10000
    def connectTimeoutInMilliseconds = ConfigurationHolder?.config?.unshorten?.http?.connectTimeout ?: 1000
    def readTimeoutInMilliseconds = ConfigurationHolder?.config?.unshorten?.http?.readTimeout ?: 1000
    def cache = new LRUCache(maxCacheSize)
    static scope = "singleton"
    
    /**
     * unshorten()                         
     *
     * Takes a shortened string and gives you back the expanded string
     * 
     * If the linkString parameter matches the expanded version, then the normalized version of the original linkString is returned.
     * If the linkString parameter is an invalid URL, then location is null
     * 
     * This method will first attempt to load the expanded URL from the LRUCache. If the linkString matches the expanded version, the
     * result is not stored in the cache.
     *
     * @param  linkString A string representing a shortened URL (i.e. http://bit.ly/jkD0Qr)           
     * @return A Map representing the properties of the expanded, original URL
     *  
     *      returnMap.shortUrl = the original URL
     *      returnMap.fullUrl = the unshortened URL
     *      returnMap.cached = A boolean that is true if the shortenUrl was retrieved from the cache, and false if HTTP rigamarole was required
     *      returnMap.status = A String, the state of the URL and one of the following possible values:
     *                          "unshortened" = successfully unshortened
     *                          "not_shortened" = URL was already expanded, i.e. no redirect required
     *                          "404_not_found" = No document at this URL, returned 404 Error
     *                          "timed_out" = Destination server or Shortener API did not respond within the timeouts specified by Config.groovy
     *                          "invalid" = Poorly formed, or non-existent URL
     *                          "unknown" = Something else happened
     *                          
     * @version     1.0.3   2011.05.20                              
     * @since       1.0     2011.05.17  
     *               
     */
    def unshorten(linkString) {
        log.debug "Unshortening Link ${linkString}"
        def link = linkString?.trim()
        
        if(link
           &&!link.toLowerCase().contains("http://")
           &&!link.toLowerCase().contains("https://")
           &&!link.toLowerCase().contains("ftp://")) {
            link = "http://${link}"
        }
        
        def location = cache.get(link.toString())
        
        if(location) {
            location.cached = true
            log.debug "Fetched Link from cache - [${link} : ${location}]"
            return location
        } else {
            location = new HashMap()
            location.shortUrl = link
        }
        
        try {
            def url = new URL(link)
            URLConnection urlc = url.openConnection();
            urlc.setRequestMethod("HEAD");
            urlc.setInstanceFollowRedirects( false );
            urlc.setConnectTimeout(connectTimeoutInMilliseconds);
            urlc.setReadTimeout(readTimeoutInMilliseconds);
            urlc.connect()
            def responseCode = urlc.getResponseCode();
            if(responseCode>300&&responseCode<304) {
                location.fullUrl = urlc.getHeaderField("Location");
                if(location?.fullUrl?.contains(url?.getHost())) {
                    location.status = UrlStatus.REDIRECTED.toString()
                } else if(location.fullUrl
                       &&!location?.fullUrl?.toLowerCase().contains("http://")
                       &&!location?.fullUrl?.toLowerCase().contains("https://")
                       &&!location?.fullUrl?.toLowerCase().contains("ftp://")) {
                    location.fullUrl = "${url?.getAuthority()}${location?.fullUrl}"
                    location.status = UrlStatus.REDIRECTED
                } else {
                    location.status = UrlStatus.UNSHORTENED
                }
                location.fullUrl = unshorten(location.fullUrl)?.fullUrl?.trim()
            } else if(responseCode==404) {
                location.fullUrl = link
                location.status = UrlStatus.NOT_FOUND
            } else {
                location.fullUrl = link
                location.status = UrlStatus.NOT_SHORTENED
            }
        } catch(SocketTimeoutException e) {
            log.debug "Timeout Unshortening URL ${linkString}: ${e}"
            location.fullUrl = link
            location.status = UrlStatus.TIMED_OUT
        } catch (Exception e) {
            location.fullUrl = link
            location.status = UrlStatus.INVALID
            log.debug "Error Unshortening URL ${linkString}: ${e}"
        }
        
        if(location&&location.status!=UrlStatus.TIMED_OUT) {
            log.debug "Caching [${link} : ${location}]"
            cache.put(link.toString(),location)
        } else {
            location.fullUrl = link
            if(!location.status)
                location.status = UrlStatus.UNKNOWN
            log.debug "Not Caching [${link} : ${location}]"
        }
        location.cached = false
        location.status = location.status.toString()
        return location
    }

    /**
     * unshortenAll()                         
     *
     * Takes a list of strings representing shortened urls and gives you back the expanded strings in a Map keyed by the shortened URL
     * 
     * This method can take a single String object OR a list of Strings. The return value is always a map of Expanded String values
     * keyed by the shortened URLs
     *
     * @param  list A list of strings representing 1 - n shortened URLs (i.e. http://bit.ly/jkD0Qr)           
     * @return A Map of Maps representing the list of expanded, original URLs, keyed by the shortened URLs
     *
     * @version     1.0.1   2011.05.18                              
     * @since       1.0     2011.05.17
     */
    def unshortenAll(list) {
       log.debug "unshortenAll() : ${list}"
       def returnMap = new HashMap()
       for(link in asList(list)) {
           def fullLink = unshorten(link)
           if(fullLink) {
            returnMap.put(link,fullLink)
           }
       } 
       log.debug "unshortenAll() RETURNING: ${returnMap}"
       return returnMap
    }
    
    /**
     * expandUrlsInText()                         
     *
     * Takes in a text string - for example, a Tweet - that may or may not contain any URLs
     * 
     * If any URLs are found, this method attempts to unshorten it, and replaces the original shortened
     * URL with the expanded URL text. 
     *
     * @param  text A string of text that may or may not contain URLs         
     * @return The original input string, with all shortened URLs replaced by their unshortened versions
     *
     * @version     1.0.1   2011.05.18                              
     * @since       1.0     2011.05.17
     */
    def expandUrlsInText(text) {
        log.debug "expandUrlsInText(): ${text}"
        def expandText = text
        def urls = expandText?.findAll(urlRegex) as Set
        for(url in urls) {
            def fullUrl = unshorten(url)?.fullUrl
            if(fullUrl&&url!=fullUrl) {
                log.debug "expandUrlsInText - Replacing Url - [${url} : ${fullUrl}]"
                expandText = expandText.replaceAll(url,fullUrl)
            }
        }
        log.debug "expandUrlsInText() RETURNING: ${expandText}"
        return expandText
    }
    
    /**
     * expandUrlsInTextAll()                         
     *
     * Takes in a multiple text string - for example, a Tweet - that may or may not contain any URLs
     * 
     * This method can take a single String object OR a list of Strings. The return value is always a map of Expanded String values
     * keyed by the shortened URLs*
     * 
     * @param  list A list of strings representing 1 - n text blobs, possibly containing URLs (i.e. Hello, this is a link http://bit.ly/jkD0Qr)           
     * @return A Map of Maps representing the list of text blobs containing expanded, original URLs, keyed by the shortened text blobs
     *
     * @version     1.0.2   2011.05.19                              
     * @since       1.0.2   2011.05.19
     */
    def expandUrlsInTextAll(text) {
       log.debug "expandUrlsInTextAll() : ${text}"
       def returnMap = new HashMap()
       for(tBlob in asList(text)) {
           def expandedText = expandUrlsInText(tBlob)
           if(expandedText) {
            returnMap.put(tBlob,expandedText)
           }
       } 
       log.debug "expandUrlsInTextAll() RETURNING: ${returnMap}"
       return returnMap
    }
    
    /**
     * asList()
     * 
     * Private helper method that flattens a list. This is useful because it will take lists of lists, or even a single object
     * and return everything in one list.
     * 
     * Mostly useful for the case of dealing with a single object and wrapping it in a list.
     * @version     1.0.1   2011.05.18                              
     * @since       1.0     2011.05.17
    */
    private asList(orig) {
        return [orig].flatten()
    }
}
