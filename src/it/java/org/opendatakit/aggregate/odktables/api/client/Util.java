package org.opendatakit.aggregate.odktables.api.client;

import java.net.URI;

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

}
