package org.opendatakit.aggregate.externalservice;

import org.apache.commons.logging.Log;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

public abstract class OAuthExternalService extends AbstractExternalService {

  protected OAuthExternalService(IForm form, FormServiceCursor formServiceCursor, ElementFormatter formatter, HeaderFormatter headerFormatter, CallingContext cc){
    super(form, formServiceCursor, formatter, headerFormatter, cc);
  }
  
  public abstract void authenticateAndCreate(OAuthToken authToken, CallingContext cc) throws ODKExternalServiceException, ODKDatastoreException;
  
  protected abstract OAuthToken getAuthToken();
  
  protected GoogleOAuthParameters getOAuthParams() {
    OAuthToken token = getAuthToken();  
    GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
    oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
    oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
    oauthParameters.setOAuthToken(token.getToken());
    oauthParameters.setOAuthTokenSecret(token.getTokenSecret());
    return oauthParameters;
  }

  protected void logOAuthException(Log logger, OAuthException e) throws ODKExternalServiceException {
    String str = "Unable to set Oauth credentials";
    logger.error(str + "\nReason: " + e.getMessage());
    e.printStackTrace();
    throw new ODKExternalServiceCredentialsException(str, e);
  }
  
  @Override
  public void delete(CallingContext cc) throws ODKDatastoreException {
    // remove fusion table permission as no longer needed
    // TODO: test that the revoke REALLY works, can be easy to miss since we
    // ignore exception
    try {
      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
      oauthHelper.revokeToken(getOAuthParams());
      
    } catch (OAuthException e) {
      // just moving on, as we still want to delete
      e.printStackTrace();
    }

   super.delete(cc);
  }
  
}
