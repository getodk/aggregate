/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Util {

  public static String buildUri(String base, String... path) {
    String pathElts = StringUtils.join(path, "/");
    URI uri = URI.create(base + "/" + pathElts).normalize();
    return uri.toASCIIString();
  }

  /**
   * V must implement equals.
   */
  public static <V> void assertCollectionSameElements(Collection<V> expected, Collection<V> actual) {
    Collection<V> expectedCopy = new ArrayList<V>(expected);
    Collection<V> actualCopy = new ArrayList<V>(actual);

    for (V item : actualCopy) {
      assertTrue(expectedCopy.remove(item));
    }
    assertTrue(expectedCopy.isEmpty());
  }

  public static <V> List<V> list(V... values) {
    ArrayList<V> list = new ArrayList<V>();
    for (V value : values) {
      list.add(value);
    }
    return list;
  }
}
