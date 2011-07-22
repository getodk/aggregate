/**
 * <p>
 * Contains all relations used by the odktables API for both simple backup and 
 * synchronization services.
 * </p>
 * 
 * <p>
 * The following diagrams present an overview of the relations and their attributes. 
 * (STRING) in parentheses means that the field is represented by the default Uri 
 * generated for each entity. See the respective entity (e.g. {@link InternalColumn}, 
 * {@link InternalUserTableMapping}, etc.) for definitions of the attributes.
 * <center>
 * <table>
 * <tr>
 * <td><center><img src="doc-files/Columns.png"/></center></td>
 * <td><center><img src="doc-files/Cursors.png"/></center></td>
 * <td><center><img src="doc-files/Permissions.png"/></center></td>
 * <tr>
 * <td><center><img src="doc-files/Rows.png"/></center></td>
 * <td><center><img src="doc-files/TableEntries.png"/></center></td>
 * <td><center><img src="doc-files/Users.png"/></center></td>
 * </tr>
 * </table>
 * </center>
 * </p>
 * 
 */
package org.opendatakit.aggregate.odktables.relation;