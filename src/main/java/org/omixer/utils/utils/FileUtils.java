package org.omixer.utils.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.omixer.utils.Constants;
import org.omixer.utils.exceptions.IncorrectNumberOfEntriesException;
import org.omixer.utils.readers.MatrixLineProcessor;

/**
 * Add functionalities not provided by commons-fileUtils
 * 
 * @author Youssef Darzi
 *
 */
public final class FileUtils {

	private static final String IO_TMP_DIR  = "java.io.tmpdir";

	/**
	 * Private Constructor to avoid instantiation
	 */
	private FileUtils() {
	}

	/**
	 * Copies the source file to the destination file
	 * 
	 * @param sourceFile
	 * @param destination
	 * @throws IOException
	 */
	public static synchronized void copy(File sourceFile, File destination) throws IOException {

		try (FileInputStream fis = new FileInputStream(sourceFile);
				FileChannel in = fis.getChannel();
				FileOutputStream fos = new FileOutputStream(destination);
				FileChannel out = fos.getChannel();) {

			in.transferTo(0, sourceFile.length(), out);
		}
	}

	public static final String getIOTmpDir() {
		return System.getProperty(IO_TMP_DIR);
	}
	
	/**
	 * @throws IOException 
	 * 
	 * Consumes the given number of lines on a {@link LineIterator},
	 * 
	 * If the {@link LineIterator} is consumed before the number of desired
	 * lines is reached the method returns without any warning
	 * 
	 * @param li
	 * @param skip
	 * @return
	 * @throws
	 */
	public static final BufferedReader skip(BufferedReader li, int skip) throws IOException {
		int skipped = 0;
		while (li.ready() && skipped < skip) {
			li.readLine();
			skipped++;
		}
		return li;
	}

	/**
	 * Reads a key value file: duplicated keys are overridden by the last key.
	 * If a file has multiple columns then the first 2 are returned only
	 * 
	 * @param file
	 * @param delimiter
	 * @param skip
	 * @return {@link HashMap}<{@link String}, {@link String}> of key-value
	 *         pairs
	 * @throws IOException 
	 */
	public static final Map<String, String> readKeyValue(String file,
			String delimiter, int skip) throws IOException {

		final Map<String, String> keyVals = new HashMap<String, String>();
				
		try (BufferedReader lineReader = new BufferedReader(new FileReader(file))) {

			// this is for the user to handle
			skip(lineReader, skip);

			String[] tokens = null;
			while (lineReader.ready()) {
				// as many time as possible and keep trailing characters
				tokens = lineReader.readLine().split(delimiter, -1);
				keyVals.put(tokens[0], tokens[1]);
			}
		}

		return keyVals;
	}

	public static final Map<String, Double> readKeyDoubleValue(File file,
			String delimiter, int skip) throws IOException {

		final Map<String, Double> keyVals = new HashMap<String, Double>();
		
		try (BufferedReader lineReader = new BufferedReader(new FileReader(file))) {
			// this is for the user to handle
			skip(lineReader, skip);

			String[] tokens = null;
			while (lineReader.ready()) {
				// as many time as possible and keep trailing characters
				tokens = lineReader.readLine().split(delimiter, -1);
				keyVals.put(tokens[0], Double.valueOf(tokens[1]));
			}
		}

		return keyVals;
	}
	
	/**
	 * Maps a key value file to a {@link Map}
	 * 
	 * Read a file as key values Handles multi-values per lines as well as
	 * multiple occurrences of the same key i.e do not override pre-existing key
	 * entries with new ones
	 *  
	 * @param file
	 * @param delimiter
	 * @param skip
	 * @return
	 * @throws IOException 
	 */
	public static final Map<String, List<String>> readKeyValues(File file,
			String delimiter, int skip) throws IOException {
		final Map<String, List<String>> keyValues = new HashMap<String, List<String>>();
		
		try (BufferedReader lineReader = new BufferedReader(new FileReader(file))){
			// skip lines (skip is the number of lines) 
			skip(lineReader, skip);
			// a recyclable object for splitting lines into
			String[] tockens = null;
			// while there are more lines to read
			while (lineReader.ready()) {
				// split 
				tockens = lineReader.readLine().split(delimiter);
				// get the values for the current key (tokens[0])
				List<String> values = keyValues.get(tockens[0]);
				// if this key is observer for the first time
				if (values == null) {
					// create a list to hold the values
					values = new LinkedList<String>();
					// register the values with the current key
					keyValues.put(tockens[0], values);
				}
				//populate the values
				for (int i = 1; i < tockens.length; i++) {
					values.add(tockens[i]);
				}
			}
		}
		// return the read keyValues
		return keyValues;
	}
	
	public static final <T> Map<String, T> readKeyValuesEngin(File file,
			String delimiter, int skip, Function<Object[], T> mapFiller)
			throws IOException {

		final Map<String, T> keyValues = new HashMap<String, T>();

		try (BufferedReader lineReader = new BufferedReader(new FileReader(file))) {

			skip(lineReader, skip);
			String[] tockens = null;

			while (lineReader.ready()) {
				tockens = lineReader.readLine().split(delimiter);
				mapFiller.apply(new Object[] { keyValues, tockens });
			}
		}

		return keyValues;
	}

	/**
	 * 
	 * Maps the headers of a matrix to the columns
	 * 
	 * @param <T>
	 * @param file
	 * @param delimiter
	 * @param lineProcessor
	 * @return
	 * @throws IncorrectNumberOfEntriesException
	 * @throws IOException 
	 */

	public static final <T> Map<String, List<T>> readMatrix(File file,
			final String delimiter, MatrixLineProcessor<T> lineProcessor)
			throws IncorrectNumberOfEntriesException, IOException {

		// will never be null since the try condition will initialize it or
		// throw an excption
		final Map<String, List<T>> matrix;

		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			// keep the header: [0] = mass , [rest] = sample names
			final String[] header = br.readLine().split(delimiter);
			// initialize the matrix
			matrix = new HashMap<String, List<T>>();

			// get sample names from header
			for (int i = lineProcessor.getHeaderSampleStartIndex(); i < header.length; i++) {
				matrix.put(header[i], new LinkedList<T>());
			}

			// start at two cos header is at 1
			int lineNum = 2;
			while (br.ready()) {
				// map the line to a list of T
				List<T> entries = lineProcessor.process(br.readLine(),
						delimiter);
				// the number of entries that were mapped
				int entriesSize = entries.size();

				// ensure correct number of lines
				if (entriesSize + lineProcessor.getHeaderSampleStartIndex() != header.length) {
					throw new IncorrectNumberOfEntriesException("Line "
							+ lineNum + " have " + entriesSize
							+ " entries instead of " + header.length);
				}
				// increment line number
				lineNum++;
				// populate matrix
				for (int i = 0; i < entriesSize; i++) {
					// sync the fetch from header by getting : i +
					// lineProcessor.getHeaderSampleStartIndex()
					matrix.get(
							header[i
									+ lineProcessor.getHeaderSampleStartIndex()])
							.add(entries.get(i));
				}
			}
		}

		return matrix;
	}

	@SuppressWarnings("unchecked")
	public static final <T> List<T> readCSV(File file, final String delimiter,
			int skip)
			throws IOException {

		return readCSV(file, skip, (Function<String, T>) new Function<String, String[]>() {
			public String[] apply(String input) {
				// in case delimiter is null a null pointer exception is
				// thrown
				return input.split(delimiter);
			}
		});
	}
	
	public static final <T> List<T> readCSV(File file,
			int skip, Function<String, T> rowMapper)
			throws IOException {

		// init the list
		final List<T> mappedRows = new LinkedList<T>();		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			skip(br, skip);
			// process each row
			while (br.ready()) {
				mappedRows.add(rowMapper.apply(br.readLine()));
			}
		}
		return mappedRows;
	}
	
	/**
	 * 
	 * Given a tab delimited file map each of its rows to an object
	 * 
	 * @return
	 * @throws IOException 
	 * @Deprecated Use the simplified versions instead: with either a delimiter or a rowmapper
	 */
	@SuppressWarnings("unchecked")
	public static final <T> List<T> readCSV(File file, final String delimiter,
			int skip, Function<String, T> rowMapper)
			throws IOException {

		// init the list
		final List<T> mappedRows = new LinkedList<T>();

		Function<String, T> rowMapperIntern = rowMapper; 
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			FileUtils.skip(br, skip);
			// if the provided rowMapper is null, create a default which return
			// a String[]
			if (rowMapperIntern == null) {
				rowMapperIntern = (Function<String, T>) new Function<String, String[]>() {
					public String[] apply(String input) {
						// in case delimiter is null a null pointer exception is
						// thrown
						return input.split(delimiter);
					}
				};
			}
			// process each row
			while (br.ready()) {
				mappedRows.add(rowMapperIntern.apply(br.readLine()));
			}
		} 

		return mappedRows;
	}
	
	/**
	 * 
	 * @param file
	 * @return a String representation of the content of a file 
	 * @throws IOException
	 */
	public static final String readContentAsString(File file) throws IOException {

		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			String content = Constants.EMPTY_STRING;
			while (br.ready()) {
				content += br.readLine();
			}
			return content;
		}
	}
	
	/**
	 * Return the first line in a file
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static final String getFileHeader(String fileName)
			throws IOException {

		String header = null;
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			// if exception is thrown then the file is not correct and should
			// not be passed to this function
			header = br.readLine();
		}

		return header;
	}

	/**
	 * Given a Map<K, List<V>, save it's {@link Entry}s as key value each time
	 * on a new line
	 * 
	 * @param <K>
	 * @param <V>
	 * @param input
	 * @throws IOException
	 */
	public static final <K, V> void writeKeyValues(Map<K, Iterable<V>> input,
			String outfile, final String delimiter) throws IOException {

		// the platform's line separator
		final String lineSeparator = System.getProperty("line.separator");

		writeObjects(outfile, null, input.entrySet(),
				new Function<Entry<K, Iterable<V>>, String>() {
					public String apply(Entry<K, Iterable<V>> entry) {
						StringBuilder s = new StringBuilder();

						for (V value : entry.getValue()) {
							s.append(entry.getKey()).append(delimiter)
									.append(value).append(lineSeparator);
						}
						// replace the trailing characters by an empty space 
						return s.toString().replaceFirst(lineSeparator + "$",
								Constants.EMPTY_STRING);
					}
				});

	}

	/**
	 * 
	 * File are note deleted in case of {@link IOException}s. This is left to
	 * the programmer to handle.
	 * 
	 * New lines are handled by this method
	 * 
	 * @param file
	 * @param header
	 * 			a header {@link String} or <code>null</code>. An empty
	 *          {@link String} is considered as a valid header.
	 * @param objects
	 * @param formater
	 *            an implementation that return a String format of the object
	 *            without a new line character
	 * @throws IOException
	 */
	public static final synchronized <O> void writeObjects(File file,
			String header, Iterable<O> objects, Function<O, String> formater)
			throws IOException {
		// the platform's line separator
		final String lineSeparator = System.getProperty("line.separator");
		// initialize the format function with the one given in params
		Function<O, String> format = formater;
		// if nothing is given make sure to provide a default
		if (formater == null) {
			// assign a plain toString format
			format = new Function<O, String>() {
				public String apply(O input) {
					return input.toString();
				}
			};
		}

		try (FileWriter fileWriter = new FileWriter(file);) {			
			
			// is there a header to print?
			if (header != null) {
				fileWriter.write(header);
				fileWriter.write(lineSeparator);
			}
			// feature formatter provided then use it to format each object
			for (O o : objects) {
				fileWriter.write(format.apply(o));
				fileWriter.write(lineSeparator);
			}

		}
	}
	
	/**
	 * 
	 * 
	 * File are note deleted in case of {@link IOException}s. This is left to
	 * the programmer to handle.
	 * 
	 * New lines are handled by this method
	 * 
	 * 
	 * @param <O>
	 * @param file
	 * @param header
	 *            a header {@link String} or <code>null</code>. An empty
	 *            {@link String} is considered as a valid header.
	 *            
	 * @param objects
	 * @param formater
	 *            an implementation that return a String format of the object
	 *            without a new line character
	 * @throws IOException
	 */
	public static final synchronized <O> void writeObjects(String file,
			String header, Iterable<O> objects, Function<O, String> formater)
			throws IOException {
		writeObjects(new File(file), header, objects, formater);
	}

	/**
	 * Deletes the file and handle {@link NullPointerException}s
	 * 
	 * @param file
	 */
	public static final void deleteQuietly(File file) {
		if (file != null) {
			file.delete();
		}

	}

	public static <K, V> void writeKeyValue(Map<K, V> map, String header,
			File output, final String delimiter) throws IOException {
		writeObjects(output, header, map.entrySet(),
				new Function<Entry<K, V>, String>() {

					public String apply(Entry<K, V> input) {
						return input.getKey().toString() + delimiter
								+ input.getValue().toString();
					}
				});		
	}
}
