package org.omixer.utils.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.junit.Test;
import org.omixer.utils.utils.CompressUtils;
import org.omixer.utils.utils.FileUtils;

public class CompressUtilTestCase {

	@Test
	public void testGunzip() throws IOException {
		
		final String text = "Hello Gzip";
		final String out = "test.txt";
		final String gzipped = "test.txt.gz";
		final String flat = "test2.txt";
		FileUtils.writeObjects(out, null, new LinkedList<String>() {
			{
				add(text);
			}
		}, null);

		CompressUtils.gzip(out, gzipped);
		CompressUtils.gunzip(gzipped, flat);
		
		assertEquals(text, FileUtils.readContentAsString(new File(flat)));
		// clean up
		new File(flat).delete();
		new File(out).delete();
		new File(gzipped).delete();	
	}
	
	@Test
	public void testUnzip() throws IOException {
		
		final String text = "Hello Zip";
		final File zipDir = new File("testZip");
		final File out = new File(zipDir, "test.txt");
		final String zipped = "testZip.zip";
		final File unzipDir = new File("testUnZip");
		final File flat = new File(unzipDir, "testZip/test.txt");
		
		zipDir.mkdir();
		
		FileUtils.writeObjects(out, null, new LinkedList<String>() {
			{
				add(text);
			}
		}, null);
		
		CompressUtils.zipDirectory(zipDir);
		CompressUtils.unzip(zipped, unzipDir.getPath());
		assertEquals(text, FileUtils.readContentAsString(flat));
		
		// clean up
		out.delete();
		flat.delete();
		new File(unzipDir, "testZip").delete();
		zipDir.delete();
		new File(zipDir.getAbsolutePath() + ".zip").delete();
		unzipDir.delete();
		
		
	}
	
	@Test
	public void testUnzipFile() throws IOException {
		// zip a file and add as a resource
		CompressUtils.unzip("src/test/resources/testfile.zip", "/tmp");
		assertTrue(new File("/tmp/testfile").exists());
	}
}
