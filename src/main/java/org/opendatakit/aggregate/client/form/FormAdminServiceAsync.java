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

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Date;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;

public interface FormAdminServiceAsync {

  void deleteForm(String formId, AsyncCallback<Void> callback);

  void purgePublishedData(String uriExternalService, Date earliest, AsyncCallback<Date> callback);

  void setFormAcceptSubmissions(String formId, Boolean acceptSubmissions, AsyncCallback<Void> callback);

  void setFormDownloadable(String formId, Boolean downloadable, AsyncCallback<Void> callback);

  void deleteSubmission(String submissionKeyAsString, AsyncCallback<Void> callback);

  void getIncompleteSubmissions(FilterGroup filter, AsyncCallback<SubmissionUISummary> callback);

  void markSubmissionAsComplete(String submissionKeyAsString, AsyncCallback<Void> callback);

  void getFormMediaFileList(String formId, AsyncCallback<ArrayList<MediaFileSummary>> callback);

  void purgeSubmissionsData(String formId, Date value, AsyncCallback<Date> asyncCallback);

}
