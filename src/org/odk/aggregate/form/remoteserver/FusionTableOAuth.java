package org.odk.aggregate.form.remoteserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.esxx.js.protocol.GAEConnectionManager;
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
    OAuthConsumer consumer = new CommonsHttpOAuthConsumer(ServletConsts.OAUTH_CONSUMER_KEY,
        ServletConsts.OAUTH_CONSUMER_SECRET);
    consumer.setTokenWithSecret(authToken.getToken(), authToken.getTokenSecret());

    URI uri;
	try {
		uri = new URI(ServletConsts.FUSION_SCOPE);
	} catch (URISyntaxException e1) {
		e1.printStackTrace();
		throw new MalformedURLException(e1.getMessage());
	}

    System.out.println(uri.toString());
    HttpParams httpParams = new BasicHttpParams();
    ClientConnectionManager mgr = new GAEConnectionManager();
    HttpClient client = new DefaultHttpClient(mgr, httpParams);
    HttpPost post = new HttpPost(uri);
    List<NameValuePair> formParams = new ArrayList<NameValuePair>();
    formParams.add( new BasicNameValuePair("sql", statement));
    UrlEncodedFormEntity form = new UrlEncodedFormEntity(formParams, "UTF-8");
    post.setEntity(form);
    
    try {
      consumer.sign(post);
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

    HttpResponse resp = client.execute(post);
    // TODO: this section of code is possibly causing 'WARNING: Going to buffer
    // response body of large or unknown size. Using getResponseBodyAsStream
    // instead is recommended.'
    // The WARNING is most likely only happening when running appengine locally,
    // but we should investigate to make sure
    BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
    StringBuffer response = new StringBuffer();
    String responseLine;
    while ((responseLine = reader.readLine()) != null) {
      response.append(responseLine);
    }
    if (resp.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
      throw new ServiceException(response.toString() + statement);
    }
    return response.toString();

  }
}
