package org.odk.aggregate.form.remoteserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.table.SubmissionFusionTable;

import com.google.appengine.api.datastore.Key;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.Service.GDataRequest.RequestType;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

@Entity
public class FusionTableOAuth implements RemoteServer {

   /**
    * GAE datastore key that uniquely identifies the form element
    */
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Key key;

   /**
    * Table Name
    */
   @Enumerated
   private String tableName;

   /**
    * Authorization Token for fusion table
    */
   @Enumerated
   private String authToken;
   
   /**
    * Authorization Secret for fusion table
    */
   @Enumerated
   private String tokenSecret;

   public FusionTableOAuth(String table, OAuthToken token) {
      this.tableName = table;
      this.authToken = token.getToken();
      this.tokenSecret = token.getTokenSecret();
   }

   public String getTableName() {
      return tableName;
   }

   public OAuthToken getAuthToken() {
      return new OAuthToken(this.authToken, this.tokenSecret);
   }

   public void sendSubmissionToRemoteServer(Form xform, String serverName, EntityManager em,
         String appName, Submission submission) {

      SubmissionFusionTable fusionsSubmission = new SubmissionFusionTable(xform, serverName, em);

      fusionsSubmission.insertNewDataInSpreadsheet(submission, this);
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof FusionTable)) {
         return false;
      }
      FusionTableOAuth other = (FusionTableOAuth) obj;
      return (key == null ? (other.key == null) : (key.equals(other.key)))
            && (tableName == null ? (other.tableName == null) : (tableName.equals(other.tableName)))
            && (authToken == null ? (other.authToken == null) : (authToken.equals(other.authToken)))
            && (tokenSecret == null ? (other.tokenSecret == null) : (tokenSecret.equals(other.tokenSecret)));
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int hashCode = 13;
      if (key != null)
         hashCode += key.hashCode();
      if (tableName != null)
         hashCode += tableName.hashCode();
      return hashCode;
   }

   static public String executeInsert(GoogleService service, String insertStmt) throws IOException,
         ServiceException, MalformedURLException, UnsupportedEncodingException {
      GDataRequest request = service.getRequestFactory().getRequest(RequestType.INSERT,
            new URL(SubmissionFusionTable.URL_STRING),
            new ContentType("application/x-www-form-urlencoded"));
      OutputStreamWriter writer = new OutputStreamWriter(request.getRequestStream());

      System.out.println(URLEncoder.encode(insertStmt, "UTF-8"));
      writer.append("sql=" + URLEncoder.encode(insertStmt, "UTF-8"));
      writer.flush();
      request.execute();

      BufferedReader line = new BufferedReader(new InputStreamReader(request.getResponseStream()));
      String tmpString = line.readLine();
      String response = null;
      while (tmpString != null) {
         response += tmpString + BasicConsts.NEW_LINE;
         tmpString = line.readLine();
      }

      return response;

   }

   static public String executeQuery(GoogleService service, String queryStmt) throws IOException,
         ServiceException, MalformedURLException, UnsupportedEncodingException {
      URL url = new URL(SubmissionFusionTable.URL_STRING + "?sql="
            + URLEncoder.encode(queryStmt, "UTF-8"));
      GDataRequest request = service.getRequestFactory().getRequest(RequestType.QUERY, url,
            ContentType.TEXT_PLAIN);

      request.execute();
      BufferedReader line = new BufferedReader(new InputStreamReader(request.getResponseStream()));
      String tmpString = line.readLine();
      String response = null;
      while (tmpString != null) {
         response += tmpString + BasicConsts.NEW_LINE;
         tmpString = line.readLine();
      }

      return response;

   }
}
