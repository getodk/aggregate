/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.odk.aggregate.constants.ParserConsts;

/**
 * Parses a multi part form request into a set of multiPartFormItems. The
 * information stored in items are indexed by either the field name or 
 * the file name (or both) provided in the http submission
 * 
 * @author wbrunette@gmail.com
 *
 */
public class MultiPartFormData {

  private Map<String, MultiPartFormItem> fieldNameMap;
  
  private Map<String, MultiPartFormItem> fileNameMap;
  
  /**
   * Construct a mult-part form data container by parsing
   * a multi part form request into a set of multipartformitems. The
   * information are stored in items and are indexed by either 
   * the field name or the file name (or both) provided in the http submission
   * 
   * @param req
   *     an HTTP request from a multipart form 

   * @throws FileUploadException
   * @throws IOException
   */
  public MultiPartFormData(HttpServletRequest req)
      throws FileUploadException, IOException {
    
    fieldNameMap = new HashMap<String, MultiPartFormItem>();
    fileNameMap = new HashMap<String, MultiPartFormItem>();
    
    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
    upload.setFileSizeMax(ParserConsts.FILE_SIZE_MAX);

    FileItemIterator items = upload.getItemIterator(req);
    while (items.hasNext()) {
      FileItemStream item = items.next();
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      BufferedInputStream formStream = new BufferedInputStream(item.openStream());
      
      // TODO: determine ways to possibly improve efficiency
      int nextByte = formStream.read();
      while (nextByte != -1) {
        byteStream.write(nextByte);
        nextByte = formStream.read();
      }
      
      MultiPartFormItem data = new MultiPartFormItem(item.getFieldName(), item.getName(), item.getContentType(), byteStream);
      
      String fieldName = item.getFieldName();
      if(fieldName != null) {
        fieldNameMap.put(fieldName, data);
      }
      
      String fileName = item.getName();
      if(fileName != null) {
        // TODO: possible bug in ODK collect is truncating file extension
        // may need to remove this code after ODK collect is fixed
        int indexOfExtension = fileName.lastIndexOf(".");
        if(indexOfExtension > 0) {
          fileNameMap.put(fileName.substring(0, indexOfExtension), data);
        }
        fileNameMap.put(fileName, data);
      }
      formStream.close();
    }
  }
  
  public MultiPartFormItem getFormDataByFieldName(String fieldName) {
    return fieldNameMap.get(fieldName);
  }
  
  public MultiPartFormItem getFormDataByFileName(String fileName) {
    return fileNameMap.get(fileName);
  }
  
}


