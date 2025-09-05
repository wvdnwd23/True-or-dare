package com.wes.truthdare.core.util

/**
 * Simple LRU (Least Recently Used) cache implementation
 * @param maxSize The maximum number of elements to keep in the cache
 */
class LruCache<K, V>(private val maxSize: Int) {
    private val map = LinkedHashMap<K, V>(maxSize, 0.75f, true)
    
    /**
     * Get a value from the cache
     * @param key The key to look up
     * @return The value associated with the key, or null if not found
     */
    fun get(key: K): V? {
        return map[key]
    }
    
    /**
     * Put a value in the cache
     * @param key The key to store
     * @param value The value to store
     */
    fun put(key: K, value: V) {
        map[key] = value
        
        // Remove oldest entries if we exceed the max size
        if (map.size > maxSize) {
            val eldest = map.entries.iterator().next()
            map.remove(eldest.key)
        }
    }
    
    /**
     * Remove a value from the cache
     * @param key The key to remove
     * @return The removed value, or null if not found
     */
    fun remove(key: K): V? {
        return map.remove(key)
    }
    
    /**
     * Clear the cache
     */
    fun clear() {
        map.clear()
    }
    
    /**
     * Get the current size of the cache
     * @return The number of elements in the cache
     */
    fun size(): Int {
        return map.size
    }
    
    /**
     * Check if the cache contains a key
     * @param key The key to check
     * @return True if the key is in the cache, false otherwise
     */
    fun contains(key: K): Boolean {
        return map.containsKey(key)
    }
    
    /**
     * Get all keys in the cache
     * @return A set of all keys in the cache
     */
    fun keys(): Set<K> {
        return map.keys
    }
    
    /**
     * Get all values in the cache
     * @return A collection of all values in the cache
     */
    fun values(): Collection<V> {
        return map.values
    }
}