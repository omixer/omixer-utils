package io.omixer.utils.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import io.omixer.utils.Constants;
import io.omixer.utils.exceptions.DuplicateColumnException;
import io.omixer.utils.exceptions.IncorrectNumberOfEntriesException;
import io.omixer.utils.readers.FileLine;
import io.omixer.utils.readers.MatrixLineProcessor;
import io.omixer.utils.readers.MatrixRowHandler;

/**
 * Reading and writing text files: delimited key-value files, delimited rows, matrices with a header line, and objects
 * written one per line. Readers read every line to the end of the file. Exceptions of the functions and handlers given
 * to these methods are not caught: they reach the caller unchanged.
 *
 * @author <a href="mailto:youssef.darzi@omixer.io">Youssef Darzi</a>
 */
public final class FileUtils {

	private FileUtils() {
	}

	/**
	 * Copies a file
	 *
	 * @deprecated use {@link java.nio.file.Files#copy(java.nio.file.Path, java.nio.file.Path, java.nio.file.CopyOption...)}
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static synchronized void copy(File sourceFile, File destination) throws IOException {
		try (FileInputStream fis = new FileInputStream(sourceFile);
				FileChannel in = fis.getChannel();
				FileOutputStream fos = new FileOutputStream(destination);
				FileChannel out = fos.getChannel()) {
			in.transferTo(0, sourceFile.length(), out);
		}
	}

	/**
	 * @return the system's temporary directory
	 * @deprecated use {@code System.getProperty("java.io.tmpdir")}, or
	 *             {@link java.nio.file.Files#createTempFile(String, String, java.nio.file.attribute.FileAttribute...)}
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static String getIOTmpDir() {
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * Skips lines of a reader: the given number, or fewer when the reader ends first
	 *
	 * @param reader
	 * @param lines the number of lines to skip
	 * @return the reader
	 * @throws IOException
	 */
	public static BufferedReader skip(BufferedReader reader, int lines) throws IOException {
		int skipped = 0;
		while (skipped < lines && reader.readLine() != null) {
			skipped++;
		}
		return reader;
	}

	/**
	 * Reads the first two fields of every line as a key and its value; a key repeated on a later line takes the later
	 * value. Trailing empty fields are kept, so "key&lt;delimiter&gt;" gives an empty value.
	 *
	 * @param file
	 * @param delimiter a regular expression, as for {@link String#split(String)}
	 * @param skip      the number of lines to skip first, e.g. 1 for a header
	 * @return the keys and their values
	 * @throws IOException
	 */
	public static Map<String, String> readKeyValue(String file, String delimiter, int skip) throws IOException {
		Map<String, String> keyValues = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			skip(reader, skip);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(delimiter, -1);
				keyValues.put(fields[0], fields[1]);
			}
		}
		return keyValues;
	}

	/**
	 * As {@link #readKeyValue(String, String, int)}, with the values read as numbers
	 *
	 * @throws NumberFormatException if a value is not a number
	 */
	public static Map<String, Double> readKeyDoubleValue(File file, String delimiter, int skip) throws IOException {
		Map<String, Double> keyValues = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			skip(reader, skip);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(delimiter, -1);
				keyValues.put(fields[0], Double.valueOf(fields[1]));
			}
		}
		return keyValues;
	}

	/**
	 * Reads the first field of every line as a key and the other fields as its values. The values of a key repeated on
	 * several lines are all kept, in file order.
	 *
	 * @param file
	 * @param delimiter a regular expression, as for {@link String#split(String)}
	 * @param skip      the number of lines to skip first, e.g. 1 for a header
	 * @return the keys and their values
	 * @throws IOException
	 */
	public static Map<String, List<String>> readKeyValues(File file, String delimiter, int skip) throws IOException {
		Map<String, List<String>> keyValues = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			skip(reader, skip);
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(delimiter);
				keyValues.computeIfAbsent(fields[0], key -> new ArrayList<>())
						.addAll(Arrays.asList(fields).subList(1, fields.length));
			}
		}
		return keyValues;
	}

	/**
	 * Gives every line, split on the delimiter, to a function that fills the map
	 *
	 * @param mapFiller called with {map, fields} for every line
	 * @return the map
	 * @deprecated read the lines with {@link #readCSV(File, int, Function)} and fill the map from the result
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static <T> Map<String, T> readKeyValuesEngin(File file, String delimiter, int skip,
			Function<Object[], T> mapFiller) throws IOException {
		Map<String, T> keyValues = new HashMap<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			skip(reader, skip);
			String line;
			while ((line = reader.readLine()) != null) {
				mapFiller.apply(new Object[] { keyValues, line.split(delimiter) });
			}
		}
		return keyValues;
	}

	/**
	 * Reads a matrix into columns: every row is mapped to one entry per data column by a line processor.
	 *
	 * @param file
	 * @param delimiter     a regular expression, as for {@link String#split(String)}
	 * @param lineProcessor maps a row to its entries, one per data column
	 * @return the entries of every data column, iterating over the columns in header order
	 * @throws IncorrectNumberOfEntriesException if a row has another number of entries than data columns
	 * @throws DuplicateColumnException          if the header names a data column twice
	 * @throws IOException
	 * @see #readMatrixRows(File, String, int, MatrixRowHandler) to read a matrix without collecting it
	 */
	public static <T> Map<String, List<T>> readMatrix(File file, String delimiter, MatrixLineProcessor<T> lineProcessor)
			throws IncorrectNumberOfEntriesException, IOException {

		Map<String, List<T>> matrix = new LinkedHashMap<>();
		readMatrixRows(file, delimiter, lineProcessor.getHeaderSampleStartIndex(), new MatrixRowHandler() {

			// the lists of the matrix, in header order
			private final List<List<T>> columns = new ArrayList<>();

			@Override
			public void columns(List<String> names) {
				for (String name : names) {
					List<T> column = new ArrayList<>();
					matrix.put(name, column);
					columns.add(column);
				}
			}

			@Override
			public int row(FileLine line, String[] fields) {
				List<T> entries = lineProcessor.process(line, delimiter);
				// a row of the wrong size is rejected by readMatrixRows, so it is not added
				if (entries.size() == columns.size()) {
					for (int i = 0; i < entries.size(); i++) {
						columns.get(i).add(entries.get(i));
					}
				}
				return entries.size();
			}
		});
		return matrix;
	}

	/**
	 * Reads a matrix one row at a time and hands each row to a handler, without collecting anything: the first line
	 * is the header, every other line a row.
	 *
	 * @param file
	 * @param delimiter              the column delimiter, a regular expression as for {@link String#split(String)}
	 * @param headerSampleStartIndex the (zero-based) index of the first data column
	 * @param handler                gets the data column names, then every row
	 * @throws DuplicateColumnException          if the header names a data column twice
	 * @throws IncorrectNumberOfEntriesException if the handler finds a number of entries in a row other than the
	 *                                           number of data columns
	 * @throws IOException                       if the file cannot be read, or is empty (no header line)
	 */
	public static void readMatrixRows(File file, String delimiter, int headerSampleStartIndex,
			MatrixRowHandler handler) throws IncorrectNumberOfEntriesException, IOException {

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String headerLine = reader.readLine();
			if (headerLine == null) {
				throw new IOException(file.getName() + " is empty: a matrix starts with a header line");
			}
			String[] header = headerLine.split(delimiter);
			// the data column names, in header order; a repeated name would merge two columns into one
			List<String> columns = new ArrayList<>();
			Set<String> names = new HashSet<>();
			for (int i = headerSampleStartIndex; i < header.length; i++) {
				if (!names.add(header[i])) {
					throw new DuplicateColumnException(file.getName() + ": column '" + header[i]
							+ "' appears more than once in the header");
				}
				columns.add(header[i]);
			}
			handler.columns(Collections.unmodifiableList(columns));

			// the header is line 1
			int lineNumber = 2;
			String text;
			while ((text = reader.readLine()) != null) {
				int entries = handler.row(new FileLine(file.getName(), lineNumber, text), text.split(delimiter));
				if (entries + headerSampleStartIndex != header.length) {
					throw new IncorrectNumberOfEntriesException("Line " + lineNumber + " have " + entries
							+ " entries instead of " + header.length);
				}
				lineNumber++;
			}
		}
	}

	/**
	 * Reads every line, split on the delimiter, after skipping the first lines
	 *
	 * @param delimiter a regular expression, as for {@link String#split(String)}
	 * @return the fields of every line, as {@code String[]}
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> readCSV(File file, String delimiter, int skip) throws IOException {
		return readCSV(file, skip, line -> (T) line.split(delimiter));
	}

	/**
	 * Maps every line, after skipping the first lines
	 *
	 * @param skip      the number of lines to skip first, e.g. 1 for a header
	 * @param rowMapper maps a line to an object
	 * @return the mapped lines, in file order
	 */
	public static <T> List<T> readCSV(File file, int skip, Function<String, T> rowMapper) throws IOException {
		List<T> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			skip(reader, skip);
			String line;
			while ((line = reader.readLine()) != null) {
				rows.add(rowMapper.apply(line));
			}
		}
		return rows;
	}

	/**
	 * Maps every line with the row mapper, or splits it on the delimiter when there is none
	 *
	 * @deprecated use {@link #readCSV(File, String, int)} or {@link #readCSV(File, int, Function)}
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static <T> List<T> readCSV(File file, String delimiter, int skip, Function<String, T> rowMapper)
			throws IOException {
		return rowMapper == null ? readCSV(file, delimiter, skip) : readCSV(file, skip, rowMapper);
	}

	/**
	 * @return the lines of a file joined without line separators
	 * @deprecated use {@link java.nio.file.Files#readString(java.nio.file.Path)}, which keeps the line separators
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static String readContentAsString(File file) throws IOException {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
		}
		return content.toString();
	}

	/**
	 * @return the first line of a file, or null if it is empty
	 * @deprecated read the first line with {@link java.nio.file.Files#newBufferedReader(java.nio.file.Path)}
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static String getFileHeader(String fileName) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			return reader.readLine();
		}
	}

	/**
	 * Writes one line per key and value: key&lt;delimiter&gt;value
	 *
	 * @param input   the keys and their values
	 * @param outfile
	 * @param delimiter
	 * @throws IOException
	 */
	public static <K, V> void writeKeyValues(Map<K, Iterable<V>> input, String outfile, String delimiter)
			throws IOException {
		writeObjects(outfile, null, input.entrySet(), entry -> {
			StringJoiner lines = new StringJoiner(Constants.NEW_LINE);
			for (V value : entry.getValue()) {
				lines.add(entry.getKey() + delimiter + value);
			}
			return lines.toString();
		});
	}

	/**
	 * Writes a header line, then one line per object. The file is not deleted when writing fails.
	 *
	 * @param file
	 * @param header    a header line, or null for none; an empty string is a header (an empty line)
	 * @param objects
	 * @param formatter formats an object as one line, without a line separator; null for {@link Object#toString()}
	 * @throws IOException
	 */
	public static synchronized <O> void writeObjects(File file, String header, Iterable<O> objects,
			Function<O, String> formatter) throws IOException {
		Function<O, String> format = formatter != null ? formatter : Object::toString;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			if (header != null) {
				writer.write(header);
				writer.write(Constants.NEW_LINE);
			}
			for (O object : objects) {
				writer.write(format.apply(object));
				writer.write(Constants.NEW_LINE);
			}
		}
	}

	/**
	 * As {@link #writeObjects(File, String, Iterable, Function)}, for a file name
	 */
	public static synchronized <O> void writeObjects(String file, String header, Iterable<O> objects,
			Function<O, String> formatter) throws IOException {
		writeObjects(new File(file), header, objects, formatter);
	}

	/**
	 * Deletes a file, if there is one
	 *
	 * @deprecated use {@link java.nio.file.Files#deleteIfExists(java.nio.file.Path)}
	 */
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static void deleteQuietly(File file) {
		if (file != null) {
			file.delete();
		}
	}

	/**
	 * Writes a header line, then one line per entry: key&lt;delimiter&gt;value
	 *
	 * @param map
	 * @param header a header line, or null for none
	 * @param output
	 * @param delimiter
	 * @throws IOException
	 */
	public static <K, V> void writeKeyValue(Map<K, V> map, String header, File output, String delimiter)
			throws IOException {
		writeObjects(output, header, map.entrySet(),
				(Entry<K, V> entry) -> entry.getKey().toString() + delimiter + entry.getValue().toString());
	}
}
