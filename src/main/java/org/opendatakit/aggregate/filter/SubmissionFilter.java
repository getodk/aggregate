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
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Creates the database interface for filter objects
 *
 * @author wbrunette@gmail.com
 */
public class SubmissionFilter extends CommonFieldsBase {

  private static final String TABLE_NAME = "_filter";

  private static final DataField URI_FILTER_GROUP_PROPERTY = new DataField("URI_FILTER_GROUP", DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
  private static final DataField VISIBILITY_PROPERTY = new DataField("VISIBILITY", DataField.DataType.STRING, true, 80L);
  private static final DataField ROWORCOL_PROPERTY = new DataField("ROWORCOL", DataField.DataType.STRING, true, 80L);
  private static final DataField COL_TITLE_PROPERTY = new DataField("COL_TITLE", DataField.DataType.STRING, true, 80L); // TODO: determine length
  private static final DataField COL_ENCODING_PROPERTY = new DataField("COL_ENCODING", DataField.DataType.STRING, true, 1000L); // TODO: determine length
  private static final DataField OPERATION_PROPERTY = new DataField("OPERATION", DataField.DataType.STRING, true, 80L);
  private static final DataField CLAUSE_PROPERTY = new DataField("INPUT_CLAUSE", DataField.DataType.STRING, true, 4096L);
  private static final DataField ORDINAL_PROPERTY = new DataField("ORDINAL", DataField.DataType.INTEGER, true);
  private static final DataField COL_GPS_ORD_PROPERTY = new DataField("GPS_ORD", DataField.DataType.INTEGER, true);
  private static SubmissionFilter relation = null;
  private List<SubmissionColumnFilter> colFilters;

  private SubmissionFilter(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(URI_FILTER_GROUP_PROPERTY);
    fieldList.add(VISIBILITY_PROPERTY);
    fieldList.add(ROWORCOL_PROPERTY);
    fieldList.add(COL_TITLE_PROPERTY);
    fieldList.add(COL_ENCODING_PROPERTY);
    fieldList.add(OPERATION_PROPERTY);
    fieldList.add(CLAUSE_PROPERTY);
    fieldList.add(ORDINAL_PROPERTY);
    fieldList.add(COL_GPS_ORD_PROPERTY);
  }

  private SubmissionFilter(SubmissionFilter ref, User user) {
    super(ref, user);
  }

  private static synchronized final SubmissionFilter assertRelation(CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      SubmissionFilter relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new SubmissionFilter(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  static final SubmissionFilter transform(Filter filter, SubmissionFilterGroup filterGroup, CallingContext cc) throws ODKDatastoreException {

    SubmissionFilter relation = assertRelation(cc);
    String uri = filter.getUri();
    SubmissionFilter subFilter;

    if (uri.equals(UIConsts.URI_DEFAULT)) {
      subFilter = cc.getDatastore().createEntityUsingRelation(relation, cc.getCurrentUser());
    } else {
      subFilter = cc.getDatastore().getEntity(relation, uri, cc.getCurrentUser());
    }


    subFilter.setRowOrColumn(filter.getRc());
    subFilter.setFilterGroup(filterGroup.getUri());
    subFilter.setOrdinalNumber(filter.getOrdinal());
    subFilter.setVisibility(filter.getVisibility());

    if (filter instanceof ColumnFilter) {
      ColumnFilter cf = (ColumnFilter) filter;

      for (Column column : cf.getColumnFilterHeaders()) {
        SubmissionColumnFilter columnFilter = SubmissionColumnFilter.transform(column, subFilter, cc);
        subFilter.addColumn(columnFilter);
      }

    } else if (filter instanceof RowFilter) {
      RowFilter rf = (RowFilter) filter;
      subFilter.setFilterOperation(rf.getOperation());
      subFilter.setFilterInputClause(rf.getInput());
      Column column = rf.getColumn();
      subFilter.setColumnTitle(column.getDisplayHeader());
      subFilter.setColumnEncoding(column.getColumnEncoding());
      subFilter.setGpsColumnCode(column.getGeopointColumnCode());
    }

    return subFilter;
  }

  static final List<SubmissionFilter> getFilterList(String uriFilterGroup, CallingContext cc) throws ODKDatastoreException {
    SubmissionFilter relation = assertRelation(cc);
    Query query = cc.getDatastore().createQuery(relation, "SubmissionFilter.getFilterList", cc.getCurrentUser());
    query.addFilter(SubmissionFilter.URI_FILTER_GROUP_PROPERTY,
        org.opendatakit.common.persistence.Query.FilterOperation.EQUAL, uriFilterGroup);

    List<SubmissionFilter> filterList = new ArrayList<SubmissionFilter>();

    List<? extends CommonFieldsBase> results = query.executeQuery();
    for (CommonFieldsBase cb : results) {
      if (cb instanceof SubmissionFilter) {
        SubmissionFilter filter = (SubmissionFilter) cb;
        filter.populate(cc);
        filterList.add(filter);
      }
    }
    return filterList;
  }

  @Override
  public SubmissionFilter getEmptyRow(User user) {
    return new SubmissionFilter(this, user);
  }

  public void setFilterGroup(String groupUri) {
    if (!setStringField(URI_FILTER_GROUP_PROPERTY, groupUri)) {
      throw new IllegalArgumentException("overflow filterGroup");
    }
  }

  public Visibility getVisibility() {
    String visibility = getStringField(VISIBILITY_PROPERTY);
    try {
      if (visibility == null) {
        // row filters had null values that should be interpretted
        // as DISPLAY values.  Return DISPLAY if we find a null.
        setVisibility(Visibility.DISPLAY);
        return Visibility.DISPLAY;
      }
      return Visibility.valueOf(visibility);
    } catch (IllegalArgumentException e) {
      // try again using historical values
      // this is to allow an upgrade when we change visibility constant names
      return Visibility.historicalConverter(visibility);
    }
  }

  public void setVisibility(Visibility visibility) {
    if (!setStringField(VISIBILITY_PROPERTY, visibility.name())) {
      throw new IllegalArgumentException("overflow visibility");
    }
  }

  public RowOrCol getRowOrColumn() {
    String roworcol = getStringField(ROWORCOL_PROPERTY);
    return RowOrCol.valueOf(roworcol);
  }

  public void setRowOrColumn(RowOrCol roworcol) {
    if (!setStringField(ROWORCOL_PROPERTY, roworcol.name())) {
      throw new IllegalArgumentException("overflow row or col");
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

  public FilterOperation getFilterOperation() {
    String op = getStringField(OPERATION_PROPERTY);
    return FilterOperation.valueOf(op);
  }

  public void setFilterOperation(FilterOperation op) {
    if (!setStringField(OPERATION_PROPERTY, op.name())) {
      throw new IllegalArgumentException("overflow filter operation");
    }
  }

  public String getFilterInputClause() {
    return getStringField(CLAUSE_PROPERTY);
  }

  public void setFilterInputClause(String clause) {
    if (!setStringField(CLAUSE_PROPERTY, clause)) {
      throw new IllegalArgumentException("overflow clause");
    }
  }

  public Long getOrdinalNumber() {
    return getLongField(ORDINAL_PROPERTY);
  }

  public void setOrdinalNumber(Long ordinal) {
    setLongField(ORDINAL_PROPERTY, ordinal);
  }

  public Long getGpsColumnCode() {
    return getLongField(COL_GPS_ORD_PROPERTY);
  }

  public void setGpsColumnCode(Long gpsColumnCode) {
    setLongField(COL_GPS_ORD_PROPERTY, gpsColumnCode);
  }

  void addColumn(SubmissionColumnFilter column) {
    if (colFilters == null) {
      colFilters = new ArrayList<SubmissionColumnFilter>();
    }
    colFilters.add(column);
  }

  void populate(CallingContext cc) throws ODKDatastoreException {
    if (getRowOrColumn().equals(RowOrCol.COLUMN)) {
      if (colFilters == null) {
        colFilters = new ArrayList<SubmissionColumnFilter>();
      }
      colFilters = SubmissionColumnFilter.getFilterList(this.getUri(), cc);
    }
  }

  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    ds.putEntity(this, user);
    if (colFilters != null) {
      ds.putEntities(colFilters, user);
    }

  }

  public void delete(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    if (colFilters != null) {
      for (SubmissionColumnFilter filter : colFilters) {
        ds.deleteEntity(filter.getEntityKey(), user);
      }
    }
    ds.deleteEntity(getEntityKey(), user);
  }

  public Filter transform() {
    RowOrCol type = getRowOrColumn();
    Filter filter;
    if (type.equals(RowOrCol.COLUMN)) {
      ColumnFilter columnFilter = new ColumnFilter(this.getUri());


      // populate the list of column filter headers
      if (colFilters != null) {
        for (SubmissionColumnFilter cols : colFilters) {
          columnFilter.addColumnFilterHeader(cols.transform());
        }
      }
      filter = columnFilter;
    } else {
      RowFilter rowFilter = new RowFilter(this.getUri());
      rowFilter.setOperation(getFilterOperation());
      rowFilter.setInput(getFilterInputClause());
      Column header = new Column(getColumnTitle(), getColumnEncoding(), getGpsColumnCode());
      rowFilter.setColumn(header);
      filter = rowFilter;
    }

    filter.setVisibility(getVisibility());
    filter.setOrdinal(getOrdinalNumber());
    filter.setRc(getRowOrColumn());

    return filter;
  }

}
