package org.omixer.utils.readers.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import org.omixer.utils.model.KeyValue;
import org.omixer.utils.readers.MatrixLineProcessor;

/**
 * 
 * Simply
 * 
 * @author <a href="mailto:youssef.darzi@gmail.com">Youssef Darzi</a>
 * 
 */
public class SimpleLineProcessor<K, V> implements
		MatrixLineProcessor<KeyValue<K, V>> {

	private final int headerSampleStartIndex = 1;
	private final Class<K> keyClazz;
	private final Class<V> valueClazz;

	public SimpleLineProcessor(Class<K> class1, Class<V> class2) {
		this.keyClazz = class1;
		this.valueClazz = class2;
	}

	/**
	 * If a cell is empty then it's replaced by null
	 */
	public List<KeyValue<K, V>> process(String line, String delimiter) {

		List<KeyValue<K, V>> res = new LinkedList<KeyValue<K, V>>();

		// line tockens
		String[] tokens = line.split(delimiter);

		try {
			Constructor<K> c = keyClazz.getDeclaredConstructor(String.class);
			K key = c.newInstance(tokens[0]);

			Constructor<V> valConstr = valueClazz.getDeclaredConstructor(String.class);

			/**
			 * 
			 */
			for (int i = headerSampleStartIndex; i < tokens.length; i++) {
				if (!tokens[i].isEmpty()) {
					res.add(new KeyValue<K, V>(key, valConstr
							.newInstance((tokens[i]))));
				} else {
					res.add(new KeyValue<K, V>(key, null));	
				}
				
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return res;
	}
	

	/**
	 * {@inheritDoc} 
	 */
	public int getHeaderSampleStartIndex() {
		return headerSampleStartIndex;
	}
}
