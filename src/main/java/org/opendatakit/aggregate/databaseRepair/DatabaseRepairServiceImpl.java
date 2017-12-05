package org.opendatakit.aggregate.databaseRepair;

import static org.opendatakit.aggregate.form.FormInfoFilesetTable.IS_DOWNLOAD_ALLOWED;
import static org.opendatakit.aggregate.form.FormInfoFilesetTable.IS_ENCRYPTED_FORM;
import static org.opendatakit.aggregate.form.FormInfoTable.FORM_ID;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.form.FormInfoFilesetTable;
import org.opendatakit.aggregate.form.FormInfoTable;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class DatabaseRepairServiceImpl extends RemoteServiceServlet implements DatabaseRepairService {

  private static final String LOGGING_CONTEXT_TAG = "DatabaseRepair.DatabaseRepairService";

  @Override

  public List<FormCorruptionReport> formsMissingFileset() throws DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    Map<String, FilesetReport> filesetsPerForm = getFilesetReports(cc);

    List<FormCorruptionReport> incompleteForms = new ArrayList<>();
    for (FormInfoTable fi : fetchFormInfos(cc)) {
      incompleteForms.add(new FormCorruptionReport(
          fi.getUri(),
          fi.getStringField(FORM_ID),
          filesetsPerForm.containsKey(fi.getUri()) ? filesetsPerForm.get(fi.getUri()) : FilesetReport.empty()
      ));
    }

    return incompleteForms;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void fixFilesets(FilesetReport.Row theRow) throws DatastoreFailureException {
    HttpServletRequest req = this.getThreadLocalRequest();
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    try {
      FormInfoFilesetTable filesetTable = getFilesetTable(cc);
      Query query = ds.createQuery(filesetTable, LOGGING_CONTEXT_TAG, user);
      List<FormInfoFilesetTable> filesets = (List<FormInfoFilesetTable>) query.executeQuery();
      for (FormInfoFilesetTable row : filesets)
        if (row.getParentAuri().equals(theRow.getParentUri()) && !row.getUri().equals(theRow.getURI()))
          ds.deleteEntity(row.getEntityKey(), user);
        else {
          row.setBooleanField(IS_ENCRYPTED_FORM, theRow.isEncryptedForm());
          row.setBooleanField(IS_DOWNLOAD_ALLOWED, theRow.isDownloadAllowed());
          ds.putEntity(row, user);
        }
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, FilesetReport> getFilesetReports(CallingContext cc) throws DatastoreFailureException {
    Map<String, FilesetReport> filesets = new HashMap<>();
    List<FormInfoFilesetTable> result;
    try {
      FormInfoFilesetTable table = getFilesetTable(cc);
      result = (List<FormInfoFilesetTable>) cc.getDatastore().createQuery(
          table,
          LOGGING_CONTEXT_TAG,
          cc.getCurrentUser()
      ).executeQuery();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
    for (FormInfoFilesetTable fit : result) {
      String parentAuri = fit.getParentAuri();
      if (!filesets.containsKey(parentAuri))
        filesets.put(parentAuri, FilesetReport.empty());
      filesets.put(parentAuri, filesets.get(parentAuri).add(
          fit.getUri(),
          fit.getParentAuri(),
          fit.getBooleanField(IS_ENCRYPTED_FORM),
          fit.getBooleanField(IS_DOWNLOAD_ALLOWED)
      ));
    }
    return filesets;
  }

  @SuppressWarnings("unchecked")
  private List<FormInfoTable> fetchFormInfos(CallingContext cc) throws DatastoreFailureException {
    try {
      return (List<FormInfoTable>) cc.getDatastore().createQuery(
          FormInfoTable.assertRelation(cc),
          LOGGING_CONTEXT_TAG,
          cc.getCurrentUser()
      ).executeQuery();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }

  private FormInfoFilesetTable getFilesetTable(CallingContext cc) throws DatastoreFailureException {
    try {
      return FormInfoFilesetTable.assertRelation(cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new DatastoreFailureException(e);
    }
  }
}
