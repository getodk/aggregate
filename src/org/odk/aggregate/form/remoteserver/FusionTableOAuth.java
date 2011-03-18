package org.odk.aggregate.form.remoteserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.table.SubmissionFusionTable;

import com.google.appengine.api.datastore.Key;
import com.google.gdata.client.GoogleService;
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
        && (tokenSecret == null ? (other.tokenSecret == null) : (tokenSecret
            .equals(other.tokenSecret)));
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

  static public String executeInsert(GoogleService service, String statement, OAuthToken authToken)
      throws IOException, ServiceException, MalformedURLException, UnsupportedEncodingException {
    OAuthConsumer consumer = new DefaultOAuthConsumer(ServletConsts.OAUTH_CONSUMER_KEY,
        ServletConsts.OAUTH_CONSUMER_SECRET);
    consumer.setTokenWithSecret(authToken.getToken(), authToken.getTokenSecret());

    URL url = new URL(ServletConsts.FUSION_SCOPE + HtmlConsts.BEGIN_PARAM + ServletConsts.BEGIN_SQL
        + URLEncoder.encode(statement, ServletConsts.FUSTABLE_ENCODE));

    System.out.println(url.toString());

    HttpURLConnection request = (HttpURLConnection) url.openConnection();
    request.setDoOutput(true);
    request.setDoInput(true);
    request.setRequestMethod(HtmlConsts.POST);
    request.setFixedLengthStreamingMode(0);
    try {
      consumer.sign(request);
    } catch (OAuthMessageSignerException e) {
      e.printStackTrace();
      throw new ServiceException("Failed to sign request: " + e.getMessage());
    } catch (OAuthExpectationFailedException e) {
      e.printStackTrace();
      throw new ServiceException("Failed to sign request: " + e.getMessage());
    } catch (OAuthCommunicationException e) {
      e.printStackTrace();
      throw new IOException("Failed to sign request: " + e.getMessage());
    }

    // TODO: this section of code is possibly causing 'WARNING: Going to buffer
    // response body of large or unknown size. Using getResponseBodyAsStream
    // instead is recommended.'
    // The WARNING is most likely only happening when running appengine locally,
    // but we should investigate to make sure
    InputStream is = request.getInputStream();
    request.connect();

    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuffer response = new StringBuffer();
    String responseLine;
    while ((responseLine = reader.readLine()) != null) {
      response.append(responseLine);
    }
    if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new ServiceException(response.toString() + statement);
    }
    return response.toString();

  }
}
