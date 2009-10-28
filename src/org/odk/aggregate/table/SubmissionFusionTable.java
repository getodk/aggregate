package org.odk.aggregate.table;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.FusionTable;
import org.odk.aggregate.servlet.FormMultipleValueServlet;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionRepeat;

import com.google.appengine.api.datastore.Key;
import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

public class SubmissionFusionTable extends SubmissionTable {

    public static final String USER = "odk.team";
    public static final String PASS = "";
	
	  public static final String URL_STRING = "http://tables.googlelabs.com/api/query";

	/**
	   * Construct a CSV table object for form with the specified ODK ID
	   * @param xform TODO
	   * @param serverName TODO
	   * @param entityManager
	   *    the persistence manager used to manage generating the tables
	   * 
	   */
	  public SubmissionFusionTable(Form xform, String serverName, EntityManager entityManager) {
	    super(xform, serverName, entityManager, TableConsts.QUERY_ROWS_MAX, false);
	  }
	  
	  /**
	   * Helper function to create the view link for images
	   * @param subKey
	   *    datastore key to the submission entity
	   * @param porpertyName
	   *    entity's property to retrieve and display
	   * 
	   * @return
	   *     link to view the image
	   */
	  @Override
	  protected String createViewLink(Key subKey, String porpertyName) {
	    Map<String, String> properties = createViewLinkProperties(subKey);
	    return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl() + ImageViewerServlet.ADDR, properties);
	  }
	 
	  /**
	   * Helper function to create the link to repeat results
	   * @param repeat
	   *    the repeat object
	   * @param parentSubmissionSetKey
	   *    the submission set that contains the repeat value
	   *    
	   * @return
	   *    link to repeat results
	   */
	  @Override
	  protected String createRepeatLink(SubmissionRepeat repeat, Key parentSubmissionSetKey) { 
	    Map<String, String> properties = createRepeatLinkProperties(repeat, parentSubmissionSetKey);
	    return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl() + FormMultipleValueServlet.ADDR, properties);
	  }
	  
	  public void uploadSubmissionDataToSpreadsheet(GoogleService service, FusionTable fusion)
      	throws ODKIncompleteSubmissionData, IOException, ServiceException {
		  ResultTable results = generateResultTable(TableConsts.EPOCH, false);
		  // TODO: migrate to batch uploads
		  List<List<String>> resultRows = results.getRows();

		  for (List<String> row : resultRows) {
			  insertNewData(service, fusion, headers, row);
		  }
	  }
	  
	  
	  public void insertNewDataInSpreadsheet(Submission submission, FusionTable fusion) {	  
			ResultTable result = generateSingleEntryResultTable(submission);

		    try {
				GoogleService service = new GoogleService("fusiontables", "fusiontables.FusionTables");
				service.setUserCredentials(USER, PASS, ClientLoginAccountType.GOOGLE);
				insertNewData(service, fusion, result.getHeader(), result.getRows().get(0));
			} catch (AuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		  }
	  
	   private void insertNewData(GoogleService service, FusionTable fusion, List<String> headers, List<String> row) throws IOException, ServiceException {
	        GDataRequest request =
	                service.getRequestFactory().getRequest(RequestType.INSERT, new URL(SubmissionFusionTable.URL_STRING),
	                        new ContentType("application/x-www-form-urlencoded"));
	        OutputStreamWriter writer = new OutputStreamWriter(request.getRequestStream());
	        String insertQuery = "INSERT INTO " 
	        					+ fusion.getTableName() 
	        					+ createCsvString(headers.iterator(), false)
	        					+ " VALUES "
	        					+ createCsvString(row.iterator(), true);
	        System.out.println(insertQuery);
	        writer.append("sql=" + URLEncoder.encode(insertQuery, "UTF-8"));
	        writer.flush();

	        request.execute();
	    }
	  
	   // TODO: make more of a utility function with CSV
	   private String createCsvString(Iterator<String> itr, boolean quoted) {
		   String str = " (" ;
		   while (itr.hasNext()) {
			  if(quoted) {
				  str += BasicConsts.SINGLE_QUOTE + itr.next() + BasicConsts.SINGLE_QUOTE;
			  } else {
				  str += itr.next();
			  }
			  
		      if (itr.hasNext()) {
		        str += BasicConsts.CSV_DELIMITER;
		      }
		   }
		   return str + ") ";
	   }
	   
}
