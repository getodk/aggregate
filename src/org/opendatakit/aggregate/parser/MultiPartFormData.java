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

package org.opendatakit.aggregate.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opendatakit.aggregate.constants.ParserConsts;

/**
 * Parses a multi part form request into a set of multiPartFormItems. The
 * information stored in items are indexed by either the field name or the file
 * name (or both) provided in the http submission
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class MultiPartFormData {

	private final Map<String, String> simpleFieldNameMap;
	
	private final Map<String, MultiPartFormItem> fieldNameMap;

	private final Map<String, MultiPartFormItem> fileNameMap;

	private final Map<String, MultiPartFormItem> fileNameWithoutExtensionNameMap;

	/**
	 * Construct a mult-part form data container by parsing a multi part form
	 * request into a set of multipartformitems. The information are stored in
	 * items and are indexed by either the field name or the file name (or both)
	 * provided in the http submission
	 * 
	 * @param req
	 *            an HTTP request from a multipart form
	 * 
	 * @throws FileUploadException
	 * @throws IOException
	 */
	public MultiPartFormData(HttpServletRequest req)
			throws FileUploadException, IOException {

		simpleFieldNameMap = new HashMap<String, String>();
		fieldNameMap = new HashMap<String, MultiPartFormItem>();
		fileNameMap = new HashMap<String, MultiPartFormItem>();
		fileNameWithoutExtensionNameMap = new HashMap<String, MultiPartFormItem>();

		ServletFileUpload upload = new ServletFileUpload(
				new DiskFileItemFactory());
		int size = req.getContentLength();
		if (size > 0) {
			upload.setFileSizeMax(size);
		} else {
			upload.setFileSizeMax(ParserConsts.FILE_SIZE_MAX);
		}

		List<MultiPartFormItem> fileNameList = new ArrayList<MultiPartFormItem>();
		
		FileItemIterator items = upload.getItemIterator(req);
		while (items.hasNext()) {
			FileItemStream item = items.next();
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			BufferedInputStream formStream = new BufferedInputStream(item
					.openStream());

			// TODO: determine ways to possibly improve efficiency
			int nextByte = formStream.read();
			while (nextByte != -1) {
				byteStream.write(nextByte);
				nextByte = formStream.read();
			}
			formStream.close();

			if ( item.isFormField() ) {
				simpleFieldNameMap.put(item.getFieldName(), byteStream.toString());
			} else {
				MultiPartFormItem data = new MultiPartFormItem(item.getFieldName(),
						item.getName(), item.getContentType(), byteStream);
	
				String fieldName = item.getFieldName();
				if (fieldName != null) {
					fieldNameMap.put(fieldName, data);
				}
				String fileName = item.getName();
				if (fileName != null && fileName.length() != 0) {
					fileNameList.add(data);
				}
			}
		}

		// Find the common prefix to the filenames being uploaded...
		// Deal with Windows backslash file separator...
		boolean first = true;
		String[] commonPath = null;
		int commonPrefix = 0;
		for (MultiPartFormItem e : fileNameList) {
			String fullFilePath = e.getFilename();
			if (first) {
				commonPath = fullFilePath.split("[/\\\\]");
				commonPrefix = commonPath.length - 1; // everything but
														// filename...
				first = false;
			} else {
				String[] path = fullFilePath.split("[/\\\\]");
				int pathPrefix = path.length - 1; // everything but
													// filename...
				if (pathPrefix < commonPrefix)
					commonPrefix = pathPrefix;
				for (int i = 0; i < commonPrefix; ++i) {
					if (!commonPath[i].equals(path[i])) {
						commonPrefix = i;
						break;
					}
				}
			}
		}

		// and now go back through the attachments, adjusting the filename
		// and building the filename mapping.
		for (MultiPartFormItem e : fileNameList) {
			String fullFilePath = e.getFilename();
			String[] filePath = fullFilePath.split("[/\\\\]");
			StringBuilder b = new StringBuilder();
			first = true;
			// start at the first entry after the common prefix...
			for (int i = commonPrefix; i < filePath.length; ++i) {
				if (!first) {
					b.append("/");
				}
				first = false;
				b.append(filePath[i]);
			}
			// and construct the filename with common directory prefix
			// stripped.
			String fileName = b.toString();
			e.setFilename(fileName);
			if (fileName != null) {
				// TODO: possible bug in ODK collect truncating file extension
				// may need to remove this code after ODK collect is fixed
				int indexOfExtension = fileName.lastIndexOf(".");
				if (indexOfExtension > 0) {
					fileNameWithoutExtensionNameMap.put(
							fileName.substring(0, indexOfExtension), e);
				}
				fileNameMap.put(fileName, e);
			}
		}
	}

	public String getSimpleFormField(String fieldName) {
		return simpleFieldNameMap.get(fieldName);
	}
	
	public MultiPartFormItem getFormDataByFieldName(String fieldName) {
		return fieldNameMap.get(fieldName);
	}

	public MultiPartFormItem getFormDataByFileName(String fileName) {
		MultiPartFormItem item = fileNameMap.get(fileName);
		if ( item != null ) return item;
		// workaround for truncated filenames in bad versions of Collect
		// TODO: keep or remove?
		return fileNameWithoutExtensionNameMap.get(fileName);
	}
	
	public Set<Map.Entry<String,MultiPartFormItem>> getFileNameEntrySet() {
		return Collections.unmodifiableSet(fileNameMap.entrySet());
	}

	public Set<Map.Entry<String,MultiPartFormItem>> getFieldNameEntrySet() {
		return Collections.unmodifiableSet(fieldNameMap.entrySet());
	}
}
