package org.opendatakit.aggregate.externalservice;

import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.header.HeaderFormatter;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public abstract class OAuth2ExternalService extends AbstractExternalService {
  protected static ObjectMapper mapper = new ObjectMapper();

  protected OAuth2ExternalService(IForm form, FormServiceCursor formServiceCursor, ElementFormatter formatter, HeaderFormatter headerFormatter, CallingContext cc){
    super(form, formServiceCursor, formatter, headerFormatter, cc);
  }

  @Override
  public void delete(CallingContext cc) throws ODKDatastoreException {
    // remove fusion table permission as no longer needed
    // TODO: test that the revoke REALLY works, can be easy to miss since we
    // ignore exception
//    try {
////      GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
////      oauthHelper.revokeToken(getOAuthParams());
//
//    } catch (OAuthException e) {
//      // just moving on, as we still want to delete
//      e.printStackTrace();
//    }

   super.delete(cc);
  }

}
