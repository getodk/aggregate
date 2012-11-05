package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;
import java.util.List;

/**
 * This represents the contents of a table that will display information about
 * the files that are associated with a given table. It contains the column
 * names that should be displayed as well as a list of FileSummaryClient 
 * objects that contain information about which 
 * <br>
 * It only exists so that you can essentially combine two service calls, 
 * one that gets the column names and one that gets the rows, without 
 * having to worry about the services returning at different times. For
 * this reason there is no corresponding client-side TableContents object.
 * @author sudar.sam@gmail.com
 *
 */
public class TableContentsForFilesClient implements Serializable {
   
   /**
   * 
   */
  private static final long serialVersionUID = -5644953958281330962L;
  
  /**
   * Necessary for GWT serialization.
   */
  public TableContentsForFilesClient() {
  }
   
   /**
    * The names of the table's columns.
    */
   public List<String> columnNames;
   
   /**
    * The non-media files for the table. Any media file associated with the
    * file will exist in the mediaFiles list for that file.
    */
   public List<FileSummaryClient> nonMediaFiles;

}
