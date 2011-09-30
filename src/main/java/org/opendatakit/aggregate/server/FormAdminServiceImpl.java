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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.MediaFileSummary;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.UiElementFormatter;
import org.opendatakit.aggregate.process.DeleteSubmissions;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup.CompletionFlag;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.SubmissionVisitor;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.task.FormDelete;
import org.opendatakit.aggregate.task.PurgeOlderSubmissions;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FormAdminServiceImpl extends RemoteServiceServlet implements
		org.opendatakit.aggregate.client.form.FormAdminService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2513124088714784947L;

	@Override
	public Boolean setFormDownloadable(String formId, Boolean downloadable) {
		HttpServletRequest req = this.getThreadLocalRequest();
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		try {
			Form form = Form.retrieveFormByFormId(formId, cc);
			if ( form.getFormDefinition() == null ) return false; // ill-formed definition
			form.setDownloadEnabled(downloadable);
			form.persist(cc);
			return true;
		} catch (ODKFormNotFoundException e1) {
			return false;
		} catch (ODKDatastoreException e) {
			return false;
		}
	}

	@Override
	public Boolean setFormAcceptSubmissions(String formId,
			Boolean acceptSubmissions) {
		HttpServletRequest req = this.getThreadLocalRequest();
		CallingContext cc = ContextFactory.getCallingContext(this, req);

		try {
			Form form = Form.retrieveFormByFormId(formId, cc);
			if ( form.getFormDefinition() == null ) return false; // ill-formed definition
			form.setSubmissionEnabled(acceptSubmissions);
			form.persist(cc);
			return true;
		} catch (ODKFormNotFoundException e1) {
			return false;
		} catch (ODKDatastoreException e) {
			return false;
		}
	}

	public Date purgePublishedData(String uriExternalService, Date earliest)
			throws AccessDeniedException, DatastoreFailureException, RequestFailureException {
		HttpServletRequest req = this.getThreadLocalRequest();
		CallingContext cc = ContextFactory.getCallingContext(this, req);
		
		FormServiceCursor fsc;
		try {
			fsc = FormServiceCursor.getFormServiceCursor(uriExternalService, cc);
		} catch (ODKEntityNotFoundException e) {
			e.printStackTrace();
			throw new RequestFailureException("Unable to retrieve Publishing configuration");
		}

		// any confirm parameter value means OK -- purge it!
		PurgeOlderSubmissions pos = 
			(PurgeOlderSubmissions) cc.getBean(BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN);
		
		if ( pos == null ) {
			// TODO: throw generic server failure exception...
			return null;
		}
		// set up the purge request here...
		Map<String,String> parameters = new HashMap<String,String>();
	  
		parameters.put( PurgeOlderSubmissions.PURGE_DATE, 
						PurgeOlderSubmissions.PURGE_DATE_FORMAT.format(earliest));
		Form form;
		try {
			form = Form.retrieveFormByFormId(fsc.getFormId(), cc);
			if ( form.getFormDefinition() == null ) {
				throw new RequestFailureException("Form " + fsc.getFormId() + " is ill-formed.");
			}
		} catch (ODKFormNotFoundException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to retrieve form " + fsc.getFormId());
		}

		MiscTasks m;
		try {
			m = new MiscTasks(TaskType.PURGE_OLDER_SUBMISSIONS, form, parameters, cc);
			m.persist(cc);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to establish task to purge submitted data for form " + fsc.getFormId());
		}
    	CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
    	ccDaemon.setAsDaemon(true);
    	try {
			pos.createPurgeOlderSubmissionsTask(form, m.getSubmissionKey(), 1L, ccDaemon);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to establish task to purge submitted data for form " + fsc.getFormId());
		} catch (ODKFormNotFoundException e) {
			e.printStackTrace();
        	throw new RequestFailureException("Unable to establish task to purge submitted data for form " + fsc.getFormId());
		}
    	return earliest;
	}

	  @Override
	  public Boolean deleteForm(String formId) throws AccessDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);

	    try {
	      FormDelete formDelete = (FormDelete) cc.getBean(BeanDefs.FORM_DELETE_BEAN);

	      Form formToDelete = Form.retrieveFormByFormId(formId, cc);

	      // If the FormInfo table is the target, log an error!
	      if (formToDelete != null) {
	        MiscTasks m = new MiscTasks(TaskType.DELETE_FORM, formToDelete, null, cc);
	        m.persist(cc);
	        CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
	        ccDaemon.setAsDaemon(true);
	        formDelete.createFormDeleteTask(formToDelete, m.getSubmissionKey(), 1L, ccDaemon);
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      return false;
	    }

	    return true;
	  }

    @Override
    public Boolean deleteSubmission(String submissionKeyAsString) throws AccessDeniedException {
      HttpServletRequest req = this.getThreadLocalRequest();
      CallingContext cc = ContextFactory.getCallingContext(this, req);
      
      // create a list because the submission deleter require a list
      SubmissionKey subKey = new SubmissionKey(submissionKeyAsString);
      List<SubmissionKey> keyList = new ArrayList<SubmissionKey>();
      keyList.add(subKey);
      
      // delete the submission
      try {
        DeleteSubmissions deleter = new DeleteSubmissions(keyList);
        deleter.deleteSubmissions(cc);
      } catch (ODKDatastoreException e) {
        return Boolean.FALSE;
      }
      
      return Boolean.TRUE;
    }
	
    @Override
    public SubmissionUISummary getIncompleteSubmissions(FilterGroup filterGroup) throws AccessDeniedException,
        FormNotAvailableException {
      
      HttpServletRequest req = this.getThreadLocalRequest();
      CallingContext cc = ContextFactory.getCallingContext(this, req);

      
      try {
        String formId = filterGroup.getFormId();
        Form form = Form.retrieveFormByFormId(formId, cc);
        if (form.getFormDefinition() == null)
          return null; // ill-formed definition
        SubmissionUISummary summary = new SubmissionUISummary(form.getViewableName());
        
        QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup, CompletionFlag.ONLY_INCOMPLETE_SUBMISSIONS, cc);
        List<Submission> submissions = query.getResultSubmissions(cc);

        getSubmissions(filterGroup, cc, summary, form, submissions);

        return summary;
      } catch (ODKFormNotFoundException e) {
        throw new FormNotAvailableException(e);
      } catch (ODKDatastoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }

    }

    private void getSubmissions(FilterGroup filterGroup, CallingContext cc,
        SubmissionUISummary summary, Form form, List<Submission> submissions)
        throws ODKDatastoreException {
      GenerateHeaderInfo headerGenerator = new GenerateHeaderInfo(filterGroup, summary, form);
      headerGenerator.processForHeaderInfo(form.getTopLevelGroupElement());

      List<FormElementModel> filteredElements = headerGenerator.getIncludedElements();
      ElementFormatter elemFormatter = new UiElementFormatter(cc.getServerURL(),
          headerGenerator.getGeopointIncludes());

      // format row elements
      for (Submission sub : submissions) {
        Row row = sub.getFormattedValuesAsRow(headerGenerator.includedFormElementNamespaces(),
            filteredElements, elemFormatter, false, cc);

        try {
          SubmissionKey subKey = sub.constructSubmissionKey(form.getTopLevelGroupElement());
          summary.addSubmission(new SubmissionUI(row.getFormattedValues(), subKey.toString()));
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    private static final class RemoveIncompleteAttachmentVisitor implements SubmissionVisitor {
      final CallingContext cc;
      boolean success = true; // assume it completes successfully...
      
      RemoveIncompleteAttachmentVisitor(CallingContext cc) {
        this.cc = cc;
      }
      
      boolean getSuccess() {
        return success;
      }

      @Override
      public boolean traverse(SubmissionElement element) {
        if ( element instanceof SubmissionSet ) {
          SubmissionSet set = (SubmissionSet) element;
          List<FormElementModel> elements = set.getFormElements();
          for ( FormElementModel e : elements ) {
            SubmissionValue v = set.getElementValue(e);
            if ( v instanceof BlobSubmissionType ) {
              BlobSubmissionType blob = (BlobSubmissionType) v;
              if ( blob.getAttachmentCount() == 1 &&
                   blob.getContentHash(1) == null ) {
                // we have a missing attachment...
                try {
                  blob.deleteAll(cc);
                  set.removeElementValue(e);
                } catch (ODKDatastoreException e1) {
                  e1.printStackTrace();
                  success = false;
                }
              }
            }
          }
        }
        return false;
      }

    }
    @Override
    public Boolean markSubmissionAsComplete(String submissionKeyAsString)
        throws AccessDeniedException {
      HttpServletRequest req = this.getThreadLocalRequest();
      CallingContext cc = ContextFactory.getCallingContext(this, req);
      
      // create a list because the submission deleter require a list
      SubmissionKey submissionKey = new SubmissionKey(submissionKeyAsString);
      try {
        List<SubmissionKeyPart> parts = submissionKey.splitSubmissionKey();
        Submission sub = Submission.fetchSubmission(parts, cc);
        // recursively examine all attachments and remove any that are
        // missing their files...
        RemoveIncompleteAttachmentVisitor visitor = 
          new RemoveIncompleteAttachmentVisitor(cc);
        sub.depthFirstTraversal(visitor);
        if ( visitor.getSuccess() ) {
          sub.setIsComplete(visitor.getSuccess());
          sub.setMarkedAsCompleteDate(new Date());
        }
        sub.persist(cc);
        return visitor.getSuccess();
      } catch (ODKEntityNotFoundException e) {
        e.printStackTrace();
        return Boolean.FALSE;
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
        return Boolean.FALSE;
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        return Boolean.FALSE;
      }
    }

    @Override
    public ArrayList<MediaFileSummary> getFormMediaFileList(String formId) throws FormNotAvailableException, AccessDeniedException {
      HttpServletRequest req = this.getThreadLocalRequest();
      CallingContext cc = ContextFactory.getCallingContext(this, req);

      ArrayList<MediaFileSummary> mediaSummaryList = new ArrayList<MediaFileSummary>();
      
      try {
        Form form = Form.retrieveFormByFormId(formId, cc);
        if (form.getFormDefinition() == null)
          return mediaSummaryList; // ill-formed definition
        
        BinaryContentManipulator bcm = form.getManifestFileset();
        for ( int i = 0 ; i < bcm.getAttachmentCount() ; ++i ) {
          MediaFileSummary mfs = new MediaFileSummary( bcm.getUnrootedFilename(i+1), bcm.getContentType(i+1), bcm.getContentLength(i+1));
          mediaSummaryList.add(mfs);
        }
      } catch (ODKFormNotFoundException e) {
        throw new FormNotAvailableException(e);
      }
      return mediaSummaryList;
    }

}
