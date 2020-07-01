package com.indezer;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpDownloadUtility {
	private static final int BUFFER_SIZE = 4096;

	static String userCredentials = "gouni:Progteam20201@";

	public static void main(String[] args) throws URISyntaxException {
		try {
			downloadFile("http://665dev02:9090/jenkins/me/my-views/view/All/", "c:\\demo\\pdf\\test2.html");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Downloads a file from a URL
	 * 
	 * @param fileURL
	 *            HTTP URL of the file to be downloaded
	 * @param saveDir
	 *            path of the directory to save the file
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static void downloadFile(String fileURL, String saveDir) throws IOException, URISyntaxException {
		URL url = new URL(fileURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		// con.setRequestProperty("User-Agent", USER_AGENT);
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
		con.setRequestProperty("Content-Type", "application/json;");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		con.setRequestProperty("Authorization", basicAuth);

		String sessionId = "";
		con.setRequestProperty("Cookie", "JSESSIONID=" + URLEncoder.encode(sessionId, "UTF-8"));
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		int responseCode = con.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String disposition = con.getHeaderField("Content-Disposition");
			String contentType = con.getContentType();
			int contentLength = con.getContentLength();

			if (disposition != null) {
				// extracts file name from header field
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 10, disposition.length() - 1);
				}
			} else {
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
			}

			System.out.println("Content-Type = " + contentType);
			System.out.println("Content-Disposition = " + disposition);
			System.out.println("Content-Length = " + contentLength);
			System.out.println("fileName = " + fileName);

			// opens input stream from the HTTP connection
			InputStream inputStream = con.getInputStream();
			String saveFilePath = saveDir + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			HttpCookie cookie = new HttpCookie("lang", "fr");
			cookie.setDomain("twitter.com");
			cookie.setPath("/");
			cookie.setVersion(0);
			cookieManager.getCookieStore().add(new URI("http://twitter.com/"), cookie);
			
			
			

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			System.out.println("File downloaded");

			// Parser
			File input = new File("c:/demo/pdf/test2.html");
			Document doc = Jsoup.parse(input, "UTF-8");
			Elements links = doc.select("link[href]");
			for (Element e : links) {
				// baseUri will be used by absUrl
				String absUrl = e.attr("href");
				e.attr("href", "http://665dev02:9090" + absUrl);
			}

			// now we process the imgs
			Elements imgs = doc.select("img");
			System.out.println("img count : " + imgs.size());
			for (Element e : imgs) {
				String absUrl = e.attr("src");
				e.attr("src", "http://665dev02:9090" + absUrl);
				// Save image
				// getImages(e.absUrl("src"));;
			}

			FileUtils.writeStringToFile(input, doc.outerHtml(), "UTF-8");

		} else {
			System.out.println("No file to download. Server replied HTTP code: " + responseCode);
		}
		// con.disconnect();
	}

	/**
	 * @param imageUrl
	 * @param fileName
	 * @param relativePath
	 * @return
	 */
	public static String storeImageIntoFS(String imageUrl, String fileName) {
		String imagePath = null;
		try {
			byte[] bytes = Jsoup.connect(imageUrl).ignoreContentType(true).execute().bodyAsBytes();
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			String rootTargetDirectory = "c:/demo/pdf/images/";
			imagePath = rootTargetDirectory + "/" + fileName;
			saveByteBufferImage(buffer, rootTargetDirectory, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imagePath;
	}

	/**
	 * @param imageDataBytes
	 * @param rootTargetDirectory
	 * @param savedFileName
	 */
	public static void saveByteBufferImage(ByteBuffer imageDataBytes, String rootTargetDirectory, String savedFileName) {
		String uploadInputFile = rootTargetDirectory + "/" + savedFileName;

		File rootTargetDir = new File(rootTargetDirectory);
		if (!rootTargetDir.exists()) {
			boolean created = rootTargetDir.mkdirs();
			if (!created) {
				System.out.println("Error while creating directory for location- " + rootTargetDirectory);
			}
		}
		String[] fileNameParts = savedFileName.split("\\.");
		String format = fileNameParts[fileNameParts.length - 1];

		File file = new File(uploadInputFile);
		BufferedImage bufferedImage;

		InputStream in = new ByteArrayInputStream(imageDataBytes.array());
		try {
			bufferedImage = ImageIO.read(in);
			ImageIO.write(bufferedImage, format, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void getImages(String src) throws IOException {

		String folder = null;

		// Exctract the name of the image from the src attribute
		int indexname = src.lastIndexOf("/");

		if (indexname == src.length()) {
			src = src.substring(1, indexname);
		}

		indexname = src.lastIndexOf("/");
		String name = src.substring(indexname, src.length());

		System.out.println(name);

		// Open a URL Stream
		URL url = new URL(src);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		// con.setRequestProperty("User-Agent", USER_AGENT);
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
		con.setRequestProperty("Content-Type", "application/json;");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);
		con.setRequestProperty("Authorization", basicAuth);

		InputStream in = con.getInputStream();

		OutputStream out = new BufferedOutputStream(new FileOutputStream("c:/demo/pdf/images/" + name));

		for (
				int b;
				(b = in.read()) != -1;) {
			out.write(b);
		}
		out.close();
		in.close();

	}

}
