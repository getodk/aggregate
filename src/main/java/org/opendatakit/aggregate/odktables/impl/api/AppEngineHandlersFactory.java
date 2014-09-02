package org.opendatakit.aggregate.odktables.impl.api;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.web.CallingContext;

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

}
