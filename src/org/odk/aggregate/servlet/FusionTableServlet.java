package org.odk.aggregate.servlet;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ExternalServiceOption;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.FusionTable;
import org.odk.aggregate.table.SubmissionFusionTable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gdata.client.ClientLoginAccountType;
import com.google.gdata.client.GoogleService;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class FusionTableServlet extends ServletUtilBase {
    
	/**
	 * Serial number for serialization
	 */
	// TODO: replace this
	// private static final long serialVersionUID = 456146061385437109L;
	private static final long serialVersionUID = 109713025906710021L;
	/**
	 * URI from base
	 */
	public static final String ADDR = "fusiontables";

	/**
	 * Handler for HTTP Get request to create a google spreadsheet
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		// verify user is logged in
		if (!verifyCredentials(req, resp)) {
			return;
		}

		// TODO: rename params so not spreadsheet

		// get parameter
		// really the table id
		String spreadsheetName = getParameter(req,
				ServletConsts.SPREADSHEET_NAME_PARAM);
		String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
	    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);

		
		GoogleService service = new GoogleService("fusiontables", "fusiontables.FusionTables");
        try {
			service.setUserCredentials(SubmissionFusionTable.USER, SubmissionFusionTable.PASS, ClientLoginAccountType.GOOGLE);
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// get form
		EntityManager em = EMFactory.get().createEntityManager();
		Key formKey = KeyFactory.stringToKey(odkFormKey);
		Form form = em.getReference(Form.class, formKey);

		FusionTable fusion = new FusionTable(spreadsheetName);

		ExternalServiceOption esType = ExternalServiceOption
				.valueOf(esTypeString);
		

		if (!esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
			form.addFusionExternalRepos(fusion);
		}

		if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
			 SubmissionFusionTable submissions =
	              new SubmissionFusionTable(form, req.getServerName(), em);
			 try {
				submissions.uploadSubmissionDataToSpreadsheet(service, fusion);
			} catch (ODKIncompleteSubmissionData e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		em.close();
	    resp.sendRedirect(ServletConsts.WEB_ROOT);

	}
	

}
