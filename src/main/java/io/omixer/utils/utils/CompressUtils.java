package io.omixer.utils.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Zip and gzip files and directories.
 *
 * @author <a href="mailto:youssef.darzi@omixer.io">Youssef Darzi</a>
 */
public final class CompressUtils {

	static final String ZIP_SUFFIX = ".zip";

	private CompressUtils() {
	}

	/**
	 * Zips a directory into directoryPath.zip, next to it (see {@link #zipDirectory(String, String)})
	 *
	 * @return the zip file
	 */
	public static File zipDirectory(String directoryPath) throws IOException {
		String output = directoryPath + ZIP_SUFFIX;
		zipDirectory(directoryPath, output);
		return new File(output);
	}

	/**
	 * Zips a directory into directoryPath.zip, next to it (see {@link #zipDirectory(String, String)})
	 *
	 * @return the zip file
	 */
	public static File zipDirectory(File directoryPath) throws IOException {
		return zipDirectory(directoryPath.getAbsolutePath());
	}

	/**
	 * Zips the files of a directory and of its sub-directories. Entries are named directory/relative/path, so that
	 * unzipping recreates the directory.
	 *
	 * @param directoryPath
	 * @param output        the zip file
	 * @throws IOException if the directory cannot be read or the zip file cannot be written
	 */
	public static void zipDirectory(String directoryPath, String output) throws IOException {
		Path directory = Path.of(directoryPath);
		if (!Files.isDirectory(directory)) {
			throw new IOException(directoryPath + " is not a directory");
		}
		try (Stream<Path> paths = Files.walk(directory);
				OutputStream out = Files.newOutputStream(Path.of(output));
				ZipOutputStream zos = new ZipOutputStream(out)) {
			for (Path file : paths.filter(Files::isRegularFile).sorted().toList()) {
				// entry names always use /, whatever the platform's separator
				String relative = directory.relativize(file).toString().replace(File.separatorChar, '/');
				zos.putNextEntry(new ZipEntry(directory.getFileName() + "/" + relative));
				Files.copy(file, zos);
				zos.closeEntry();
			}
		}
	}

	/**
	 * Compresses a file with gzip
	 *
	 * @param input
	 * @param output the gzipped file
	 * @throws IOException
	 */
	public static void gzip(String input, String output) throws IOException {
		try (InputStream in = Files.newInputStream(Path.of(input));
				OutputStream out = Files.newOutputStream(Path.of(output));
				GZIPOutputStream compressor = new GZIPOutputStream(out)) {
			in.transferTo(compressor);
		}
	}

	/**
	 * Decompresses a gzipped file
	 *
	 * @param input  the gzipped file
	 * @param output
	 * @throws IOException
	 */
	public static void gunzip(String input, String output) throws IOException {
		try (InputStream in = Files.newInputStream(Path.of(input));
				GZIPInputStream decompressor = new GZIPInputStream(in);
				OutputStream out = Files.newOutputStream(Path.of(output))) {
			decompressor.transferTo(out);
		}
	}

	/**
	 * Unzips a zip file into a folder, creating the folder and the directories of the entries. An existing file is
	 * replaced.
	 *
	 * @param zipFile
	 * @param outputFolder
	 * @return the files written, as outputFolder/entry name, in zip order
	 * @throws IOException if an entry would be written outside the output folder (e.g. a name with ../), or when the
	 *                     zip file cannot be read or a file cannot be written
	 */
	public static List<File> unzip(String zipFile, String outputFolder) throws IOException {
		Path folder = Path.of(outputFolder).toAbsolutePath().normalize();
		Files.createDirectories(folder);
		List<File> files = new ArrayList<>();
		try (InputStream in = Files.newInputStream(Path.of(zipFile)); ZipInputStream zis = new ZipInputStream(in)) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				// a name is relative to the output folder, also one starting with /
				Path target = Path.of(outputFolder, entry.getName()).toAbsolutePath().normalize();
				if (!target.startsWith(folder)) {
					throw new IOException("Zip entry '" + entry.getName() + "' in " + zipFile
							+ " would be written outside " + outputFolder);
				}
				if (entry.isDirectory()) {
					Files.createDirectories(target);
				} else {
					Files.createDirectories(target.getParent());
					Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
					files.add(new File(outputFolder + File.separator + entry.getName()));
				}
			}
		}
		return files;
	}
}
