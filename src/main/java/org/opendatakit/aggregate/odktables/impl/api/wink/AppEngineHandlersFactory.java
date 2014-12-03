/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.aggregate.odktables.impl.api.wink;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.ResponseHandler;
import org.opendatakit.aggregate.odktables.rest.ApiConstants;
import org.opendatakit.common.persistence.PersistenceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppEngineHandlersFactory extends HandlersFactory {

  public AppEngineHandlersFactory() {
    // super();
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
