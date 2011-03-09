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
package org.opendatakit.aggregate.task;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.PersistentResults.Status;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.structure.KmlFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;

/**
 * Common worker implementation for the generation of kml files.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class KmlWorkerImpl {

	private final Form form;
	private final SubmissionKey persistentResultsKey;
	private final Long attemptCount;
	private final FormElementModel titleField;
	private final FormElementModel geopointField;
	private final FormElementModel imageField;
	private final CallingContext cc;

	public KmlWorkerImpl(Form form, SubmissionKey persistentResultsKey,
			long attemptCount, FormElementModel titleField,
			FormElementModel geopointField, FormElementModel imageField,
			CallingContext cc) {
		this.form = form;
		this.persistentResultsKey = persistentResultsKey;
		this.attemptCount = attemptCount;
		this.titleField = titleField;
		this.geopointField = geopointField;
		this.imageField = imageField;
		this.cc = cc;
	}

	public void generateKml() {

	  try {
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    PrintWriter pw = new PrintWriter(stream);

	    // create KML
	    QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false, ServletConsts.FETCH_LIMIT,
	    		cc);
	    SubmissionFormatter formatter = new KmlFormatter(form, cc.getServerURL(), geopointField,
	        titleField, imageField, pw, null, cc);
	    formatter.processSubmissions(query.getResultSubmissions(cc), cc);

	    // output file
	    pw.close();
	    byte[] outputFile = stream.toByteArray();

	    Submission s = Submission.fetchSubmission(persistentResultsKey.splitSubmissionKey(), cc);
	    PersistentResults r = new PersistentResults(s);
	    if ( attemptCount.equals(r.getAttemptCount()) ) {
			r.setResultFile(outputFile, HtmlConsts.RESP_TYPE_PLAIN, Long.valueOf(outputFile.length), form.getViewableFormNameSuitableAsFileName() + ServletConsts.KML_FILENAME_APPEND, cc);
			r.setStatus(Status.AVAILABLE);
			r.setCompletionDate(new Date());
			r.objectEntity.persist(cc);
	    }
	  } catch (Exception e ) {
		  failureRecovery(e);
	  }
	}

	private void failureRecovery(Exception e) {
		// three exceptions possible: 
		// ODKFormNotFoundException, ODKDatastoreException, Exception
		e.printStackTrace();
	    Submission s;
		try {
			s = Submission.fetchSubmission(persistentResultsKey.splitSubmissionKey(), cc);
		    PersistentResults r = new PersistentResults(s);
		    if ( attemptCount.equals(r.getAttemptCount()) ) {
		    	r.deleteResultFile(cc);
		    	r.setStatus(Status.FAILED);
		    	r.objectEntity.persist(cc);
		    }
		} catch (Exception ex) {
			// something is hosed -- don't attempt to continue.
			// TODO: watchdog: find this once lastRetryDate is way late?
		}
	}

}
