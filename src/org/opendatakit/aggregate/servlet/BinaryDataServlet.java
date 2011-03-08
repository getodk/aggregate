/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * Servlet to display the binary data from a submission
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BinaryDataServlet extends ServletUtilBase {

  private static final String NOT_BINARY_OBJECT = "Requested element is not a binary object";

  private static final Logger logger = Logger.getLogger(BinaryDataServlet.class.getName());
  
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7917801268158165832L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/binaryData";

  /**
   * Handler for HTTP Get request that responds with an Image
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

	CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);
	
    // verify parameters are present
    String keyString = getParameter(req, ServletConsts.BLOB_KEY);
    String downloadAsAttachmentString = getParameter(req, ServletConsts.AS_ATTACHMENT);
    
    if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    SubmissionKey key = new SubmissionKey(keyString);

    byte[] imageBlob = null;
    String unrootedFileName = null;
    String contentType = null;
    Long contentLength = null;
    
    List<SubmissionKeyPart> parts = key.splitSubmissionKey();
    Submission sub = null;
	try {
		sub = Submission.fetchSubmission(parts, cc);
	} catch (ODKFormNotFoundException e1) {
		odkIdNotFoundError(resp);
		return;
	} catch (ODKDatastoreException e) {
		e.printStackTrace();
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Unable to retrieve attachment");
		return;
	}
	
    if ( sub != null ) {
    	BlobSubmissionType b = null;
    	
    	try {
    		SubmissionElement v = null;
    		v = sub.resolveSubmissionKey(parts);
    		if ( v instanceof BlobSubmissionType ) {
    			b = (BlobSubmissionType) v;
    		} else {
        		String path = getKeyPath(parts);
    			logger.severe(NOT_BINARY_OBJECT + ": " + path);
    			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
    					NOT_BINARY_OBJECT);
    			return;
    		}
    	} catch ( Exception e ) {
    		e.printStackTrace();
    		String path = getKeyPath(parts);
    		logger.severe("Unable to retrieve part identified by path: " + path);
    		errorBadParam(resp);
    		return;
    	}
    		
    	if ( b.getAttachmentCount() == 1 ) {
    		try {
				imageBlob = b.getBlob(1, cc);
				unrootedFileName = b.getUnrootedFilename(1);
				contentType = b.getContentType(1);
				contentLength = b.getContentLength(1);
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Unable to retrieve attachment");
				return;
			}
    	} else {
    		SubmissionKeyPart p = parts.get(parts.size()-1);
    		Long ordinal = p.getOrdinalNumber();
    		if (ordinal == null) {
    			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
    					"attachment request must be fully qualified");
				return;
    		} else {
	    		try {
	    			imageBlob = b.getBlob(ordinal.intValue(), cc);
	    			unrootedFileName = b.getUnrootedFilename(ordinal.intValue());
	    			contentType = b.getContentType(ordinal.intValue());
	    			contentLength = b.getContentLength(ordinal.intValue());
	    		} catch (ODKDatastoreException e) {
	    			e.printStackTrace();
	    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	    							"Unable to retrieve attachment");
					return;
	    		}
    		}
    	}
    }
    
    if ( imageBlob != null ) {
    	if ( contentType == null ) {
            resp.setContentType(HtmlConsts.RESP_TYPE_IMAGE_JPEG);
        } else {
          resp.setContentType(contentType);
        }
    	if ( contentLength != null ) {
    		resp.setContentLength(contentLength.intValue());
    	}
    	
    	if ( downloadAsAttachmentString != null && ! "".equals(downloadAsAttachmentString) ) {
    		// set filename if we are downloading to disk...
    		// need this for manifest fetch logic...
	    	if ( unrootedFileName != null ) {
	    		resp.addHeader("Content-Disposition:", "attachment; filename=\""+unrootedFileName+"\"");
	    	}
    	}
    	
        OutputStream os = resp.getOutputStream();
        os.write(imageBlob);
        os.close();
    } else {
    	resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
    	resp.getWriter().print(ErrorConsts.NO_IMAGE_EXISTS);
    }
  }
  
  private final String getKeyPath(List<SubmissionKeyPart> parts) {
	StringBuilder b = new StringBuilder();
	for ( SubmissionKeyPart p : parts ) {
		b.append("/");
		b.append(p.toString());
	}
	return b.toString();
  }

}
