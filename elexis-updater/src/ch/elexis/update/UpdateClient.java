package ch.elexis.update;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ch.elexis.util.Log;


/*
 * Created on 16-Dec-2006
 * @author Matthew Wilson
 *
 */

/**
 * Class implementing a web update client.  Communicates with the server via HTTP POST requests.
 * To perform a check, the client POST method=check and filename=<filename>. The server responds with return value.
 * To download, the client POSTs as above except sets method=check. The server responds with a 0 followed by a bytestream corresponding to the file,
 * or a 1 which indicates an error occurred.
 * 
 */
public class UpdateClient{
	Log log=Log.get("UpdateClient");
	/** File does not exist on server */
	public static final int FILE_INEXISTENT=0;
	/** File on server is older than local file */
	public static final int FILE_OLDER=1;
	/** local file and remote file have same version */
	public static final int FILE_SAME=2;
	/** remote file is a major update */
	public static final int FILE_MAJOR=6;
	/** remote file is a minor update */
	public static final int FILE_MINOR=5;
	/** remote file is a newer revision */
	public static final int FILE_REVISION=4;
	/** remote file is a newer build */
	public static final int FILE_BUILD=3;

	/**
     * Maximum length of the filename that can be read from the server.
     */
    private static final int MAX_FILENAME_LENGTH = 255;
    
    /* (non-Javadoc)
     * @see IWebUpdate#checkUpdate(java.lang.String, java.lang.String)
     * @throws MalformedURLException if the URL passed is invalid
     * @throws IOException if a problem occurs reading from the server
     */
    public int checkUpdate(String filename, String hostaddress) throws IOException, MalformedURLException {
        
        // Connect to server
        URLConnection conn = (new URL(hostaddress)).openConnection();
        
        // Get output stream
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        
        // POST requet
        out.write("method=check&filename=" + filename);
        out.close();
        
        // Read response
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output = in.readLine();
        in.close();
        
        
        if (output == null) {
            return 0;
        } else {
        	try{
        		return Integer.parseInt(output);
        	}catch(Exception ex){
        		log.log("Unexpected response "+output, Log.WARNINGS);
        		return 0;
        	}
        }
    }
    
    /* (non-Javadoc)
     * @see IWebUpdate#download(java.lang.String, java.lang.String)
     * Returns true if the same number bytes are written as read from the server 
     * (indicating a successful download)
     * Returns false if the server does not offer a file (due to an error), or if
     * the number of bytes written is different to the number read from the server.
     * @throws FileNotFoundException if the directory does not exist
     * @throws MalformedURLException if the URL supplied is invalid
     * @throws IOException if there is a problem reading from the server or writing the file
     */
    public File download(String filename, String destination, String hostaddress) throws IOException, MalformedURLException {
        //      Connect to server
        URLConnection conn = (new URL(hostaddress)).openConnection();
        
        // Get output stream
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        
        // POST request
        out.write("method=download&filename=" + filename);
        out.close();
        
        // Read response and write to file
        InputStream in = conn.getInputStream();
        
        
        byte[] filenameBytes = new byte[MAX_FILENAME_LENGTH];
        int count = 0;
        int chunk = in.read();
        
        if (chunk != 48) {
            return null;
        } else { 
           // Read the filename, delimited by a pipe
            while((chunk = in.read()) != 124) {
                filenameBytes[count] = (byte)chunk;
                count++;
            }
            
            File destFile = new File(destination + File.separator + new String(filenameBytes,0,count));
            FileOutputStream tofile = new FileOutputStream(destFile);
            count = 0;
            while ((chunk = in.read()) != -1) {
                tofile.write(chunk);
                count++;
            }
            tofile.close();
            
            
            if (destFile.length() == count) {
                return destFile;
            } else {
                // We haven't written the same number of bytes that we read, something's gone wrong
                return null;
            }
        }
    }
}

