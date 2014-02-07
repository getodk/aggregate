/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.server;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.ETagMismatchExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.ServerPropertiesService;
import org.opendatakit.aggregate.client.odktables.TablePropertiesClient;
import org.opendatakit.aggregate.odktables.PropertiesManager;
import org.opendatakit.aggregate.odktables.entity.UtilTransforms;
import org.opendatakit.aggregate.odktables.exception.ETagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.rest.entity.TableProperties;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissionsImpl;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerPropertiesServiceImpl extends RemoteServiceServlet implements
    ServerPropertiesService {

  /**
	 *
	 */
  private static final long serialVersionUID = -109897492777781438L;

  @Override
  public TablePropertiesClient getProperties(String tableId) throws AccessDeniedException,
      RequestFailureException, DatastoreFailureException, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      PropertiesManager pm = new PropertiesManager(tableId, userPermissions, cc);
      TableProperties properties = pm.getProperties();
      return UtilTransforms.transform(properties);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    }
  }

  @Override
  public TablePropertiesClient setProperties(TablePropertiesClient properties, String tableId)
      throws AccessDeniedException, RequestFailureException, DatastoreFailureException,
      ETagMismatchExceptionClient, PermissionDeniedExceptionClient {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    try {
      TablesUserPermissions userPermissions = new TablesUserPermissionsImpl(cc.getCurrentUser()
          .getUriUser(), cc);
      PropertiesManager pm = new PropertiesManager(tableId, userPermissions, cc);
      // this seems fishy. Originally was passing in the parameter of type
      // TablePropertiesClient, but it isn't clear why I need to, when
      // nothing is being explicitly set. Fixed by changing the type of
      // the variable, but should be wary of this.
      // first make the type TableProperties object
      TableProperties tableProperties = new TableProperties(properties.getSchemaETag(), properties.getPropertiesETag(),
          properties.getTableId(), UtilTransforms.transformToServerEntries(properties
              .getKeyValueStoreEntries()));
      tableProperties = pm.setProperties(tableProperties);
      return UtilTransforms.transform(tableProperties);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw new PermissionDeniedExceptionClient(e);
    } catch (ETagMismatchException e) {
      e.printStackTrace();
      throw new ETagMismatchExceptionClient(e);
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw new RequestFailureException(e);
    }
  }

}
