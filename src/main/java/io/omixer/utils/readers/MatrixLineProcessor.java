package io.omixer.utils.readers;

import java.util.List;

/**
 * 
 * @author <a href="mailto:youssef.darzi@gmail.com">Youssef Darzi</a>
 * 
 * @param <T>
 */
public interface MatrixLineProcessor<T> {

	/**
	 * @return the (zero-based) index of the first data column  
	 */
	int getHeaderSampleStartIndex();
	
	/**
	 * Map a line to the desired type T
	 * @param line
	 * @param delimiter
	 * @return
	 */
	List<T> process(String line, String delimiter);

	/**
	 * Map a line to the desired type T, knowing where it comes from (e.g. to report an invalid line)
	 *
	 * The default ignores the origin and calls {@link #process(String, String)}. Exceptions are not caught by the
	 * readers calling this method: they reach the reader's caller unchanged.
	 *
	 * @param line the line, with its file name and line number
	 * @param delimiter
	 * @return
	 */
	default List<T> process(FileLine line, String delimiter) {
		return process(line.text(), delimiter);
	}
}
