package io.omixer.utils.exceptions;

/**
 * Thrown when a matrix header names the same column twice
 *
 * Unchecked: the caller decides how to report it
 */
public class DuplicateColumnException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateColumnException(String reason) {
		super(reason);
	}
}
