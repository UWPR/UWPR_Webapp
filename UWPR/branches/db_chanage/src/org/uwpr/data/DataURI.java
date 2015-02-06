/**
 * 
 */
package org.uwpr.data;

import java.util.Date;

import org.yeastrc.project.Project;

/**
 * @author Mike
 *
 */
public class DataURI {

	public DataURI() {
		id = 0;
		uri = null;
		comments = null;
		lastChange = null;
	}

	private int id;
	private String uri;
	private String comments;
	private Date lastChange;
	private Project project;

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}
	/**
	 * @param project the project to set
	 */
	public void setProject(Project project) {
		this.project = project;
	}
	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	protected void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the lastChange
	 */
	public Date getLastChange() {
		return lastChange;
	}
	/**
	 * @param lastChange the lastChange to set
	 */
	public void setLastChange(Date lastChange) {
		this.lastChange = lastChange;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
