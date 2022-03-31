package pt.unl.fct.unl.gcn.pereira.individualeval.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class MediaResourceServlet extends HttpServlet {
	
	/**
	 * Download
	 * 
	 * Retrieves a file from Google Cloud Storage and returns it in the http response.
	 * If the request path is /gcs/Foo/Bar this will be interpreted as a request 
	 * to read the Google Cloud Storage file named Bar in the bucket Foo.
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Download file from a specified bucket. The request must have the form /gcs/<bucket>/<object>
		Storage storage = StorageOptions.getDefaultInstance().getService();
		
		// Parse the request URL
		Path objectPath = Paths.get(req.getPathInfo());
		if(objectPath.getNameCount() != 2) {
			throw new IllegalArgumentException("The URL is not in the correct form. Expecting /gcs/<bucket>/<object>");
		}
		// Get the bucket and the object names
		String bucketName = objectPath.getName(0).toString();
		String srcFilename = objectPath.getName(1).toString();
		
		Blob blob = storage.get(BlobId.of(bucketName, srcFilename));
		// Download object to the output stream. See Google's documentation
		blob.downloadTo(resp.getOutputStream());
	}
	
	
	/**
	 * Upload
	 */
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse  resp) throws IOException {
		// Upload a file to specified bucket. The request must have the form /gcs/<bucket>/<object>
		Path objectPath = Paths.get(req.getPathInfo());
		if(objectPath.getNameCount() != 2) {
			throw new IllegalArgumentException("The URL is not in the correct form. Expecting /gcs/<bucket>/<object>");
		}
		// Get the bucket and object from the URL
		String bucketname = objectPath.getName(0).toString();
		String srcFilename = objectPath.getName(1).toString();
		
		// Upload to Google Cloud Storage. See Google's documentation
		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of(bucketname, srcFilename);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(req.getContentType()).build();
		// The following is deprecated since it is better to upload directly to Google Cloud Storage from the client
		Blob blob = storage.create(blobInfo, req.getInputStream());
	}
	
	
}