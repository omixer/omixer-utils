package org.omixer.utils.readers;

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
	
}
