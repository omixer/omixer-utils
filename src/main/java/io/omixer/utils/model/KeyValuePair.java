package io.omixer.utils.model;

/**
 * An association between a key and a value; either may be null. Immutable.
 *
 * @param key
 * @param value
 * @author <a href="mailto:youssef.darzi@omixer.io">Youssef Darzi</a>
 */
public record KeyValuePair<K, V>(K key, V value) {
}
