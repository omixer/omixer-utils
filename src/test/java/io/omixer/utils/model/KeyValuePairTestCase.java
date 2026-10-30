package io.omixer.utils.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class KeyValuePairTestCase {

	@Test
	public void twoPairsAreEqualOnlyWhenTheirKeysAndValuesAre() {
		assertEquals(new KeyValuePair<>("a", 1), new KeyValuePair<>("a", 1));
		assertEquals(new KeyValuePair<>("a", 1).hashCode(), new KeyValuePair<>("a", 1).hashCode());
		assertNotEquals(new KeyValuePair<>("a", 1), new KeyValuePair<>("a", 2));
		assertNotEquals(new KeyValuePair<>("a", 1), new KeyValuePair<>("b", 1));
	}

	@Test
	public void aNullKeyOrValueIsAllowed() {
		assertEquals(new KeyValuePair<>("a", null), new KeyValuePair<>("a", null));
		assertEquals(new KeyValuePair<>(null, 1).hashCode(), new KeyValuePair<>(null, 1).hashCode());
		assertEquals("KeyValuePair[key=a, value=null]", new KeyValuePair<>("a", null).toString());
	}
}
