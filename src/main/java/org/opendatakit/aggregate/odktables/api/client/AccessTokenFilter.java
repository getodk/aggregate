package org.opendatakit.aggregate.odktables.api.client;

import javax.ws.rs.core.UriBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

@RequiredArgsConstructor
public class AccessTokenFilter extends ClientFilter {

  @Getter
  private final String accessToken;

  @Override
  public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
    // Modify the request
    ClientRequest mcr = modifyRequest(cr);

    // Call the next client handler in the filter chain
    ClientResponse resp = getNext().handle(mcr);

    // Modify the response
    return resp;
  }

  public ClientRequest modifyRequest(ClientRequest cr) {
    UriBuilder ub = UriBuilder.fromUri(cr.getURI());
    ub.queryParam("access_token", accessToken);
    cr.setURI(ub.build());
    return cr;
  }

}