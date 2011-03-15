package org.opendatakit.aggregate.client.form;

import org.opendatakit.aggregate.constants.common.ExternalServiceOption;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("formservice")
public interface FormService extends RemoteService {

  FormSummary [] getForms();

  ExternServSummary [] getExternalServices(String formid);
  
  ExportSummary [] getExports();
 
  Boolean createCsv(String formId);
  
  KmlSettings getPossibleKmlSettings(String formId);
  
  Boolean createKml(String formId, String geopointKey, String titleKey, String binaryKey);
  
  Boolean setFormDownloadable(String formId, Boolean downloadable);
  
  Boolean setFormAcceptSubmissions(String formId, Boolean acceptSubmissions);
  
  String createFusionTable(String formId, ExternalServiceOption esType);
  
}
