package org.opendatakit.aggregate.odktables.entity.serialization;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.ResponseHandler;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NotModifiedHandler implements ResponseHandler {

  public static final String jsonBufferKey = NotModifiedHandler.class.getCanonicalName()
      + ":jsonBufferKey";

  private static final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void init(Properties properties) {
  }

  @Override
  public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
    Object result = context.getResponseEntity();
    if (result instanceof Response) {
      Response response = (Response) result;

      String eTag = null;
      boolean overrideWithNotModifiedStatus = false;
      
      // if the implementation provides an ETAG, do nothing. Otherwise
      // compute the ETAG from the md5hash of the JSON serialization of
      // whatever the implementation is providing.

      if (response.getEntity() != null && !response.getMetadata().containsKey(HttpHeaders.ETAG)) {
        // This is extremely wasteful, but I don't see a way to avoid it
        // given the handler stack structure and its lack of flexibility.

        // write it to a byte array
        ByteArrayOutputStream bas = new ByteArrayOutputStream(8192);
        OutputStreamWriter w = new OutputStreamWriter(bas,
            Charset.forName(ApiConstants.UTF8_ENCODE));

        mapper.writeValue(w, response.getEntity());
        // get the array and compute md5 hash
        byte[] bytes = bas.toByteArray();
        try {
          MessageDigest md = MessageDigest.getInstance("MD5");
          md.update(bytes);

          byte[] messageDigest = md.digest();

          BigInteger number = new BigInteger(1, messageDigest);
          String md5 = number.toString(16);
          while (md5.length() < 32)
            md5 = "0" + md5;
          eTag = "md5:" + md5;
        } catch (NoSuchAlgorithmException e) {
          throw new IllegalStateException("Unexpected problem computing md5 hash", e);
        }

        // check if there is an IF_NONE_MATCH header...
        List<String> ifNoneMatchTags = context.getHttpHeaders().getRequestHeader(
            HttpHeaders.IF_NONE_MATCH);
        String ifNoneMatchTag = null;
        if (ifNoneMatchTags != null && ifNoneMatchTags.size() > 0) {
          ifNoneMatchTag = ifNoneMatchTags.get(0);
        }

        if (ifNoneMatchTag != null && eTag.equals(ifNoneMatchTag)) {
          // OK -- we have a if-none-match header on the request that
          // matches the eTag of the entity that we would return.
          // Rewrite the response to be a NOT_MODIFIED response
          // without any body. We apparently need to force the headers...
          overrideWithNotModifiedStatus = true;
        } else {
          // just add the ETAG to the response...

          ServletContext sc = (ServletContext) context.getAttributes().get(
              ServletContext.class.getCanonicalName());
          // sc.setAttribute(jsonBufferKey, new SimpleJSONMessageReaderWriter.JSONWrapper(bytes));

          response.getMetadata().add(HttpHeaders.ETAG, eTag);
        }
      } else if ( response.getStatus() == HttpStatus.SC_NOT_MODIFIED ) {
        if ( response.getMetadata().containsKey(HttpHeaders.ETAG) ) {
          eTag = (String) response.getMetadata().getFirst(HttpHeaders.ETAG);
          overrideWithNotModifiedStatus = true;
        }
      }
      
      if ( overrideWithNotModifiedStatus ) {
        context.setResponseEntity(null);
        context.setResponseStatusCode(HttpStatus.SC_NOT_MODIFIED);
        
        // force the response...
        final HttpServletResponse httpResponse = context
            .getAttribute(HttpServletResponse.class);
        
        httpResponse.addHeader(HttpHeaders.ETAG, eTag);
        httpResponse.addHeader(ApiConstants.OPEN_DATA_KIT_VERSION_HEADER, ApiConstants.OPEN_DATA_KIT_VERSION);
        httpResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpResponse.addHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setStatus(HttpStatus.SC_NOT_MODIFIED);
        httpResponse.flushBuffer();
      }
    }
    chain.doChain(context);
  }
}