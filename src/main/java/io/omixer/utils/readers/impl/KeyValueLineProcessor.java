package io.omixer.utils.readers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.omixer.utils.model.KeyValuePair;
import io.omixer.utils.readers.MatrixLineProcessor;

/**
 * Maps a matrix row (key, then values) to one key-value pair per value, converting the key and the values from their
 * text with the given functions, e.g. {@code new KeyValueLineProcessor<>(Function.identity(), Double::valueOf)}.
 *
 * @author <a href="mailto:youssef.darzi@omixer.io">Youssef Darzi</a>
 */
public class KeyValueLineProcessor<K, V> implements MatrixLineProcessor<KeyValuePair<K, V>> {

	private static final int HEADER_SAMPLE_START_INDEX = 1;

	private final Function<String, K> keyParser;
	private final Function<String, V> valueParser;

	/**
	 * @param keyParser   converts the first field of a row to its key
	 * @param valueParser converts every other non-empty field to a value
	 */
	public KeyValueLineProcessor(Function<String, K> keyParser, Function<String, V> valueParser) {
		this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
		this.valueParser = Objects.requireNonNull(valueParser, "valueParser");
	}

	/**
	 * An empty field gives a null value. Exceptions of the parsers (e.g. {@link NumberFormatException}) reach the
	 * caller unchanged.
	 */
	@Override
	public List<KeyValuePair<K, V>> process(String line, String delimiter) {
		String[] tokens = line.split(delimiter);
		K key = keyParser.apply(tokens[0]);
		List<KeyValuePair<K, V>> pairs = new ArrayList<>();
		for (int i = HEADER_SAMPLE_START_INDEX; i < tokens.length; i++) {
			pairs.add(new KeyValuePair<>(key, tokens[i].isEmpty() ? null : valueParser.apply(tokens[i])));
		}
		return pairs;
	}

	@Override
	public int getHeaderSampleStartIndex() {
		return HEADER_SAMPLE_START_INDEX;
	}
}
