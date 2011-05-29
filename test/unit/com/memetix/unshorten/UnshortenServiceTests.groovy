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

class UnshortenServiceTests extends GrailsUnitTestCase {
    def unshortenService
    def log

    protected void setUp() {
        setupLogger()
        unshortenService = new UnshortenService()
        unshortenService.afterPropertiesSet()
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
    
    private setupLogger() {
        // build a logger...
        BasicConfigurator.configure() 
        LogManager.rootLogger.level = Level.DEBUG
        log = LogManager.getLogger("UnshortenService")

        // use groovy metaClass to put the log into your class
        UnshortenService.class.metaClass.getLog << {-> log}
    }

    void testExpandUrlsInTextAll() {
        def shortTweet1 = "On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://ff.im/-DlkAq http://www.the-grotto.com/tits.net http://minu.me/4fmw hahaha"
        def expandedTweet1 = "On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://www.twitcaps.com/search?q=basket# http://www.the-grotto.com/tits.net http://www.twitcaps.com/search?q=dsk hahaha"
        
        def shortTweet2 = "This is a tweet, http://bit.ly/mtXafs 1 http://bit.ly/mtXafs 2 http://bit.ly/mtXafs 3 http://bit.ly/mtXafs 4"
        def expandedTweet2 = "This is a tweet, http://www.careerrocketeer.com/2011/05/are-you-promotable.html 1 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 2 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 3 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 4"
        
        def tweetList = [shortTweet1,shortTweet2]
        
        def expandedTweetMap = unshortenService?.expandUrlsInTextAll(tweetList)
        
        assertEquals expandedTweet1,    expandedTweetMap.get(shortTweet1)
        assertEquals expandedTweet2,    expandedTweetMap.get(shortTweet2)
        
        assertNotNull       expandedTweetMap
        assertEquals    2,  expandedTweetMap.size()
    }
    
    void testExpandUrlsInText_DifferentLinks() {
        def shortTweet = "On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://ff.im/-DlkAq http://www.the-grotto.com/tits.net http://minu.me/4fmw hahaha"
        def expandedTweet = "On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://www.twitcaps.com/search?q=basket# http://www.the-grotto.com/tits.net http://www.twitcaps.com/search?q=dsk hahaha"
        def groomedTweet = unshortenService?.expandUrlsInText(shortTweet)
        
        assertNotNull               groomedTweet
        assertEquals expandedTweet, groomedTweet
    }
    
    void testExpandUrlsInText_SameLinks() {
        def shortTweet = "On a more pleasant note, http://bit.ly/mtXafs 1 http://bit.ly/mtXafs 2 http://bit.ly/mtXafs 3 http://bit.ly/mtXafs 4"
        def expandedTweet = "On a more pleasant note, http://www.careerrocketeer.com/2011/05/are-you-promotable.html 1 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 2 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 3 http://www.careerrocketeer.com/2011/05/are-you-promotable.html 4"
        def groomedTweet = unshortenService?.expandUrlsInText(shortTweet)
        
        assertNotNull               groomedTweet
        assertEquals expandedTweet, groomedTweet
    }
    
    void testExpandUrlsInText_SingleTextLink() {
        def shortTweet = "http://bit.ly/jkD0Qr"
        def expandedTweet = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
        def groomedTweet = unshortenService?.expandUrlsInText(shortTweet)
        
        assertNotNull               groomedTweet
        assertEquals expandedTweet, groomedTweet
    }

    void testUnshortenMinu() {
        def shortLink = "http://minu.me/4fmw"
        def fullLink = "http://www.twitcaps.com/search?q=dsk"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl  
    }
    
    void testUnshortenHttps() {
        def shortLink = "https://twitter.com"
        def fullLink = "https://twitter.com"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
    }
    
    void testUnshorten_TCo() {
        def shortLink = "http://t.co/8lrqrZf"
        def fullLink = "http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
    }
    
    void testUnshorten_AweSm() {
        def shortLink = "http://awe.sm/5JXg0"
        def fullLink = "http://www.amazon.co.jp/Mauretania-Cunard-Turbine-Driven-Quadruple-Screw-Atlantic/dp/0850599148"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
    }
    
    void testUnshorten_TinyUrl() {
        def shortLink = "http://tinyurl.com/3vy9xga"
        def fullLink = "http://www.ladygaga.com/news/default.aspx?nid=35447&aid=599"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
    }
    
    void testUnshorten_BitLy() {
        def shortLink = "http://bit.ly/jkD0Qr"
        def fullLink = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl  
    }
    
    void testUnshorten_Status_Unshortened() {
        def shortLink = "http://bit.ly/jkD0Qr"
        def fullLink = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl  
        assertEquals "UNSHORTENED", location?.status
    }
    
    void testUnshorten_Status_NotShortened() {
        def shortLink = "http://www.the-grotto.com"
        def fullLink = "http://www.the-grotto.com"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,          location?.fullUrl 
        assertEquals "NOT_SHORTENED",   location?.status
    }
    
    void testUnshorten_Status_404() {
        def shortLink = "http://bit.ly/jkD0Qr33333"
        def fullLink = "http://bit.ly/jkD0Qr33333"
        
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
        assertEquals "NOT_FOUND",   location?.status
    }
    
    void testUnshorten_Status_Invalid() {
        def shortLink = "http://bit"
        def fullLink = "http://bit"
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
        assertEquals "INVALID",location?.status 
    }
    
    void testUnshorten_Whitespace() {
        def shortLink = " http://bit.ly/jkD0Qr "
        def fullLink = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
        
        def location = unshortenService?.unshorten(shortLink)
            
        assertEquals fullLink,      location?.fullUrl 
        assertEquals "UNSHORTENED", location?.status
        assertFalse                 location?.cached
        
        location = unshortenService?.unshorten(shortLink)
        assertEquals fullLink,          location?.fullUrl 
        assertEquals "UNSHORTENED",     location?.status 
        assertTrue                      location?.cached
        
        location = unshortenService?.unshorten(shortLink.trim())
        
        assertEquals fullLink,      location?.fullUrl 
        assertEquals "UNSHORTENED", location?.status
        assertTrue                  location?.cached
    }
    
    void testUnshorten_NoProtocol_Cached() {
        def shortLink = " bit.ly/jkD0Qr "
        def fullLink = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
        
        def location = unshortenService?.unshorten(shortLink)
            
        assertEquals fullLink,                      location?.fullUrl 
        assertEquals "http://${shortLink.trim()}",  location?.shortUrl
        assertEquals "UNSHORTENED",                 location?.status 
        assertFalse                                 location?.cached
        
        location = unshortenService?.unshorten(shortLink.trim())
        assertEquals fullLink,      location?.fullUrl 
        assert location?.shortUrl?.equals("http://${shortLink.trim()}")
        assertEquals "UNSHORTENED",location?.status 
        assertTrue location?.cached
        
        location = unshortenService?.unshorten("http://${shortLink.trim()}")
        assertEquals fullLink,      location?.fullUrl  
        assert location?.shortUrl?.equals("http://${shortLink.trim()}")
        assertEquals "UNSHORTENED",location?.status 
        assertTrue location?.cached
    }
    
    void testUnshortenAll() {
        def mShort = "http://minu.me/4fmw"
        def mFull = "http://www.twitcaps.com/search?q=dsk"
        def bShort = "http://bit.ly/jkD0Qr"
        def bFull = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
        def tShort = "http://tinyurl.com/3vy9xga"
        def tFull = "http://www.ladygaga.com/news/default.aspx?nid=35447&aid=599"
        def cShort = "http://t.co/8lrqrZf"
        def cFull = "http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/"
        def nShort = "http://t.co/8lrqrZffff"
        def nFull = "http://t.co/8lrqrZffff"
        def unShort = "twitter.com"
        def unFull = "http://twitter.com"
        def iShort = "http://is.gd/fB6zyU"
        def iFull = "http://www.empireonline.com/news/story.asp?NID=31004"
        def invalidShort = "http://the-grotto"
        
        
        def start = System.currentTimeMillis() 
        def linkMap = unshortenService?.unshortenAll([mShort,unShort,tShort,invalidShort,nShort,cShort,bShort,iShort])
        
        assertEquals mFull,             linkMap.get(mShort)?.fullUrl
        assertEquals "UNSHORTENED",     linkMap.get(mShort)?.status
        
        assertEquals tFull,             linkMap.get(tShort)?.fullUrl
        assertEquals "UNSHORTENED",     linkMap.get(tShort)?.status
        
        assertEquals cFull,             linkMap.get(cShort)?.fullUrl
        assertEquals "UNSHORTENED",     linkMap.get(cShort)?.status
        
        assertEquals bFull,             linkMap.get(bShort)?.fullUrl
        assertEquals "UNSHORTENED",     linkMap.get(bShort)?.status
        
        assertEquals nFull,             linkMap.get(nShort)?.fullUrl
        assertEquals "NOT_FOUND",       linkMap.get(nShort)?.status
        
        assertEquals unFull,            linkMap.get(unShort)?.fullUrl
        assertEquals "NOT_SHORTENED",   linkMap.get(unShort)?.status
        
        assertEquals iFull,             linkMap.get(iShort)?.fullUrl
        assertEquals "UNSHORTENED",     linkMap.get(iShort)?.status
        
        assertEquals invalidShort,      linkMap.get(invalidShort)?.fullUrl
        assertEquals "INVALID",         linkMap.get(invalidShort)?.status
        
        println System.currentTimeMillis() - start + " ms to unshortenAll"
    }
    
    void testUnshorten_Cached() {
        def shortLink = "http://minu.me/4fmw"
        def fullLink = "http://www.twitcaps.com/search?q=dsk"
        
        def start = System.currentTimeMillis() 
        def location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand MinuMe ${shortLink}"
        assertEquals fullLink,      location?.fullUrl 
        assertFalse                 location?.cached
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand MinuMe ${shortLink} a second time"
        assertEquals fullLink,      location?.fullUrl 
        assertTrue                  location?.cached
        
        shortLink = "http://t.co/8lrqrZf"
        fullLink = "http://iamthetrend.com/2011/02/10/10-examples-of-awesome-indie-clothing-look-books/"
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand T.Co ${shortLink}"
        assertEquals fullLink,      location?.fullUrl 
        assertFalse                 location?.cached
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand T.Co ${shortLink} a second time"
        assertEquals fullLink,      location?.fullUrl 
        assertTrue                  location?.cached
        
        shortLink = "http://awe.sm/5JXg0"
        fullLink = "http://www.amazon.co.jp/Mauretania-Cunard-Turbine-Driven-Quadruple-Screw-Atlantic/dp/0850599148"
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand Awe.sm ${shortLink}"
        assertEquals fullLink,      location?.fullUrl 
        assertFalse                 location?.cached
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand Awe.sm ${shortLink} a second time"
        assertEquals fullLink,      location?.fullUrl 
        assertTrue                  location?.cached
        
        shortLink = "http://tinyurl.com/3vy9xga"
        fullLink = "http://www.ladygaga.com/news/default.aspx?nid=35447&aid=599"
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand TinyUrl ${shortLink}"
        assertEquals fullLink,      location?.fullUrl 
        assertFalse                 location?.cached
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand TinyUrl ${shortLink} a second time"
        assertEquals fullLink,      location?.fullUrl 
        assertTrue                  location?.cached
        
        shortLink = "http://bit.ly/jkD0Qr"
        fullLink = "http://www.cbsnews.com/8301-503543_162-20063168-503543.html"
                
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand Bit.Ly ${shortLink}"
        assertEquals fullLink,      location?.fullUrl 
        assertFalse                 location?.cached
        
        start = System.currentTimeMillis() 
        location = unshortenService?.unshorten(shortLink)
        println System.currentTimeMillis() - start + " ms to expand Bit.Ly ${shortLink} a second time"
        
        assertEquals fullLink,      location?.fullUrl 
        assertTrue                  location?.cached
    }
    
    void testUnshorten_Digg() {
        def shortLink = "http://digg.com/d1e5BK"
        def fullLink = "http://m.digg.com/news/story/Are_Our_Brains_Becoming_Googlized"
        
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals "REDIRECTED",  location?.status 
        assertEquals fullLink,      location?.fullUrl 
    }
    
    void testUnshorten_302_Redirect() {
        def shortLink = "http://twurl.nl/nbbpdb"
        def fullLink = "http://swiftywriting.blogspot.com/2011/05/i-make-living-as-filmmaker.html"
        
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals "UNSHORTENED", location?.status 
        assertEquals fullLink,      location?.fullUrl 
    }
    
    void testUnshorten_Status_Redirect_Chained() {
        def shortLink = "http://tinyurl.com/3vy9xga"
        def fullLink = "http://www.ladygaga.com/news/default.aspx?nid=35447&aid=599"
        
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl
        assertEquals "UNSHORTENED", location?.status
    }
    
    void testUnshorten_Status_Redirect_Relative_Chained() {
        def shortLink = "http://the-grotto.com/talk/"
        def fullLink = "http://the-grotto.com/talk/forum.jsp?forum=1"
        
        def location = unshortenService?.unshorten(shortLink)
        
        assertEquals fullLink,      location?.fullUrl 
        assertEquals "REDIRECTED",  location?.status 
    }
}
