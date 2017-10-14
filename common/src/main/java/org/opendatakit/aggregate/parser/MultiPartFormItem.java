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

import java.io.ByteArrayOutputStream;

/**
 * Object representation of multi part form data
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class MultiPartFormItem {

  /**
   * form data field name
   */
  private final String name;

  /**
   * form data file name
   */
  private String filename;
  
  /**
   * form data content type
   */
  private final String contentType;

  /**
   * form data's stream
   */
  private final ByteArrayOutputStream stream;
  
  /**
   * Constructor of a multi part of data 
   * 
   * @param fieldName
   *    form data field name   
   * @param fileName
   *    form data file name
   * @param contentType
   *    form data content type
   * @param byteStream
   *    form data's stream
   */
  public MultiPartFormItem(String fieldName, String fileName, String contentType, ByteArrayOutputStream byteStream) {
    this.name = fieldName;
    this.filename = fileName;
    this.contentType = contentType;
    this.stream = byteStream;
  }
  
  /**
   * Get form data field name
   * 
   * @return
   *    field name
   */
  public String getName() {
    return name;
  }

  /**
   * Get form data file name
   * 
   * @return
   *    file name
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Only used during construction of the MultiPartFormData
   * to strip the common leading directory elements off the 
   * file path.  Needed for uploading of data from a PC directory.
   * 
   * @param fileName
   */
  public void setFilename(String fileName) {
	this.filename = fileName;
  }  

  /**
   * Get form data content type
   * 
   * @return
   *    content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Get form data's stream
   * 
   * @return
   *    stream
   */
  public ByteArrayOutputStream getStream() {
    return stream;
  }
}
