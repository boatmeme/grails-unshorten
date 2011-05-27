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

class UnshortenTagLibTests extends GroovyPagesTestCase  {
    
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testExpandUrls() {
        def template = "<unshorten:expandUrls>On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://ff.im/-DlkAq http://www.the-grotto.com/tits.net http://minu.me/4fmw hahaha</unshorten:expandUrls>"
        assertOutputEquals( 'On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://www.twitcaps.com/search?q=basket# http://www.the-grotto.com/tits.net http://www.twitcaps.com/search?q=dsk hahaha', 
            template )
    }
    
    void testExpandAndLinkUrls() {
        def template = "<unshorten:expandAndLinkUrls>On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://ff.im/-DlkAq http://www.the-grotto.com/tits.net http://minu.me/4fmw hahaha</unshorten:expandAndLinkUrls>"
        assertOutputEquals( 'On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. <a href="http://www.twitcaps.com/search?q=basket#">http://www.twitcaps.com/search?q=basket#</a> <a href="http://www.the-grotto.com/tits.net">http://www.the-grotto.com/tits.net</a> <a href="http://www.twitcaps.com/search?q=dsk">http://www.twitcaps.com/search?q=dsk</a> hahaha', 
            template )
    }
    
    void testExpandAndLinkUrlsSame() {
        def template = "<unshorten:expandAndLinkUrls>On a more pleasant note, http://bit.ly/mtXafs 1 http://bit.ly/mtXafs 2 http://bit.ly/mtXafs 3 http://bit.ly/mtXafs 4</unshorten:expandAndLinkUrls>"
        assertOutputEquals( 'On a more pleasant note, <a href="http://www.careerrocketeer.com/2011/05/are-you-promotable.html">http://www.careerrocketeer.com/2011/05/are-you-promotable.html</a> 1 <a href="http://www.careerrocketeer.com/2011/05/are-you-promotable.html">http://www.careerrocketeer.com/2011/05/are-you-promotable.html</a> 2 <a href="http://www.careerrocketeer.com/2011/05/are-you-promotable.html">http://www.careerrocketeer.com/2011/05/are-you-promotable.html</a> 3 <a href="http://www.careerrocketeer.com/2011/05/are-you-promotable.html">http://www.careerrocketeer.com/2011/05/are-you-promotable.html</a> 4', 
            template )
    }
    
    void testExpandAndLinkUrlsClass() {
        def myClass = "myLinkClass"
        def template = "<unshorten:expandAndLinkUrls linkClass='${myClass}'>On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. http://ff.im/-DlkAq http://www.the-grotto.com/tits.net http://minu.me/4fmw hahaha</unshorten:expandAndLinkUrls>"
        assertOutputEquals( 'On a more pleasant note, pets and kids in baskets are a very popular photo subject. I could make a collection. <a class="myLinkClass" href="http://www.twitcaps.com/search?q=basket#">http://www.twitcaps.com/search?q=basket#</a> <a class="myLinkClass" href="http://www.the-grotto.com/tits.net">http://www.the-grotto.com/tits.net</a> <a class="myLinkClass" href="http://www.twitcaps.com/search?q=dsk">http://www.twitcaps.com/search?q=dsk</a> hahaha', 
            template )
    }
    
    void testUnshortenUrl() {
        def myUrl = 'http://ff.im/-DlkAq'
        def template = "<unshorten:unshortenUrl url='${myUrl}'/>"
        assertOutputEquals( 'http://www.twitcaps.com/search?q=basket#', 
            template )
    }
    
    void testUnshortenAndLinkUrl() {
        def myUrl = 'http://ff.im/-DlkAq'
        def template = "<unshorten:unshortenAndLinkUrl url='${myUrl}'/>"
        assertOutputEquals( '<a href="http://www.twitcaps.com/search?q=basket#">http://www.twitcaps.com/search?q=basket#</a>', 
            template )
    }
    
    void testUnshortenAndLinkUrlClass() {
        def myUrl = 'http://ff.im/-DlkAq'
        def myClass  = 'myLinkClass'
        def template = "<unshorten:unshortenAndLinkUrl linkClass='${myClass}' url='${myUrl}'/>"
        assertOutputEquals( '<a class="myLinkClass" href="http://www.twitcaps.com/search?q=basket#">http://www.twitcaps.com/search?q=basket#</a>', 
            template )
    }
}
