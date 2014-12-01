package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.ResponseHandler;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;

public class GZIPResponseHandler implements ResponseHandler {

  private static final Log logger = LogFactory.getLog(GZIPResponseHandler.class.getName());

  public static final String emitGZIPContentEncodingKey = GZIPRequestHandler.class
      .getCanonicalName() + ":emitGZIPContentEncodingKey";

  public static final String FALSE = "false";

  private boolean suppressAcceptContentEncoding = false;

  /**
   * taken verbatim from wink ContentEncodingReponseFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static abstract class EncodedOutputStream extends ServletOutputStream {

      private boolean              isWritten = false;

      private DeflaterOutputStream outputStream;

      public EncodedOutputStream(DeflaterOutputStream outputStream) {
          this.outputStream = outputStream;
      }

      @Override
      public void write(int b) throws IOException {
          if (!isWritten) {
              isFirstWrite();
              isWritten = true;
          }
          outputStream.write(b);
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
          if (!isWritten) {
              isFirstWrite();
              isWritten = true;
          }
          outputStream.write(b, off, len);
      }

      @Override
      public void write(byte[] b) throws IOException {
          if (!isWritten) {
              isFirstWrite();
              isWritten = true;
          }
          outputStream.write(b);
      }

      @Override
      public void flush() throws IOException {
          if (!isWritten) {
              isFirstWrite();
              isWritten = true;
          }
          outputStream.flush();
      }

      @Override
      public void close() throws IOException {
          outputStream.finish();
          outputStream.close();
      }

      public void finish() throws IOException {
          outputStream.finish();
      }

      public abstract void isFirstWrite();
  }

  /**
   * taken verbatim from wink ContentEncodingReponseFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static class GzipEncoderOutputStream extends EncodedOutputStream {

      final private HttpServletResponse response;

      public GzipEncoderOutputStream(OutputStream outputStream, HttpServletResponse response)
          throws IOException {
          super(new GZIPOutputStream(outputStream));
          this.response = response;
      }

      @Override
      public void isFirstWrite() {
          response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip"); //$NON-NLS-1$
          response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
      }
  }

  /**
   * taken verbatim from wink ContentEncodingReponseFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static class DeflaterContentEncodedOutputStream extends EncodedOutputStream {

      final private HttpServletResponse response;

      public DeflaterContentEncodedOutputStream(OutputStream outputStream,
                                                HttpServletResponse response) throws IOException {
          super(new DeflaterOutputStream(outputStream));
          this.response = response;
      }

      @Override
      public void isFirstWrite() {
          response.addHeader(HttpHeaders.CONTENT_ENCODING, "deflate"); //$NON-NLS-1$
          response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
      }
  }

  /**
   * taken mostly verbatim from wink ContentEncodingReponseFilter
   * - modified the logging statements to use ACL vs SLF4J
   * - modified to not use the AcceptEncoding HeaderDelegate
   * 
   * @author mitchellsundt@gmail.com
   *
   */
  static class HttpServletResponseContentEncodingWrapperImpl extends HttpServletResponseWrapper {

      final private String acceptEncoding;

      private ServletOutputStream  outputStream;

      private EncodedOutputStream  encodedOutputStream;

      private int                  varyHeaderCount = 0;

      public EncodedOutputStream getEncodedOutputStream() {
          return encodedOutputStream;
      }

      public HttpServletResponseContentEncodingWrapperImpl(HttpServletResponse response,
                                                           String acceptEncoding) {
          super(response);
          this.acceptEncoding = acceptEncoding;
      }

      private boolean containsAcceptEncoding(String value) {
          String[] str = value.split(","); //$NON-NLS-1$
          for (String s : str) {
              if (HttpHeaders.ACCEPT_ENCODING.equalsIgnoreCase(s.trim())) {
                  return true;
              }
          }
          return false;
      }

      @Override
      public void addHeader(String name, String value) {
          logger.trace("addHeader(" + name + ", " + value + ") entry"); //$NON-NLS-1$
          
          /*
           * this logic is added to append Accept-Encoding to the first Vary
           * header value.
           */
          if (HttpHeaders.VARY.equalsIgnoreCase(name)) {
              ++varyHeaderCount;
              logger.trace("Vary header count is now " + varyHeaderCount); //$NON-NLS-1$
              if (varyHeaderCount == 1) {
                  // add the Accept-Encoding value to the Vary header
                  if (!"*".equals(value) && !containsAcceptEncoding(value)) { //$NON-NLS-1$
                      logger
                          .trace("Vary header did not contain Accept-Encoding so appending to Vary header value"); //$NON-NLS-1$
                      super.addHeader(HttpHeaders.VARY, value + ", " //$NON-NLS-1$
                          + HttpHeaders.ACCEPT_ENCODING);
                      return;
                  }
              } else if (HttpHeaders.ACCEPT_ENCODING.equals(value)) {
                  logger
                      .trace("Skipping Vary header that was only Accept-Encoding since it was already appended to a previous Vary header value"); //$NON-NLS-1$
                  // skip this addition since it has already been appended to
                  // the first Vary value by the "if true" block above
                  return;
              }
          }
          super.addHeader(name, value);
      }

      @Override
      public ServletOutputStream getOutputStream() throws IOException {
          logger.trace("getOutputStream() entry"); //$NON-NLS-1$
          if (outputStream == null) {
              logger.trace("output stream was null"); //$NON-NLS-1$
              this.outputStream = super.getOutputStream();
              logger.trace("encoding under test is " + acceptEncoding); //$NON-NLS-1$
              if ("gzip".equalsIgnoreCase(acceptEncoding)) { //$NON-NLS-1$
                  logger.trace("going to use gzip encoding"); //$NON-NLS-1$
                  this.encodedOutputStream = new GzipEncoderOutputStream(outputStream, this);
                  this.outputStream = encodedOutputStream;
                  logger.trace("getOutputStream() exit - returning gzipped encode stream"); //$NON-NLS-1$
                  return outputStream;
              } else if ("deflate".equalsIgnoreCase(acceptEncoding)) { //$NON-NLS-1$
                  logger.trace("going to use deflate encoding"); //$NON-NLS-1$
                  this.encodedOutputStream =
                      new DeflaterContentEncodedOutputStream(outputStream, this);
                  this.outputStream = encodedOutputStream;
                  logger.trace("getOutputStream() exit - returning deflate encode stream"); //$NON-NLS-1$
                  return outputStream;
              }
          }
          logger.trace("getOutputStream() exit - returning output stream"); //$NON-NLS-1$
          return outputStream;
      }
  }

  @Override
  public void init(Properties props) {
    suppressAcceptContentEncoding = !FALSE.equalsIgnoreCase(props.getProperty(
        "suppressAcceptContentEncoding", FALSE));
  }

  @Override
  public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
    UriInfo info = context.getUriInfo();
    System.out.println("The path relative to the base URI is : " + context.getHttpMethod() + " "
        + info.getPath());

    ServletContext sc = (ServletContext) context.getAttributes().get(
        ServletContext.class.getCanonicalName());
    HttpServletRequest req = (HttpServletRequest) context.getAttributes().get(
        HttpServletRequest.class.getCanonicalName());
    HttpServletResponse resp = (HttpServletResponse) context.getAttributes().get(
        HttpServletResponse.class.getCanonicalName());
    CallingContext cc = ContextFactory.getCallingContext(sc, req);
    String server = sc.getServerInfo();

    /*
     * AppEngine leaves the GZIP header even though it unzips the content
     * before delivering it to the app.
     */
    boolean isGaeDevelopmentEnvironment = server.contains("Development");
    boolean isGaeEnvironment = false;
    try {
      UserService us = cc.getUserService();
      if ( us != null ) {
        Realm realm = us.getCurrentRealm();
        if ( realm != null ) {
          Boolean outcome = realm.getIsGaeEnvironment();
          if ( outcome != null ) {
            isGaeEnvironment = outcome;
          }
        }
      }
    } catch ( Exception e ) {
      // ignore...
    }
    MultivaluedMap<String, String> headers = context.getHttpHeaders().getRequestHeaders();

    // And process the Accept-Content-Encoding headers
    boolean effectiveSuppressAcceptContentEncoding = this.suppressAcceptContentEncoding
        || (isGaeEnvironment && !isGaeDevelopmentEnvironment);

    List<String> accepts = headers.get(ApiConstants.ACCEPT_CONTENT_ENCODING_HEADER);

    boolean requestWildcardOutputEncoding = false;
    boolean requestGzipOutputEncoding = false;
    boolean requestDeflateOutputEncoding = false;
    if (!effectiveSuppressAcceptContentEncoding && accepts != null ) {
      // there might be multiple headers.
      // Each header is a comma-separated list of encodings.
      for ( String acc : accepts ) {
        String[] accEncodings = acc.split(",");
        for ( String enc : accEncodings ) {
          if ( enc.trim().equals(ApiConstants.GZIP_CONTENT_ENCODING) ) {
            requestGzipOutputEncoding = true;
          }
          if ( enc.trim().equals(ApiConstants.DEFLATE_CONTENT_ENCODING) ) {
            requestDeflateOutputEncoding = true;
          }
          if ( enc.trim().equals("*") ) {
            requestWildcardOutputEncoding = true;
          }
        }
      }
    }
    
    if (requestGzipOutputEncoding || requestWildcardOutputEncoding) {
      HttpServletResponseContentEncodingWrapperImpl wrappedServletResponse =
          new HttpServletResponseContentEncodingWrapperImpl(resp,
                                                            ApiConstants.GZIP_CONTENT_ENCODING);
      context.setAttribute(HttpServletResponse.class, wrappedServletResponse);
    } else if (requestDeflateOutputEncoding) {
      HttpServletResponseContentEncodingWrapperImpl wrappedServletResponse =
          new HttpServletResponseContentEncodingWrapperImpl(resp,
                                                            ApiConstants.DEFLATE_CONTENT_ENCODING);
      context.setAttribute(HttpServletResponse.class, wrappedServletResponse);
    }

    sc.setAttribute(emitGZIPContentEncodingKey, Boolean.toString(requestGzipOutputEncoding));

    chain.doChain(context);
  }

}
