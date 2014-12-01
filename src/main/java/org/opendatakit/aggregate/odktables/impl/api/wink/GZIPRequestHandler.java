package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.UserService;
import org.opendatakit.common.web.CallingContext;

public class GZIPRequestHandler implements RequestHandler {

  private static final Log logger = LogFactory.getLog(GZIPRequestHandler.class.getName());

  public static final String emitGZIPContentEncodingKey = GZIPRequestHandler.class
      .getCanonicalName() + ":emitGZIPContentEncodingKey";

  private boolean suppressContentEncoding = false;
  
  public static final String FALSE = "false";

  /**
   * taken verbatim from wink ContentEncodingRequestFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static class DecoderServletInputStream extends ServletInputStream {

      final private InputStream is;

      public DecoderServletInputStream(InputStream is) {
          this.is = is;
      }

      @Override
      public int readLine(byte[] b, int off, int len) throws IOException {
          return is.read(b, off, len);
      }

      @Override
      public int available() throws IOException {
          return is.available();
      }

      @Override
      public void close() throws IOException {
          is.close();
      }

      @Override
      public synchronized void mark(int readlimit) {
          is.mark(readlimit);
      }

      @Override
      public boolean markSupported() {
          return is.markSupported();
      }

      @Override
      public int read() throws IOException {
          return is.read();
      }

      @Override
      public int read(byte[] b, int off, int len) throws IOException {
          return is.read(b, off, len);
      }

      @Override
      public int read(byte[] b) throws IOException {
          return is.read(b);
      }

      @Override
      public synchronized void reset() throws IOException {
          is.reset();
      }

      @Override
      public long skip(long n) throws IOException {
          return is.skip(n);
      }
  }

  /**
   * taken verbatim from wink ContentEncodingRequestFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static class GZIPDecoderInputStream extends DecoderServletInputStream {

      public GZIPDecoderInputStream(InputStream is) throws IOException {
          super(new GZIPInputStream(is));
      }
  }

  /**
   * taken verbatim from wink ContentEncodingRequestFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static class InflaterDecoderInputStream extends DecoderServletInputStream {

      public InflaterDecoderInputStream(InputStream is) {
          super(new InflaterInputStream(is));
      }

  }

  /**
   * taken verbatim from wink ContentEncodingRequestFilter
   * @author mitchellsundt@gmail.com
   *
   */
  static class HttpServletRequestContentEncodingWrapperImpl extends HttpServletRequestWrapper {

      private ServletInputStream inputStream;

      final private String       contentEncoding;

      public HttpServletRequestContentEncodingWrapperImpl(HttpServletRequest request,
                                                          String contentEncoding) {
          super(request);
          this.contentEncoding = contentEncoding;
      }

      @Override
      public ServletInputStream getInputStream() throws IOException {
          logger.trace("getInputStream() entry"); //$NON-NLS-1$
          if (inputStream == null) {
              inputStream = super.getInputStream();
              if ("gzip".equals(contentEncoding)) { //$NON-NLS-1$
                  logger.trace("Wrapping ServletInputStream with GZIPDecoder"); //$NON-NLS-1$
                  inputStream = new GZIPDecoderInputStream(inputStream);
              } else if ("deflate".equals(contentEncoding)) { //$NON-NLS-1$
                  logger.trace("Wrapping ServletInputStream with Inflater"); //$NON-NLS-1$
                  inputStream = new InflaterDecoderInputStream(inputStream);
              }
          }
          logger.trace("getInputStream() exit - returning " + inputStream.toString()); //$NON-NLS-1$
          return inputStream;
      }

      @Override
      public String getHeader(String name) {
          if (HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(name)) {
              return null;
          }
          return super.getHeader(name);
      }

      @SuppressWarnings("unchecked")
      @Override
      public Enumeration<String> getHeaders(String name) {
          if (HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(name)) {
              // an empty enumeration
              return new Enumeration<String>() {

                  public boolean hasMoreElements() {
                      return false;
                  }

                  public String nextElement() {
                      return null;
                  }
              };
          }
          return super.getHeaders(name);
      }

      @SuppressWarnings("unchecked")
      @Override
      public Enumeration getHeaderNames() {
          final Enumeration<String> headers = super.getHeaderNames();
          List<String> httpHeaders = new ArrayList<String>();
          while (headers.hasMoreElements()) {
              String header = headers.nextElement();
              if (!HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(header)) {
                  httpHeaders.add(header);
              }
          }
          final Iterator<String> iterator = httpHeaders.iterator();
          return new Enumeration<String>() {

              public boolean hasMoreElements() {
                  return iterator.hasNext();
              }

              public String nextElement() {
                  return iterator.next();
              }

          };
      }
  }

  @Override
  public void init(Properties props) {
    suppressContentEncoding = !FALSE.equalsIgnoreCase(props.getProperty(
        "suppressContentEncoding", FALSE));
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

    boolean effectiveSuppressContentEncoding = this.suppressContentEncoding
        || (isGaeEnvironment && !isGaeDevelopmentEnvironment);

    List<String> encodes = headers.get(ApiConstants.CONTENT_ENCODING_HEADER);

    boolean encodesContainsDeflate = false;
    boolean encodesContainsGzip = false;
    if (!effectiveSuppressContentEncoding && encodes != null ) {
      // don't know what to do if there are multiple content-encoding headers.
      // assume that if any of them specify gzip, that the content is simply
      // gzip'd.
      for ( String enc : encodes ) {
        if ( enc.trim().equals(ApiConstants.GZIP_CONTENT_ENCODING) ) {
          encodesContainsGzip = true;
          break;
        }
        if ( enc.trim().equals(ApiConstants.DEFLATE_CONTENT_ENCODING) ) {
          encodesContainsDeflate = true;
          break;
        }
      }
    }

    if ( encodesContainsGzip ) {
      HttpServletRequest sreq = new HttpServletRequestContentEncodingWrapperImpl(req,ApiConstants.GZIP_CONTENT_ENCODING);
      context.setAttribute(HttpServletRequest.class, sreq);
    }
    if ( encodesContainsDeflate ) {
      HttpServletRequest sreq = new HttpServletRequestContentEncodingWrapperImpl(req,ApiConstants.DEFLATE_CONTENT_ENCODING);
      context.setAttribute(HttpServletRequest.class, sreq);
    }

    chain.doChain(context);
  }

}