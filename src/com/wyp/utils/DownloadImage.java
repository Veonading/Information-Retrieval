package com.wyp.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImage {

	public DownloadImage (String imageUrl, String imageName) throws IOException {
		URL url = new URL(imageUrl);
		InputStream is = null;
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.connect();
		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    // everything ok
		    is = connection.getInputStream();
		    
		    OutputStream os = new FileOutputStream("/Users/weiding/Desktop/product_images/"+imageName);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}
			os.close();
			is.close();
		}  

	}

}