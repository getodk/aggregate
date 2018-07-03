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

package org.opendatakit.common.security.client.security.admin;

import com.google.gwt.user.server.rpc.XsrfProtect;
import java.util.ArrayList;

import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * These are the APIs available to users with the ROLE_ACCESS_ADMIN privilege.
 * Because this interface includes the change-password API, it requires
 * an SSL connection on servers that support SSL.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@RemoteServiceRelativePath("securityadminservice")
public interface SecurityAdminService extends RemoteService {

    /**
     * 
     * @param withAuthorities if true, populate the groups and granted authorities sets.
     * @return all registered users and the Anonymous user.
     * @throws AccessDeniedException
     * @throws DatastoreFailureException
     */
    ArrayList<UserSecurityInfo> getAllUsers(boolean withAuthorities) throws AccessDeniedException, DatastoreFailureException;

    @XsrfProtect
    void setUsersAndGrantedAuthorities( String xsrfString, ArrayList<UserSecurityInfo> users, ArrayList<GrantedAuthorityName> allGroups ) throws AccessDeniedException, DatastoreFailureException;
}
