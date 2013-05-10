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
import org.opendatakit.aggregate.form.FormInfo;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.servlet.XFormsDownloadServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

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

  private IForm form;

  public XFormsManifestXmlTable(IForm form, String webServerUrl) {
    this.downloadRequestURL = webServerUrl + BasicConsts.FORWARDSLASH + XFormsDownloadServlet.ADDR;
    this.form = form;
  }

  public void generateXmlManifestList(PrintWriter output, CallingContext cc) throws IOException, ODKDatastoreException {
	Document d = new Document();
	d.setStandalone(true);
	d.setEncoding(HtmlConsts.UTF8_ENCODE);
	Element e = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.MANIFEST_TAG);
	e.setPrefix(null, XML_TAG_NAMESPACE);
	d.addChild(0, Node.ELEMENT, e);
	int idx = 0;
	e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);

    // build XML table of form information
    BinaryContentManipulator manifest = form.getManifestFileset();
    if ( manifest != null ) {
    	int fileCount = manifest.getAttachmentCount(cc);
    	for ( int i = 1 ; i <= fileCount ; ++i ) {
	      idx = generateManifestXmlEntry(d, e, idx, form.getUri(), manifest, i, cc);
    	}
    }

	KXmlSerializer serializer = new KXmlSerializer();
	serializer.setOutput(output);
	// setting the response content type emits the xml header.
	// just write the body here...
	d.writeChildren(serializer); 
	serializer.flush();
  }

  private int generateManifestXmlEntry(Document d, Element e, int idx, String uri, BinaryContentManipulator m, int i, CallingContext cc) throws ODKDatastoreException {
	  String filename = m.getUnrootedFilename(i, cc);
	  String hash = m.getContentHash(i, cc);

	  // if we don't have the file (hash==null), then don't emit anything.
	  if ( hash == null ) return idx;
	  
	  int feIdx = 0;
	  Element fileEntryElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.MEDIA_FILE_TAG);
	  e.addChild(idx++, Node.ELEMENT, fileEntryElement);
	  Element fileNameElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.FILE_NAME_TAG);
	  fileEntryElement.addChild(feIdx++, Node.ELEMENT, fileNameElement);
	  fileNameElement.addChild(0, Node.TEXT, filename);
	  Element hashElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.HASH_TAG);
	  fileEntryElement.addChild(feIdx++, Node.ELEMENT, hashElement);
	  hashElement.addChild(0, Node.TEXT, hash);
	  Element downloadElement = d.createElement(XML_TAG_NAMESPACE, XFormsTableConsts.DOWNLOAD_URL_TAG);
	  fileEntryElement.addChild(feIdx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
	  fileEntryElement.addChild(feIdx++, Node.ELEMENT, downloadElement);
	  {
		Map<String, String> properties = new HashMap<String, String>();
		SubmissionKey k = FormInfo.getManifestSubmissionKey(uri, i);
	    properties.put(ServletConsts.BLOB_KEY, k.toString());
	    properties.put(ServletConsts.AS_ATTACHMENT, "true");
	    String urlLink = HtmlUtil.createLinkWithProperties(downloadRequestURL, properties);
	    downloadElement.addChild(0, Node.TEXT, urlLink);
	  }
      e.addChild(idx++, Node.IGNORABLE_WHITESPACE, BasicConsts.NEW_LINE);
      return idx;
  }
}
