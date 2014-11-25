package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpStatus;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.handlers.ResponseHandler;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.persistence.PersistenceUtils;
import org.opendatakit.common.web.CallingContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppEngineHandlersFactory extends HandlersFactory {

  public static class GZIPRequestHandler implements RequestHandler {
    public static final String noUnGZIPContentEncodingKey = GZIPRequestHandler.class
        .getCanonicalName() + ":disregardGZIPContentEncodingKey";
    public static final String emitGZIPContentEncodingKey = GZIPRequestHandler.class
        .getCanonicalName() + ":emitGZIPContentEncodingKey";

    public static final String FALSE = "false";

    boolean suppressContentEncoding = false;
    boolean suppressAcceptContentEncoding = false;

    @Override
    public void init(Properties props) {
      suppressContentEncoding = !FALSE.equalsIgnoreCase(props.getProperty(
          "suppressContentEncoding", FALSE));
      suppressAcceptContentEncoding = !FALSE.equalsIgnoreCase(props.getProperty(
          "suppressAcceptContentEncoding", FALSE));
      // TODO Auto-generated method stub
    }

    @Override
    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
      UriInfo info = context.getUriInfo();
      System.out.println("The path relative to the base URI is : " + context.getHttpMethod() + " "
          + info.getPath());

      ServletContext sc = (ServletContext) context.getAttributes().get(
          ServletContext.class.getCanonicalName());
      HttpServletRequest req = (HttpServletRequest) context.getAttributes().get(
          HttpServletRequest.class.getCanonicalName());
      CallingContext cc = ContextFactory.getCallingContext(sc, req);
      String server = sc.getServerInfo();

      /*
       * AppEngine leaves the GZIP header even though it unzips the content
       * before delivering it to the app.
       */
      boolean isGaeDevelopmentEnvironment = server.contains("Development");
      boolean isGaeEnvironment = cc.getUserService().getCurrentRealm().getIsGaeEnvironment();
      MultivaluedMap<String, String> headers = context.getHttpHeaders().getRequestHeaders();

      boolean effectiveSuppressContentEncoding = suppressContentEncoding
          || (isGaeEnvironment && !isGaeDevelopmentEnvironment);

      List<String> encodes = headers.get(ApiConstants.CONTENT_ENCODING_HEADER);

      if (effectiveSuppressContentEncoding || encodes == null
          || !encodes.contains(ApiConstants.GZIP_CONTENT_ENCODING)) {
        sc.setAttribute(noUnGZIPContentEncodingKey, Boolean.toString(true));
      } else {
        sc.setAttribute(noUnGZIPContentEncodingKey, Boolean.toString(false));
      }

      boolean effectiveSuppressAcceptContentEncoding = suppressAcceptContentEncoding
          || (isGaeEnvironment && !isGaeDevelopmentEnvironment);

      List<String> accepts = headers.get(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER);

      if (effectiveSuppressAcceptContentEncoding || accepts == null
          || !accepts.contains(ApiConstants.GZIP_CONTENT_ENCODING)) {
        sc.setAttribute(emitGZIPContentEncodingKey, Boolean.toString(false));
      } else {
        sc.setAttribute(emitGZIPContentEncodingKey, Boolean.toString(true));
      }

      chain.doChain(context);
    }

  }

  public AppEngineHandlersFactory() {
    // super();
  }

  @Override
  public List<? extends RequestHandler> getRequestHandlers() {
    return Arrays.asList(new GZIPRequestHandler());
  }

  public static class NotModifiedHandler implements ResponseHandler {

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
          eTag = PersistenceUtils.newMD5HashUri(bytes);

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

  @Override
  public List<? extends ResponseHandler> getResponseHandlers() {
    ArrayList<ResponseHandler> myHandlers = new ArrayList<ResponseHandler>();
    myHandlers.add(new NotModifiedHandler());
    myHandlers.addAll(super.getResponseHandlers());
    return myHandlers;
  }
}
