package com.jobpilotai.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRU Memory Cache to store temporary data (e.g. AI responses).
 */
public class MemoryCache<K, V> {

    private final Map<K, V> cacheMap;

    public MemoryCache(final int maxCapacity) {
        this.cacheMap = Collections.synchronizedMap(new LinkedHashMap<K, V>(maxCapacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxCapacity;
            }
        });
    }

    public void put(K key, V value) {
        cacheMap.put(key, value);
    }

    public V get(K key) {
        return cacheMap.get(key);
    }
    
    public void remove(K key) {
        cacheMap.remove(key);
    }

    public void clear() {
        cacheMap.clear();
    }
    
    public int size() {
        return cacheMap.size();
    }
}
