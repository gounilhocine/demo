Excel to pdf:
https://github.com/nakazawaken1/Excel-To-PDF-with-POI-and-PDFBox

Merge word doc:
https://stackoverflow.com/questions/11592028/merging-ms-word-documents-with-java

Upload + download file:
https://dzone.com/articles/java-springboot-rest-api-to-uploaddownload-file-on


commande pour upload un fichier et download en meme temps 
 curl  -i -X GET  "http://localhost:8080/download?param=C:/demo/indezer/upload/1.docx" -o C:/demo/indezer/upload/111.d
ocx


Voici le code java:


package se.ivankrizsan.restexample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

@RestController
public class Controller {

	private static String UPLOADED_FOLDER = "c:/demo/indezer/upload/";

	@PostMapping("/rest/upload")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) throws FileNotFoundException, IOException {
		if (uploadfile.isEmpty()) {
			return new ResponseEntity("You must select a file!", HttpStatus.OK);
		}
		String filename = "c:/demo/indezer/output.doc";
		File file = new File(filename);
		String result;
		if (!uploadfile.isEmpty()) {
			try {
				try (OutputStream os = new FileOutputStream(file)) {
					os.write(uploadfile.getBytes());
					os.close();
				}
				result = "You successfully uploaded " + uploadfile.toString() + " into " + uploadfile.toString() + "-uploaded !";
			} catch (Exception e) {
				result = "You failed to upload " + uploadfile.toString() + " => " + e.getMessage();
			}
		} else {
			result = "You failed to upload " + uploadfile.toString() + " because the file was empty.";
		}
		return new ResponseEntity(result + uploadfile.getOriginalFilename(), new HttpHeaders(), HttpStatus.OK);
	}

	@RequestMapping(value = "/test", method = {RequestMethod.GET})
	public ResponseEntity<?> test() { return new ResponseEntity("Successfully uploaded", new HttpHeaders(), HttpStatus.OK); }

	// multiple upload
	/**
	 * @param metaData
	 * @param uploadfiles
	 * @return
	 */
	@RequestMapping(value = "/multipleupload", method = RequestMethod.POST)
	public ResponseEntity uploadFile(@RequestPart(required = false) String metaData, @RequestPart("file") MultipartFile[] uploadfiles) {
		// Get file name
		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename()).filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity("please select a file!", HttpStatus.OK);
		}
		try {
			saveUploadedFiles(Arrays.asList(uploadfiles));
		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity("Successfully uploaded - " + uploadedFileName, HttpStatus.OK);
	}

	// file download
	/**
	 * @param param
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(path = "/download", method = RequestMethod.GET)
	public ResponseEntity<Resource> download(String param, HttpServletRequest request, HttpServletResponse response) {

		HttpHeaders headers = new HttpHeaders();
		ByteArrayResource resource = null;
		File file = null;
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			try {
				System.out.println(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
				file = new File(param);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				Path path = Paths.get(file.getAbsolutePath());
				resource = new ByteArrayResource(Files.readAllBytes(path));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
	}

	@RequestMapping(path = "/add-barcode-to-pdf", method = RequestMethod.GET)
	public ResponseEntity<Resource> addingBarCode(@RequestParam("file") MultipartFile pdfFile, @RequestParam("code") String code, @RequestParam("x") Long x, @RequestParam("y") Long y)
			throws InvalidPasswordException, IOException {
		if (pdfFile.isEmpty()) {
			return new ResponseEntity("You must select a file!", HttpStatus.OK);
		}
		String filename = "c:/demo/indezer/upload/output.pdf";
		File file = new File(filename);
		String result;
		if (!pdfFile.isEmpty()) {
			try {
				try (OutputStream os = new FileOutputStream(file)) {
					os.write(pdfFile.getBytes());
					os.close();
				}
				result = "You successfully uploaded " + pdfFile.toString() + " into " + pdfFile.toString() + "-uploaded !";
			} catch (Exception e) {
				result = "You failed to upload " + pdfFile.toString() + " => " + e.getMessage();
			}
		} else {
			result = "You failed to upload " + pdfFile.toString() + " because the file was empty.";
		}

		// Add barcode
		// Loading an existing document
		PDDocument doc = PDDocument.load(file);

		// Retrieving the page
		PDPage page = doc.getPage(0);

		// Creating PDImageXObject object
		PDImageXObject pdImage = PDImageXObject.createFromFile("c:/demo/indezer/upload/barcode.png", doc);

		// creating the PDPageContentStream object
		PDPageContentStream contents = new PDPageContentStream(doc, page);

		// Drawing the image in the PDF document
		contents.drawImage(pdImage, x, y);
		System.out.println("Image inserted");

		// Closing the PDPageContentStream object
		contents.close();

		// Saving the document
		doc.save("c:/demo/indezer/upload/output.pdf");

		// Closing the document
		doc.close();

		return new ResponseEntity("Successfully add ", HttpStatus.OK);
	}

	// save file
	/**
	 * @param files
	 * @throws IOException
	 */
	private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

		for (MultipartFile file : files) {

			if (file.isEmpty()) {
				continue; // next pls
			}

			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
			Files.write(path, bytes);

		}

	}

}

