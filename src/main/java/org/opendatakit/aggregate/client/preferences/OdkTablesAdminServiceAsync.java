package org.opendatakit.aggregate.client.preferences;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface OdkTablesAdminServiceAsync {

  void addAdmin(OdkTablesAdmin admin, AsyncCallback<Boolean> callback);

  void deleteAdmin(String aggregateUid, AsyncCallback<Boolean> callback);

  void listAdmin(AsyncCallback<OdkTablesAdmin[]> callback);

  void updateAdmin(OdkTablesAdmin admin, AsyncCallback<Boolean> callback);

}
