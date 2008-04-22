package ch.elexis.exchange;

import org.eclipse.swt.graphics.Image;

/**
 * Interface to specify a Java (SWT) connection to scanner devices e.g. via TWAIN or SANE
 * Implementing class must implement IScannerAccess and IScannerAccess.ISource.
 * 
 * It must return all available Sources on a call to getSources(), let the user
 * configure a source via configureSource(), and must aquire an image from the source
 * on a call to aquire
 * @author gerry
 *
 */
public interface IScannerAccess {
	
	/**
	 * Retrieve all available Sources (scanners, cameras etc)
	 * @return an Array of SourceDefinitions
	 */
	public ISource[] getSources();
	
	/**
	 * let the user enter configuration details (e.g. resolution settings, color settings)
	 * with the scanner specific dialog
	 * @param src the scan source o configure
	 * @return an implementation specific configuration result
	 */
	public Object configureSource(ISource src);
	
	
	/**
	 * Aquire an Image from the Scanner and create an SWT image from it.
	 * @param src the Scanner to use
	 * @param configuration the configuration details as set in configureSource(). Might be null
	 * @return the image from the scanner
	 */
	public Image aquire(ISource src, Object configuration) throws Exception;
	
	
	/**
	 * An Image source
	 *
	 */
	public interface ISource{
		/**
		 * Name of the source
		 * @return a human readable name
		 */
		public String getName();
		/**
		 * A longer description
		 * @return
		 */
		public String getDescription();
		
		/**
		 * Is the source ready at the moment?
		 * @return true if it is ready
		 */
		public boolean isAvailable();
	}
}
