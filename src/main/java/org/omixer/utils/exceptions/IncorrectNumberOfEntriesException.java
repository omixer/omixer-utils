package org.omixer.utils.exceptions;

/**
 * Thrown when the number of entries to add is not equal to that in the
 * receiver structure
 * 
 * @author <a href="mailto:youssef.darzi@gmail.com">Youssef Darzi</a>
 * 
 */
public class IncorrectNumberOfEntriesException extends Exception {

	private static final long serialVersionUID = 1L;

	public IncorrectNumberOfEntriesException() {
		super();
	}

	public IncorrectNumberOfEntriesException(String reason) {
		super(reason);
	}
}
