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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.XFormsTableConsts;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.servlet.XFormsDownloadServlet;
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
  private static final String XML_TAG_NAMESPACE = "http://openrosa.org/xforms/xformsManifest";

  private final String downloadRequestURL;

  private Form form;

  public XFormsManifestXmlTable(Form form, String webServerURL) {
    this.downloadRequestURL = HtmlUtil.createUrl(webServerURL) + XFormsDownloadServlet.ADDR;
    this.form = form;
  }

  public void generateXmlManifestList(PrintWriter output) throws IOException {
	Document d = new Document();
	d.setStandalone(true);
	d.setEncoding(HtmlConsts.UTF8_ENCODE);
	Element e = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.MANIFEST_TAG);
	d.addChild(0, Node.ELEMENT, e);
	int idx = 0;
	e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);

    // build XML table of form information
    BlobSubmissionType manifest = form.getManifestFileset();
    if ( manifest != null ) {
    	int fileCount = manifest.getAttachmentCount();
    	for ( int i = 1 ; i <= fileCount ; ++i ) {
	      idx = generateManifestXmlEntry(d, e, idx, manifest, i);
    	}
    }

	KXmlSerializer serializer = new KXmlSerializer();
	serializer.setOutput(output);
	// setting the response content type emits the xml header.
	// just write the body here...
	d.writeChildren(serializer); 
  }

  private int generateManifestXmlEntry(Document d, Element e, int idx, BlobSubmissionType m, int i) {
	  String filename = m.getUnrootedFilename(i);
	  String hash = m.getContentHash(i, m.getCurrentVersion(i));

	  int feIdx = 0;
	  Element fileEntryElement = d.createElement(BasicConsts.EMPTY_STRING, XFormsTableConsts.MEDIA_FILE_TAG);
	  e.addChild(idx++, Node.ELEMENT, fileEntryElement);
	  Element fileNameElement = d.createElement(BasicConsts.EMPTY_STRING, XFormsTableConsts.FILE_NAME_TAG);
	  fileEntryElement.addChild(feIdx++, Node.ELEMENT, fileNameElement);
	  fileNameElement.addChild(0, Node.TEXT, filename);
	  Element hashElement = d.createElement(BasicConsts.EMPTY_STRING, XFormsTableConsts.HASH_TAG);
	  fileEntryElement.addChild(feIdx++, Node.ELEMENT, hashElement);
	  hashElement.addChild(0, Node.TEXT, hash);
	  Element downloadElement = d.createElement(BasicConsts.EMPTY_STRING, XFormsTableConsts.DOWNLOAD_URL_TAG);
	  fileEntryElement.addChild(feIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
	  fileEntryElement.addChild(feIdx++, Node.ELEMENT, downloadElement);
	  {
		Map<String, String> properties = new HashMap<String, String>();
		SubmissionKey k = m.generateSubmissionKey(i, m.getCurrentVersion(i));
	    properties.put(ServletConsts.BLOB_KEY, k.toString());
	    properties.put(ServletConsts.AS_ATTACHMENT, "true");
	    String urlLink = HtmlUtil.createLinkWithProperties(downloadRequestURL, properties);
	    downloadElement.addChild(0, Node.TEXT, urlLink);
	  }
      e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      return idx;
  }
}
