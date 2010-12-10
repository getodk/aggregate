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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet to display the binary data from a submission
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BinaryDataServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7917801268158165832L;

  /**
   * URI from base
   */
  public static final String ADDR = "binaryData";

  /**
   * Handler for HTTP Get request that responds with an Image
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    // TOOD: figure out how to do picture security
    // verify user is logged in
    // if (!verifyCredentials(req, resp)) {
    // return;
    // }
      UserService userService = (UserService) ContextFactory.get().getBean(
              BeanDefs.USER_BEAN);
      User user = userService.getCurrentUser();
	
    // verify parameters are present
    String keyString = getParameter(req, ServletConsts.BLOB_KEY);
    String downloadAsAttachmentString = getParameter(req, ServletConsts.AS_ATTACHMENT);
    
    if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    SubmissionKey key = new SubmissionKey(keyString);

    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);

    byte[] imageBlob = null;
    String unrootedFileName = null;
    String contentType = null;
    Long contentLength = null;
    
    List<SubmissionKeyPart> parts = key.splitSubmissionKey();
    Submission sub = null;
	try {
		sub = Submission.fetchSubmission(parts, ds, user);
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
    	SubmissionElement v = sub.resolveSubmissionKey(parts);
    	BlobSubmissionType b = (BlobSubmissionType) v;
    	if ( b.getAttachmentCount() == 1 ) {
    		String version = b.getCurrentVersion(1);
    		try {
				imageBlob = b.getBlob(1, version);
				unrootedFileName = b.getUnrootedFilename(1);
				contentType = b.getContentType(1, version);
				contentLength = b.getContentLength(1, version);
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"Unable to retrieve attachment");
				return;
			}
    	} else {
    		SubmissionKeyPart p = parts.get(parts.size()-1);
    		String version = p.getVersion();
    		Long ordinal = p.getOrdinalNumber();
    		if ((version == null) || (ordinal == null)) {
    			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, 
    					"attachment request must be fully qualified");
    		} else {
	    		try {
	    			imageBlob = b.getBlob(ordinal.intValue(), version);
	    			unrootedFileName = b.getUnrootedFilename(ordinal.intValue());
	    			contentType = b.getContentType(ordinal.intValue(), version);
	    			contentLength = b.getContentLength(ordinal.intValue(), version);
	    		} catch (ODKDatastoreException e) {
	    			e.printStackTrace();
	    			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
	    							"Unable to retrieve attachment");
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

}
