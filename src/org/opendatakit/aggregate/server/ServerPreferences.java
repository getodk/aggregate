package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class ServerPreferences  extends CommonFieldsBase {

  private static final String TABLE_NAME = "_server_preferences";

  private static final DataField GOOGLE_MAP_KEY = new DataField("GOOG_MAPS_API_KEY", DataField.DataType.STRING,
      true, 128L);
  
  /**
   * Construct a relation prototype.
   * 
   * @param databaseSchema
   * @param tableName
   */
  private ServerPreferences(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(GOOGLE_MAP_KEY);
  }

  /**
   * Construct an empty entity. Only called via {@link #getEmptyRow(User)}
   * 
   * @param ref
   * @param user
   */
  private ServerPreferences(ServerPreferences ref, User user) {
    super(ref, user);
  }

  @Override
  public ServerPreferences getEmptyRow(User user) {
    return new ServerPreferences(this, user);
  }

  public String getGoogleMapApiKey() {
    return getStringField(GOOGLE_MAP_KEY);
  }
  
  public void setGoogleMapApiKey(String googleMapsApiKey) {
    if (!setStringField(GOOGLE_MAP_KEY, googleMapsApiKey)) {
      throw new IllegalArgumentException("overflow filterGroup");
    }
  }
  
  public void persist(CallingContext cc) throws ODKEntityPersistException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    ds.putEntity(this, user);    
  }
  
  private static ServerPreferences relation = null;

  public static synchronized final ServerPreferences assertRelation(CallingContext cc)
      throws ODKDatastoreException {
    if (relation == null) {
      ServerPreferences relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new ServerPreferences(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  
  public static final ServerPreferences getServerPreferences(CallingContext cc) throws ODKEntityNotFoundException {
    try {
      ServerPreferences relation = assertRelation(cc);
      Query query = cc.getDatastore().createQuery(relation, cc.getCurrentUser());

      List<? extends CommonFieldsBase> results = query.executeQuery(0);
      if(!results.isEmpty()) {
        if(results.get(0) instanceof ServerPreferences) {
          ServerPreferences preferences = (ServerPreferences)results.get(0);
          return preferences;
        }
      }
      return null;
    } catch (ODKDatastoreException e) {
      throw new ODKEntityNotFoundException(e);
    }
  }
}
