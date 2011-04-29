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
package org.opendatakit.aggregate.task.tomcat;

import java.util.Map;

import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.servlet.KmlServlet;
import org.opendatakit.aggregate.servlet.KmlSettingsServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.KmlGenerator;
import org.opendatakit.aggregate.task.KmlWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This is a singleton bean.  It cannot have any per-request state.
 * It uses a static inner class to encapsulate the per-request state
 * of a running background task.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class KmlGeneratorImpl implements KmlGenerator {

	static class KmlRunner implements Runnable {
		final KmlWorkerImpl impl;

		public KmlRunner(Form form, SubmissionKey persistentResultsKey,
				long attemptCount, FormElementModel titleField,
				FormElementModel geopointField, FormElementModel imageField,
				CallingContext cc) {
			
			impl = new KmlWorkerImpl(form, persistentResultsKey, attemptCount,
					titleField, geopointField, imageField, cc);
		}

		@Override
		public void run() {
			impl.generateKml();
		}
	}

	@Override
	public void createKmlTask(Form form, SubmissionKey persistentResultsKey, long attemptCount,
			CallingContext cc) throws ODKDatastoreException, ODKFormNotFoundException {
		Submission s = Submission.fetchSubmission(persistentResultsKey.splitSubmissionKey(), cc);
	    PersistentResults r = new PersistentResults(s);
	    Map<String,String> params = r.getRequestParameters();
	    FormElementModel titleField = null;
	    FormElementModel imageField = null;
	    FormElementModel geopointField = null;
	    if ( params != null ) {
	    	String field;
	    	field = params.get(KmlServlet.TITLE_FIELD);
	        if (field != null) {
	          FormElementKey titleKey = new FormElementKey(field);
	          titleField = FormElementModel.retrieveFormElementModel(form, titleKey);
	        }
	        field = params.get(KmlServlet.GEOPOINT_FIELD);
	        if (field != null) {
	          FormElementKey geopointKey = new FormElementKey(field);
	          geopointField = FormElementModel.retrieveFormElementModel(form, geopointKey);
	        }
	        field = params.get(KmlServlet.IMAGE_FIELD);
	        if (field != null) {
	          if (!field.equals(KmlSettingsServlet.NONE)) {
	            FormElementKey imageKey = new FormElementKey(field);
	            imageField = FormElementModel.retrieveFormElementModel(form, imageKey);
	          }
	        }
	    }
		KmlRunner runner = new KmlRunner(form, persistentResultsKey, attemptCount,
				titleField, geopointField, imageField, cc);
		AggregrateThreadExecutor exec = AggregrateThreadExecutor
				.getAggregateThreadExecutor();
		exec.execute(runner);
	}
}
