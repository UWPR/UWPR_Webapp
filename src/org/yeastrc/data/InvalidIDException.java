package org.yeastrc.data;

/**
 * This represent an exception to be thrown when an error is encountered
 * during the loading of a data object (experiment, mass spec row, whatever)
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @version 2003-11-04
 */
 public class InvalidIDException extends Exception {

 	/** Constructs an InvalidIDException with no detail message. */
 	public InvalidIDException () {
 		super();
 	}

	/** Constructs an InvalidIDException with the specified detail message. */
 	public InvalidIDException (String message) {
 		super(message);
 	}

 }