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

package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.ExportSummary;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * List all the requests for downloadable documents and their status.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class ExportSheet extends FlexTable {

	private int FILE_TYPE = 0;
	private int STATUS = 1;
	private int TIME_REQUESTED = 2;
	private int TIME_COMPLETED = 3;
	private int TIME_LAST_RETRY = 4;
	private int DOWNLOAD_FILE = 5;

	public ExportSheet() {
		super();
		this.setText(0, FILE_TYPE, "File Type");
		this.setText(0, STATUS, "Status");
		this.setText(0, TIME_REQUESTED, "Time Requested");
		this.setText(0, TIME_COMPLETED, "Time Completed");
		this.setText(0, TIME_LAST_RETRY, "Last Retry");
		this.setText(0, DOWNLOAD_FILE, "Download File");
		this.addStyleName("dataTable");
		this.getRowFormatter().addStyleName(0, "titleBar");
	}

	public void updateExportPanel(ExportSummary[] eS) {
		if (eS == null)
			return;
		while (this.getRowCount() > 1)
			this.removeRow(1);
		for (int i = 0; i < eS.length; i++) {
			ExportSummary e = eS[i];
			if (e.getFileType() != null)
				this.setText(i + 1, FILE_TYPE, e.getFileType().toString());
			if (e.getStatus() != null)
				this.setText(i + 1, STATUS, e.getStatus().toString());
			if (e.getTimeRequested() != null)
				this.setText(i + 1, TIME_REQUESTED, e.getTimeRequested()
						.toString());
			if (e.getTimeCompleted() != null)
				this.setText(i + 1, TIME_COMPLETED, e.getTimeCompleted()
						.toString());
			if (e.getTimeLastAction() != null)
				this.setText(i + 1, TIME_LAST_RETRY, e.getTimeLastAction()
						.toString());
			if (e.getResultFile() != null)
				this.setWidget(i + 1, DOWNLOAD_FILE,
						new HTML(e.getResultFile()));
		}
	}

}
