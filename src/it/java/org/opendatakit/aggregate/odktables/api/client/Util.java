package org.opendatakit.aggregate.odktables.api.client;

import static org.junit.Assert.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class Util {

  public static String buildUri(String base, String... path) {
    String pathElts = StringUtils.join(path, "/");
    URI uri = URI.create(base + "/" + pathElts).normalize();
    return uri.toASCIIString();
  }

  public static void checkUniformInterfaceException(UniformInterfaceException e,
      Status expectedStatus) {
    int statusCode = e.getResponse().getStatus();
    Status status = Status.fromStatusCode(statusCode);
    if (status != expectedStatus)
      throw e;
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
}
