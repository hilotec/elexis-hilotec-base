// $Id: Resource.java 1182 2006-10-29 14:48:00Z rgw_ch $

package ch.rgw.IO;
import java.io.*;
import java.awt.*;

import ch.rgw.tools.ExHandler;
import ch.elexis.util.Log;

/**
 * �berschrift:   Toolbox
 * Beschreibung:
 * Copyright:     Copyright (c) 2002
 * Organisation:  rgw
 * @author G. Weirich
 * @version 1.0
 */

public class Resource {
  public static final String Version(){return Messages.getString("Resource.0");} //$NON-NLS-1$
  Class clazz;
  String resbase;
  static Log log;
  String basedir; 

  static{
    log=Log.get(Messages.getString("Resource.1")); //$NON-NLS-1$
  }
  public Resource(String packagename)
  { clazz=getClass();
    resbase=Messages.getString("Resource.2")+packagename.replace('.','/')+Messages.getString("Resource.3"); //$NON-NLS-1$ //$NON-NLS-2$
    log.log(Messages.getString("Resource.createResource")+resbase,Log.DEBUGMSG); //$NON-NLS-1$
  }
  public InputStream getInputStream(String name)
  { String resname=resbase+name;
    InputStream is=clazz.getResourceAsStream(resname);
    if(is==null)
    { log.log(Messages.getString("Resource.cantOpenInput"),Log.ERRORS); //$NON-NLS-1$
      return null;
    }
    return is;
  }
  public String getText(String name)
  { InputStream is=getInputStream(name);
    StringBuffer sb=new StringBuffer();
    int c;
    try{
      while((c=is.read())!=-1)
      { sb.append((char)c);
      }
      is.close();
    }
    catch(Exception ex)
    {   ExHandler.handle(ex);
        return null;
    }
    return sb.toString();
  }
  public byte[] getBytes(String name)
  { InputStream is=getInputStream(name);
    byte[] buffer=new byte[0];
    byte[] tmpbuf=new byte[1024];
    int len;
    try{
      while((len=is.read(tmpbuf))>0)
      { byte[] newbuf=new byte[buffer.length+len];
        System.arraycopy(buffer,0,newbuf,0,buffer.length);
        System.arraycopy(tmpbuf,0,newbuf,buffer.length,len);
        buffer= newbuf;
      }
      is.close();
    }
    catch(Exception ex)
    { ExHandler.handle(ex);
      return null;
    }
    log.log(Messages.getString("Resource.6")+buffer.length+Messages.getString("Resource.7"),Log.DEBUGMSG); //$NON-NLS-1$ //$NON-NLS-2$
    return buffer;
  }
  public Image getImage(String name)
  { byte[] buffer=getBytes(name);
    Image ret=Toolkit.getDefaultToolkit().createImage(buffer);
    return ret;
  }
  
  public java.net.URL getBaseDir(String rsc)
  {	String p=Messages.getString("Resource.8")+rsc.replace('.','/'); //$NON-NLS-1$
  	return clazz.getResource(p);
  }
}