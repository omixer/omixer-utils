package org.omixer.utils.utils;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.omixer.utils.Constants;
import org.omixer.utils.exceptions.IncorrectNumberOfEntriesException;
import org.omixer.utils.model.KeyValue;
import org.omixer.utils.readers.impl.SimpleLineProcessor;
import org.omixer.utils.utils.FileUtils;

public class FileUtilsTestCase {

	private File matrixFile = new File("src/test/resources/matrix.tsv");

	@Test
	public void testReadMatix() throws IncorrectNumberOfEntriesException, IOException {

		int expectedNumberOfColumns = 8;
		int expectedNumberOfEntries = 3;
		
		Map<String, List<KeyValue<String, Double>>> matrix = FileUtils.readMatrix(matrixFile, Constants.TAB, new SimpleLineProcessor<String, Double>(String.class, Double.class));
		// right number of columns?
		assertEquals(expectedNumberOfColumns, matrix.keySet().size());
		
		// correct colnames ?
		String[] colnames = new String[] {"1a", "3a", "2a", "2b", "3b", "4a", "4b", "1b"};
		for (int i = 0; i < colnames.length; i++) {
			assertTrue(matrix.keySet().contains(colnames[i]));
			// read all lines?
			assertEquals(expectedNumberOfEntries, matrix.get(colnames[i]).size());
		}
	}
	
}
