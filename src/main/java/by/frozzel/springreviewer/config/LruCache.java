package by.frozzel.springreviewer.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LruCache<K, V> {
    private final Map<K, V> cache;
    private final int maxSize;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public LruCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                boolean shouldRemove = size() > LruCache.this.maxSize;
                if (shouldRemove) {
                    log.info("LRU Cache limit ({}) reached. Removing eldest (least recently used) entry with key: {}",
                            LruCache.this.maxSize, eldest.getKey());
                }
                return shouldRemove;
            }
        };
        log.info("LruCache instance created with max size: {}", this.maxSize);
    }

    public V get(K key) {
        lock.writeLock().lock();
        try {
            V value = cache.get(key);
            if (value != null) {
                log.info("LRU Cache HIT for key: {}", key);
            } else {
                log.info("LRU Cache MISS for key: {}", key);
            }
            return value;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            log.info("Putting data into LRU cache with key: {}", key);
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(K key) {
        lock.writeLock().lock();
        try {
            log.info("Removing data from LRU cache with key: {}", key);
            cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            log.warn("Clearing the entire LRU cache!");
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Collection<V> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(cache.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}