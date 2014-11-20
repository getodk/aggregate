package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.handlers.ResponseHandler;
import org.eclipse.jetty.http.HttpStatus;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.entity.serialization.SimpleJSONMessageReaderWriter;
import org.opendatakit.aggregate.odktables.exception.NotModifiedException;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.web.CallingContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppEngineHandlersFactory extends HandlersFactory {

  public static class GZIPRequestHandler implements RequestHandler {
    public static final String noUnGZIPContentEncodingKey =
        GZIPRequestHandler.class.getCanonicalName() + ":disregardGZIPContentEncodingKey";
    public static final String emitGZIPContentEncodingKey =
        GZIPRequestHandler.class.getCanonicalName() + ":emitGZIPContentEncodingKey";

    public static final String FALSE = "false";

    boolean suppressContentEncoding = false;
    boolean suppressAcceptContentEncoding = false;

    @Override
    public void init(Properties props) {
      suppressContentEncoding = !FALSE.equalsIgnoreCase(props.getProperty("suppressContentEncoding", FALSE));
      suppressAcceptContentEncoding = !FALSE.equalsIgnoreCase(props.getProperty("suppressAcceptContentEncoding", FALSE));
      // TODO Auto-generated method stub
    }

    @Override
    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
      UriInfo info = context.getUriInfo();
      System.out.println("The path relative to the base URI is : " + context.getHttpMethod() + " " + info.getPath());

      ServletContext sc = (ServletContext) context.getAttributes().get(ServletContext.class.getCanonicalName());
      HttpServletRequest req = (HttpServletRequest) context.getAttributes().get(HttpServletRequest.class.getCanonicalName());
      CallingContext cc = ContextFactory.getCallingContext(sc, req);
      String server = sc.getServerInfo();

      /*
       * AppEngine leaves the GZIP header even though it unzips
       * the content before delivering it to the app.
       */
      boolean isGaeDevelopmentEnvironment = server.contains("Development");
      boolean isGaeEnvironment = cc.getUserService().getCurrentRealm().getIsGaeEnvironment();
      MultivaluedMap<String, String> headers = context.getHttpHeaders().getRequestHeaders();

      boolean effectiveSuppressContentEncoding =
          suppressContentEncoding || (isGaeEnvironment && !isGaeDevelopmentEnvironment);

      List<String> encodes = headers.get(ApiConstants.CONTENT_ENCODING_HEADER);

      if ( effectiveSuppressContentEncoding || encodes == null || !encodes.contains(ApiConstants.GZIP_CONTENT_ENCODING)) {
        sc.setAttribute(noUnGZIPContentEncodingKey, Boolean.toString(true));
      } else {
        sc.setAttribute(noUnGZIPContentEncodingKey, Boolean.toString(false));
      }

      boolean effectiveSuppressAcceptContentEncoding =
          suppressAcceptContentEncoding || (isGaeEnvironment && !isGaeDevelopmentEnvironment);

      List<String> accepts = headers.get(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER);

      if ( effectiveSuppressAcceptContentEncoding || accepts == null || !accepts.contains(ApiConstants.GZIP_CONTENT_ENCODING)) {
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
    
    public static final String jsonBufferKey  =
        NotModifiedHandler.class.getCanonicalName() + ":jsonBufferKey";

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(Properties properties) {
    }

    @Override
    public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
      List<String> ifNoneMatchTags = context.getHttpHeaders().getRequestHeader(HttpHeaders.IF_NONE_MATCH);
      if ( ifNoneMatchTags != null && ifNoneMatchTags.size() > 0) {
        String ifNoneMatchTag = ifNoneMatchTags.get(0);

        Object result = context.getResponseEntity();
        if ( result instanceof Response ) {
          Response response = (Response)result;
          
          // if the implementation provides an ETAG, do nothing. Otherwise
          // compute the ETAG from the md5hash of the JSON serialization of
          // whatever the implementation is providing.
          
          if ( response.getEntity() != null &&
              !response.getMetadata().containsKey(HttpHeaders.ETAG) ) {
            // This is extremely wasteful, but I don't see a way to avoid it
            // given the handler stack structure and its lack of flexibility.
            
            // write it to a byte array
            ByteArrayOutputStream bas = new ByteArrayOutputStream(8192);
            OutputStreamWriter w = new OutputStreamWriter(bas,
                Charset.forName(ApiConstants.UTF8_ENCODE));

            mapper.writeValue(w, response.getEntity());
            // get the array and compute md5 hash
            byte[] bytes = bas.toByteArray();
            String eTag;
            try {
              MessageDigest md = MessageDigest.getInstance("MD5");
              md.update(bytes);

              byte[] messageDigest = md.digest();

              BigInteger number = new BigInteger(1, messageDigest);
              String md5 = number.toString(16);
              while (md5.length() < 32)
                md5 = "0" + md5;
              eTag = "md5_" + md5;
            } catch (NoSuchAlgorithmException e) {
              throw new IllegalStateException("Unexpected problem computing md5 hash", e);
            }
            
            if ( eTag.equals(ifNoneMatchTag) ) {
              // OK -- we have a if-none-match header on the request that 
              // matches the eTag of the entity that we would return. 
              // Rewrite the response to be a NOT_MODIFIED response.
              context.setResponseEntity(null);
              context.setResponseStatusCode(HttpStatus.NOT_MODIFIED_304);
            } else {
              ServletContext sc = (ServletContext) context.getAttributes().get(ServletContext.class.getCanonicalName());
              sc.setAttribute(jsonBufferKey, new SimpleJSONMessageReaderWriter.JSONWrapper(bytes));
              
              response.getMetadata().add(HttpHeaders.ETAG, eTag);
            }
          }
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
