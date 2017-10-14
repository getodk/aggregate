/*
 * Copyright (C) 2017 University of Washington
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

package org.opendatakit.aggregate.odktables.rest.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This holds the list of UserInfo structures describing
 * the users that can manipulate odkTables appName data. This may
 * include users with a ROLE_USER but without any synchronization
 * capability.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="userInfoList")
public class UserInfoList extends ArrayList<UserInfo> {

  /**
   * 
   */
  private static final long serialVersionUID = -566890619427903300L;

  /**
   * Constructor used by Jackson
   */
  public UserInfoList() {
  }

  public UserInfoList(List<UserInfo> userInfo) {
    super.addAll(userInfo);
  }
}
