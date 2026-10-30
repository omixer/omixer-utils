package io.omixer.utils.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import io.omixer.utils.Constants;
import io.omixer.utils.exceptions.DuplicateColumnException;
import io.omixer.utils.exceptions.IncorrectNumberOfEntriesException;
import io.omixer.utils.model.KeyValuePair;
import io.omixer.utils.readers.FileLine;
import io.omixer.utils.readers.MatrixLineProcessor;
import io.omixer.utils.readers.MatrixRowHandler;
import io.omixer.utils.readers.impl.KeyValueLineProcessor;

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

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> FileUtils.readMatrix(matrixFile, Constants.TAB, processor),
				"the processor's exception must reach the caller");
		assertSame(thrown, e);
	}

	@Test
	public void readMatrixKeepsTheHeaderOrderOfColumns() throws IncorrectNumberOfEntriesException, IOException {

		Map<String, List<KeyValuePair<String, Double>>> matrix = FileUtils.readMatrix(matrixFile, Constants.TAB,
				new KeyValueLineProcessor<String, Double>(Function.identity(), Double::valueOf));

		assertEquals(Arrays.asList("1a", "3a", "2a", "2b", "3b", "4a", "4b", "1b"), new ArrayList<>(matrix.keySet()));
	}

	@Test
	public void readMatrixRejectsADuplicateColumnName() throws IncorrectNumberOfEntriesException, IOException {

		File duplicates = File.createTempFile("duplicate-columns", ".tsv");
		duplicates.deleteOnExit();
		Files.write(duplicates.toPath(), Arrays.asList("rows\tS1\tS2\tS1", "k1\t1\t2\t3"));

		DuplicateColumnException e = assertThrows(DuplicateColumnException.class,
				() -> FileUtils.readMatrix(duplicates, Constants.TAB,
				new KeyValueLineProcessor<String, Double>(Function.identity(), Double::valueOf)),
				"a duplicate column name must not be merged silently");
		assertTrue(e.getMessage().contains("'S1'"));
		assertTrue(e.getMessage().contains(duplicates.getName()));
	}

	@Test
	public void testReadMatix() throws IncorrectNumberOfEntriesException, IOException {

		int expectedNumberOfColumns = 8;
		int expectedNumberOfEntries = 3;
		
		Map<String, List<KeyValuePair<String, Double>>> matrix = FileUtils.readMatrix(matrixFile, Constants.TAB, new KeyValueLineProcessor<String, Double>(Function.identity(), Double::valueOf));
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

	/** Records the columns and rows it gets; every row has the entries of its data columns */
	private static class RecordingHandler implements MatrixRowHandler {

		final List<String> events = new ArrayList<>();

		@Override
		public void columns(List<String> columns) {
			events.add("columns " + columns);
		}

		@Override
		public int row(FileLine line, String[] fields) {
			events.add(line.file() + ":" + line.number() + " " + Arrays.asList(fields));
			return fields.length - 1;
		}
	}

	private static File matrix(String... lines) throws IOException {
		File file = File.createTempFile("matrix", ".tsv");
		file.deleteOnExit();
		Files.write(file.toPath(), Arrays.asList(lines));
		return file;
	}

	@Test
	public void readMatrixRowsGivesTheColumnsInHeaderOrderThenEveryRowWithItsFileAndLine()
			throws IncorrectNumberOfEntriesException, IOException {

		File file = matrix("rows\tS2\tS1", "k1\t1\t2", "k2\t3\t4");
		RecordingHandler handler = new RecordingHandler();

		FileUtils.readMatrixRows(file, Constants.TAB, 1, handler);

		assertEquals(Arrays.asList("columns [S2, S1]", file.getName() + ":2 [k1, 1, 2]", file.getName() + ":3 [k2, 3, 4]"),
				handler.events);
	}

	@Test
	public void readMatrixRowsRejectsADuplicateColumnName() throws IncorrectNumberOfEntriesException, IOException {

		File file = matrix("rows\tS1\tS2\tS1", "k1\t1\t2\t3");
		DuplicateColumnException e = assertThrows(DuplicateColumnException.class,
				() -> FileUtils.readMatrixRows(file, Constants.TAB, 1, new RecordingHandler()),
				"a duplicate column name must be rejected");
		assertEquals(file.getName() + ": column 'S1' appears more than once in the header", e.getMessage());
	}

	@Test
	public void readMatrixRowsRejectsARowWithAnotherNumberOfEntriesThanColumns() throws IOException {

		File file = matrix("rows\tS1\tS2", "k1\t1\t2", "k2\t3");
		IncorrectNumberOfEntriesException e = assertThrows(IncorrectNumberOfEntriesException.class,
				() -> FileUtils.readMatrixRows(file, Constants.TAB, 1, new RecordingHandler()),
				"a row with too few entries must be rejected");
		assertEquals("Line 3 have 1 entries instead of 3", e.getMessage());
	}

	@Test
	public void readMatrixRowsLetsAnExceptionOfTheHandlerReachTheCallerUnchanged()
			throws IncorrectNumberOfEntriesException, IOException {

		IllegalStateException thrown = new IllegalStateException("from the handler");
		MatrixRowHandler handler = new MatrixRowHandler() {
			@Override
			public void columns(List<String> columns) {
			}

			@Override
			public int row(FileLine line, String[] fields) {
				throw thrown;
			}
		};
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> FileUtils.readMatrixRows(matrix("rows\tS1", "k1\t1"), Constants.TAB, 1, handler),
				"the handler's exception must reach the caller");
		assertSame(thrown, e);
	}

	@Test
	public void readMatrixRejectsARowWithAnotherNumberOfEntriesThanColumns() throws IOException {

		File file = matrix("rows\tS1\tS2", "k1\t1\t2", "k2\t3");
		IncorrectNumberOfEntriesException e = assertThrows(IncorrectNumberOfEntriesException.class,
				() -> FileUtils.readMatrix(file, Constants.TAB, new KeyValueLineProcessor<String, Double>(Function.identity(), Double::valueOf)),
				"a row with too few entries must be rejected");
		assertEquals("Line 3 have 1 entries instead of 3", e.getMessage());
	}

	@Test
	public void readMatrixRowsRejectsAnEmptyFile() throws IOException {

		File file = matrix();
		IOException e = assertThrows(IOException.class,
				() -> FileUtils.readMatrixRows(file, Constants.TAB, 1, new RecordingHandler()));
		assertEquals(file.getName() + " is empty: a matrix starts with a header line", e.getMessage());
	}
}
