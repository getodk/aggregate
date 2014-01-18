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

package org.opendatakit.aggregate.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.UiElementFormatter;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup.CompletionFlag;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.RepeatSubmissionType;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SubmissionServiceImpl extends RemoteServiceServlet implements
    org.opendatakit.aggregate.client.submission.SubmissionService {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -7997978505247614945L;

  @Override
  public SubmissionUISummary getSubmissions(FilterGroup filterGroup)
      throws FormNotAvailableException, RequestFailureException, DatastoreFailureException,
      AccessDeniedException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    try {
      String formId = filterGroup.getFormId();
      IForm form = FormFactory.retrieveFormByFormId(formId, cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed
                                                                                // definition
      }
      QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup,
          CompletionFlag.ONLY_COMPLETE_SUBMISSIONS, cc);

      SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
      GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
      headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());
      List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();
      ElementFormatter elemFormatter = new UiElementFormatter(cc.getServerURL(),
          headerGenerator.getGeopointIncludes());
      List<FormElementNamespace> includedTypes = headerGenerator.includedFormElementNamespaces();
      query.populateSubmissions(summary, filteredElements, elemFormatter, includedTypes, cc);

      return summary;

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }

  }

  @Override
  public SubmissionUISummary getRepeatSubmissions(String keyString)
      throws FormNotAvailableException, RequestFailureException, DatastoreFailureException,
      AccessDeniedException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    if (keyString == null) {
      return null;
    }

    try {
      SubmissionKey key = new SubmissionKey(keyString);
      List<SubmissionKeyPart> parts = key.splitSubmissionKey();
      IForm form = FormFactory.retrieveFormByFormId(parts.get(0).getElementName(), cc);
      if (!form.hasValidFormDefinition()) {
        throw new RequestFailureException(ErrorConsts.FORM_DEFINITION_INVALID); // ill-formed
                                                                                // definition
      }
      Submission sub = Submission.fetchSubmission(parts, cc);

      if (sub != null) {
        SubmissionElement tmp = sub.resolveSubmissionKey(parts);
        RepeatSubmissionType repeat = (RepeatSubmissionType) tmp;

        SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
        GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(null, summary, form);
        headerGenerator.processForHeaderInfo(repeat.getElement());
        List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();
        ElementFormatter elemFormatter = new UiElementFormatter(cc.getServerURL(),
            headerGenerator.getGeopointIncludes());

        // format row elements
        for (SubmissionSet subSet : repeat.getSubmissionSets()) {
          Row row = subSet.getFormattedValuesAsRow(filteredElements, elemFormatter, false, cc);
          try {
            summary.addSubmission(new SubmissionUI(row.getFormattedValues(), null));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        return summary;

      } else {
        return null;
      }

    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      throw new FormNotAvailableException(e);
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      throw new RequestFailureException(ErrorConsts.QUOTA_EXCEEDED);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

}
