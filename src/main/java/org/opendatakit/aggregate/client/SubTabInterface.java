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

package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.constants.common.HelpSliderConsts;

public interface SubTabInterface {
  /**
   * The canLeave predicate returns true if the focus of the app can move
   * off of the current SubTab.  This is called when navigating between tabs,
   * before setCurrentSubTab is called, so that a tab with focus may prompt
   * the user to see if they really want to move off of that SubTab.
   *
   * @return
   */
  public boolean canLeave();

  public void update();

  public HelpSliderConsts[] getHelpSliderContent();
}
