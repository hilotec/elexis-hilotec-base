package ch.elexis.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * This is a basic wrapper around the sun.net.ftp.FtpClient class,  
 * which is included with Sun Java that allows you to make
 * FTP connections and file transfers.
 * <p>
 * Based on programm version 1.0 of Julian Robichaux, http://www.nsftools.com
 * 
 * @author Immi
 * @version 1.0
 */
public class FtpServer extends FTPClient {

	private void setWorkingDirectory(String serverFile) throws IOException {
		String path = UtilFile.getFilepath(serverFile);
		if (path != null && path.length() > 0) {
			changeWorkingDirectory(path);
		}
	}

	/** 
	 * Download a file from the server, 
	 * and save it to the specified local file 
	 */
	public boolean downloadFile(String remoteFilenamePath,
			String localFilenamePath) throws IOException {
		setWorkingDirectory(remoteFilenamePath);
		String remoteFile = UtilFile.getFilename(remoteFilenamePath);

		FileOutputStream localFile = null;

		try {
			localFile = new FileOutputStream(localFilenamePath);
			if (!retrieveFile(remoteFile, localFile)) {
				throw new IOException("File not received succesfully: " //$NON-NLS-1$
						+ getReplyString());
			}
		} finally {
			if (localFile != null) {
				localFile.close();
			}
		}

		return true;
	}

	/** 
	 * Upload a file to the server
	 */
	public boolean uploadFile(String remoteFilenamePath,
			String localFilenamePath) throws IOException {
		setWorkingDirectory(remoteFilenamePath);
		String remoteFile = UtilFile.getFilename(remoteFilenamePath);

		FileInputStream localFile = null;

		try {
			localFile = new FileInputStream(localFilenamePath);

			if (!storeFile(remoteFile, localFile)) {
				throw new IOException("File not sent succesfully: " //$NON-NLS-1$
						+ getReplyString());
			}
		} finally {
			if (localFile != null) {
				localFile.close();
			}
		}
		return true;
	}

	/** 
	 * Disconnect from Server
	 */
	public void disconnect() throws IOException {
		if (isConnected()) {
			super.disconnect();
		}
	}

	/** 
	 * List of filenames on ftp server
	 */
	public String[] listNames() throws IOException {
		String[] files = super.listNames();
		if (files == null) {
			return new String[0];
		}
		return files;
	}

	/** 
	 * List of files on ftp server
	 */
	public FTPFile[] listFiles() throws IOException {
		FTPFile[] files = super.listFiles();
		if (files == null) {
			return new FTPFile[0];
		}
		return files;
	}
}
