/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.filter;

import java.util.ArrayList;
import java.util.List;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class SubmissionColumnFilter extends CommonFieldsBase {

  private static final String TABLE_NAME = "_multi_column_filter";

  private static final DataField URI_PARENT_FILTER_PROPERTY = new DataField("URI_PARENT_FILTER_",
      DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
  private static final DataField COL_TITLE_PROPERTY = new DataField("COL_TITLE", DataField.DataType.STRING,
      true, 80L); // TODO: determine length
  private static final DataField COL_ENCODING_PROPERTY = new DataField("COL_ENCODING", DataField.DataType.STRING,
      true, 1000L); // TODO: determine length
  private static final DataField COL_GPS_ORD_PROPERTY = new DataField("GPS_ORD", DataField.DataType.INTEGER,
      true);
  private static SubmissionColumnFilter relation = null;

  /**
   * Construct a relation prototype.
   *
   * @param databaseSchema
   * @param tableName
   */
  private SubmissionColumnFilter(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(URI_PARENT_FILTER_PROPERTY);
    fieldList.add(COL_TITLE_PROPERTY);
    fieldList.add(COL_ENCODING_PROPERTY);
    fieldList.add(COL_GPS_ORD_PROPERTY);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   *
   * @param ref
   * @param user
   */
  private SubmissionColumnFilter(SubmissionColumnFilter ref, User user) {
    super(ref, user);
  }

  private static synchronized final SubmissionColumnFilter assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      SubmissionColumnFilter relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new SubmissionColumnFilter(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  static final SubmissionColumnFilter transform(Column column, SubmissionFilter parentFilter,
                                                CallingContext cc) throws ODKOverQuotaException, ODKEntityNotFoundException, ODKDatastoreException {

    SubmissionColumnFilter relation = assertRelation(cc);
    String uri = column.getUri();
    SubmissionColumnFilter columnFilter;

    if (uri.equals(UIConsts.URI_DEFAULT)) {
      columnFilter = cc.getDatastore().createEntityUsingRelation(relation, cc.getCurrentUser());
    } else {
      columnFilter = cc.getDatastore().getEntity(relation, uri, cc.getCurrentUser());
    }

    columnFilter.setParentFilterUri(parentFilter.getUri());
    columnFilter.setColumnTitle(column.getDisplayHeader());
    columnFilter.setColumnEncoding(column.getColumnEncoding());
    columnFilter.setGpsColumnCode(column.getGeopointColumnCode());

    return columnFilter;
  }

  static final List<SubmissionColumnFilter> getFilterList(String uriFilter, CallingContext cc)
      throws ODKDatastoreException {
    SubmissionColumnFilter relation = assertRelation(cc);
    Query query = cc.getDatastore().createQuery(relation, "SubmissionColumnFilter.getFilterList", cc.getCurrentUser());
    query.addFilter(SubmissionColumnFilter.URI_PARENT_FILTER_PROPERTY,
        org.opendatakit.common.persistence.Query.FilterOperation.EQUAL, uriFilter);

    List<SubmissionColumnFilter> column = new ArrayList<SubmissionColumnFilter>();

    List<? extends CommonFieldsBase> results = query.executeQuery();
    for (CommonFieldsBase cb : results) {
      if (cb instanceof SubmissionColumnFilter) {
        column.add((SubmissionColumnFilter) cb);
      }
    }
    return column;
  }

  @Override
  public SubmissionColumnFilter getEmptyRow(User user) {
    return new SubmissionColumnFilter(this, user);
  }

  public void setParentFilterUri(String parentUri) {
    if (!setStringField(URI_PARENT_FILTER_PROPERTY, parentUri)) {
      throw new IllegalArgumentException("overflow parent uri");
    }
  }

  public String getColumnTitle() {
    return getStringField(COL_TITLE_PROPERTY);
  }

  public void setColumnTitle(String name) {
    if (!setStringField(COL_TITLE_PROPERTY, name)) {
      throw new IllegalArgumentException("overflow name");
    }
  }

  public String getColumnEncoding() {
    return getStringField(COL_ENCODING_PROPERTY);
  }

  public void setColumnEncoding(String name) {
    if (!setStringField(COL_ENCODING_PROPERTY, name)) {
      throw new IllegalArgumentException("overflow column encoding");
    }
  }

  public Long getGpsColumnCode() {
    return getLongField(COL_GPS_ORD_PROPERTY);
  }

  public void setGpsColumnCode(Long gpsColumnCode) {
    setLongField(COL_GPS_ORD_PROPERTY, gpsColumnCode);
  }

  public Column transform() {
    return new Column(this.getUri(), getColumnTitle(), getColumnEncoding(), getGpsColumnCode());
  }
}
