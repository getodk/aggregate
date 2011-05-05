/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.constants.externalservice;

import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.externalservice.JsonServer;
import org.opendatakit.aggregate.servlet.FusionTableServlet;
import org.opendatakit.aggregate.servlet.JsonServlet;
import org.opendatakit.aggregate.servlet.SpreadsheetServlet;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.web.CallingContext;

/**
 * Enum of all the available external services. Provides UI and type mapping
 * information
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public enum ExternalServiceType {
  GOOGLE_SPREADSHEET("Google Spreadsheet", SpreadsheetServlet.ADDR,
      SpreadsheetConsts.SPEADSHEET_NAME_LABEL), JSON_SERVER("JSON Server",
      JsonServlet.ADDR, "JSON Server Address"), GOOGLE_FUSIONTABLES(
      "Google FusionTables", FusionTableServlet.ADDR, null);

  private String serviceName;

  private String addr;

  private String descriptionOfParam;

  private ExternalServiceType(String name, String servletAddr, String desc) {
    serviceName = name;
    addr = servletAddr;
    descriptionOfParam = desc;
  }

  public String getAddr() {
    return addr;
  }

  public String getDescriptionOfParam() {
    return descriptionOfParam;
  }

  public String getServiceName() {
    return serviceName;
  }

  public ExternalService constructExternalService(FormServiceCursor fsc, CallingContext cc)
  			throws ODKEntityNotFoundException {
    try {
      switch (this) {
      case GOOGLE_FUSIONTABLES:
        return new FusionTable(fsc, cc);
      case GOOGLE_SPREADSHEET:
        return new GoogleSpreadsheet(fsc, cc);
      case JSON_SERVER:
        return new JsonServer(fsc, cc);
      default:
        return null;
      }
    } catch (Exception e) {
      throw new ODKEntityNotFoundException("Some how DB entities got into problem state", e);
    }
  }

}