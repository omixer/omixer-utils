package io.omixer.utils.readers;

import java.util.List;

/**
 * Receives a matrix one row at a time, as read by
 * {@link io.omixer.utils.utils.FileUtils#readMatrixRows(java.io.File, String, int, MatrixRowHandler)}, so that the
 * caller decides what to keep and nothing is collected per cell.
 *
 * Exceptions are not caught by the reader: they reach the reader's caller unchanged.
 */
public interface MatrixRowHandler {

	/**
	 * Called once, before any row
	 *
	 * @param columns the names of the data columns (from the first data column on), in header order
	 */
	void columns(List<String> columns);

	/**
	 * Called for every row after the header, in file order
	 *
	 * @param line   the row, with its file name and line number
	 * @param fields the row split on the delimiter
	 * @return the number of entries (values) of the row, which must equal the number of data columns
	 */
	int row(FileLine line, String[] fields);
}
