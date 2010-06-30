/*******************************************************************************
 * Copyright (c) 2010, St. Schenk and Medshare GmbH
 *
 * Login EBM by HTTP POST
 *
 *******************************************************************************/

package ch.medshare.ebm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

import ch.elexis.Hub;

public class EbmLogIn
{
    private static final String POST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private String x = "";
    private String g = "";

    public static void main (String args[]) { }
    
    
    public String doPostLogin(String suchstring){
    	HttpClient httpClient = new HttpClient();
    
    	String url = Hub.userCfg.get(Preferences.URL, Preferences.Defaults.URL);
    	PostMethod post = new PostMethod(url);
    
    	post.addParameter("USER",Hub.userCfg.get(Preferences.USER, Preferences.Defaults.USER));
    	post.addParameter("PASS",Hub.userCfg.get(Preferences.PASS, Preferences.Defaults.PASS));
    	if(!suchstring.isEmpty()){
    		post.addParameter("suchstring", suchstring);
    	}

    	try {
			httpClient.executeMethod(post);
		} catch (HttpException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		    
	    try {
			x = post.getResponseBodyAsString();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
		}
    
    final Header l = post.getResponseHeader("location");
    if(l != null) {
    	g = l.getValue();
    }
    else{
    	g = x;
    }

    post.releaseConnection();

    return g;
}


 

    /**
     * Makes a HTTP POST to the target URL by using an HttpURLConnection.
     *
     * @param targetUrl
     * The URL to which the HTTP POST is made.
     *
     * @param content
     * The contents which will be POSTed to the target URL.
     *
     * @return
     * The open URLConnection which can be used to read any response.
     *
     * @throws IOException
     */
    public HttpURLConnection doHttpPost(String targetUrl) throws IOException
    {
        HttpURLConnection urlConnection = null;
        DataOutputStream dataOutputStream = null;
        try
        {
            // Open a connection to the target URL
            // throws IOException
            urlConnection = (HttpURLConnection)(new URL(targetUrl).openConnection());

            // Specifying that we intend to use this connection for input
            urlConnection.setDoInput(true);

            // Specifying that we intend to use this connection for output
            urlConnection.setDoOutput(true);

            // Specifying the content type of our post
            urlConnection.setRequestProperty("Content-Type", POST_CONTENT_TYPE);

            // Specifying the method of HTTP request which is POST
            // throws ProtocolException
            urlConnection.setRequestMethod("POST");

            // Prepare an output stream for writing data to the HTTP connection
            // throws IOException
            dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());

            // throws IOException
            //dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();

            return urlConnection;
        }
        catch(IOException ioException)
        {
            System.out.println("I/O problems while trying to do a HTTP post.");
            ioException.printStackTrace();

            // Good practice: clean up the connections and streams
            // to free up any resources if possible
            if (dataOutputStream != null)
            {
                try
                {
                    dataOutputStream.close();
                }
                catch(Throwable ignore)
                {
                    // Cannot do anything about problems while
                    // trying to clean up. Just ignore
                }
            }
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }

            // throw the exception so that the caller is aware that
            // there was some problems
            throw ioException;
        }
    }   
}
