package org.opendatakit.aggregate.odktables;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {

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
