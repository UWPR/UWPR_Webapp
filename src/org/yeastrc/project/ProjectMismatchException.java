/**
 * ProjectMismatchException.java
 * @author Vagisha Sharma
 */
package org.yeastrc.project;

/**
 * Thrown when an object loaded by its own ID does not belong to the project acting on it -- for
 * example a payment method or a usage block named in a request alongside a projectId it is not
 * linked to.  Checked, so any caller that loads an object by ID under a separately supplied
 * projectId is forced to handle the mismatch rather than silently act on another project's data.
 */
public class ProjectMismatchException extends Exception {

	public ProjectMismatchException(String message) {
		super(message);
	}
}
