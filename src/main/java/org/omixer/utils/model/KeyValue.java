package org.omixer.utils.model;

/**
 * 
 * A class to hold associations between two values
 * 
 * @author <a href="mailto:youssef.darzi@gmail.com">Youssef Darzi</a>
 * 
 * @param <K>
 * @param <V>
 */
public final class KeyValue<K, V> {

	private static final String KEY_VAL_SEPARATOR = "<=>";
	private K key;
	private V value;

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public V getValue() {
		return value;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(K key) {
		this.key = key;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(V value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// check the hashCodeBuilder for more details
		return (((31 * 11) + key.hashCode()) * 11) + value.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}

		@SuppressWarnings("unchecked")
		KeyValue<K, V> keyValuePair = (KeyValue<K, V>) o;

		if (keyValuePair.getKey().equals(this.getKey())) {
			return true;
		}

		if (keyValuePair.getValue().equals(this.getValue())) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getKey().toString() + KEY_VAL_SEPARATOR + getValue().toString();
	}

	/**
	 * Simple toString with a custom delimiter
	 * 
	 * @param delimiter
	 * @return
	 */
	public String toString(String delimiter) {
		return getKey().toString() + delimiter + getValue().toString();
	}
}
