package org.opendatakit.aggregate.odktables.relation;


/**
 * @author the.dylan.price@gmail.com
 * 
 */
public class RUtil {

  public static final String NAMESPACE = "ODKTABLES";

  /**
   * Converts a uuid to a datastore table and column name friendly format.
   */
  public static String convertIdentifier(String id) {
    id = "table:" + id;
    return id.replace('-', '_').replace(':', '_').toUpperCase();
  }
  
  /**
   * Return a db-safe version of the proposed column name. This should be 
   * equivalent to how it would be on the phone. At the moment replaces spaces
   * with underscores and precedes with an underscore.
   * @param proposedName
   * @return
   */
  public static String convertToDbSafeBackingColumnName(String proposedName) {
    return "_" + proposedName.replace(" ", "_");
  }

}
