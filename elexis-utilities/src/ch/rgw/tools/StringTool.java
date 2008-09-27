// $Id: StringTool.java 4442 2008-09-25 20:30:29Z rgw_ch $

package ch.rgw.tools;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.compress.bzip2.CBZip2OutputStream;

import ch.rgw.compress.CompEx;
import ch.rgw.compress.GLZInputStream;
import ch.rgw.compress.GLZOutputStream;
import ch.rgw.compress.HuffmanInputStream;
import ch.rgw.compress.HuffmanOutputStream;
import ch.rgw.compress.HuffmanTree;
import ch.rgw.tools.net.NetTool;
/**
 * Einige Hilfsfunktionen mit und an Strings und String-Collections
 * @author Gerry Weirich
 */

public class StringTool
{
  
  public static final String Version(){return "2.0.0";}
  public static final String default_charset="utf-8";
  public static final String leer="";
  public static final String space=" ";
  public static final String equals="=";
  public static final String crlf="\r\n";
  public static final String wordSeparators="[\\t ,\\.:\\?!\\n\\r]";
  public static final String lineSeparators="[\\n\\r\\.\\?!]";
  //public static final String wordChars="a-zA-ZäöüÄÖÜéèàâê\'";
  public static final String wordChars="\\p{L}\'";
  private static int ipHash;
  private static long sequence;
  public static final int LEFT=1;
  public static final int RIGHTS=2;
  
  public static String createString(byte[] bytes){
	  try {
		return new String(bytes,default_charset);
	} catch (UnsupportedEncodingException e) {
		// should not happen
		ExHandler.handle(e);
	}
	return null;
  }
  
  public static byte[] getBytes(String string){
	  try {
		return string.getBytes(default_charset);
	} catch (UnsupportedEncodingException e) {
		// should not happen
		ExHandler.handle(e);
	}
	return null;
  }
  /*public StringTool(String s)
  { mine=new String(s);
  }*/
  public static Rectangle2D getStringBounds(final String s, final Graphics g)
  { if(isNothing(s)){
      return new Rectangle(0,0);
  	}
    FontRenderContext frc=((Graphics2D)g).getFontRenderContext();
    Font fnt=g.getFont();
    Rectangle2D r=fnt.getStringBounds(s,frc);
    return r;  
  }
  
  /**
   * Split a String into a String Arry
   * @deprecated obsoleted by java 1.4x 's {@link String#split(String) String.split} method.
   */
  @Deprecated
@SuppressWarnings("unchecked")
public static String[] split(final String m, final String delim)
  {   Vector v=splitV(m,delim);
      if(v ==null) {
		return null;
	}
      String[] ret=(String[])v.toArray(new String[1]);
        return ret;
  }
  /** Spaltet einen String in einen Vektor
   * @param m der zu splittende String
   * @param delim Trennzeichen, an dem zu splitten ist.
   */
  
  @SuppressWarnings("unchecked")
public static Vector splitV(final String m, final String delim)
  {   String mi=m;
      if(mi.equals("")) {
		return null;
	}
      Vector v=new Vector(30,30);
    
      int i=0,j=0;
      while(true)
      { j=mi.indexOf(delim,i);
        if(j==-1)
        { v.add(mi.substring(i));
          break;
        }
        String l=mi.substring(i,j).trim();
        if(!l.equals("")) {
			v.add(l);
		}
            i=j+1;
          }
  
    return v;  
  }
  @SuppressWarnings("unchecked")
public static List<String> splitAL(final String m, final String delim)
  { ArrayList al=new ArrayList();
    String mi=m;
    int i=0,j=0;
    while(true)
    { j=mi.indexOf(delim,i);
        if(j==-1)
        { al.add(mi.substring(i));
          break;
        }
        String l=mi.substring(i,j).trim();
        if(!l.equals("")) {
			al.add(l);
		}
        i=j+1;
     }
     return al;
  }
  /** Wandelt eine Hashtable in einen String aus Kommagetrennten
   *  a=b-Paaren um.
   */
  public static final String flattenSeparator="~#<";
  @SuppressWarnings("unchecked")
public static String flattenStrings(final Hashtable h)
  {
  	return flattenStrings(h,null);
  }
  public static String flattenStrings(final Hashtable<Object,Object> h, final flattenFilter fil)
  { if(h==null) {
	return null;
}
  	Enumeration<Object> keys=h.keys();
    StringBuffer res=new StringBuffer(1000);
    res.append("FS1").append(flattenSeparator);
    while(keys.hasMoreElements())
    {   Object ko=(keys.nextElement());
    	if(fil!=null) {
    		if(fil.accept(ko)==false) {
    			continue;
    		}
    	}
        String v=ObjectToString(h.get(ko));
        String k=ObjectToString(ko);
        if( (k==null) || (v==null) || k.matches(".*=.*"))
        {  //log.log("attempt to flatten unsupported object type",Log.FATALS);
           return null;
        }
        res.append(k).append("=").append(v).append(flattenSeparator);
    }
    String r=res.toString();
    return r.replaceFirst(flattenSeparator+"$","");
  }
  
  public static final int NONE=0;
  public static final int HUFF=1;
  public static final int BZIP=2;
  public static final int GLZ=3;
  public static final int ZIP=4;
  public static final int GUESS=99;

  /**
   * Eine String-Collection comprimieren
   * @param strings
   * @param compressMode
   * @return
   */
  public static byte[] pack(final Collection<String> strings){
	  String res=join(strings,"\n");
	  return CompEx.Compress(res,CompEx.ZIP);
  }
  
  public static byte[] pack(final String[] strings){
	  String res=join(strings,"\n");
	  return CompEx.Compress(res,CompEx.ZIP);
  }
  
  public static List<String> unpack(final byte[] pack){
	  try{
		  String raw=new String(CompEx.expand(pack),default_charset);
		  return splitAL(raw,"\n");
	  }catch(Exception ex){
		  return null; // Sollte sowieso nie vorkommen
	  }
	  
  }
  @SuppressWarnings("unchecked")
public static byte[] flatten(final Hashtable hash){
      try{
    	  ByteArrayOutputStream baos=new ByteArrayOutputStream(hash.size()*30);
          ZipOutputStream zos=new ZipOutputStream(baos);
       	  zos.putNextEntry(new ZipEntry("hash"));
       	  ObjectOutputStream oos=new ObjectOutputStream(zos);
          oos.writeObject(hash);
		  zos.close();
		  baos.close();
          return baos.toByteArray();
      }catch(Exception ex){
          ExHandler.handle(ex);
          return null;
      }
  }
  /**
   * Eine Hashtable in ein komprimiertes Byte-Array umwandeln
   * @param hash	die Hashtable
   * @param compressMode	GLZ, HUFF, BZIP2
   * @param ExtInfo	Je nach Kompressmode nötige zusatzinfo
   * @return das byte-Array mit der komprimierten Hashtable
   */
  @SuppressWarnings("unchecked")
  @Deprecated
public static byte[] flatten(final Hashtable hash, final int compressMode, final Object ExtInfo)
  {	  ByteArrayOutputStream baos=null;
      OutputStream os=null;
  	  ObjectOutputStream oos=null;
      try{
          baos=new ByteArrayOutputStream(hash.size()*30);
          switch (compressMode) {
          	case GUESS:
       		case ZIP:
       			os=new ZipOutputStream(baos);
       			((ZipOutputStream)os).putNextEntry(new ZipEntry("hash"));
       			break;
          	case BZIP: os=new CBZip2OutputStream(baos);break;
          	case HUFF: os=new HuffmanOutputStream(baos,(HuffmanTree)ExtInfo,0); break;
          	case GLZ:  os=new GLZOutputStream(baos,hash.size()*30); break;
          	default:
          		os=baos;
          }
          
          oos=new ObjectOutputStream(os);
          oos.writeObject(hash);
		  if(os!=null) {
			os.close();
		}
		  baos.close();
          return baos.toByteArray();
      }catch(Exception ex){
          ExHandler.handle(ex);
          return null;
      }
  }
  
  @SuppressWarnings("unchecked")
public static Hashtable fold(final byte[] flat){
      try{
          ByteArrayInputStream bais=new ByteArrayInputStream(flat);
        	ZipInputStream zis=new ZipInputStream(bais); 
			zis.getNextEntry();
			ObjectInputStream ois=new ObjectInputStream(zis);
			Hashtable<Object,Object> res=(Hashtable<Object,Object>)ois.readObject();
			ois.close();
			bais.close();
			return res;
      }catch(Exception ex){
          ExHandler.handle(ex);
          return null;
      }
  }
  /** Ein mit flatten() erzeugtes Byte-Array wieder in eine HAshtable zurückverwandeln
   * 
   * @param flat Die komprimierte Hashtable
   * @param compressMode Expnad-Modus
   * @param ExtInfo
   * @return die Hastbale
   */
  @SuppressWarnings("unchecked")
  @Deprecated
public static Hashtable fold(final byte[] flat, final int compressMode, final Object ExtInfo)
  {	
      ObjectInputStream ois=null;
      try{
          ByteArrayInputStream bais=new ByteArrayInputStream(flat);
          switch (compressMode) {
          	case BZIP:  ois=new ObjectInputStream(new CBZip2InputStream(bais)); break;
          	case HUFF:  ois=new ObjectInputStream(new HuffmanInputStream(bais));	break;
          	case GLZ:	ois=new ObjectInputStream(new GLZInputStream(bais)); break;
          	case ZIP:	ZipInputStream zis=new ZipInputStream(bais); 
					zis.getNextEntry();
					ois=new ObjectInputStream(zis);
					break;
          	case GUESS:
          		Hashtable<Object,Object> res=fold(flat,ZIP,null);
          		if(res==null){
          			res=fold(flat,GLZ,null);
          			if(res==null){
          				res=fold(flat,BZIP,null);
          				if(res==null){
          					res=fold(flat,HUFF,ExtInfo);
          					if(res==null){
          						return null;
          					}
          				}
          			}
          		}
          		return res;
          	default:
          		ois=new ObjectInputStream(bais);
          	break;
          }
          
          Hashtable<Object,Object> res=(Hashtable<Object,Object>)ois.readObject();
          ois.close();
          bais.close();
          return res;
      }catch(Exception ex){
          ExHandler.handle(ex);
          return null;
      }
      
  }
  
  static String ObjectToString(final Object o)
  {	
      if(o instanceof String){
          return "A"+(String)o;
      }
      if(o instanceof Integer){
          return "B"+((Integer)o).toString();
      }
      if(o instanceof Serializable){
          ByteArrayOutputStream baos=new ByteArrayOutputStream();
          ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            byte[] ret=baos.toByteArray();
            return "Z"+enPrintable(ret);
            
        } catch (IOException e) {
            ExHandler.handle(e);
            return null;
        }
      }
      return null;
  }
  static Object StringToObject(final String s)
  {
      String sx=s.substring(1);
      char pref=s.charAt(0);
      switch(pref)
      {	case 'A': return sx;
      	case 'B': return(new Integer(Integer.parseInt(sx)));
      	case 'Z':
      	  byte[] b=dePrintable(sx);
          try{
              ByteArrayInputStream bais=new ByteArrayInputStream(b);
              ObjectInputStream ois=new ObjectInputStream(bais);
              Object ret=ois.readObject();
              ois.close();
              bais.close();
              return ret;
          }
          catch(Exception ex){
              ExHandler.handle(ex);
              return null;
          }
      }
      return null;
  }
  @SuppressWarnings("unchecked")
public static Hashtable foldStrings(final String s)
  { Hashtable h=new Hashtable();
    if(StringTool.isNothing(s)) {
		return h;
	}
    String[] elems=s.split(flattenSeparator);
    if(!elems[0].equals("FS1")){
        return null;
    }
    for(int i=1;i<elems.length;i++)
    {   String[] elem=elems[i].split("=",2);
        if(elem.length!=2)
        { //log.log("Fehler in Hash-Repr�sentation",Log.ERRORS);
          return null;  
        }
        Object k=StringToObject(elem[0].trim());
        Object v=StringToObject(elem[1].trim());
        if((k==null) || (v==null)){
            return null;
        }
        h.put(k,v);
    }
    return h;  
  }
  /** gibt true zurück, wenn das Objekt kein String oder null oder "" ist */
  static public boolean isNothing(final Object n)
  { if(n==null) {
	return true;
}
    if(n instanceof String)
    {   //if(((String)n).equals("")) return true;
        if(((String)n).trim().equals("")) {
			return true;
		}
        return false;
    }
    return true;
  }
  
  /** Gibt true zurück, wenn das Feld null ist, leer ist, oder nur Leerstrings
   * enthält
   */
  static public boolean isEmpty(final String[] f){
      if(f==null){
          return true;
      }
      for(int i=0;i<f.length;i++){
          if(!isNothing(f[i])){
              return false;
          }
      }
      return true;
  }
    /** Verleicht zwei byte-Arrays */
   static public boolean compare(final byte[] a, final byte[] b)
   { if(a.length==b.length)
     { for(int i=0;i<a.length;i++)
       { if(a[i] != b[i]) {
		return false;
	}
       }
       return true;
     }
     return false;
   }
   /**
    * Sucht einen String in einem String-Array und gibt dessen Index zurück.
    * Die Suche erfolgt ohne Berücksichtigung von Gross/Kleinschreibung.
    * @return den index von val in arr oder -1 wenn nicht gefunden.
    */
    static public int getIndex(final String[] arr,final String val)
    { for(int i=0;i<arr.length;i++)
      { if(val.equalsIgnoreCase(arr[i])) {
		return i;
	}
      }
      return -1;
    }
    /**
     * Verlängert oder kürzt einen String.
     * @param where SWT.LEFT vorne füllen, SWT.RIGHT hinten füllen
     * @param chr Zeichen zum Füllen
     * @param src Quellstring
     * @param size erwünschte Länge
     * @return	der neue String
     */
    static public String pad(final int where, final char chr, final String src, final int size)
    { int diff=size-src.length();
      if(diff>0)
      { StringBuffer s=new StringBuffer(diff);
        for(int i=0;i<diff;i++)
        { s.append(chr);
        }
        if(where==LEFT)
        { return s+src;
        }
        return src+s;
      }
      return src.substring(0,size);
    }
    /**
     * Erstellt einen String aus mehreren nacheinander folgenden Strings
     * @param str der zu multiplizierende string
     * @param num Zahl der Multiplikationen
     */
	static public String filler(final String str, int num)
	{	StringBuffer s=new StringBuffer(num);
		while(num-->0)
		{	s.append(str);
		}
		return s.toString();
	}

    /**
     * Verknüpft die Elemente eines String-Arrays mittels tren zu einem
     * String
     * @param arr - String Array
     * @param tren - Verbindingsstring
     * @return den verknüpften String
     */
    static public String join(final String[] arr,final String tren)
    { if( (arr==null) || (arr.length==0)) {
		return "";
	}
      StringBuffer res=new StringBuffer(100);
      for(int i=0;i<arr.length;i++)
      { if(arr[i]==null) {
		continue;
	}
        res.append(arr[i]).append(tren);
      }
      String r2=res.toString();
      return r2.replaceFirst(tren+"$","");
    }

    /*
    static public String join(Vector v, String tren)
    { if( (v==null) || (v.size()==0)) return "";
      StringBuffer res=new StringBuffer(100);
      for(int i=0;i<v.size();i++)
      { res.append(v.get(i)).append(tren);
      }
      String r2=res.toString();
      return r2.replaceFirst(tren+"$","");
    }*/
    public static String join(final Iterable<String> i, final String tren){
    	StringBuilder ret=new StringBuilder();
    	Iterator<String> it=i.iterator();
    	while(it.hasNext()){
    		ret.append(it.next());
    		if(it.hasNext()){
    			ret.append(tren);
    		}
    	}
    	/*
		StringBuffer ret=new StringBuffer(100);
		for(String s:i){
			ret.append(s).append(tren);
		}
		if(ret.length()>(tren.length()+1)){
			ret.delete(ret.length()-tren.length(),20000);
		}
		*/
		return ret.toString();
	}
/*
    static public String join(java.util.List al, String tren)
    { if( (al==null) || (al.size()==0)) return "";
        StringBuffer res=new StringBuffer(100);
        for(int i=0;i<al.size();i++)
        { res.append(al.get(i)).append(tren);
        }
        String r2=res.toString();
        return r2.replaceFirst(tren+"$","");
    }
    */
    private static final int offset=70;
    /**
     * Wandelt ein Byte-Array in einen druckbaren String um. (Alle Bytes werden in
     * ihre Nibbles zerlegt, diese werden ähnlich wie mit base64 als Zeichen gespeichert
     */
    static public String enPrintable(final byte[] src)
    { // if()
    	byte[] out=new byte[src.length*2];
      for(int i=0;i<src.length;i++)
      { out[2*i]=(byte)((src[i]>>4)+offset);
        out[2*i+1]=(byte)((src[i]&0x0f)+offset);
      }
      try{
      return new String(out,default_charset);
      }
      catch(Exception ex)
      { ExHandler.handle(ex);
        return null;
      }
    }
    /**
     * Wandelt einen mit enPrintable erhaltenen String in ein byte-Array zurück.
     */
    static public byte[] dePrintable(final String src)
    { byte[] input=null;
      try{
        input=src.getBytes(default_charset);
      }
      catch(Exception ex)
      { ExHandler.handle(ex);
        return null;
      }
      byte[]out=new byte[input.length/2];
      for(int i=0;i<out.length;i++)
      { out[i]=(byte)((input[2*i]-offset)*16+(input[2*i+1]-offset));
      }
      return out;
    }
    /** 
       *  Gibt eine zufällige und eindeutige Zeichenfolge zurück
       *  @param salt Ein beliebiger String oder null
       */
      public static String unique(final String salt)
      { if(ipHash==0)
        {   Iterator<String> it=NetTool.IPs.iterator();
            while(it.hasNext()) {
				ipHash+=((String)it.next()).hashCode();
			}
        }
       
      	long t=System.currentTimeMillis();
        int t1=System.getProperty("user.name").hashCode();
        long t2=((long)ipHash)<<32;
        long t3=Math.round(Math.random()*Long.MAX_VALUE);
        long t4=t+t1+t2+t3;
    	if(salt!=null)
    	{	long t0=salt.hashCode();
    		t4^=t0;
    	}
    	t4+=sequence++;
        return Long.toHexString(t4)+Long.toHexString((long)(Math.random()*1000))+sequence;
      }
      
     public static String unNull(final String in){
    	 return (in==null)?"":in;
     }
    /**
     * Dem StreamTokenizer nachempfundene Klasse, die auf einem String arbeitet.
     * Kann gequotete und geklammerte ausdrücke als token zusammenfassen.
     * Wirft exceptions bei unmatched quotes oder klammern.
     * @author Gerry Weirich
     *
     */
    static public class tokenizer
    {   /** Betrachte in " eingeschlossene Phrasen als ein token */
        public static final int DOUBLE_QUOTED_TOKENS=1;
        /** Betrachte in ' eingeschlossene Phrasen als ein token */
        public static final int SINGLE_QUOTED_TOKENS=2;
        /** 
         * In () geklammerte phrasen als ein token betrachten. Verschachtelte
         * Klammern werden unverändert übernommen
         */
        public static final int ROUND_BRACKET_TOKENS=4;
        /** In [] geklammerte Phrasen als ein token betrachten */
        public static final int EDGE_BRACKET_TOKENS=8;
        /** in {} geklammerte Phrasen als ein token betrachten */
        public static final int CURLY_BRACKET_TOKENS=16;
        /** Zeilenende bricht token ab */
        public static final int CRLF_MATTERS=32;
        private final String delim;
        private final int mode;
        private int pos;
        private final String mine;
        
        /**
         * Einziger Konstruktor
         * @param m der Quellstring
         * @param delim Zeichen, die als Tokengrenze betrachtet werden
         * @param mode OR-Kombination der obigen Token-Konstanten
         */
        public tokenizer(final String m, final String delim,final int mode)
        {   mine=m;
            this.delim=delim;
            this.mode=mode;
            pos=0;
        }
        /** Splittet den String auf und liefert die tokens als List */
        @SuppressWarnings("unchecked")
		public List<String> tokenize() throws IOException
        { 
            ArrayList ret=new ArrayList();
            StringBuffer token=new StringBuffer();
            while(pos<mine.length())
            {   char c=mine.charAt(pos++);
                if(delim.indexOf(c)!=-1)
                {   ret.add(token.toString());
                    token.setLength(0);
                    continue;
                }
                token.append(c);
                switch(c)
                {   case '\"':
                        if((mode&DOUBLE_QUOTED_TOKENS)!=0)
                        {   token.append(readToMatching('\"','\"'));
                        }
                        break;
                    case '\'':
                        if((mode&SINGLE_QUOTED_TOKENS)!=0)
                        {   token.append(readToMatching('\'','\''));
                        }
                        break;
                    case '(':
                        if((mode&ROUND_BRACKET_TOKENS)!=0)
                        {   token.append(readToMatching('(',')'));
                        }
                        break;
                    case ')':
                        if((mode&ROUND_BRACKET_TOKENS)!=0)
                        {   throw new IOException("unmatched bracket");
                        }
                        break;
                    case '[':
                        if((mode&EDGE_BRACKET_TOKENS)!=0)
                        {   token.append(readToMatching('[',']'));
                        }
                        break;
                    case ']':
                        if((mode&EDGE_BRACKET_TOKENS)!=0)
                        {   throw new IOException("unmatched bracket");
                        }
                        break;
                    case '{':
                        if((mode&CURLY_BRACKET_TOKENS)!=0)
                        {   token.append(readToMatching('{','}'));
                        }
                        break;
                    case '}': 
                        if((mode&CURLY_BRACKET_TOKENS)!=0)
                        {   throw new IOException("unmatched bracket"); 
                        }
                }
            }
            ret.add(token.toString());
            return ret;
        }
        private StringBuffer readToMatching(final char open,final char close) throws IOException
        {   StringBuffer ret=new StringBuffer();
            int level=1;
            while(pos<mine.length())
            {   char c=mine.charAt(pos++);
                ret.append(c);
                if(c==close)
                {   if(--level==0)
                    {  return ret; 
                    }
                }
                else if(c==open)
                {   level++;
                }
                else if(c=='\r')
                {   if((mode&CRLF_MATTERS)!=0)
                    {   throw new IOException("Unexpected end of line while looking for "+close);
                    }
                }
            }
            throw new IOException("Unexpected end of line while looking for "+close);
        }
      }
    public interface flattenFilter{
    	boolean accept(Object key);
    }
    /**
     * Versucht herauszufinden, ob ein Name weiblich ist
     * @param name der Name
     * @return true wenn der Name vielleicht weiblich ist
     */
    public static boolean isFemale(final String name){
    	if(isNothing(name)){
    		return false;
    	}
    	final String[] suffices={"a","is","e","id"};
    	for(String s:suffices){
    		if(name.endsWith(s)){
    			return true;
    		}
    	}
    	return false;
    }
    
    public static boolean isMailAddress(final String in){
    	return in.matches("\\w[\\w|\\.\\-]+@\\w[\\w\\.\\-]+\\.[a-zA-Z]{2,4}");
    	// oder \w[\w|\.\-]+@\w[\w\.\-]+\.[a-zA-Z]{2,4}
    }
    
    /**
     * Return the first word of the given String
     */
    public static String getFirstWord(final String in){
    	if(isNothing(in)){
    		return "";
    	}
    	String[] words=in.split(wordSeparators);
    	return words[0];
    }
    
    /**
     * Return the first line if the given String but at most maxChars
     */
    public static String getFirstLine(final String in, final int maxChars){
    	if(isNothing(in)){
    		return "";
    	}
    	String[] lines=in.split(lineSeparators);
    	if(lines[0].length()>maxChars){
    		int ix=lines[0].lastIndexOf(' ',maxChars);
    		return lines[0].substring(0,ix);
    	}
    	return lines[0];
    }

    @SuppressWarnings("unchecked")
	public static void dumpHashtable(final Log log, final Hashtable table){
    	Set<String> keys=table.keySet();
    	log.log("Dump Hashtable\n", Log.INFOS);
    	for(String key:keys){
    		log.log(key+": "+table.get(key).toString(), Log.INFOS);
    	}
    	log.log("End dump\n", Log.INFOS);
    }
    
    /**
     * Change first lettere to uppercase, other letters to lowercase
     * @param orig the word to change (at least 2 characters)
     * @return the normalized word. Tis will return orig if orig is less than 2 characters 
     */
    public static String normalizeCase(final String orig){
    	if(orig==null){
    		return "";
    	}
    	if(orig.length()<2){
    		return orig;
    	}
    	return orig.substring(0,1).toUpperCase()+orig.substring(1).toLowerCase();
    }
    
    /**
     * Zwei Strings verleichen. Berücksichtigen, dass einer oder beide auch
     * Null sein könnten.
     * @param a erster String
     * @param b zweiter String
     * @return -1,0 oder 1
     */
    public static int compareWithNull(String a, String b){
    	if(a==null){
    		if(b==null){
    			return 0;
    		}else{
    			return -1;
    		}
    	}else if(b==null){
    		return 1;
    	}else{
    		return a.compareTo(b);
    	}
    }
    
    /**
     * Stering wenn nötig kürzen
     * @param orig Originalstring
     * @param len maximal zulöässige Lenge
     * @return den String, der maximal len Zeichen lang ist
     */
    public static String limitLength(final String orig, final int len){
    	if(orig==null){
    		return "";
    	}
    	if(orig.length()>len){
    		return orig.substring(0, len);
    	}
    	return orig;
    }
    
    /**
     * String aus einem Array holen. Leerstring, wenn der angeforderte Index ausserhalb des Arrays liegt
     * @param array
     * @param index
     * @return
     */
    public static String getSafe(final String[] array, final int index){
    	if((index>-1) && (array.length>index)){
    		return array[index];
    	}
    	return "";
    }
    
    /**
     * String mit unterschiedlicher möglicher Schreibweise in einheitliche Schreibweise bringen
     * @param in
     * @return
     */
    public static String unambiguify(final String in){
    	String ret=in.toLowerCase();
    	ret=ret.replaceAll("ä", "ae");
    	ret=ret.replaceAll("ö", "oe");
    	ret=ret.replaceAll("ü", "ue");
    	ret=ret.replaceAll("é", "e");
    	ret=ret.replaceAll("è", "e");
    	ret=ret.replaceAll("à", "a");
    	ret=ret.replaceAll("â", "a");
    	ret=ret.replaceAll("ê", "e");
    	return ret;
    }
    /** 
	 * Eine beliebige Ziffernfolge mit der Modulo-10 Prüfsumme verpacken
	 * @param number darf nur aus Ziffern bestehen
	 * @return die Eingabefolge, ergänzt um ihre Prüfziffer
	 */
	public static String addModulo10(final String number){
		int row=0;
		String nr=number.replaceAll("[^0-9]","");
		for(int i=0;i<nr.length();i++){
			int col=Integer.parseInt(nr.substring(i,i+1));
			row=mod10Checksum[row][col];
		}
		return number+Integer.toString(mod10Checksum[row][10]);
			
	}
	/**
	 * Die Modulo-10-Prüfsumme wieder entfernen
	 * @param number eine um eine prüfziffer ergänzte Zahl
	 * @return die Zahl ohne prüfziffer oder null, wenn die Prüfziffer falsch war.
	 */
	public static String checkModulo10(final String number){
		String check=number.substring(0,number.length()-1);
		String should=addModulo10(check);
		if(should.equals(number)){
			return check;
		}
		return null;
	}
	/** Array für den modulo-10-Prüfsummencode */
	private static final int[][] mod10Checksum={
		{0,9,4,6,8,2,7,1,3,5,0},
		{9,4,6,8,2,7,1,3,5,0,9},
		{4,6,8,2,7,1,3,5,0,9,8},
		{6,8,2,7,1,3,5,0,9,4,7},
		{8,2,7,1,3,5,0,9,4,6,6},
		{2,7,1,3,5,0,9,4,6,8,5},
		{7,1,3,5,0,9,4,6,8,2,4},
		{1,3,5,0,9,4,6,8,2,7,3},
		{3,5,0,9,4,6,8,2,7,1,2},
		{5,0,9,4,6,8,2,7,1,3,1}
	};
}