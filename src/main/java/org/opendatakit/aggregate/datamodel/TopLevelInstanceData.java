/*
  Copyright (C) 2010 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.aggregate.datamodel;

import org.opendatakit.common.datamodel.BinaryContent;
import org.opendatakit.common.datamodel.BinaryContentRefBlob;
import org.opendatakit.common.datamodel.RefBlob;
import org.opendatakit.common.security.User;

/**
 * For use by dynamically discovered tables.  Tables that
 * have specific structures required by Aggregate should
 * extend TopLevelInstanceDataBase or InstanceDataBase.
 * <p>
 * All instance data for an xform is stored in InstanceData tables
 * specific to that xform.
 * <p>
 * The xml tag for the xform's instance data field is used to name
 * the specific column that holds that tag's data value.
 * <p>Databases generally have an upper limit to how many columns a
 * database table can have.  If the xform exceeds this limit, the
 * data fields will be split across one primary table and one or more
 * phantom tables with the <code>PARENT_AURI</code> of the phantom
 * tables pointing to the row of the primary table from which this
 * phantom table was cleaved.
 * <p>Each repeat group in an xform is represented as its own
 * InstanceData table with the <code>PARENT_AURI</code> pointing to
 * the enclosing repeat group (if it is nested within one) or to the
 * top level xform table.
 * <p>Non-repeat groups are not split into separate tables; their
 * instance data fields are in-lined into the enclosing repeat group
 * (if it is nested within one) or into the top level xform table.
 * <p>Each selection choice data field exists as a separate
 * {@link SelectChoice} table. This is
 * to support multiple-choice selections and to allow a single-choice
 * selection to be easily converted into a multiple-choice selection
 * without altering the structures of the tables.
 * <p>Each binary data field exists as a separate
 * {@link BinaryContent}
 * table and associated
 * {@link BinaryContentRefBlob}
 * and {@link RefBlob}
 * table.  Together, these support binary data attachments either as
 * standalone tables or for submissions.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 */
public final class TopLevelInstanceData extends TopLevelDynamicBase {

  public TopLevelInstanceData(String databaseSchema, String tableName) {
    super(databaseSchema, tableName);
  }

  private TopLevelInstanceData(TopLevelInstanceData ref, User user) {
    super(ref, user);
  }

  @Override
  public TopLevelInstanceData getEmptyRow(User user) {
    return new TopLevelInstanceData(this, user);
  }
}
