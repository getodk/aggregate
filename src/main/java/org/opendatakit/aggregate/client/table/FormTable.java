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

package org.opendatakit.aggregate.client.table;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.popups.MediaFileListPopup;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.widgets.AcceptSubmissionCheckBox;
import org.opendatakit.aggregate.client.widgets.DeleteFormButton;
import org.opendatakit.aggregate.client.widgets.DownloadableCheckBox;
import org.opendatakit.aggregate.client.widgets.EnketoWebformButton;
import org.opendatakit.aggregate.client.widgets.ExportButton;
import org.opendatakit.aggregate.client.widgets.PublishButton;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.common.security.client.UserSecurityInfo;

public class FormTable extends FlexTable {

  private static int TITLE_COLUMN = 0;
  private static String TITLE_HEADING = "Title";
  private static int FORM_ID_COLUMN = 1;
  private static String FORM_ID_HEADING = "Form Id";
  private static int MEDIA_COUNT_COLUMN = 2;
  private static String MEDIA_COUNT_HEADING = "Media files";
  private static int USER_COLUMN = 3;
  private static String USER_HEADING = "User";
  private static int DOWNLOADABLE_COLUMN = 4;
  private static String DOWNLOADABLE_HEADING = "Downloadable";
  private static int ACCEPT_SUBMISSIONS_COLUMN = 5;
  private static String ACCEPT_SUBMISSIONS_HEADING = "Accept Submissions";
  private static int PUBLISH_COLUMN = 6;
  private static String PUBLISH_HEADING = "Publish";
  private static int EXPORT_COLUMN = 7;
  private static String EXPORT_HEADING = "Export";
  private static int DELETE_COLUMN = 8;
  private static String DELETE_HEADING = "Delete";
  private static int ENKETO_COLUMN = 9;
  private static String ENKETO_HEADING = "Webform";
  private static String ENKETO_BUTTON_TEXT = "Enketo";

  public FormTable() {

    // create table headers
    setText(0, TITLE_COLUMN, TITLE_HEADING);
    setText(0, FORM_ID_COLUMN, FORM_ID_HEADING);
    setText(0, MEDIA_COUNT_COLUMN, MEDIA_COUNT_HEADING);
    setText(0, USER_COLUMN, USER_HEADING);
    setText(0, DOWNLOADABLE_COLUMN, DOWNLOADABLE_HEADING);
    setText(0, ACCEPT_SUBMISSIONS_COLUMN, ACCEPT_SUBMISSIONS_HEADING);
    setText(0, PUBLISH_COLUMN, PUBLISH_HEADING);
    setText(0, EXPORT_COLUMN, EXPORT_HEADING);
    setText(0, DELETE_COLUMN, DELETE_HEADING);
    if (Preferences.showEnketoIntegration()) {
      setText(0, ENKETO_COLUMN, ENKETO_HEADING);
    }
    // add styling
    getRowFormatter().addStyleName(0, "titleBar");
    addStyleName("dataTable");
  }

  /**
   * Update the list of forms
   *
   */
  public void updateFormTable(ArrayList<FormSummary> forms) {
    int i = 0;
    boolean shouldAddEnketoButton = Preferences.showEnketoIntegration();
    for (int j = 0; j < forms.size(); j++) {
      FormSummary form = forms.get(j);
      // don't show form if it has an active deletion request...
      FormActionStatusTimestamp t = form.getMostRecentDeletionRequestStatus();
      if (t != null && t.getStatus().isActiveRequest()) {
        continue;
      }
      // ok -- we should show this form...
      ++i;
      setWidget(i, TITLE_COLUMN, new HTML(new SafeHtmlBuilder().appendHtmlConstant(form.getViewableURL()).toSafeHtml()));
      setWidget(i, FORM_ID_COLUMN, new HTML(new SafeHtmlBuilder().appendEscaped(form.getId()).toSafeHtml()));

      Widget mediaCount;
      if (form.getMediaFileCount() > 0) {
        Anchor mediaCountLink = new Anchor(Integer.toString(form.getMediaFileCount()), true);
        mediaCountLink.addClickHandler(new MediaFileListClickHandler(form.getId()));
        mediaCount = mediaCountLink;
      } else {
        mediaCount = new HTML(new SafeHtmlBuilder().appendEscaped(Integer.toString(form.getMediaFileCount())).toSafeHtml());
      }
      setWidget(i, MEDIA_COUNT_COLUMN, mediaCount);

      String user = form.getCreatedUser();
      String displayName = UserSecurityInfo.getDisplayName(user);
      setText(i, USER_COLUMN, displayName);

      setWidget(i, DOWNLOADABLE_COLUMN,
          new DownloadableCheckBox(form.getId(), form.isDownloadable()));
      setWidget(i, ACCEPT_SUBMISSIONS_COLUMN,
          new AcceptSubmissionCheckBox(form.getId(), form.receiveSubmissions()));
      setWidget(i, PUBLISH_COLUMN, new PublishButton(form.getId()));
      setWidget(i, EXPORT_COLUMN, new ExportButton(form.getId()));
      setWidget(i, DELETE_COLUMN, new DeleteFormButton(form.getId()));
      if (shouldAddEnketoButton) {
        setWidget(i, ENKETO_COLUMN, new EnketoWebformButton(null, form.getId(), ENKETO_BUTTON_TEXT));

      }

      if (i % 2 == 0)
        getRowFormatter().addStyleName(i, "evenTableRow");
    }

    // remove any trailing rows...
    ++i; // to get number or rows in actual table...
    while (getRowCount() > i) {
      removeRow(getRowCount() - 1);
    }
  }

  private class MediaFileListClickHandler implements ClickHandler {

    private String formId;

    public MediaFileListClickHandler(String formId) {
      this.formId = formId;
    }

    @Override
    public void onClick(ClickEvent event) {
      MediaFileListPopup mediaListpopup = new MediaFileListPopup(formId);
      mediaListpopup.setPopupPositionAndShow(mediaListpopup.getPositionCallBack());
    }

  }

}
