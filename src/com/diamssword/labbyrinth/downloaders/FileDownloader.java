package com.diamssword.labbyrinth.downloaders;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

/**
 * A simple utility class which provides methods for file downloading using HTTP
 * protocol.
 */
public class FileDownloader {

	/**
	 * Gets file size using an HTTP connection with GET method
	 * 
	 * @return file size in bytes
	 * @throws IOException
	 */
	public static long getFileSize(String fileUrl) throws IOException {

		URL oracle = new URL(fileUrl);

		HttpURLConnection yc = (HttpURLConnection) oracle.openConnection();
		populateDesktopHttpHeaders(yc);

		long fileSize = 0;
		try {
			// retrieve file size from Content-Length header field
			fileSize = Long.parseLong(yc.getHeaderField("Content-Length"));
		} catch (NumberFormatException nfe) {
		}

		return fileSize;
	}

	/**
	 * Downloads a file from a given url and writes it to a file in current
	 * working directory
	 * 
	 * @param urli
	 *            Input file url
	 * 
	 * @throws IOException
	 *             ,MalformedURLException
	 */
	public static void downloadFile(String urli) throws IOException,
			MalformedURLException {
		String fileName = urli.substring(urli.lastIndexOf('/') + 1,
				urli.length());

		// download the file in the current working directory
		File outFile = new File(fileName);


		downloadFile(urli, outFile,(i)->{});
	}

	public static boolean streamToFile(InputStream inputStream, File outputFile, Consumer<Integer> percentage) throws IOException, MalformedURLException {
		// Get a connection to the URL and start up a buffered reader.
		int total=inputStream.available();
		FileOutputStream writer = new FileOutputStream(outputFile);
		byte[] buffer = new byte[153600];
		long totalBytesRead = 0;
		int bytesRead = 0;

		while ((bytesRead = inputStream.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			totalBytesRead += bytesRead;
			percentage.accept((int)(((float)totalBytesRead/total)*100f));
		}

		percentage.accept((int)(totalBytesRead/total)*100);
		writer.close();
		inputStream.close();
		return totalBytesRead>=total;
	}
	public static void streamToPrint(InputStream inputStream, PrintStream output) throws IOException, MalformedURLException {

		// Get a connection to the URL and start up a buffered reader.
		int total=inputStream.available();
        byte[] buffer = new byte[153600];
		int bytesRead = 0;
		while ((bytesRead = inputStream.read(buffer)) > 0) {
			output.write(buffer, 0, bytesRead);
		}
		output.close();
		inputStream.close();
	}
	/**
	 * Downloads a file from a given url and writes it to a given File object
	 * 
	 * @param urli
	 *            Input file url
	 * @param outputFile
	 *            The output file to write to
	 * 
	 */
	public static boolean downloadFile(String urli, File outputFile, Consumer<Integer> percentage) throws IOException, MalformedURLException {
		// Get a connection to the URL and start up a buffered reader.
		long total=getFileSize(urli);
		if(total<=0)
			total=100;
		URL url = new URL(urli);
		url.openConnection();
		InputStream reader = url.openStream();

		// Setup a buffered file writer to write out what we read from the
		// website.
		FileOutputStream writer = new FileOutputStream(outputFile);
		byte[] buffer = new byte[153600];
		long totalBytesRead = 0;
		int bytesRead = 0;

		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			totalBytesRead += bytesRead;
			percentage.accept((int)(((float)totalBytesRead/total)*100f));
		}

		percentage.accept((int)(totalBytesRead/total)*100);
		writer.close();
		reader.close();
		return totalBytesRead>=total;
	}

	/**
	 * Downloads a file from a given url and writes it byte array
	 * 
	 * @param urli
	 *            Input file url
	 * 
	 * 
	 */
	public static byte[] downloadFileToArray(String urli) throws IOException,
			MalformedURLException {

		return downloadFileToArray(urli, 0, 0);
	}

	/**
	 * Downloads a file from a given url and writes it to a byte array
	 * 
	 * @param urli
	 *            Input file URL
	 * @param connectionTimeout
	 *            the maximum time in milliseconds to wait while connecting
	 * @param readTimeout
	 *            the read timeout in milliseconds, or 0 if reads never timeout
	 * 
	 * 
	 */
	public static byte[] downloadFileToArray(String urli,
			int connectionTimeout, int readTimeout) throws IOException,
			MalformedURLException {

		long startTime = System.currentTimeMillis();

		// Get a connection to the URL and start up a buffered reader.
		URL url = new URL(urli);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		populateDesktopHttpHeaders(con);

		con.setConnectTimeout(connectionTimeout);
		con.setReadTimeout(readTimeout);

		InputStream reader = con.getInputStream();

		// Setup a buffered file writer to write out what we read from the
		// website.
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		byte[] buffer = new byte[153600];
		long totalBytesRead = 0;
		int bytesRead = 0;

		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			// buffer = new byte[153600];
			totalBytesRead += bytesRead;
			// logger.debug("Downloaded {} Kb ", (totalBytesRead / 1024));
		}

		long endTime = System.currentTimeMillis();

		// write all bytes to buffer
		buffer = writer.toByteArray();

		// logger.debug(
		// "Downloaded {}. {} bytes read in {} ",
		// new Object[] { urli, String.valueOf(totalBytesRead),
		// TimeUtils.getDuration(startTime, endTime) });

		con.disconnect();
		writer.close();
		reader.close();

		return buffer;
	}

	private static void populateDesktopHttpHeaders(URLConnection urlCon) {
		// add custom header in order to be easily detected
		urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		urlCon.setRequestProperty("Accept-Language",
				"el-gr,el;q=0.8,en-us;q=0.5,en;q=0.3");
		urlCon.setRequestProperty("Accept-Charset",
				"ISO-8859-7,utf-8;q=0.7,*;q=0.7");
	}

}