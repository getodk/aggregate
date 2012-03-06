package org.opendatakit.aggregate.odktables.api.client;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import lombok.val;

import org.opendatakit.aggregate.odktables.api.DataService;
import org.opendatakit.aggregate.odktables.api.entity.RowResource;
import org.opendatakit.aggregate.odktables.entity.Row;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class ClientDataService implements DataService {

  public static final String API_PATH = "rows";

  private UriBuilder ub;
  private Client c;

  public ClientDataService(UriBuilder ub, Client c) {
    ub.path(API_PATH);
    this.ub = ub;
    this.c = c;
  }

  @Override
  public List<RowResource> getRows() {
    WebResource r = c.resource(ub.build());
    val list = new GenericType<List<RowResource>>() {};
    val rows = r.accept(MediaType.TEXT_XML, MediaType.TEXT_PLAIN).get(list);
    return rows;
  }

  @Override
  public RowResource getRow(String rowId) {
    WebResource r = c.resource(ub.clone().path(rowId).build());
    val row = r.accept(MediaType.TEXT_XML).get(RowResource.class);
    return row;
  }

  @Override
  public RowResource createOrUpdateRow(String rowId, Row row) {
    WebResource r = c.resource(ub.clone().path(rowId).build());
    val resource = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML)
        .put(RowResource.class, row);
    return resource;
  }

  @Override
  public void deleteRow(String rowId) {
    WebResource r = c.resource(ub.clone().path(rowId).build());
    r.delete();
  }

}
