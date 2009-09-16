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

package org.odk.aggregate.constants;

/**
 * Enum that contains values used in ODK aggregate 
 * to aid with external repository management 
 *  
 * @author wbrunette@gmail.com
 *
 */
public enum ExternalServiceOption {
  NONE("SHOULD NOT APPEAR"),
  UPLOAD_ONLY("Upload Existing Submission Data ONLY"),
  STREAM_ONLY("Stream New Submission Data ONLY"),
  UPLOAD_N_STREAM("BOTH Upload Existing & Stream New Submission Data");
  
  private String descriptionOfOption;
  
  private ExternalServiceOption(String desc) {
    descriptionOfOption = desc;
  }
  
  public String getDescriptionOfOption() {
    return descriptionOfOption;
  }
}
