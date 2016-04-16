/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.form.KmlOptionSetting;
import org.opendatakit.aggregate.constants.common.UIConsts;

public final class KmlSettingListBox extends AggregateListBox {

  public KmlSettingListBox(String tooltipText, String balloonText) {
    super(tooltipText, false, balloonText);
  }

  public void updateValues(ArrayList<KmlOptionSetting> options, boolean addNoneOption) {
    clear();
    for (KmlOptionSetting kSO : options) {
      addItem(kSO.getDisplayName(), kSO.getElementKey());
    }
    
    if(addNoneOption) {
      addItem(UIConsts.KML_NONE_OPTION, UIConsts.KML_NONE_ENCODE_KEY);
    }
  }

  public String getElementKey() {
    int index = getSelectedIndex();
    int size = getItemCount();

    String geoPointValue = null;
    if (size > index && size > 0) {
      geoPointValue = getValue(index);
    }
    return geoPointValue;
  }
}
