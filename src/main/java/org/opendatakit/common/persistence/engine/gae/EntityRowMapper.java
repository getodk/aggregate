/**
 * Copyright (C) 2010 University of Washington
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.gae;

import java.sql.SQLException;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.security.User;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class EntityRowMapper {

  private final CommonFieldsBase relation;
  private final User user;

  EntityRowMapper(CommonFieldsBase tableDefn, User user) {
    this.relation = tableDefn;
    this.user = user;
  }

  public Object mapRow(DatastoreImpl ds, com.google.appengine.api.datastore.Entity rs, int rowNum)
      throws SQLException {

    CommonFieldsBase row;
    try {
      row = relation.getEmptyRow(user);
      row.setFromDatabase(true);
    } catch (Exception e) {
      throw new IllegalStateException("failed to create empty row", e);
    }
    ds.updateRowFromGae(row, rs);
    return row;
  }
}
