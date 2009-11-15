/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.aggregate.process;

import java.util.ArrayList;
import java.util.List;

import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.parser.MultiPartFormItem;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ProcessParams {

  private String odkId;

  private String buttonText;
  
  private List<Key> keys;
  
  public ProcessParams(MultiPartFormData reqData) {
    MultiPartFormItem odkIdData = reqData
        .getFormDataByFieldName(ServletConsts.ODK_ID);
    MultiPartFormItem numRecordData = reqData
        .getFormDataByFieldName(ServletConsts.PROCESS_NUM_RECORDS);
    MultiPartFormItem processTypeData = reqData
        .getFormDataByFieldName(ServletConsts.PROCESS_TYPE);
    
    if(odkIdData != null) {
      odkId = odkIdData.getStream().toString();
    } 
    
    if(processTypeData != null) {
      buttonText = processTypeData.getStream().toString();      
    }
    
    int numRecord = 0;
    if(numRecordData != null) {
      numRecord = Integer.parseInt(numRecordData.getStream().toString());
      keys = new ArrayList<Key>();
    }
    
    for (int i = 0; i < numRecord; i++) {
      MultiPartFormItem recordKeyItem = reqData
          .getFormDataByFieldName(ServletConsts.PROCESS_RECORD_PREFIX + i);
      if (recordKeyItem != null) {
        String keyString = recordKeyItem.getStream().toString();
        if (keyString != null) {
          keys.add(KeyFactory.stringToKey(keyString));
        }
      }
    }
  }

  public String getOdkId() {
    return odkId;
  }

  public String getButtonText() {
    return buttonText;
  }

  public List<Key> getKeys() {
    return keys;
  }

}
