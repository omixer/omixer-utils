package io.omixer.utils.readers.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.omixer.utils.model.KeyValue;
import io.omixer.utils.readers.MatrixLineProcessor;

/**
 * Maps a matrix row (key, then values) to one key-value pair per value, converting both with the given classes'
 * {@link String} constructors
 *
 * @author <a href="mailto:youssef.darzi@omixer.io">Youssef Darzi</a>
 * @deprecated use {@link KeyValueLineProcessor}, which takes conversion functions (e.g. {@code Double::valueOf})
 *             instead of classes, and gives {@link io.omixer.utils.model.KeyValuePair}s
 */
@Deprecated(since = "1.2.0", forRemoval = true)
@SuppressWarnings("removal")
public class SimpleLineProcessor<K, V> implements MatrixLineProcessor<KeyValue<K, V>> {

	private final KeyValueLineProcessor<K, V> processor;

	public SimpleLineProcessor(Class<K> class1, Class<V> class2) {
		this.processor = new KeyValueLineProcessor<>(fromString(class1), fromString(class2));
	}

	/**
	 * As {@link KeyValueLineProcessor#process(String, String)}
	 *
	 * @throws RuntimeException         what the key or value class throws for a field it cannot read, e.g.
	 *                                  {@link NumberFormatException} for a {@link Double}
	 * @throws IllegalArgumentException if the key or value class has no public constructor taking a {@link String}
	 */
	@Override
	public List<KeyValue<K, V>> process(String line, String delimiter) {
		return processor.process(line, delimiter).stream()
				.map(pair -> new KeyValue<>(pair.key(), pair.value()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@Override
	public int getHeaderSampleStartIndex() {
		return processor.getHeaderSampleStartIndex();
	}

	/** A conversion through the class's String constructor */
	private static <T> Function<String, T> fromString(Class<T> clazz) {
		return text -> {
			try {
				return clazz.getDeclaredConstructor(String.class).newInstance(text);
			} catch (InvocationTargetException e) {
				// the constructor rejected the text: give its exception, e.g. NumberFormatException
				if (e.getCause() instanceof RuntimeException cause) {
					throw cause;
				}
				throw new IllegalArgumentException("Cannot create a " + clazz.getSimpleName() + " from '" + text + "'",
						e.getCause());
			} catch (ReflectiveOperationException e) {
				throw new IllegalArgumentException(clazz.getName() + " has no public constructor taking a String", e);
			}
		};
	}
}
