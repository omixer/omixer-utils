package io.omixer.utils.readers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.omixer.utils.model.KeyValuePair;
import io.omixer.utils.readers.impl.KeyValueLineProcessor;
import io.omixer.utils.readers.impl.SimpleLineProcessor;

public class KeyValueLineProcessorTestCase {

	private final KeyValueLineProcessor<String, Double> processor = new KeyValueLineProcessor<>(Function.identity(),
			Double::valueOf);

	@Test
	public void aRowGivesOnePairPerValue() {
		assertEquals(List.of(new KeyValuePair<>("a", 1d), new KeyValuePair<>("a", 2d), new KeyValuePair<>("a", 4d)),
				processor.process("a\t1\t2\t4", "\t"));
	}

	@Test
	public void anEmptyValueBecomesNull() {
		assertEquals(Arrays.asList(new KeyValuePair<>("a", 1d), new KeyValuePair<>("a", null), new KeyValuePair<>("a", 4d)),
				processor.process("a\t1\t\t4", "\t"));
	}

	@Test
	public void anExceptionOfAParserReachesTheCallerUnchanged() {
		assertThrows(NumberFormatException.class, () -> processor.process("a\t1\tabc", "\t"));
	}

	@Test
	public void anyConversionCanBeUsedNotOnlyAStringConstructor() {
		KeyValueLineProcessor<Integer, Integer> parsed = new KeyValueLineProcessor<>(Integer::parseInt,
				text -> text.length());
		assertEquals(List.of(new KeyValuePair<>(7, 3), new KeyValuePair<>(7, 1)), parsed.process("7\tabc\tx", "\t"));
	}

	@Test
	@SuppressWarnings("removal")
	public void theDeprecatedSimpleLineProcessorGivesTheSameKeysAndValues() {
		var deprecated = new SimpleLineProcessor<String, Double>(String.class, Double.class).process("a\t1\t\t4", "\t");
		assertEquals(Arrays.asList("a", "a", "a"), deprecated.stream().map(pair -> pair.getKey()).toList());
		assertEquals(Arrays.asList(1d, null, 4d), deprecated.stream().map(pair -> pair.getValue()).toList());
	}

	@Test
	@SuppressWarnings("removal")
	public void theDeprecatedSimpleLineProcessorRejectsAClassWithoutAStringConstructor() {
		var deprecated = new SimpleLineProcessor<String, Object>(String.class, Object.class);
		assertThrows(IllegalArgumentException.class, () -> deprecated.process("a\t1", "\t"));
		assertThrows(NumberFormatException.class,
				() -> new SimpleLineProcessor<String, Double>(String.class, Double.class).process("a\tabc", "\t"));
	}
}
