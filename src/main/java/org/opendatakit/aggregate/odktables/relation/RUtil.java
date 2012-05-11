package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.opendatakit.aggregate.odktables.entity.TableRole;

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

}
