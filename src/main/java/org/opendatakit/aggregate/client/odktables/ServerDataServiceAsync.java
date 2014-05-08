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

/**
 * The async server side.
 *
 * @author sudar.sam@gmail.com
 *
 */
public interface ServerDataServiceAsync {

  void getRows(String tableId, AsyncCallback<ArrayList<RowClient>> callback);

  void getRow(String tableId, String rowId, AsyncCallback<TableContentsClient> callback);

  void createOrUpdateRow(String tableId, String rowId, RowClient row,
      AsyncCallback<RowClient> callback);

  void deleteRow(String tableId, String rowId, AsyncCallback<Void> callback);

  void getColumnNames(String tableId, AsyncCallback<ArrayList<String>> callback);

  void getNonMediaFiles(String tableId, AsyncCallback<ArrayList<FileSummaryClient>> callback);

  void getFileRowInfoColumnNames(AsyncCallback<ArrayList<String>> callback);

  void getTableContents(String tableId, AsyncCallback<TableContentsClient> callback);

  void getAppLevelFileInfoContents(AsyncCallback<TableContentsForFilesClient> callback);

  void getTableFileInfoContents(String tableId, AsyncCallback<TableContentsForFilesClient> callback);

  void getInstanceFileInfoContents(String tableId, AsyncCallback<TableContentsForFilesClient> callback);

  void deleteTableFile(String tableId, String rowId, AsyncCallback<Void> callback);

  void getMedialFilesKey(String tableId, String key,
      AsyncCallback<ArrayList<FileSummaryClient>> callback);

}
