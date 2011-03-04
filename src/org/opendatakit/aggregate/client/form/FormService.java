package org.opendatakit.aggregate.client.form;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("formservice")
public interface FormService extends RemoteService {

  FormSummary [] getForms();

  ExternServSummary [] getExternalServices(String formid);
  
}
