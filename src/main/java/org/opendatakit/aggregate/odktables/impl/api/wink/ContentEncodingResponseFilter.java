/*
 * Copyright (C) 2014 University of Washington
 * Substantially copied from org.apache.wink.server.internal.servlet.contentencode
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.common.internal.http.AcceptEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied mostly verbatim from org.apache.wink.server.internal.servlet.contentencode
 * 
 * However, that implementation did not properly recompute the Content-Length.
 * This implementation does.
 * 
 * For increased efficiency, the client and server should be converted to use 
 * the chunked Transfer-Encoding.
 * 
 * @author mitchellsundt@gmail.com
 */

/**
 * A servlet filter which changes the HttpServletResponse to
 * automatically deflate or GZIP encode an outgoing response if the incoming
 * request has an appropriate Accept-Encoding request header value. Add to your
 * web.xml like: <br/>
 * <code>
 * &lt;filter&gt;<br/>
        &lt;filter-name&gt;ContentEncodingResponseFilter&lt;/filter-name&gt;<br/>
        &lt;filter-class&gt;org.apache.wink.server.internal.servlet.contentencode.ContentEncodingResponseFilter&lt;/filter-class&gt;<br/>
    &lt;/filter&gt;<br/>
    <br/>
    &lt;filter-mapping&gt;<br/>
        &lt;filter-name&gt;ContentEncodingResponseFilter&lt;/filter-name&gt;<br/>
        &lt;url-pattern&gt;/*&lt;/url-pattern&gt;<br/>
    &lt;/filter-mapping&gt;<br/>
 * </code>
 */
public class ContentEncodingResponseFilter implements Filter {

  private static final Log log = LogFactory.getLog(ContentEncodingResponseFilter.class);

    private final static Logger                         logger                       =
                                                                                         LoggerFactory
                                                                                             .getLogger(ContentEncodingResponseFilter.class);

    private final static HeaderDelegate<AcceptEncoding> acceptEncodingHeaderDelegate =
                                                                                         RuntimeDelegate
                                                                                             .getInstance()
                                                                                             .createHeaderDelegate(AcceptEncoding.class);

    public void init(FilterConfig arg0) throws ServletException {
        logger.trace("init({}) entry", arg0); //$NON-NLS-1$
        /* do nothing */
        logger.trace("init() exit"); //$NON-NLS-1$
    }

    public void destroy() {
        logger.trace("destroy() entry"); //$NON-NLS-1$
        /* do nothing */
        logger.trace("destroy() exit"); //$NON-NLS-1$
    }

    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        if (logger.isTraceEnabled()) {
            logger.trace("doFilter({}, {}, {}) entry", new Object[] {servletRequest, //$NON-NLS-1$
                servletResponse, chain});
        }
        /*
         * wraps the servlet response if necessary
         */
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
            final AcceptEncoding acceptEncoding = getAcceptEncodingHeader(httpServletRequest);
            logger.trace("AcceptEncoding header was {}", acceptEncoding); //$NON-NLS-1$
            if (acceptEncoding != null && (acceptEncoding.isAnyEncodingAllowed() || acceptEncoding
                .getAcceptableEncodings().size() > 0)) {
                logger.trace("AcceptEncoding header was set so wrapping HttpServletResponse"); //$NON-NLS-1$
                HttpServletResponseContentEncodingWrapperImpl wrappedServletResponse =
                    new HttpServletResponseContentEncodingWrapperImpl(
                                                                      (HttpServletResponse)servletResponse,
                                                                      acceptEncoding);
                logger.trace("Passing on request and response down the filter chain"); //$NON-NLS-1$
                chain.doFilter(servletRequest, wrappedServletResponse);
                logger.trace("Finished filter chain"); //$NON-NLS-1$
                EncodedOutputStream encodedOutputStream =
                    wrappedServletResponse.getEncodedOutputStream();
                if (encodedOutputStream != null) {
                    logger.trace("Calling encodedOutputStream finish"); //$NON-NLS-1$
                    // Changed from finish(): close the stream to complete the write...
                    encodedOutputStream.close();
                }
                logger.trace("doFilter exit()"); //$NON-NLS-1$
                return;
            }
        }
        logger.trace("AcceptEncoding header not found so processing like normal request"); //$NON-NLS-1$
        chain.doFilter(servletRequest, servletResponse);
        logger.trace("doFilter exit()"); //$NON-NLS-1$
    }

    /**
     * Returns an AcceptEncoding object if there is an Accept Encoding header.
     * 
     * @param httpServletRequest
     * @return
     */
    static AcceptEncoding getAcceptEncodingHeader(HttpServletRequest httpServletRequest) {
        logger.trace("getAcceptEncodingHeader({}) entry", httpServletRequest); //$NON-NLS-1$
        Enumeration<String> acceptEncodingEnum =
            httpServletRequest.getHeaders(HttpHeaders.ACCEPT_ENCODING);
        StringBuilder sb = new StringBuilder();
        if (acceptEncodingEnum.hasMoreElements()) {
            sb.append(acceptEncodingEnum.nextElement());
            while (acceptEncodingEnum.hasMoreElements()) {
                sb.append(","); //$NON-NLS-1$
                sb.append(acceptEncodingEnum.nextElement());
            }
            String acceptEncodingHeader = sb.toString();
            logger.trace("acceptEncodingHeader is {} so returning as AcceptEncodingHeader", //$NON-NLS-1$
                         acceptEncodingHeader);
            return acceptEncodingHeaderDelegate.fromString(acceptEncodingHeader);
        }
        logger.trace("No Accept-Encoding header"); //$NON-NLS-1$
        logger.trace("getAcceptEncodingHeader() exit - returning null"); //$NON-NLS-1$
        return null;
    }

    static abstract class EncodedOutputStream extends ServletOutputStream {

    	private boolean  isReady = false;
		private boolean              isWritten = false;

        private DeflaterOutputStream outputStream = null;
        private ByteArrayOutputStream byteStream = null;
        private OutputStream actualOutputStream = null;

        public EncodedOutputStream() {
        }

        public void init(DeflaterOutputStream outputStream, ByteArrayOutputStream byteStream, OutputStream actualOutputStream) {
            this.outputStream = outputStream;
            this.byteStream = byteStream;
            this.actualOutputStream = actualOutputStream;
            isReady = true;
        }

		public boolean isReady() {
			return isReady;
		}

		public void setWriteListener(WriteListener arg0) {
			throw new IllegalStateException("WriteListener functionality is not implemented!");
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
            setContentLength(byteStream.size());
            actualOutputStream.write(byteStream.toByteArray());
            actualOutputStream.flush();
            actualOutputStream.close();
        }

        public void finish() throws IOException {
            outputStream.finish();
        }

        public abstract void isFirstWrite();
        
        public abstract void setContentLength(int length);
    }

    static class GzipEncoderOutputStream extends EncodedOutputStream {

        final private HttpServletResponseContentEncodingWrapperImpl response;

        public GzipEncoderOutputStream(OutputStream outputStream, 
            HttpServletResponseContentEncodingWrapperImpl response) throws IOException {
            super();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            init(new GZIPOutputStream(byteStream), byteStream, outputStream);
            this.response = response;
        }

        @Override
        public void isFirstWrite() {
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip"); //$NON-NLS-1$
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        }

        @Override
        public void setContentLength(int length) {
          response.privateAddContentLengthHeader(length);
        }
    }

    static class DeflaterContentEncodedOutputStream extends EncodedOutputStream {

        final private HttpServletResponseContentEncodingWrapperImpl response;

        public DeflaterContentEncodedOutputStream(OutputStream outputStream,
            HttpServletResponseContentEncodingWrapperImpl response) throws IOException {
            super();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            init(new DeflaterOutputStream(byteStream), byteStream, outputStream );
            this.response = response;
        }

        @Override
        public void isFirstWrite() {
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "deflate"); //$NON-NLS-1$
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        }

        @Override
        public void setContentLength(int length) {
          response.privateAddContentLengthHeader(length);
        }
    }

    static class HttpServletResponseContentEncodingWrapperImpl extends HttpServletResponseWrapper {

        private final static Logger  logger          =
                                                         LoggerFactory
                                                             .getLogger(HttpServletResponseContentEncodingWrapperImpl.class);

        final private AcceptEncoding acceptEncoding;

        private ServletOutputStream  outputStream;

        private EncodedOutputStream  encodedOutputStream;

        private int                  varyHeaderCount = 0;

        public EncodedOutputStream getEncodedOutputStream() {
            return encodedOutputStream;
        }

        public HttpServletResponseContentEncodingWrapperImpl(HttpServletResponse response,
                                                             AcceptEncoding acceptEncoding) {
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
        
        void privateAddContentLengthHeader(int value) {
          super.addHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(value));
        }

        @Override
        public void addHeader(String name, String value) {
            logger.trace("addHeader({}, {}) entry", name, value); //$NON-NLS-1$
            /*
             * this logic is added to append Accept-Encoding to the first Vary
             * header value.
             */
            if (HttpHeaders.VARY.equalsIgnoreCase(name)) {
                ++varyHeaderCount;
                logger.trace("Vary header count is now {}", varyHeaderCount); //$NON-NLS-1$
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
            // Content-Length is incorrect if we are compressing....
            if (!HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name) ) {
              super.addHeader(name, value);
            }
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            logger.trace("getOutputStream() entry"); //$NON-NLS-1$
            if (outputStream == null) {
                logger.trace("output stream was null"); //$NON-NLS-1$
                this.outputStream = super.getOutputStream();
                List<String> acceptableEncodings = acceptEncoding.getAcceptableEncodings();
                logger.trace("acceptableEncodings is {}", acceptableEncodings); //$NON-NLS-1$

                // prefer GZip...
                boolean hasDeflate = false;
                boolean hasGZip = false;
                for (String encoding : acceptableEncodings) {
                    logger.trace("encoding under test is {}", encoding); //$NON-NLS-1$
                    if ("gzip".equalsIgnoreCase(encoding)) { //$NON-NLS-1$
                      hasGZip = true;
                    } else if ("deflate".equalsIgnoreCase(encoding)) { //$NON-NLS-1$
                      hasDeflate = true;
                    }
                }

                if (hasGZip || (acceptEncoding.isAnyEncodingAllowed() && !acceptEncoding.getBannedEncodings()
                    .contains("gzip"))) { //$NON-NLS-1$
                  if ( hasGZip ) {
                    log.info("going to use gzip encoding"); //$NON-NLS-1$
                  } else {
                    log.info("going to use gzip encoding because any encoding is allowed"); //$NON-NLS-1$
                  }
                    this.encodedOutputStream = new GzipEncoderOutputStream(outputStream, this);
                    this.outputStream = encodedOutputStream;
                    logger.trace("getOutputStream() exit - returning gzipped encode stream"); //$NON-NLS-1$
                    return outputStream;
                } else if ( hasDeflate ) {
                  log.info("going to use deflate encoding"); //$NON-NLS-1$
                  this.encodedOutputStream =
                      new DeflaterContentEncodedOutputStream(outputStream, this);
                  this.outputStream = encodedOutputStream;
                  logger.trace("getOutputStream() exit - returning deflate encode stream"); //$NON-NLS-1$
                  return outputStream;
                }
            }
            log.info("no content encoding");
            logger.trace("getOutputStream() exit - returning output stream"); //$NON-NLS-1$
            return outputStream;
        }
    }
}
