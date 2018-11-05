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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class UrlHash {
  public static final int MAIN_MENU = 0;
  public static final int SUB_MENU = 1;
  public static final int FORM = 2;
  public static final int FILTER = 3;
  private static UrlHash single;
  private String[] parameters;

  private UrlHash() {
    parameters = new String[4];
    for (int i = 0; i < parameters.length; i++)
      parameters[i] = "";
  }

  public static UrlHash getHash() {
    if (single == null)
      single = new UrlHash();
    return single;
  }

  public static void redirect() {
    String url = GWT.getHostPageBaseURL() + UIConsts.HOST_PAGE_BASE_ADDR + Window.Location.getQueryString();
    redirectTo(url);
  }

  private native static void redirectTo(String url)
    /*-{
      console.error("redirect to " + url);
      $wnd.location.replace(url);
    }-*/;

  /*
   * Gets the UrlHash from the url bar.
   */
  public void get() {
    String hash = getHashFromUrlBar();
    for (int i = 0; i < parameters.length; i++)
      parameters[i] = "";
    if (!hash.startsWith("#")) {
      return;
    }
    hash = hash.substring(1);
    String[] parts = hash.split("/");
    for (int i = 0; i < parts.length && i < parameters.length; i++)
      parameters[i] = parts[i];
  }

  /*
   * Get the value of a given hash parameter. Should use the constants declared
   * with UrlHash.
   */
  public String get(int i) {
    if (i < 0 || i >= parameters.length)
      return "";
    return parameters[i];
  }

  /*
   * Sets the value of a hash parameter. Should use the constants declared with
   * UrlHash.
   */
  public void set(int i, String s) {
    if (i < 0 || i >= parameters.length)
      return;
    parameters[i] = s == null ? "" : s;
  }

  /*
   * Removes the given hash parameter. Should use the constants declared with
   * UrlHash.
   */
  public void remove(int i) {
    if (i < 0 || i >= parameters.length)
      return;
    parameters[i] = "";
  }

  /*
   * Clears all values from hash.
   */
  public void clear() {
    for (int i = 0; i < parameters.length; i++)
      parameters[i] = "";
  }

  /*
   * Puts the UrlHash to the url bar.
   */
  public void put() {
    putHashToUrlBar(toString());
  }

  @Override
  /*
   * Converts this UrlHash to a String.
   */
  public String toString() {
    StringBuffer result = new StringBuffer("#");
    for (int i = 0; i < parameters.length; i++) {
      result.append(parameters[i] + "/");
    }
    return result.toString();
  }

  private native String getHashFromUrlBar() /*-{
    return $wnd.location.hash;
  }-*/;

  private native void putHashToUrlBar(String hash) /*-{
    $wnd.location.hash = hash;
  }-*/;
}
