package com.wyp.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
 
public class UploadImage {
	
	/**
	 * A program that demonstrates how to upload files from local computer
	 * to a remote FTP server using Apache Commons Net API.
	 * @author www.codejava.net
	 */
	 
	    public UploadImage(String imageName) {
	        String server = "ftp.haitaosuda.com";
	        int port = 21;
	        String user = "weiDing@haitaosuda.com";
	        String pass = "dYmd2asrL2omUE";
	 
	        FTPClient ftpClient = new FTPClient();
	        try {
	 
	            ftpClient.connect(server, port);
	            ftpClient.login(user, pass);
	            ftpClient.enterLocalPassiveMode();
	 
	            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	 
	            // APPROACH #1: uploads first file using an InputStream
	            File localFile = new File("/Users/weiding/Desktop/"+imageName);
	 
	            String remoteFile = "media/import/"+imageName;
	            InputStream inputStream = new FileInputStream(localFile);
	 
	            System.out.println("Start uploading first file"+remoteFile);
	            boolean done = ftpClient.storeFile(remoteFile, inputStream);
	            inputStream.close();
	            if (done) {
	                System.out.println(remoteFile+" is uploaded successfully.");
	            }
	 
	            /*
	            // APPROACH #2: uploads second file using an OutputStream
	            File secondLocalFile = new File("E:/Test/Report.doc");
	            String secondRemoteFile = "test/Report.doc";
	            inputStream = new FileInputStream(secondLocalFile);
	 
	            System.out.println("Start uploading second file");
	            OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
	            byte[] bytesIn = new byte[4096];
	            int read = 0;
	 
	            while ((read = inputStream.read(bytesIn)) != -1) {
	                outputStream.write(bytesIn, 0, read);
	            }
	            inputStream.close();
	            outputStream.close();
	 
	            boolean completed = ftpClient.completePendingCommand();
	            if (completed) {
	                System.out.println("The second file is uploaded successfully.");
	            }*/
	 
	        } catch (IOException ex) {
	            System.out.println("Error: " + ex.getMessage());
	            ex.printStackTrace();
	        } finally {
	            try {
	                if (ftpClient.isConnected()) {
	                    ftpClient.logout();
	                    ftpClient.disconnect();
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	    }
	 
	
}
