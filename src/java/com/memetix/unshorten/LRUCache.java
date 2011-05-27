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
package com.memetix.unshorten;

import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections15.map.LRUMap;

/**
 * LRUCache
 * 
 * This class encapsulates the LRUMap from Apache Commons Collections - Generics (http://sourceforge.net/projects/collections/files/)
 * within a Thread-Safe Wrapper
 * 
 * @author Jonathan Griggs  <twitcaps.developer @ gmail.com>
 * @version     1.0     2011.05.17                              
 * @since       1.0     2011.05.17    
 */
public class LRUCache {  
     private Map<Object,Object> map;
     
     public LRUCache(int maxSize) {
         //Defaults to Scan Until Removable when LRUMap is Full
         map = Collections.synchronizedMap(new LRUMap<Object,Object>(maxSize,true));
     }
     
     public LRUCache(int maxSize,boolean scanUntilRemovable) {
         map = Collections.synchronizedMap(new LRUMap<Object,Object>(maxSize,scanUntilRemovable));
     }
     
     public int size() {
         return map.size();
     }
     
     public void put(Object key, Object value) {
         map.put(key,value);
     }
     
     public Object get(Object key) {
         return map.get(key);
     }
     
     public void flush() {
         map.clear();
     }
}
