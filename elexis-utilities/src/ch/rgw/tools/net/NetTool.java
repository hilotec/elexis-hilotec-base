// $Id: NetTool.java 3862 2008-05-05 16:14:14Z rgw_ch $
package ch.rgw.tools.net;
/* Created on 27.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

/**
 * @author Gerry
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class NetTool
{   static final String Version="1.0.0";   
    public static final java.util.ArrayList<String> IPs=new java.util.ArrayList<String>();
    public static String hostname;
   
	static
    {   Enumeration    nis=null;;
        try
        {
            nis = NetworkInterface.getNetworkInterfaces();
        
            while (nis.hasMoreElements())
            {   NetworkInterface  ni  = (NetworkInterface)nis.nextElement();
                Enumeration       ias = ni.getInetAddresses();
                while (ias.hasMoreElements())
                {   InetAddress  ia  = (InetAddress)ias.nextElement();
                    String   ip  = ia.getHostAddress();
                    hostname=ia.getHostName();
                    IPs.add(ip);
                }
            }
        }
        catch(SocketException e)
        {
            e.printStackTrace();
        }
    }
	
	// Windows only
	public static String getMacAddress() throws IOException 
	  { 
	    Process proc = Runtime.getRuntime().exec( "cmd /c ipconfig /all" ); 
	    Scanner s = new Scanner( proc.getInputStream() ); 
	    return s.findInLine( "\\p{XDigit}\\p{XDigit}(-\\p{XDigit}\\p{XDigit}){5}" ); 
	  } 
	 
	  public static void main( String[] args ) throws IOException 
	  { 
	    System.out.println( getMacAddress() ); 
	  } 
}
