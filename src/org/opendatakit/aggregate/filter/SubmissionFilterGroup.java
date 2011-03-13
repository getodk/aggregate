package org.opendatakit.aggregate.filter;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.User;

/**
 * Creates the database interface for filter group objects
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionFilterGroup extends CommonFieldsBase {

  private static final String TABLE_NAME = "_filter_group";

  private static final DataField FORM_ID_PROPERTY = new DataField("FORM_ID",
      DataField.DataType.STRING, true, PersistConsts.GUARANTEED_SEARCHABLE_LEN);
  private static final DataField NAME_PROPERTY = new DataField("NAME", DataField.DataType.STRING,
      true, PersistConsts.GUARANTEED_SEARCHABLE_LEN);
  private static final DataField URI_USER_PROPERTY = new DataField("URI_USER",
      DataField.DataType.URI, false, PersistConsts.URI_STRING_LEN).setIndexable(IndexType.HASH);
  private static final DataField IS_PUBLIC = new DataField("IS_PUBLIC", DataField.DataType.BOOLEAN,
      true);

  private List<SubmissionFilter> filters;

  /**
   * Construct a relation prototype.
   * 
   * @param databaseSchema
   * @param tableName
   */
  private SubmissionFilterGroup(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(FORM_ID_PROPERTY);
    fieldList.add(NAME_PROPERTY);
    fieldList.add(URI_USER_PROPERTY);
    fieldList.add(IS_PUBLIC);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   * 
   * @param ref
   * @param user
   */
  private SubmissionFilterGroup(SubmissionFilterGroup ref, User user) {
    super(ref, user);
  }

  @Override
  public SubmissionFilterGroup getEmptyRow(User user) {
    return new SubmissionFilterGroup(this, user);
  }

  public String getFormId() {
    return getStringField(FORM_ID_PROPERTY);
  }

  public void setFormId(String formId) {
    if (!setStringField(FORM_ID_PROPERTY, formId)) {
      throw new IllegalArgumentException("overflow form id");
    }
  }

  public String getName() {
    return getStringField(NAME_PROPERTY);
  }

  public void setName(String groupName) {
    if (!setStringField(NAME_PROPERTY, groupName)) {
      throw new IllegalArgumentException("overflow name");
    }
  }

  public Boolean isPublic() {
    return getBooleanField(IS_PUBLIC);
  }

  public void setIsPublic(Boolean value) {
    setBooleanField(IS_PUBLIC, value);
  }

  List<SubmissionFilter> getFilters() {
    return filters;
  }

  void addFilter(SubmissionFilter filter) {
    if (filters == null) {
      filters = new ArrayList<SubmissionFilter>();
    }
    filters.add(filter);
  }

  void addFilters(List<SubmissionFilter> filter) {
    if (filters == null) {
      filters = new ArrayList<SubmissionFilter>();
    }
    filters.addAll(filter);
  }
  
  public FilterGroup transform() {
    FilterGroup filterGroup = new FilterGroup(this.getUri());

    filterGroup.setName(getName());
    filterGroup.setFormId(getFormId());

    if (filters != null) {
      for (SubmissionFilter filter : filters) {
        filterGroup.addFilter(filter.transform());
      }
    }
    return filterGroup;
  }

  public void persist(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    // before we persist get a list of all the filters that existed before the persist
    List<SubmissionFilter> oldFilters = SubmissionFilter.getFilterList(this.getUri(), cc);

    // persist filter group
    ds.putEntity(this, user);
    
    if(filters != null) {
      // persist filters
      for(SubmissionFilter filter : filters) {
        filter.persist(cc);
      }
      
      // remove all the old filters that are no longer part of the filterGroup
      for(SubmissionFilter oldFilter : oldFilters){
        if(!filters.contains(oldFilter)) {
          oldFilter.delete(cc);
        }
      }
    } else {
      // no filters are in this filter group so remove all old filters
      for(SubmissionFilter filter : oldFilters){
        filter.delete(cc);
      }
    }
  }
  
  public void delete(CallingContext cc) throws ODKDatastoreException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    
    if(filters != null) {
      for(SubmissionFilter filter : filters){
        filter.delete(cc);
      }
    }    
    ds.deleteEntity(new EntityKey(this, this.getUri()), user);
  }
  
  private static SubmissionFilterGroup relation = null;

  private static synchronized final SubmissionFilterGroup assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      SubmissionFilterGroup relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new SubmissionFilterGroup(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  public static final SubmissionFilterGroup getFilterGroup(String uri, CallingContext cc)
      throws ODKEntityNotFoundException {
    try {
      SubmissionFilterGroup relation = assertRelation(cc);
      CommonFieldsBase entity = cc.getDatastore().getEntity(relation, uri, cc.getCurrentUser());
      return (SubmissionFilterGroup) entity;
    } catch (ODKDatastoreException e) {
      throw new ODKEntityNotFoundException(e);
    }
  }

  public static final SubmissionFilterGroup transform(FilterGroup filterGroup, CallingContext cc)
      throws ODKDatastoreException {

    SubmissionFilterGroup relation = assertRelation(cc);
    String uri = filterGroup.getUri();
    SubmissionFilterGroup subFilterGroup;

    if (uri.equals(UIConsts.URI_DEFAULT)) {
      subFilterGroup = cc.getDatastore().createEntityUsingRelation(relation, cc.getCurrentUser());
    } else {
      subFilterGroup = getFilterGroup(uri, cc);
    }

    subFilterGroup.setName(filterGroup.getName());
    subFilterGroup.setFormId(filterGroup.getFormId());

    for (Filter filter : filterGroup.getFilters()) {
      SubmissionFilter subFilter = SubmissionFilter.transform(filter, subFilterGroup, cc);
      subFilterGroup.addFilter(subFilter);
    }

    return subFilterGroup;
  }
  
  public static final List<SubmissionFilterGroup> getFilterGroupList(String formId,
      CallingContext cc) throws ODKDatastoreException {
    SubmissionFilterGroup relation = assertRelation(cc);
    Query query = cc.getDatastore().createQuery(relation, cc.getCurrentUser());
    query.addFilter(SubmissionFilterGroup.FORM_ID_PROPERTY, FilterOperation.EQUAL, formId);
    
    List<SubmissionFilterGroup> filterGroupList = new ArrayList<SubmissionFilterGroup>();

    List<? extends CommonFieldsBase> results = query.executeQuery(0);
    for (CommonFieldsBase cb : results) {
      if(cb instanceof SubmissionFilterGroup) {
        SubmissionFilterGroup tmp = (SubmissionFilterGroup)cb;
        List<SubmissionFilter> filters = SubmissionFilter.getFilterList(tmp.getUri(), cc);
        tmp.addFilters(filters);
        filterGroupList.add(tmp);
      }
    }
    return filterGroupList;
  }
}