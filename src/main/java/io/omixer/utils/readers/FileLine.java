package io.omixer.utils.readers;

/**
 * A line of a text file, with where it comes from
 *
 * @param file the name of the file
 * @param number the 1-based line number in that file
 * @param text the content of the line
 */
public record FileLine(String file, int number, String text) {
}
