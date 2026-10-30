package io.omixer.utils.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.omixer.utils.Constants;

/**
 * The line-based readers and the writers of {@link FileUtils}
 */
public class FileUtilsReadWriteTestCase {

	@TempDir
	Path folder;

	/** A new, empty file */
	private File newFile() throws IOException {
		return Files.createTempFile(folder, "test", ".txt").toFile();
	}

	private File file(String... lines) throws IOException {
		File file = newFile();
		Files.write(file.toPath(), Arrays.asList(lines));
		return file;
	}

	private static String content(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()));
	}

	/** A reader that never says it is ready, like a pipe whose data has not arrived yet */
	private static Reader neverReady(String text) {
		return new StringReader(text) {
			@Override
			public boolean ready() {
				return false;
			}
		};
	}

	@Test
	public void skipConsumesTheGivenNumberOfLinesAndStopsAtTheEnd() throws IOException {

		BufferedReader reader = new BufferedReader(new StringReader("a\nb\nc\n"));
		FileUtils.skip(reader, 2);
		assertEquals("c", reader.readLine());

		BufferedReader shortReader = new BufferedReader(new StringReader("a\n"));
		FileUtils.skip(shortReader, 5);
		assertNull(shortReader.readLine());
	}

	@Test
	public void skipDoesNotStopWhenTheReaderIsNotReadyYet() throws IOException {

		BufferedReader reader = new BufferedReader(neverReady("a\nb\nc\n"));
		FileUtils.skip(reader, 2);
		assertEquals("c", reader.readLine());
	}

	@Test
	public void readKeyValueKeepsTheLastValueOfAKeyAndTrailingEmptyFields() throws IOException {

		File file = file("header", "k1\t1", "k2\t", "k1\t3\textra");
		Map<String, String> map = FileUtils.readKeyValue(file.getPath(), Constants.TAB, 1);
		assertEquals(Map.of("k1", "3", "k2", ""), map);
	}

	@Test
	public void readKeyDoubleValueReadsTheSecondColumnAsANumber() throws IOException {

		Map<String, Double> map = FileUtils.readKeyDoubleValue(file("k1\t1.5", "k2\t2"), Constants.TAB, 0);
		assertEquals(Map.of("k1", 1.5d, "k2", 2d), map);
	}

	@Test
	public void readKeyValuesCollectsEveryValueOfAKey() throws IOException {

		Map<String, List<String>> map = FileUtils.readKeyValues(file("header", "k1\ta\tb", "k2\tc", "k1\td"),
				Constants.TAB, 1);
		assertEquals(Map.of("k1", List.of("a", "b", "d"), "k2", List.of("c")), map);
	}

	@Test
	@SuppressWarnings({ "unchecked", "removal" })
	public void readKeyValuesEnginGivesTheMapAndTheFieldsOfEveryLine() throws IOException {

		Map<String, String> map = FileUtils.readKeyValuesEngin(file("k1\ta", "k2\tb"), Constants.TAB, 0, args -> {
			Map<String, String> keyValues = (Map<String, String>) args[0];
			String[] fields = (String[]) args[1];
			keyValues.put(fields[0], fields[1]);
			return null;
		});
		assertEquals(Map.of("k1", "a", "k2", "b"), map);
	}

	@Test
	public void readCSVSplitsEveryLineAfterTheSkippedOnes() throws IOException {

		List<String[]> rows = FileUtils.readCSV(file("header", "a,b", "c"), Constants.COMMA, 1);
		assertEquals(2, rows.size());
		assertArrayEquals(new String[] { "a", "b" }, rows.get(0));
		assertArrayEquals(new String[] { "c" }, rows.get(1));
	}

	@Test
	public void readCSVMapsEveryLine() throws IOException {

		assertEquals(List.of(1, 1), FileUtils.readCSV(file("header", "a", "b"), 1, String::length));
	}

	@Test
	@SuppressWarnings("removal")
	public void readCSVWithADelimiterAndARowMapperUsesTheMapperOrSplitsWithoutOne() throws IOException {

		File file = file("a,b", "c");
		assertEquals(List.of("A,B", "C"), FileUtils.readCSV(file, Constants.COMMA, 0, String::toUpperCase));
		List<String[]> rows = FileUtils.readCSV(file, Constants.COMMA, 0, null);
		assertArrayEquals(new String[] { "a", "b" }, rows.get(0));
	}

	@Test
	@SuppressWarnings("removal")
	public void readContentAsStringJoinsTheLinesWithoutLineSeparators() throws IOException {

		assertEquals("ab", FileUtils.readContentAsString(file("a", "b")));
	}

	@Test
	@SuppressWarnings("removal")
	public void getFileHeaderGivesTheFirstLineOrNullForAnEmptyFile() throws IOException {

		assertEquals("h", FileUtils.getFileHeader(file("h", "row").getPath()));
		assertNull(FileUtils.getFileHeader(newFile().getPath()));
	}

	@Test
	public void writeObjectsWritesTheHeaderThenOneFormattedLinePerObject() throws IOException {

		File withHeader = newFile();
		FileUtils.writeObjects(withHeader, "h", List.of(1, 2), i -> "n" + i);
		assertEquals("h" + Constants.NEW_LINE + "n1" + Constants.NEW_LINE + "n2" + Constants.NEW_LINE, content(withHeader));

		File plain = newFile();
		FileUtils.writeObjects(plain.getPath(), null, List.of(1, 2), null);
		assertEquals("1" + Constants.NEW_LINE + "2" + Constants.NEW_LINE, content(plain));
	}

	@Test
	public void writeKeyValueWritesOneKeyAndValuePerLine() throws IOException {

		Map<String, Integer> map = new LinkedHashMap<>();
		map.put("k1", 1);
		map.put("k2", 2);
		File file = newFile();
		FileUtils.writeKeyValue(map, "key,value", file, Constants.COMMA);
		assertEquals(List.of("key,value", "k1,1", "k2,2"), Files.readAllLines(file.toPath()));
	}

	@Test
	public void writeKeyValuesWritesOneLinePerKeyAndValue() throws IOException {

		Map<String, Iterable<Integer>> map = new LinkedHashMap<>();
		map.put("k1", List.of(1, 2));
		map.put("k2", List.of(3));
		File file = newFile();
		FileUtils.writeKeyValues(map, file.getPath(), Constants.TAB);
		assertEquals(List.of("k1\t1", "k1\t2", "k2\t3"), Files.readAllLines(file.toPath()));
	}

	@Test
	@SuppressWarnings("removal")
	public void copyCopiesTheContent() throws IOException {

		File source = file("a", "b");
		File target = folder.resolve("copy").toFile();
		FileUtils.copy(source, target);
		assertEquals(content(source), content(target));
	}

	@Test
	@SuppressWarnings("removal")
	public void deleteQuietlyDeletesAFileAndAcceptsNull() throws IOException {

		File file = file("a");
		FileUtils.deleteQuietly(file);
		FileUtils.deleteQuietly(null);
		assertFalse(file.exists());
	}
}
