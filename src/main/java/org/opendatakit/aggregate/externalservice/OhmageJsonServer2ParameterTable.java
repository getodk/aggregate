/*
  Copyright (C) 2010-2013 University of Washington
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
package org.opendatakit.aggregate.externalservice;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class OhmageJsonServer2ParameterTable extends CommonFieldsBase {

  private static final String TABLE_NAME = "_ohmage_json_server2";

  private static final DataField CAMPAIGN_URN_PROPERTY = new DataField("CAMPAIGN_URN", DataField.DataType.STRING, true, 4096L);
  private static final DataField CAMPAIGN_TIMESTAMP_PROPERTY = new DataField("CAMPAIGN_TIMESTAMP", DataField.DataType.STRING, true);
  private static final DataField OHMAGE_USERNAME_PROPERTY = new DataField("OHMAGE_USERNAME", DataField.DataType.STRING, true);
  private static final DataField OHMAGE_HASHED_PASSWORD_PROPERTY = new DataField("OHMAGE_HASHED_PASSWORD", DataField.DataType.STRING, true);
  private static final DataField OWNER_EMAIL_PROPERTY = new DataField("OWNER_EMAIL", DataField.DataType.STRING, true, 4096L);
  private static final DataField SERVER_URL_PROPERTY = new DataField("SERVER_URL", DataField.DataType.STRING, true, 4096L);
  private static OhmageJsonServer2ParameterTable relation = null;

  OhmageJsonServer2ParameterTable(String schemaName) {
    super(schemaName, TABLE_NAME);
    fieldList.add(CAMPAIGN_URN_PROPERTY);
    fieldList.add(CAMPAIGN_TIMESTAMP_PROPERTY);
    fieldList.add(OHMAGE_USERNAME_PROPERTY);
    fieldList.add(OHMAGE_HASHED_PASSWORD_PROPERTY);
    fieldList.add(OWNER_EMAIL_PROPERTY);
    fieldList.add(SERVER_URL_PROPERTY);
  }

  private OhmageJsonServer2ParameterTable(OhmageJsonServer2ParameterTable ref, User user) {
    super(ref, user);
  }

  public static synchronized final OhmageJsonServer2ParameterTable assertRelation(CallingContext cc) throws ODKDatastoreException {
    if (relation == null) {
      OhmageJsonServer2ParameterTable relationPrototype;
      Datastore ds = cc.getDatastore();
      User user = cc.getUserService().getDaemonAccountUser();
      relationPrototype = new OhmageJsonServer2ParameterTable(ds.getDefaultSchemaName());
      ds.assertRelation(relationPrototype, user); // may throw exception...
      // at this point, the prototype has become fully populated
      relation = relationPrototype; // set static variable only upon success...
    }
    return relation;
  }

  @Override
  public OhmageJsonServer2ParameterTable getEmptyRow(User user) {
    return new OhmageJsonServer2ParameterTable(this, user);
  }

  public String getOwnerEmail() {
    return getStringField(OWNER_EMAIL_PROPERTY);
  }

  public void setOwnerEmail(String value) {
    if (!setStringField(OWNER_EMAIL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow ownerEmail");
    }
  }

  public String getOhmageCampaignUrn() {
    return getStringField(CAMPAIGN_URN_PROPERTY);
  }

  public void setOhmageCampaignUrn(String value) {
    if (!setStringField(CAMPAIGN_URN_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of Ohmage campaignUrn");
    }
  }

  public String getOhmageCampaignCreationTimestamp() {
    return getStringField(CAMPAIGN_TIMESTAMP_PROPERTY);
  }

  public void setOhmageCampaignCreationTimestamp(String value) {
    if (!setStringField(CAMPAIGN_TIMESTAMP_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of Ohmage campaignCreationTimestamp");
    }
  }

  public String getOhmageUsername() {
    return getStringField(OHMAGE_USERNAME_PROPERTY);
  }

  public void setOhmageUsername(String value) {
    if (!setStringField(OHMAGE_USERNAME_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of Ohmage Username");
    }
  }

  public String getOhmageHashedPassword() {
    return getStringField(OHMAGE_HASHED_PASSWORD_PROPERTY);
  }

  public void setOhmageHashedPassword(String value) {
    if (!setStringField(OHMAGE_HASHED_PASSWORD_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of Ohmage hashed password");
    }
  }

  public String getServerUrl() {
    return getStringField(SERVER_URL_PROPERTY);
  }

  public void setServerUrl(String value) {
    if (!setStringField(SERVER_URL_PROPERTY, value)) {
      throw new IllegalArgumentException("overflow of serverUrl");
    }
  }

}