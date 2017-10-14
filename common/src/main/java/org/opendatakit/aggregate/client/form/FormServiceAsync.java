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

package org.opendatakit.aggregate.client.form;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.filter.FilterGroup;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FormServiceAsync {

  void getForms(AsyncCallback<ArrayList<FormSummary>> callback);

  void getExports(AsyncCallback<ArrayList<ExportSummary>> callback);

  void getPossibleKmlSettings(String formId, AsyncCallback<KmlOptionsSummary> callback);

  void getGpsCoordnates(String formId, AsyncCallback<GeopointElementList> callback);

  void createCsvFromFilter(FilterGroup group, AsyncCallback<Boolean> callback);

  void createKmlFromFilter(FilterGroup group, ArrayList<KmlSelection> kmlElementsToInclude,
      AsyncCallback<Boolean> callback);

  void createJsonFileFromFilter(FilterGroup group, AsyncCallback<Boolean> callback);

  void deleteExport(String uri, AsyncCallback<Void> callback);

}
