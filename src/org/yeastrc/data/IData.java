/*
 * IData.java
 *
 * Created October 15, 2003
 *
 * Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.data;

/**
 * This interface should be implemented by any Classes which represent a chunk of data from
 * the YRC data set.  Examples are full field images from microscopy, a specific peptide from
 * a mass. spec. run, or a protein structure prediction.
 */
public interface IData {
	
	/**
	 * Returns the project to which this set of data belongs, as an object
	 * @return A Project to which this set of data belongs
	 */
	//public Project getProject();

	/**
	 * Loads the data object from a row in the database for the given ID, where the ID is for the
	 * type of data this is.  For example, for a Mass Spec run, you'd pass in the run ID.
	 * @param id The ID to load.
	 * @throws Exception if there is an problem encountered
	 */
	public void load(int id) throws Exception;
	
	/**
	 * Use this method to save this object's data to the database.
	 * These objects should generally represent a single row of data in a table.
	 * This will update the row if this object represents a current row of data, or it will
	 * insert a new row if this data is not in the database (that is, if there is no ID in the object).
	 * @throws Exception if there is an problem encountered
	 */
	public void save() throws Exception;

	/**
	 * Use this method to delete the data underlying this object from the database.
	 * Doing so will delete the row from the table corresponding to this object, and
	 * will remove the ID value from the object (since it represents the primary key)
	 * in the database.  This will cause subsequent calls to save() on the object to
	 * insert a new row into the database and generate a new ID.
	 * This will also call delete() on instantiated IData objects for all rows in the
	 * database which are dependent on this row.  For example, calling delete() on a
	 * MS Run objects would call delete() on all Run Result objects, which would then
	 * call delete() on all dependent Peptide objects for those results.
	 * Pre: object is populated with a valid ID.
	 * @throws Exception if there is an problem encountered
	 * valid (that is, not found in the database).
	 */
	public void delete() throws Exception;


}