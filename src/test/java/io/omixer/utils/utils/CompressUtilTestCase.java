package io.omixer.utils.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CompressUtilTestCase {

	@TempDir
	Path tmp;

	/** A zip file with the given entries; an entry name ending with / is a directory */
	private Path zip(String... entries) throws IOException {
		Path zip = tmp.resolve("test.zip");
		try (OutputStream out = Files.newOutputStream(zip); ZipOutputStream zos = new ZipOutputStream(out)) {
			for (String entry : entries) {
				zos.putNextEntry(new ZipEntry(entry));
				if (!entry.endsWith("/")) {
					zos.write(("content of " + entry).getBytes());
				}
				zos.closeEntry();
			}
		}
		return zip;
	}

	@Test
	public void gzipThenGunzipGivesTheFileBack() throws IOException {
		Path text = Files.writeString(tmp.resolve("test.txt"), "Hello Gzip\n");
		Path gzipped = tmp.resolve("test.txt.gz");
		Path flat = tmp.resolve("test2.txt");

		CompressUtils.gzip(text.toString(), gzipped.toString());
		CompressUtils.gunzip(gzipped.toString(), flat.toString());

		assertEquals("Hello Gzip\n", Files.readString(flat));
	}

	@Test
	public void zipDirectoryThenUnzipGivesTheDirectoryBackWithItsSubDirectories() throws IOException {
		Path directory = Files.createDirectories(tmp.resolve("testZip"));
		Files.writeString(directory.resolve("test.txt"), "Hello Zip");
		Files.writeString(Files.createDirectories(directory.resolve("sub")).resolve("inner.txt"), "Inner");

		File zipped = CompressUtils.zipDirectory(directory.toFile());
		Path unzipped = tmp.resolve("unzipped");
		CompressUtils.unzip(zipped.getPath(), unzipped.toString());

		assertEquals(directory + ".zip", zipped.getPath());
		assertEquals("Hello Zip", Files.readString(unzipped.resolve("testZip/test.txt")));
		assertEquals("Inner", Files.readString(unzipped.resolve("testZip/sub/inner.txt")));
	}

	@Test
	public void unzipGivesTheFilesItWrote() throws IOException {
		List<File> files = CompressUtils.unzip("src/test/resources/testfile.zip", tmp.toString());

		assertEquals(List.of(new File(tmp + File.separator + "testfile")), files);
		assertTrue(files.get(0).isFile());
	}

	@Test
	public void unzipCreatesTheDirectoriesOfDirectoryEntries() throws IOException {
		Path out = tmp.resolve("out");

		List<File> files = CompressUtils.unzip(zip("dir/", "dir/file.txt").toString(), out.toString());

		assertTrue(Files.isDirectory(out.resolve("dir")));
		assertEquals("content of dir/file.txt", Files.readString(out.resolve("dir/file.txt")));
		assertEquals(List.of(new File(out + File.separator + "dir/file.txt")), files);
	}

	@Test
	public void unzipRejectsAnEntryThatWouldBeWrittenOutsideTheOutputFolder() throws IOException {
		Path out = tmp.resolve("out");
		Path zip = zip("../evil.txt");

		IOException e = assertThrows(IOException.class, () -> CompressUtils.unzip(zip.toString(), out.toString()));

		assertTrue(e.getMessage().contains("../evil.txt"));
		assertFalse(Files.exists(tmp.resolve("evil.txt")));
	}
}
