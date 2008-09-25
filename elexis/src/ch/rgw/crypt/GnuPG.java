//$Id: GnuPG.java 4439 2008-09-25 12:17:38Z rgw_ch $
package ch.rgw.crypt;
   
import java.io.*;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;


/**
  * A class that implements PGP interface for Java.
  * <P>
  * 
  * It calls gpg (GnuPG) program to do all the PGP commands. $Id: GnuPG.java 4439 2008-09-25 12:17:38Z rgw_ch $
  * 
  * @author Yaniv Yemini, January 2004.
  * @author Based on a class GnuPG by John Anderson, which can be found
  * @author at:
  *         http://lists.gnupg.org/pipermail/gnupg-devel/2002-February/018098.html
  * @author modified for use in JBother by Andrey Zakirov, February 2005
  * @created March 9, 2005
  * @version 0.5.1
  * @see GnuPG - http://www.gnupg.org/

  * Modified 2006/10 by G. Weirich for use in Elexis
 */

public class GnuPG implements Cryptologist{
    
 // Constants:
 //private final String kGnuPGCommand;

 private static final String kGnuPGArgs = " --batch --armor --output -";
 private String homedir;
 private String executable;
 
 // Class vars:
 private int gpg_exitCode = -1;

 private String gpg_result;

 private String gpg_err;
 private boolean gpgOK;
 private char[] passphrase;
 
 public void setPassphrase(char[] pwd){
	 passphrase=pwd;
 }
 
 public void setExecutable(String exe){
	 executable=exe;
 }
 
 public void setHomedir(String dir){
	 homedir=dir;
 }
 
 private String createGPGCommand(){
	 StringBuilder sb=new StringBuilder();
	 sb.append(executable).append(" ");
	 if(!StringTool.isNothing(homedir)){
		 sb.append("--homedir ").append(homedir);
	 }
	 sb.append(kGnuPGArgs);
	 return sb.toString();
 }
 /**
  * Reads an output stream from an external process. Imeplemented as a thred.
  * 
  * @author synic
  * @created March 9, 2005
  */
 class ProcessStreamReader extends Thread {
     InputStream is;

     String type;

     OutputStream os;

     String fullLine = "";

     /**
      * Constructor for the ProcessStreamReader object
      * 
      * @param is
      *            Description of the Parameter
      * @param type
      *            Description of the Parameter
      */
     ProcessStreamReader(InputStream is, String type) {
         this(is, type, null);
     }

     /**
      * Constructor for the ProcessStreamReader object
      * 
      * @param is
      *            Description of the Parameter
      * @param type
      *            Description of the Parameter
      * @param redirect
      *            Description of the Parameter
      */
     ProcessStreamReader(InputStream is, String type, OutputStream redirect) {
         this.is = is;
         this.type = type;
         this.os = redirect;
     }

     /**
      * Main processing method for the ProcessStreamReader object
      */
     public void run() {
         try {
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);
             String line = null;
             while ((line = br.readLine()) != null) {
                 fullLine = fullLine + line + "\n";
             }

         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
     }

     /**
      * Gets the string attribute of the ProcessStreamReader object
      * 
      * @return The string value
      */
     String getString() {
         return fullLine;
     }

 }

 /**
  * Sign
  * 
  * @param inStr
  *            input string to sign
  * @param secID
  *            ID of secret key to sign with
  * @param passPhrase
  *            passphrase for the secret key to sign with
  * @return true upon success
  */
 public boolean sign(String inStr, String secID, String passPhrase) {
     boolean success = false;
     File tmpFile = createTempFile(inStr);

     if (tmpFile != null) {
         success = runGnuPG("-u " + secID + " --passphrase-fd 0 -b "
                 + tmpFile.getAbsolutePath(), passPhrase);
         tmpFile.delete();
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }

 /**
  * ClearSign
  * 
  * @param inStr
  *            input string to sign
  * @param secID
  *            ID of secret key to sign with
  * @param passPhrase
  *            passphrase for the secret key to sign with
  * @return true upon success
  */
 public boolean clearSign(String inStr, String secID, String passPhrase) {
     boolean success = false;

     File tmpFile = createTempFile(inStr);

     if (tmpFile != null) {
         success = runGnuPG("-u " + secID
                 + " --passphrase-fd 0 --clearsign "
                 + tmpFile.getAbsolutePath(), passPhrase);
         tmpFile.delete();
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }

 /**
  * Signs and encrypts a string
  *
  * @param inStr
  *            input string to encrypt
  * @param secID
  *            ID of secret key to sign with
  * @param keyID
  *            ID of public key to encrypt with
  * @param passPhrase
  *            passphrase for the secret key to sign with
  * @return true upon success
  */
 public boolean signAndEncrypt(String inStr, String secID, String keyID,
         String passPhrase) {
     boolean success = false;

     File tmpFile = createTempFile(inStr);

     if (tmpFile != null) {
         success = runGnuPG("-u " + secID + " -r " + keyID
                 + " --passphrase-fd 0 -se " + tmpFile.getAbsolutePath(),
                 passPhrase);
         tmpFile.delete();
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }
 
 public boolean signAndEncrypt(File inFile,String secID, String keyID, String passphrase){
	 boolean success=false;
	 
	 success = runGnuPG("-u " + secID + " -r " + keyID
             + " --passphrase-fd 0 -se " + inFile.getAbsolutePath(),
             passphrase);
     inFile.delete();
     if (success && this.gpg_exitCode != 0) {
         success = false;
     }
     return success;
 }

 /**
  * Encrypt
  * 
  * @param inStr
  *            input string to encrypt
  * @param secID
  *            ID of secret key to use
  * @param keyID
  *            ID of public key to encrypt with
  * @return true upon success
  */
 public boolean encrypt(String inStr, String secID, String keyID) {

     boolean success;
     success = runGnuPG("-u " + secID + " -r " + keyID + " --encrypt", inStr);
     if (success && this.gpg_exitCode != 0) {
         success = false;
     }
     return success;
 }

 /**
  * Decrypt
  * 
  * @param inStr
  *            input string to decrypt
  * @param passPhrase
  *            passphrase for the secret key to decrypt with
  * @return true upon success
  */
 public boolean decrypt(String inStr, String passPhrase) {
     boolean success = false;

     File tmpFile = createTempFile(inStr);

     if (tmpFile != null) {
         success = runGnuPG("--passphrase-fd 0 --decrypt "
                 + tmpFile.getAbsolutePath(), passPhrase);
         tmpFile.delete();
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }
 
 	public boolean signKey(String keyname, String passphrase){
		boolean success=runGnuPG("--passphrase-fd 0 --yes --sign-key "+keyname, passphrase);
		if(success && this.gpg_exitCode !=0){
			success=false;
		}
		return success;
	 }

 
 public boolean decrypt(File inFile, String outFile, String passPhrase){
     boolean success = false;

     if (inFile != null) {
    	 if(outFile.indexOf(' ')!=-1){
    		 outFile="\""+outFile+"\"";
    	 }
         success = runGnuPG("-o "+outFile+" --passphrase-fd 0"+
        		 " --decrypt "+ inFile.getAbsolutePath(), passPhrase);
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }
 /**
  * List public keys in keyring
  * 
  * @param ID
  *            ID of public key to list, blank for all
  * @return true upon success
  */
 public boolean listKeys(String ID) {
     boolean success;
     success = runGnuPG("--list-keys --with-colons " + ID, null);
     if (success && this.gpg_exitCode != 0) {
         success = false;
     }
     return success;
 }

 /** 
  * get public key
  * 
  */
 public boolean getKey(String id){
	 boolean success=runGnuPG("--armor --export "+id,null);
	 if (success && this.gpg_exitCode != 0) {
         success = false;
     }
	 return success;
 }
 
 /**
  * import key
  */
 public boolean importKeyFile(String keyname){
	 boolean success=runGnuPG("--import "+keyname,null);
	 if (success && this.gpg_exitCode != 0) {
         success = false;
     }
	 return success;
 }
 
 public boolean importKey(String key){
	 File tmpFile = createTempFile(key);
	 boolean success=false;
	 if(tmpFile!=null){
		 success=runGnuPG("--import "+tmpFile,null);
		 tmpFile.delete();
		 if (success && this.gpg_exitCode != 0) {
	         success = false;
	     }
	 }
	 return success;
 }
 
  /**
  * List secret keys in keyring
  * 
  * @param ID
  *            ID of secret key to list, blank for all
  * @return true upon success
  */
 public boolean listSecretKeys(String ID) {
     boolean success;
     success = runGnuPG("--list-secret-keys --with-colons " + ID, null);
     if (success && this.gpg_exitCode != 0) {
         success = false;
     }
     return success;
 }

 /**
  * Generate a key pair. This will open the gpg-console to create the key interactively
  * @return
  */
 public boolean generateKey(String name, String mail, char[] pwd, String bem){
	 boolean success;
	 StringBuilder sb=new StringBuilder();
	 sb.append("Key-Type: DSA\n Key-Length: 2048\n Subkey-Type: ELG-E\n Subkey-Length: 2048")
	 	.append("\n Name-Real: ").append(name);
	 if(!StringTool.isNothing(bem)){
	 	sb.append("\n Name-Comment: ").append(bem);
	 }
	 sb.append("\n Name-Email: ").append(mail)
	 	.append("\n Expire-Date: 0")
	 	.append("\n Passphrase: ").append(pwd)
	 	.append("\n %commit\n");
	 	
     success = runGnuPG("--gen-key", sb.toString());
     if (success && this.gpg_exitCode != 0) {
         success = false;
     }
     return success;
 	 
 }

 
 public boolean changeKeyPassphrase(String key, String oldpwd, String newpwd){
	 boolean success;
	 StringBuilder sb=new StringBuilder();
	 sb.append("passwd\n")
	 	.append(oldpwd).append("\n")
	 	.append(newpwd).append("\n")
	 	.append(newpwd).append("\n")
	 	.append("quit\n");
	 success=runGnuPG("--edit-key "+key, sb.toString());
     if (success && this.gpg_exitCode != 0) {
         success = false;
     }
     return success;

 }
 /**
  * Verify a signature
  * 
  * @param inStr
  *            signature to verify
  * @return true if verified.
  */
 public boolean verify(String signedString, String dataString) {
     boolean success = false;
     File signedFile = createTempFile(signedString);
     File dataFile = createTempFile(dataString);

     if ((signedFile != null) && (dataFile != null)) {
         success = runGnuPG("--verify " + signedFile.getAbsolutePath() + " "
                 + dataFile.getAbsolutePath(), null);
         signedFile.delete();
         dataFile.delete();
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }

 public boolean verify(String signedString) {
     boolean success = false;
     File signedFile = createTempFile(signedString);

     if (signedFile != null) {
         success = runGnuPG("--verify " + signedFile.getAbsolutePath(), null);
         signedFile.delete();
         if (success && this.gpg_exitCode != 0) {
             success = false;
         }
     }
     return success;
 }
 
 /**
  * Get processing result
  * 
  * @return result string.
  */
 public String getResult() {
     return gpg_result;
 }

 /**
  * Get error output from GnuPG process
  * 
  * @return error string.
  */
 public String getErrorString() {
     return gpg_err;
 }

 /**
  * Get GnuPG exit code
  * 
  * @return exit code.
  */
 public int getExitCode() {
     return gpg_exitCode;
 }

public void runWithCommand(String command){
	 try {
		Process p=Runtime.getRuntime().exec(executable+" "+command);

		//p.waitFor();
	} catch (Exception e) {
		e.printStackTrace();
	}
 }
 /**
  * Runs GnuPG external program
  * 
  * @param commandArgs
  *            command line arguments
  * @param inputStr
  *            string to pass to GnuPG process
  * @return true if success.
  */
 private boolean runGnuPG(String commandArgs, String inputStr) {
     Process p;
     String fullCommand = createGPGCommand() + " " + commandArgs;
     //              String fullCommand = commandArgs;

     //log.log("GPG-Command: "+commandArgs+" "+inputStr, Log.INFOS);
     if(!gpgOK){
    	 gpg_err="GnuPG Programm nicht gefunden";
    	 return false;
     }
     try {
         p = Runtime.getRuntime().exec(fullCommand);
     } catch (IOException io) {
    	 ExHandler.handle(io);
         return false;
     }
     if (inputStr != null) {
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p
                 .getOutputStream()));
         try {
             out.write(inputStr);
             out.close();
         } catch (IOException io) {
             System.out.println("Exception at write! " + io.getMessage());
             return false;
         }
     }

     ProcessStreamReader psr_stdout = new ProcessStreamReader(p
             .getInputStream(), "ERROR");
     ProcessStreamReader psr_stderr = new ProcessStreamReader(p
             .getErrorStream(), "OUTPUT");
     psr_stdout.start();
     psr_stderr.start();
     try {

         psr_stdout.join();
         psr_stderr.join();
     } catch (InterruptedException i) {
         System.out.println("Exception at join! " + i.getMessage());
         return false;
     }

     try {
         p.waitFor();

     } catch (InterruptedException i) {
         System.out.println("Exception at waitfor! " + i.getMessage());
         return false;
     }

     try {
         gpg_exitCode = p.exitValue();
     } catch (IllegalThreadStateException itse) {
         return false;
     }
     gpg_result = psr_stdout.getString();
     gpg_err = psr_stderr.getString();

     return true;
 }

 /**
  * A utility method for creating a unique temporary file when needed by one
  * of the main methods. <BR>
  * The file handle is store in tmpFile object var.
  * 
  * @param inStr
  *            data to write into the file.
  * @return true if success
  */
 private File createTempFile(String inStr) {
     File tmpFile = null;
     FileWriter fw;

     try {
         tmpFile = File.createTempFile("YGnuPG", null);
     } catch (Exception e) {
         System.out.println("Cannot create temp file " + e.getMessage());
         return null;
     }

     try {
         fw = new FileWriter(tmpFile);
         fw.write(inStr);
         fw.flush();
         fw.close();
     } catch (Exception e) {
         // delete our file:
         tmpFile.delete();

         System.out.println("Cannot write temp file " + e.getMessage());
         return null;
     }

     return tmpFile;
 }

 /**
  * Default constructor
  */
 public GnuPG(boolean bWarnIfNotOk) {
	 //cfg=Hub.localCfg.getBranch("xchange", true);
	 // String exe="d:/apps/gpg/gpg.exe";
	 gpgOK=true;
	 //kGnuPGCommand = exe + " " + kGnuPGArgs;
 }
 
 public boolean isAvailable(){
	 return gpgOK;
 }

 
 /**
  * Description of the Method
  * 
  * @param xEncryptedData
  *            Description of the Parameter
  * @return Description of the Return Value
  */
 public String decryptExtension(String xEncryptedData) {
     //String gnupgPassword = BuddyList.getInstance().getGnuPGPassword();
	 String gnupgPassword=null;
     String encoding = null;
     xEncryptedData = xEncryptedData.replaceAll("(\n)+$", "");
     xEncryptedData = xEncryptedData.replaceAll("^(\n)+", "");
     if ((gnupgPassword != null)
             && decrypt("-----BEGIN PGP MESSAGE-----\nVersion: bla\n\n"
                     + xEncryptedData + "\n-----END PGP MESSAGE-----\n",
                     gnupgPassword)) {
         try {
             String systemEncoding = new String(getResult().getBytes(),
                     "UTF8");
             encoding = systemEncoding;
         } catch (java.io.UnsupportedEncodingException e) {
         }

     }
     return encoding.replaceAll("\n+$", "");
 }

 /**
  * Description of the Method
  * 
  * @param Data
  *            Description of the Parameter
  * @param gnupgSecretKey
  *            Description of the Parameter
  * @param gnupgPublicKey
  *            Description of the Parameter
  * @return Description of the Return Value
  */
 public String encryptExtension(String Data, String gnupgSecretKey,
         String gnupgPublicKey) {
     String encryptedData = null;
     try {
         byte[] utf8 = Data.getBytes("UTF8");
         String string = new String(utf8, streamEncoding());
         Data = string;
     } catch (java.io.UnsupportedEncodingException e) {
     }

     if (encrypt(Data, gnupgSecretKey, gnupgPublicKey)) {
         encryptedData = getResult();
         encryptedData = encryptedData.replaceAll(
                 "-----BEGIN PGP MESSAGE-----(\n.*)+\n\n", "");
         encryptedData = encryptedData.replaceAll(
                 "\n-----END PGP MESSAGE-----\n", "");

     }
     return encryptedData;
 }

 /**
  * Description of the Method
  * 
  * @param Data
  *            Description of the Parameter
  * @param gnupgSecretKey
  *            Description of the Parameter
  * @param gnupgPublicKey
  *            Description of the Parameter
  * @return Description of the Return Value
  */
 public String signExtension(String Data, String gnupgSecretKey) {
     String gnupgPassword=null;
     String signedData = null;
     try {
         byte[] utf8 = Data.getBytes("UTF8");
         String string = new String(utf8, streamEncoding());
         Data = string;
     } catch (java.io.UnsupportedEncodingException e) {
     }

     if ((gnupgPassword != null)
             && (sign(Data, gnupgSecretKey, gnupgPassword))) {
         signedData = getResult();
         signedData = signedData.replaceAll(
                 "-----BEGIN PGP SIGNATURE-----(\n.*)+\n\n", "");
         signedData = signedData.replaceAll(
                 "\n-----END PGP SIGNATURE-----\n", "");
         signedData = signedData.replaceAll("^(\n)+", "");
         signedData = signedData.replaceAll("(\n)+$", "");
     }
     return signedData;
 }

 public String verifyExtension(String xSignedData, String messageBody) {
     String id = null;
     try {
         byte[] utf8 = messageBody.getBytes("UTF8");
         String string = new String(utf8, streamEncoding());
         messageBody = string;
     } catch (java.io.UnsupportedEncodingException e) {
     }
     messageBody = messageBody.replaceAll("(\n)+$", "");
     xSignedData = xSignedData.replaceAll("(\n)+$", "");
     messageBody = messageBody.replaceAll("^(\n)+", "");
     xSignedData = xSignedData.replaceAll("^(\n)+", "");
     if (verify("-----BEGIN PGP SIGNATURE-----\nVersion: bla\n\n"
             + xSignedData + "\n-----END PGP SIGNATURE-----", messageBody)) {
         id = getErrorString();
         id = id.replaceAll(".*ID (.*)(\n.*)+", "$1");
     }
     return id;
 }
 /**
    * Gets stream encoding
    *
    * @return stream encoding.
    */
    public static String streamEncoding() {
        OutputStreamWriter out = new OutputStreamWriter(
               new ByteArrayOutputStream());
       return out.getEncoding();
   }

public byte[] decrypt(byte[] encrypted, char[] pwd){
	try {
		if(decrypt(new String(encrypted,"utf-8"), new String(pwd))){
			String dec=getResult();
			return dec.getBytes("utf-8");
		}
	} catch (UnsupportedEncodingException e) {
		// should not happen
		e.printStackTrace();
	}
	return null;
}

public Result<byte[]> decryptSigned(byte[] source, char[] pwd){
	
	return null;
}

public byte[] encrypt(byte[] source){

	return null;
}

public byte[] encryptAndSign(byte[] source){
	// TODO Auto-generated method stub
	return null;
}

public byte[] sign(byte[] source){
	// TODO Auto-generated method stub
	return null;
}

public Result<String> verify(byte[] data, byte[] signature){
	// TODO Auto-generated method stub
	return null;
}
    
    
}
