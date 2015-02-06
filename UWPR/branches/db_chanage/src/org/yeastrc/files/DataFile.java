package org.yeastrc.files;

import java.io.InputStream;
import java.util.Date;
import org.yeastrc.project.Researcher;

public class DataFile {
	
	public int getId() {
		return id;
	}
	protected void setId(int id) {
		this.id = id;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Pulls the raw data for this DataFile from the database and returns it
	 * @return
	 * @throws Exception
	 */
	public byte[] getData() throws Exception {
		return DataFileDataRetriever.getInstance().getDataFileData( this );
	}
	
	/**
	 * Note, this immediately attempts to save the supplied data to the database, even if the 
	 * DataFile itself isn't saved via the DataFileSaver class
	 * @param data
	 * @throws Exception
	 */
	public void setData(byte[] data) throws Exception {
		DataFileDataSaver.getInstance().saveData( this, data );
	}

	/**
	 * Note, this immediately attempts to save the supplied data to the database, even if the 
	 * DataFile itself isn't saved via the DataFileSaver class
	 * @param is
	 * @throws Exception
	 */
	public void setData(InputStream is) throws Exception {
		DataFileDataSaver.getInstance().saveData( this, is );
	}
	
	public int getFilesize() {
		return filesize;
	}
	public void setFilesize(int filesize) {
		this.filesize = filesize;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getMimetype() {
		return mimetype;
	}
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	public Researcher getUploader() {
		return uploader;
	}
	public void setUploader(Researcher uploader) {
		this.uploader = uploader;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getTypeID() {
		return typeID;
	}
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}



	private int id;
	private String filename;
	private String description;
	private int filesize;
	private Date timestamp;
	private String mimetype;
	private Researcher uploader;

	private String type;
	private int typeID;
}
