package com.crafter.structure.utilities

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class MemoryCache<K, V>(private val maxSize: Int) {
    private val cached = ConcurrentHashMap<K, V>()
    private val size = AtomicInteger(0)

    fun get(key: K): V? = cached[key]

    fun put(key: K, value: V): V {
        if (key in cached.keys) return value

        if (size.get() >= maxSize) clear()

        cached[key]?.let { size.decrementAndGet() }
        cached[key] = value
        size.incrementAndGet()

        return value
    }

    private fun clear() {
        val keyToEvict = cached.keys.firstOrNull() ?: return
        cached.remove(keyToEvict)
        size.decrementAndGet()
    }

    fun cache(key: K, supplier: () -> V): V =
        this.put(key, supplier())
}