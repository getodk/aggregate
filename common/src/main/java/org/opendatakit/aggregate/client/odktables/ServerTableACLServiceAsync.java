/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.odktables;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerTableACLServiceAsync {

  void getAcls(String tableId, AsyncCallback<ArrayList<TableAclClient>> callback);

  void getUserAcls(String tableId, AsyncCallback<ArrayList<TableAclClient>> callback);

  void getGroupAcls(String tableId, AsyncCallback<ArrayList<TableAclClient>> callback);

  void getDefaultAcl(String tableId, AsyncCallback<TableAclClient> callback);

  void getUserAcl(String userId, String tableId, AsyncCallback<TableAclClient> callback);

  void getGroupAcl(String groupId, String tableId, AsyncCallback<TableAclClient> callback);

  void setDefaultAcl(TableAclClient acl, String tableId, AsyncCallback<TableAclClient> callback);

  void setUserAcl(String userId, TableAclClient acl, String tableId,
      AsyncCallback<TableAclClient> callback);

  void setGroupAcl(String groupId, TableAclClient acl, String tableId,
      AsyncCallback<TableAclClient> callback);

  void deleteDefaultAcl(String tableId, AsyncCallback<Void> callback);

  void deleteUserAcl(String userId, String tableId, AsyncCallback<Void> callback);

  void deleteGroupAcl(String groupId, String tableId, AsyncCallback<Void> callback);

}
