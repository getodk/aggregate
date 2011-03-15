package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.submission.SubmissionService;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class Common {
	private static Common c;
	
	private static List<AsyncCallback<FormSummary[]>> formCallbacks;
	private static List<AsyncCallback<SubmissionUISummary>> submissionCallbacks;
	private static FormService formService;
	private static SubmissionService submissionService;
	
	private Common() {
		
	}
	
	public static Common getCommon() {
		if (c == null) {
			c = new Common();
		}
		return c;
	}
}
