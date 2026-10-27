package io.omixer.utils.utils;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import io.omixer.utils.Constants;
import io.omixer.utils.exceptions.DuplicateColumnException;
import io.omixer.utils.exceptions.IncorrectNumberOfEntriesException;
import io.omixer.utils.model.KeyValue;
import io.omixer.utils.readers.FileLine;
import io.omixer.utils.readers.MatrixLineProcessor;
import io.omixer.utils.readers.impl.SimpleLineProcessor;

public class FileUtilsTestCase {

	private File matrixFile = new File("src/test/resources/matrix.tsv");

	/** Records the lines it gets and returns the data columns as they are */
	private static class RecordingProcessor implements MatrixLineProcessor<String> {

		final List<FileLine> lines = new ArrayList<>();

		@Override
		public int getHeaderSampleStartIndex() {
			return 1;
		}

		@Override
		public List<String> process(String line, String delimiter) {
			throw new AssertionError("readMatrix must call process(FileLine, String)");
		}

		@Override
		public List<String> process(FileLine line, String delimiter) {
			lines.add(line);
			List<String> tokens = Arrays.asList(line.text().split(delimiter));
			return tokens.subList(1, tokens.size());
		}
	}

	@Test
	public void readMatrixGivesTheProcessorTheFileNameAndLineNumberOfEachDataLine()
			throws IncorrectNumberOfEntriesException, IOException {

		RecordingProcessor processor = new RecordingProcessor();
		FileUtils.readMatrix(matrixFile, Constants.TAB, processor);

		// the header is line 1
		assertEquals(Arrays.asList(2, 3, 4), processor.lines.stream().map(FileLine::number).toList());
		assertTrue(processor.lines.stream().allMatch(line -> line.file().equals("matrix.tsv")));
		assertTrue(processor.lines.get(0).text().startsWith("147.4445\t74"));
	}

	@Test
	public void readMatrixLetsAnExceptionOfTheProcessorReachTheCallerUnchanged()
			throws IncorrectNumberOfEntriesException, IOException {

		IllegalStateException thrown = new IllegalStateException("caller-specific error");
		RecordingProcessor processor = new RecordingProcessor() {
			@Override
			public List<String> process(FileLine line, String delimiter) {
				throw thrown;
			}
		};

		try {
			FileUtils.readMatrix(matrixFile, Constants.TAB, processor);
			fail("the processor's exception must reach the caller");
		} catch (IllegalStateException e) {
			assertSame(thrown, e);
		}
	}

	@Test
	public void readMatrixKeepsTheHeaderOrderOfColumns() throws IncorrectNumberOfEntriesException, IOException {

		Map<String, List<KeyValue<String, Double>>> matrix = FileUtils.readMatrix(matrixFile, Constants.TAB,
				new SimpleLineProcessor<String, Double>(String.class, Double.class));

		assertEquals(Arrays.asList("1a", "3a", "2a", "2b", "3b", "4a", "4b", "1b"), new ArrayList<>(matrix.keySet()));
	}

	@Test
	public void readMatrixRejectsADuplicateColumnName() throws IncorrectNumberOfEntriesException, IOException {

		File duplicates = File.createTempFile("duplicate-columns", ".tsv");
		duplicates.deleteOnExit();
		Files.write(duplicates.toPath(), Arrays.asList("rows\tS1\tS2\tS1", "k1\t1\t2\t3"));

		try {
			FileUtils.readMatrix(duplicates, Constants.TAB,
					new SimpleLineProcessor<String, Double>(String.class, Double.class));
			fail("a duplicate column name must not be merged silently");
		} catch (DuplicateColumnException e) {
			assertTrue(e.getMessage().contains("'S1'"));
			assertTrue(e.getMessage().contains(duplicates.getName()));
		}
	}

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
