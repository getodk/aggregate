package org.opendatakit.aggregate.filter;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
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
  private static final DataField TITLE_PROPERTY = new DataField("TITLE",
      DataField.DataType.STRING, true, 80L); // TODO: determine length
  private static final DataField OPERATION_PROPERTY = new DataField("OPERATION",
      DataField.DataType.STRING, true, 80L);
  private static final DataField CLAUSE_PROPERTY = new DataField("INPUT_CLAUSE",
      DataField.DataType.STRING, true, 4096L);
  private static final DataField ORDINAL_PROPERTY = new DataField("ORDINAL",
      DataField.DataType.INTEGER, true);

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
    fieldList.add(TITLE_PROPERTY);
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

  public String getColumn() {
    return getStringField(TITLE_PROPERTY);
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

  public void setColumn(String name) {
    if (!setStringField(TITLE_PROPERTY, name)) {
      throw new IllegalArgumentException("overflow name");
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

  public Filter transform() {
    Filter filter = new Filter(this.getUri());

    filter.setVisibility(getColumnVisibility());
    filter.setTitle(getColumn());
    filter.setOperation(getFilterOperation());
    filter.setInput(getFilterInputClause());
    filter.setOrdinal(getOrdinalNumber());
    
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
    
    subFilter.setFilterGroup(filterGroup.getUri());
    subFilter.setColumnVisibility(filter.getVisibility());
    subFilter.setColumn(filter.getTitle());
    subFilter.setFilterOperation(filter.getOperation());
    subFilter.setFilterInputClause(filter.getInput());
    subFilter.setOrdinalNumber(filter.getOrdinal());
    return subFilter;
  }
  
  static final List<SubmissionFilter> getFilterList(String uriFilterGroup,
      CallingContext cc) throws ODKDatastoreException {
    SubmissionFilter relation = assertRelation(cc);
    Query query = cc.getDatastore().createQuery(relation, cc.getCurrentUser());
    query.addFilter(SubmissionFilter.URI_FILTER_GROUP_PROPERTY, org.opendatakit.common.persistence.Query.FilterOperation.EQUAL, uriFilterGroup);
    
    List<SubmissionFilter> filterList = new ArrayList<SubmissionFilter>();

    List<? extends CommonFieldsBase> results = query.executeQuery(0);
    for (CommonFieldsBase cb : results) {
      if(cb instanceof SubmissionFilter) {        
        filterList.add((SubmissionFilter)cb);
      }
    }
    return filterList;
  }
  
}
