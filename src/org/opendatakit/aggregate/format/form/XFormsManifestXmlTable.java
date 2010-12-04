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

package org.opendatakit.aggregate.format.form;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.XFormsTableConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;

/**
 * Generates an OpenRosa-compliant xml description of forms for the servlet
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class XFormsManifestXmlTable {

  private final String downloadRequestURL;

  private Form form;

  public XFormsManifestXmlTable(Form form, String webServerURL) {
    this.downloadRequestURL = HtmlUtil.createUrl(webServerURL) + BinaryDataServlet.ADDR;
    this.form = form;
  }

  public String generateXmlManifestList() {
    StringBuilder b = new StringBuilder();
    b.append(HtmlConsts.BEGIN_OPEN_TAG);
    b.append(XFormsTableConsts.MANIFEST_TAG);
    b.append(BasicConsts.SPACE);
    b.append(HtmlUtil.createAttribute("xmlns", "http://openrosa.org/xforms/xformsManifest"));
    b.append(HtmlConsts.END_TAG);


    // build XML table of form information
    BlobSubmissionType manifest = form.getManifestFileset();
    if ( manifest != null ) {
    	int fileCount = manifest.getAttachmentCount();
    	for ( int i = 1 ; i <= fileCount ; ++i ) {
	      generateManifestXmlEntry(b, manifest, i);
	      b.append(BasicConsts.NEW_LINE);
    	}
    }
    b.append(HtmlUtil.createEndTag(XFormsTableConsts.MANIFEST_TAG));
    return b.toString();
  }

  private void generateManifestXmlEntry(StringBuilder b, BlobSubmissionType m, int i) {
	  String filename = m.getUnrootedFilename(i);
	  String hash = m.getContentHash(i, m.getCurrentVersion(i));
		  
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.FILE_TAG));
    b.append(BasicConsts.NEW_LINE);
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.FILE_NAME_TAG));
	b.append(filename);
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.FILE_NAME_TAG));
    b.append(BasicConsts.NEW_LINE);
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.HASH_TAG));
	b.append(hash);
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.HASH_TAG));
    b.append(BasicConsts.NEW_LINE);
	b.append(HtmlUtil.createBeginTag(XFormsTableConsts.DOWNLOAD_URL_TAG));
	{
		Map<String, String> properties = new HashMap<String, String>();
		SubmissionKey k = m.generateSubmissionKey(i, m.getCurrentVersion(i));
	    properties.put(ServletConsts.BLOB_KEY, k.toString());
	    String urlLink = HtmlUtil.createLinkWithProperties(downloadRequestURL, properties);
	    b.append(urlLink);
	}
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.DOWNLOAD_URL_TAG));
    b.append(BasicConsts.NEW_LINE);
	b.append(HtmlUtil.createEndTag(XFormsTableConsts.FILE_TAG));
    b.append(BasicConsts.NEW_LINE);
  }
}
