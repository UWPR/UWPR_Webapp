/* InvalidFileFormatException.java
 * Created on Apr 23, 2004
 */
package org.yeastrc.data;

/**
 * Add one sentence class summary here.
 * Add class description here
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Apr 23, 2004
 *
 */
public class InvalidFileFormatException extends Exception {

	/** Constructs an InvalidIDException with no detail message. */
	public InvalidFileFormatException () {
		super();
	}

	/** Constructs an InvalidIDException with the specified detail message. */
	public InvalidFileFormatException (String message) {
		super(message);
	}

}
