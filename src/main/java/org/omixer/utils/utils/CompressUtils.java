package org.omixer.utils.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressUtils {

	static final String ZIP_SUFFIX = ".zip";

	private CompressUtils() {
		super();
	}

	/**
	 * ZIPs a directory to directoryPath.zip
	 * 
	 * @param directoryPath
	 * @return
	 * @throws IOException
	 */
	public static final File zipDirectory(String directoryPath)
			throws IOException {
		final String outpath = directoryPath + ZIP_SUFFIX;
		zipDirectory(directoryPath, outpath);
		return new File(outpath);
	}

	public static final File zipDirectory(File directoryPath)
			throws IOException {
		return zipDirectory(directoryPath.getAbsolutePath());
	}

	/**
	 * ZIPs a directory to the given output path
	 * 
	 * @param directoryPath
	 * @param output
	 * @throws IOException
	 */
	public static final void zipDirectory(String directoryPath, String output)
			throws IOException {

		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output))) {
			// the directory containing the files to zip
			final File inputDir = new File(directoryPath);
			// list of files to zip
			final File[] files = inputDir.listFiles();
			// create a buffer to read data in
			final byte[] buffer = new byte[8192];
			// append each file to the zip
			for (File file : files) {
				try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
					/*
					 * append the entry for the current file in the zip archive use
					 * the inputDir name as a prefix to have the files extracted in
					 * a directory instead of current working directory
					 */
					zos.putNextEntry(new ZipEntry(inputDir.getName()
							+ File.separator + file.getName()));
					// number of read bytes
					int read = 0;
					// read from input stream till the end of stream is reached
					while ((read = in.read(buffer)) != -1) {
						// write the newly read bytes to the current ZIP entry
						zos.write(buffer, 0, read);
					}
					// close current zip entry
					zos.closeEntry();
					// close input stream
					in.close();	
				}
			}
		}
	}

	public static final void gzip(String input, String output)
			throws IOException {

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(input));
				GZIPOutputStream compressor = new GZIPOutputStream(new FileOutputStream(output));) {

			byte[] buffer = new byte[8192];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
				compressor.write(buffer, 0, read);
			}
			compressor.finish();
		}
	}

	public static final void gunzip(String input, String output)
			throws IOException {

		byte[] buffer = new byte[1024];
		
		try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(input));
				FileOutputStream out = new FileOutputStream(output);) {

			int len;
			while ((len = gzis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
		}
	}

	/**
	 * 
	 * @param zipFile
	 * @param outputFolder
	 * @throws IOException
	 */
	public static final List<File> unzip(String zipFile, String outputFolder)
			throws IOException {

		byte[] buffer = new byte[1024];
		
		List<File> unzippedFiles = new ArrayList<File>();
		try (ZipInputStream zis = new ZipInputStream(
						new FileInputStream(zipFile));) {

			// create output directory if not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator
						+ fileName);
				// add next unzipped file
				unzippedFiles.add(newFile);
				
				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				try (FileOutputStream fos = new FileOutputStream(newFile)){
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}	
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();	
		}
		return unzippedFiles ;
	}
}
