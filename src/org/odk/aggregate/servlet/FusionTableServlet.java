package org.odk.aggregate.servlet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.Compatibility;
import org.odk.aggregate.constants.ExternalServiceOption;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKGDataAuthenticationError;
import org.odk.aggregate.exception.ODKGDataServiceNotAuthenticated;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.FusionTable;
import org.odk.aggregate.report.FormProperties;
import org.odk.aggregate.submission.SubmissionFieldType;
import org.odk.aggregate.table.SubmissionFusionTable;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class FusionTableServlet extends ServletUtilBase {

   private static final String CREATE_FUSION_RESPONSE_HEADER = "tableid";

   /**
    * Serial number for serialization
    */
   private static final long serialVersionUID = -8068275263542194677L;

   /**
    * URI from base
    */
   public static final String ADDR = "fusiontables";

   /**
    * Title for generated webpage
    */
   private static final String TITLE_INFO = "Create Google Fusion Table";

   /**
    * Handler for HTTP Get request to create a google fusion table
    * 
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

      // verify user is logged in
      if (!verifyCredentials(req, resp)) {
         return;
      }

      String odkIdParam = getParameter(req, ServletConsts.ODK_ID);
      String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);

      Map<String, String> params = new HashMap<String, String>();
      params.put(ServletConsts.ODK_ID, odkIdParam);
      params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esTypeString);

      String authToken = null;

      try {
         authToken = verifyGDataAuthorization(req, resp, ServletConsts.FUSION_SCOPE);
      } catch (ODKGDataAuthenticationError e) {
         return; // verifyGDataAuthroization function formats response
      } catch (ODKGDataServiceNotAuthenticated e) {
         // do nothing already set to null
      }

      // need to obtain authorization to fusion table
      if (authToken == null) {
         beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
         String authButton = generateAuthButton(ServletConsts.FUSION_SCOPE,
               ServletConsts.AUTHORIZE_FUSION_CREATION, params, req, resp);
         resp.getWriter().print(authButton);
         finishBasicHtmlResponse(resp);
         return;
      }

      GoogleService service = new GoogleService("fusiontables", "fusiontables.FusionTables");
      service.setAuthSubToken(authToken);

      // get form
      EntityManager em = EMFactory.get().createEntityManager();

      Form form;
      try {
        form = Form.retrieveForm(em, odkIdParam);
      } catch (ODKFormNotFoundException e) {
        odkIdNotFoundError(resp);
        return;
      }
      
      FormProperties formProp = new FormProperties(form, em, false);
      Map<String, SubmissionFieldType> types = formProp.getHeaderTypes();
      String createQuery = "CREATE TABLE '" + form.getOdkId() + "' (";
      Iterator<Map.Entry<String, SubmissionFieldType>> entries = types.entrySet().iterator();

      while (entries.hasNext()) {
         Map.Entry<String, SubmissionFieldType> entry = entries.next();
         createQuery += BasicConsts.SINGLE_QUOTE + Compatibility.removeDashes(entry.getKey()) 
           + BasicConsts.SINGLE_QUOTE + BasicConsts.COLON + entry.getValue().getFusionType().getFusionTypeValue();
         if (entries.hasNext()) {
            createQuery += BasicConsts.COMMA;
         }
      }

      createQuery += ")";
      
      FusionTable fusion = null;
      try {
         String requestResult = FusionTable.executeInsert(service, createQuery);
         int index = requestResult.lastIndexOf(CREATE_FUSION_RESPONSE_HEADER);
         if(index > 0) {
            String tableid = requestResult.substring(index + CREATE_FUSION_RESPONSE_HEADER.length());
            fusion = new FusionTable(tableid.trim(), authToken);
         } else {
            throw new IOException("ERROR CREATING FUSION TABLE - DID NOT GET A TABLE NUMBER");
         }
      } catch (ServiceException e) {
         throw new IOException(e);
      }

      ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);

      if (!esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
         form.addFusionExternalRepos(fusion);
      }

      if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
         SubmissionFusionTable submissions = new SubmissionFusionTable(form, req.getServerName(),
               em);
         try {
            submissions.uploadSubmissionDataToSpreadsheet(service, fusion);
         } catch (ODKIncompleteSubmissionData e) {
            throw new IOException(e);
         } catch (ServiceException e) {
            throw new IOException(e);
         }
      }
      if (esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
         // remove fusion table permission as no longer needed
         try {
            AuthSubUtil.revokeToken(authToken, null);
         } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (AuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      }
      
      em.close();
      resp.sendRedirect(ServletConsts.WEB_ROOT);

   }

}
