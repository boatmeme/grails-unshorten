package com.memetix.unshorten

/**
 * UnshortenTagLib 
 * 
 * namespace:unshorten
 * 
 * tags:    expandAndLinkUrls
 *          expandUrls
 *          unshortenUrl
 *          unshortenAndLinkUrl
 *        
 * 
 * @author Jonathan Griggs  <twitcaps.developer @ gmail.com>
 * @version     1.0     2011.05.17                              
 * @since       1.0     2011.05.17                            
 */
class UnshortenTagLib {
    static namespace = "unshorten"
    def unshortenService
    static urlRegex = /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:\/~\+#]*[\w\-\@?^=%&amp;\/~\+#])/
    
 
    def expandAndLinkUrls = { attrs,body ->
        def linkClass = attrs?.linkClass ? " class=\"${attrs.linkClass}\" " : " "
        def text = body()
        
        def String expandText = unshortenService.expandUrlsInText(text)
      
        def urls = expandText?.findAll(urlRegex) as Set
        for(url in urls) {
            expandText = expandText.replace(url,"<a${linkClass}href=\"${url}\">${url}</a>")
        }
        out << expandText
    }
    
    def expandUrls = { attrs, body ->
        def text = body()  
        def expandText = unshortenService.expandUrlsInText(text)
        out << expandText
    }
    
    def unshortenUrl = { attrs ->
        def url = attrs?.url  
        if(!url) throw new IllegalArgumentException("'url' attribute is required on unshorten:unshortenUrl tag") 
        def newUrl = unshortenService?.unshorten(url)?.fullUrl
        out << newUrl
    }
    
    def unshortenAndLinkUrl = { attrs ->
        def url = attrs?.url  
        if(!url) throw new IllegalArgumentException("'url' attribute is required on unshorten:unshortenUrl tag") 
        def linkClass = attrs?.linkClass ? " class=\"${attrs.linkClass}\" " : " "
        def newUrl = unshortenService.unshorten(url)?.fullUrl
        out << "<a${linkClass}href=\"${newUrl}\">${newUrl}</a>"
    }
}
