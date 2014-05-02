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

package org.opendatakit.aggregate.client.popups;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.odktables.ColumnClient;
import org.opendatakit.aggregate.client.odktables.TableDefinitionClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.OdkTablesAddTableButton;
import org.opendatakit.aggregate.client.widgets.OdkTablesTableIdBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * This popup creates a table and adds it to the datastore.
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesAddNewTablePopup extends AbstractPopupBase {

	  private TableDefinitionClient tableDef;

	  // textbox for the tableid
	  private OdkTablesTableIdBox idBox;

	  public OdkTablesAddNewTablePopup() {
	    super();

	    AggregateButton addTableButton = new OdkTablesAddTableButton();
	    addTableButton.addClickHandler(new ExecuteAdd());

	    idBox = new OdkTablesTableIdBox(this);

	    FlexTable layout = new FlexTable();

	    HTML message = new HTML("You are adding a new table.");
	    layout.setWidget(0, 0, message);
	    layout.setWidget(1, 0, new HTML("TableId:"));
       layout.setWidget(1, 1, idBox);
	    layout.setWidget(2, 1, addTableButton);
	    layout.setWidget(2, 2, new ClosePopupButton(this));

	    setWidget(layout);
	  }

	  private class ExecuteAdd implements ClickHandler {

	    @Override
	    public void onClick(ClickEvent event) {

	      String tableId = idBox.getValue();

         ArrayList<ColumnClient> columns = new ArrayList<ColumnClient>(0);
         tableDef = new TableDefinitionClient(tableId, columns);

	      // Set up the callback object.
	      AsyncCallback<TableEntryClient> callback = new AsyncCallback<TableEntryClient>() {
	        @Override
	        public void onFailure(Throwable caught) {
	          AggregateUI.getUI().reportError(caught);
	        }

	        @Override
	        public void onSuccess(TableEntryClient table) {
	          AggregateUI.getUI().clearError();


	          AggregateUI.getUI().getTimer().refreshNow();
	        }
	      };
	      Window.alert("before call");
	      // Make the call to the form service. null tableId so that the
	      // server knows to generate a random UUID.
	      SecureGWT.getServerTableService().createTable(null,
	    		  tableDef, callback);
	      hide();
	    }
	  }
}
