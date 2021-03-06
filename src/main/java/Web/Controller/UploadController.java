package Web.Controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import AnnotedText2NIF.IOContent.TextReader;

/**
 * This class provides a file upload request for turtle files to GERBIL
 * Inspired by "BalusC" => http://stackoverflow.com/questions/2469451/upload-files-from-java-client-to-a-http-server
 * @author TTurk
 *
 */
public class UploadController 
{
	
	/**
	 * This method upload a file to the given GERBIL upload URL
	 * @param url
	 * @param path
	 * @return response code
	 * @throws IOException
	 */
	public static int uploadFile(String url, String path) throws IOException
	{
		String charset = "UTF-8";
		File ttl_file = new File(path);
		String boundary = Long.toHexString(System.currentTimeMillis()); 		// Just generate some unique random value.
		String CRLF = "\r\n"; 													// Line separator required by multipart/form-data.

		URLConnection connection = new URL(url).openConnection();
		connection.setDoOutput(true);
		((HttpURLConnection) connection).setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept-Language", "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4");

		try (	OutputStream output = connection.getOutputStream();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);) 
		{
		    // Send text file.
		    writer.append("--" + boundary).append(CRLF);
		    writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + ttl_file.getName() + "\"").append(CRLF);
		    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(ttl_file.getName())).append(CRLF);
		    writer.append(CRLF).flush();
		    Files.copy(ttl_file.toPath(), output);
		    output.flush(); // Important before continuing with writer!
		    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

		    // End of multipart/form-data.
		    writer.append("--" + boundary + "--").append(CRLF).flush();
		}
		
		return ((HttpURLConnection) connection).getResponseCode();
	}
	
	/*
	 * EXAMPLE of USE
	 */
	public static void main(String[] args ) throws IOException
	{
		TextReader tr = new TextReader();
		String upload_url ="http://gerbil.aksw.org/gerbil/file/upload";
		String file_location = tr.getResourceFileAbsolutePath("default1.ttl");
		
		System.out.println("Response CODE: "+UploadController.uploadFile(upload_url, file_location));
	}
}
