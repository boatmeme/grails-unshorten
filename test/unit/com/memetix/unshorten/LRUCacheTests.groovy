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

class LRUCacheTests extends GrailsUnitTestCase {
    
    def shortUrl
    def fullUrl
    
    protected void setUp() {
        shortUrl = "http://test.ly"
        fullUrl = "http://fullexpandedurl"
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstructor() {
        def cache = new LRUCache(50)
        assertNotNull cache
    }
    
    void testSize() {
        def cache = new LRUCache(50)
        cache.put(shortUrl,fullUrl)
        assertEquals 1, cache.size()
        cache.put(shortUrl,fullUrl)
        assertEquals 1, cache.size()
        cache.put(shortUrl+"1",fullUrl+"1")
        assertEquals 2, cache.size()
        
    }
    
    void testPut() {
        def cache = new LRUCache(50)
        cache.put(shortUrl,fullUrl)
        def value = cache.get(shortUrl)
        assertEquals fullUrl,value
    }
    
    void testGet() {
        def cache = new LRUCache(50)
        cache.put(shortUrl,fullUrl)
        def value = cache.get(shortUrl)
        assertEquals fullUrl,value
    }
    
    void testGetNull() {
        def cache = new LRUCache(50);
        cache.put(shortUrl,fullUrl);
        def value = cache.get(shortUrl+"1");
        assertNull value
    }
 
    void testMaxSize() {
        def cache = new LRUCache(2)
        cache.put(shortUrl,fullUrl)
        assertEquals 1, cache.size()
        cache.put(shortUrl,fullUrl)
        assertEquals 1, cache.size()
        cache.put(shortUrl+"1",fullUrl+"1")
        assertEquals 2, cache.size()
        cache.put(shortUrl+"2",fullUrl+"2")
        assertEquals 2, cache.size()
        cache.put(shortUrl+"3",fullUrl+"3")
        assertEquals 2, cache.size()
    } 
    
    void testLRU() {
        def cache = new LRUCache(3)
        
        cache.put(shortUrl,fullUrl)
        cache.put(shortUrl+"1",fullUrl+"1")
        cache.put(shortUrl+"2",fullUrl+"2")
        cache.put(shortUrl+"3",fullUrl+"3")
        
        assertEquals  fullUrl+"3",    cache.get(shortUrl+"3")
        assertEquals  fullUrl+"2",    cache.get(shortUrl+"2")
        assertEquals  fullUrl+"1",    cache.get(shortUrl+"1")
        
        // LRU was pushed off the Map
        assertNull cache.get(shortUrl)
        
        // add a couple more and RU shortUrl1
        cache.put(shortUrl+"4",fullUrl+"4")
        cache.put(shortUrl+"5",fullUrl+"5")
        cache.get(shortUrl+"1")
        assertEquals  fullUrl+"4",    cache.get(shortUrl+"4")
        assertEquals  fullUrl+"5",    cache.get(shortUrl+"5") 
        assertEquals  fullUrl+"1",    cache.get(shortUrl+"1")
        assertNull                  cache.get(shortUrl)
        assertNull                  cache.get(shortUrl+"2")
        assertNull                  cache.get(shortUrl+"3")        
    } 
    
    void testFlush() {
        def cache = new LRUCache(2)
        cache.put(shortUrl,fullUrl)
        cache.put(shortUrl+"1",fullUrl+"1")
        cache.put(shortUrl+"2",fullUrl+"2")
        cache.put(shortUrl+"3",fullUrl+"3")
        assertEquals  2,  cache.size()
        cache.flush()
        assertEquals  0,  cache.size()
        assertNull      cache.get(shortUrl)
    }
    
    void testTimes() {
        def max = 10000
        def cache = new LRUCache(max);
        def start = System.currentTimeMillis();
        for(int i = 0;i<max*2;i++) {
            cache.put(i+"",i + "-" + i)
        }
        println System.currentTimeMillis() - start + " ms to add ${max*2} entries"
        start = System.currentTimeMillis() 
        assertNotNull   cache.get(((max*2)-1)+"")
        println System.currentTimeMillis() - start + " ms to find newest entry"
        start = System.currentTimeMillis()
        assertNotNull   cache.get(max+"")
        println System.currentTimeMillis() - start + " ms to find oldest entry"
        start = System.currentTimeMillis()
        assertNull      cache.get(max-1+"")
        println System.currentTimeMillis() - start + " ms to search all entries"
        
    }
}
