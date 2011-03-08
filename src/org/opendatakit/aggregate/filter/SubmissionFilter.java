package org.opendatakit.aggregate.filter;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
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
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Creates the database interface for filter objects
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionFilter extends CommonFieldsBase {

  private static final String TABLE_NAME = "_filter";

  private static final DataField URI_FILTER_GROUP_PROPERTY = new DataField("URI_FILTER_GROUP",
      DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
  private static final DataField VISIBILITY_PROPERTY = new DataField("VISIBILITY",
      DataField.DataType.STRING, true, 80L);
  private static final DataField ROWORCOL_PROPERTY = new DataField("ROWORCOL",
      DataField.DataType.STRING, true, 80L);
  private static final DataField COL_TITLE_PROPERTY = new DataField("COL_TITLE", DataField.DataType.STRING,
      true, 80L); // TODO: determine length
  private static final DataField COL_ENCODING_PROPERTY = new DataField("COL_ENCODING", DataField.DataType.STRING,
      true, 200L); // TODO: determine length
  private static final DataField OPERATION_PROPERTY = new DataField("OPERATION",
      DataField.DataType.STRING, true, 80L);
  private static final DataField CLAUSE_PROPERTY = new DataField("INPUT_CLAUSE",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField ORDINAL_PROPERTY = new DataField("ORDINAL",
      DataField.DataType.INTEGER, true);

  private List<SubmissionColumnFilter> colFilters;
  
  /**
   * Construct a relation prototype.
   * 
   * @param databaseSchema
   * @param tableName
   */
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
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   * 
   * @param ref
   * @param user
   */
  private SubmissionFilter(SubmissionFilter ref, User user) {
    super(ref, user);
  }

  @Override
  public SubmissionFilter getEmptyRow(User user) {
    return new SubmissionFilter(this, user);
  }

  public String getFilterGroup() {
    return getStringField(URI_FILTER_GROUP_PROPERTY);
  }

  public Visibility getColumnVisibility() {
    String visibility = getStringField(VISIBILITY_PROPERTY);
    return Visibility.valueOf(visibility);
  }

  public RowOrCol getRowOrColumn() {
    String roworcol = getStringField(ROWORCOL_PROPERTY);
    return RowOrCol.valueOf(roworcol);
  }

  public String getColumnTitle() {
    return getStringField(COL_TITLE_PROPERTY);
  }

  public String getColumnEncoding() {
    return getStringField(COL_ENCODING_PROPERTY);
  }
  
  public FilterOperation getFilterOperation() {
    String op = getStringField(OPERATION_PROPERTY);
    return FilterOperation.valueOf(op);
  }

  public String getFilterInputClause() {
    return getStringField(CLAUSE_PROPERTY);
  }

  public Long getOrdinalNumber() {
    return getLongField(ORDINAL_PROPERTY);
  }

  public void setFilterGroup(String groupUri) {
    if (!setStringField(URI_FILTER_GROUP_PROPERTY, groupUri)) {
      throw new IllegalArgumentException("overflow filterGroup");
    }
  }

  public void setColumnVisibility(Visibility visibility) {
    if (!setStringField(VISIBILITY_PROPERTY, visibility.toString())) {
      throw new IllegalArgumentException("overflow visibility");
    }
  }

  public void setRowOrColumn(RowOrCol roworcol) {
    if (!setStringField(ROWORCOL_PROPERTY, roworcol.toString())) {
      throw new IllegalArgumentException("overflow row or col");
    }
  }

  public void setColumnTitle(String name) {
    if (!setStringField(COL_TITLE_PROPERTY, name)) {
      throw new IllegalArgumentException("overflow name");
    }
  }
  
  public void setColumnEncoding(String name) {
    if (!setStringField(COL_ENCODING_PROPERTY, name)) {
      throw new IllegalArgumentException("overflow column encoding");
    }
  }

  public void setFilterOperation(FilterOperation op) {
    if (!setStringField(OPERATION_PROPERTY, op.toString())) {
      throw new IllegalArgumentException("overflow filter operation");
    }
  }

  public void setFilterInputClause(String clause) {
    if (!setStringField(CLAUSE_PROPERTY, clause)) {
      throw new IllegalArgumentException("overflow clause");
    }
  }

  public void setOrdinalNumber(Long ordinal) {
    setLongField(ORDINAL_PROPERTY, ordinal);
  }

  void addColumn(SubmissionColumnFilter column) {
    if (colFilters == null) {
      colFilters = new ArrayList<SubmissionColumnFilter>();
    }
    colFilters.add(column);
  }

  void populate(CallingContext cc) throws ODKDatastoreException {
    if(getRowOrColumn().equals(RowOrCol.COLUMN)) {
      if (colFilters == null) {
        colFilters = new ArrayList<SubmissionColumnFilter>();
      }
      colFilters = SubmissionColumnFilter.getFilterList(this.getUri(), cc);
    }
  }
  
  public void persist(CallingContext cc) throws ODKEntityPersistException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    ds.putEntity(this, user);
    if(colFilters != null) {
      ds.putEntities(colFilters, user);
    }
    
  }
  
  public void delete(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    
    if(colFilters != null) {
      for(SubmissionColumnFilter filter : colFilters){
        ds.deleteEntity(new EntityKey(filter, filter.getUri()), user);
      }
    }    
    ds.deleteEntity(new EntityKey(this, this.getUri()), user);
  }
  
  public Filter transform() {
    RowOrCol type = getRowOrColumn();
    Filter filter;
    if (type.equals(RowOrCol.COLUMN)) {
      ColumnFilter columnFilter = new ColumnFilter(this.getUri());
      columnFilter.setVisibility(getColumnVisibility());
      
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
      Column header = new Column(getColumnTitle(), getColumnEncoding());
      rowFilter.setColumn(header);
      filter = rowFilter;
    }
   
    filter.setOrdinal(getOrdinalNumber());
    filter.setRc(getRowOrColumn());
    
    return filter;
  }

  private static SubmissionFilter relation = null;

  private static synchronized final SubmissionFilter assertRelation(CallingContext cc)
      throws ODKDatastoreException {
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

  static final SubmissionFilter transform(Filter filter, SubmissionFilterGroup filterGroup,
      CallingContext cc) throws ODKDatastoreException {

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
    
    if(filter instanceof ColumnFilter) {
      ColumnFilter cf = (ColumnFilter) filter;
      subFilter.setColumnVisibility(cf.getVisibility());
      
      for(ColumnFilterHeader column : cf.getColumnFilterHeaders()) {
        SubmissionColumnFilter columnFilter = SubmissionColumnFilter.transform(column, subFilter, cc);
        subFilter.addColumn(columnFilter);
      }
      
    } else if(filter instanceof RowFilter) {
      RowFilter rf = (RowFilter) filter;
      subFilter.setFilterOperation(rf.getOperation());
      subFilter.setFilterInputClause(rf.getInput());
      Column column = rf.getColumn();
      subFilter.setColumnTitle(column.getDisplayHeader());
      subFilter.setColumnEncoding(column.getColumnEncoding());
    }
    
    return subFilter;
  }

  static final List<SubmissionFilter> getFilterList(String uriFilterGroup, CallingContext cc)
      throws ODKDatastoreException {
    SubmissionFilter relation = assertRelation(cc);
    Query query = cc.getDatastore().createQuery(relation, cc.getCurrentUser());
    query.addFilter(SubmissionFilter.URI_FILTER_GROUP_PROPERTY,
        org.opendatakit.common.persistence.Query.FilterOperation.EQUAL, uriFilterGroup);

    List<SubmissionFilter> filterList = new ArrayList<SubmissionFilter>();

    List<? extends CommonFieldsBase> results = query.executeQuery(0);
    for (CommonFieldsBase cb : results) {
      if (cb instanceof SubmissionFilter) {
        SubmissionFilter filter = (SubmissionFilter) cb;
        filter.populate(cc);
        filterList.add(filter);
      }
    }
    return filterList;
  }

}
