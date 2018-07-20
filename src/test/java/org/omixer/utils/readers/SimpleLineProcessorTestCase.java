package org.omixer.utils.readers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.omixer.utils.model.KeyValue;
import org.omixer.utils.readers.impl.SimpleLineProcessor;

public class SimpleLineProcessorTestCase {

	@Test
	public void testProcessLine() {
		SimpleLineProcessor<String, Double> lineProcessor = new SimpleLineProcessor<String, Double>(String.class, Double.class);
	
		final String TAB = "\\t";
				
		List<KeyValue<String, Double>> res = lineProcessor.process("a	1	2	4", TAB);
		assertNotNull(res);
		assertEquals(1d, res.get(0).getValue(), 0.000000001);
		assertEquals(2d, res.get(1).getValue(), 0.000000001);
		assertEquals(4d, res.get(2).getValue(), 0.000000001);
		
	}
	
}
