package org.opendatakit.aggregate.client.odktables;


/**
 * Type of tables. The representation field is the magic string that has to
 * correspond to the correct use on the phone.
 * <p>
 * It is living in the client-side code just because it's not an important
 * enough object to have to transform.
 * @author sudars
 *
 */
public enum TableTypeClient {
  DATA, SECURITY, SHORTCUT;
}
