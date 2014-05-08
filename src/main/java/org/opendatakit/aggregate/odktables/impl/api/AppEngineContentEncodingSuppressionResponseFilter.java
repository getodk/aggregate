package org.opendatakit.aggregate.odktables.impl.api;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.web.CallingContext;

/**
 * AppEngine does automatic GZIP stream processing as part of its supplied
 * framework. Resteasy does the same; left on their own, response entities would
 * be doubly-GZIPped.
 *
 * This class suppresses the Resteasy behavior by removing the Content-Encoding
 * heading that the Resteasy framework adds before its GZIP processing tests for
 * that header and GZIPs the response entity.
 *
 * The net effect is that the entity is not compressed by the Resteasy
 * framework, and is passed un-compressed up to the Google layer.
 *
 * On Google's production servers, the response is then compressed.
 *
 * IMPORTANT: The Google development server does not compress responses (but, if
 * we were to allow the Resteasy framework to compress them, the development
 * server would strip out the Content-Encoding header before returning the data
 * to the client).
 *
 * NOTE: the Priority attribute must be a value between 3000 (when the
 * Content-Encoding header is added by Resteasy) and 4000 (when the GZIP
 * processing is performed).
 *
 * @author mitchellsundt@gmail.com
 *
 */
@ConstrainedTo(RuntimeType.SERVER)
@Priority(3500)
public class AppEngineContentEncodingSuppressionResponseFilter implements WriterInterceptor {

  @Context
  ServletContext sc;
  @Context
  HttpServletRequest req;

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException,
      WebApplicationException {
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String server = sc.getServerInfo();
    @SuppressWarnings("unused")
    boolean isGaeDevelopmentEnvironment = server.contains("Development");
    boolean suppressZipping = cc.getUserService().getCurrentRealm().getIsGaeEnvironment();
    if (suppressZipping) {
      MultivaluedMap<String, Object> headers = context.getHeaders();
      headers.remove(ApiConstants.CONTENT_ENCODING_HEADER);
    }
    context.proceed();
  }

}
