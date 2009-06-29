package ch.elexis.connect.afinion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import ch.rgw.tools.TimeTool;

public class Logger {
	PrintStream _log;
	
	public Logger()
	{
		_log = System.out;
	}
	
	public Logger(String filename) throws FileNotFoundException
	{
		_log = new PrintStream(new FileOutputStream(filename, true));
	}
	
	public Logger(boolean enable)
	{
		if (enable)
		{
			_log = System.out;
		}
		else
		{
			_log = new PrintStream(new DummyPrintStream());
		}
	}
	
	public void logRX(String s)
	{
		String debug = s.replace("<", "<LT>").replace(">", "<GT>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		debug = debug.replace("\000", "<NUL>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\001", "<SOH>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\002", "<STX>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\003", "<ETX>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\004", "<EOT>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\005", "<ENQ>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\006", "<ACK>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\016", "<DLE>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\021", "<NAK>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\023", "<ETB>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace(" ", "<SPACE>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\n", "<LF>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\t", "<HT>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\"", "<QUOTE>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		_log.println("<-- \"" + debug + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void logTX(String s)
	{
		String debug = s.replace("<", "<LT>").replace(">", "<GT>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		debug = debug.replace("\016", "<DLE>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\021", "<NAK>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\023", "<ETB>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\000", "<NUL>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\001", "<SOH>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\002", "<STX>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\003", "<ETX>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\004", "<EOT>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\005", "<ENQ>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\006", "<ACK>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace(" ", "<SPACE>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\n", "<LF>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\t", "<HT>"); //$NON-NLS-1$ //$NON-NLS-2$
		debug = debug.replace("\"", "<QUOTE>"); //$NON-NLS-1$ //$NON-NLS-2$
		
		_log.println("--> \"" + debug + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void logRaw(String s) {
		_log.println(s);
	}
	
	public void log(String s)
	{
		String debug = s.replace("<", "<LT>").replace(">", "<GT>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		debug = debug.replace("\"", "<QUOTE>"); //$NON-NLS-1$ //$NON-NLS-2$
		_log.println("-*- \"" + debug + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void logStart()
	{
		_log.println("-S- \"" + new TimeTool().toDBString(true) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void logEnd()
	{
		_log.println("-E- \"" + new TimeTool().toDBString(true) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	class DummyPrintStream extends OutputStream
	{
		@Override
		public void write(int b) throws IOException {
			// Do nothing			
		}		
	}
}
