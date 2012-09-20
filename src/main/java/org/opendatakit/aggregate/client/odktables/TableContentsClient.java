package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;
import java.util.List;

/**
 * This represents the contents of a table. Essentially it is a wrapper
 * of a list of rows and the corresponding column names.
 * <br>
 * It only exists so that you can essentially combine two service calls, 
 * one that gets the column names and one that gets the rows, without 
 * having to worry about the services returning at different times. For
 * this reason there is no corresponding client-side TableContents object.
 * @author sudar.sam@gmail.com
 *
 */
public class TableContentsClient implements Serializable {
	
	/**
	 * The tables rows.
	 */
	public List<RowClient> rows;
	
	/**
	 * The names of the table's columns.
	 */
	public List<String> columnNames;

}
