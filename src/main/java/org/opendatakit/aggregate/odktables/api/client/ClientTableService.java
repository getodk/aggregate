package org.opendatakit.aggregate.odktables.api.client;

import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.opendatakit.aggregate.odktables.api.ColumnService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.api.entity.TableResource;
import org.opendatakit.aggregate.odktables.entity.Column;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache4.ApacheHttpClient4;

public class ClientTableService implements TableService {

  public static final String API_PATH = "odktables/tables";

  private UriBuilder ub;
  private Client c;

  public ClientTableService(String aggregateUri, String accessToken) {
    UriBuilder ub = UriBuilder.fromUri(aggregateUri);
    ub.path(API_PATH);
    this.ub = ub;

    this.c = ApacheHttpClient4.create();
    // this.c.addFilter(new AccessTokenFilter(accessToken));
  }

  @Override
  public List<TableResource> getTables() {
    WebResource r = c.resource(ub.build());
    GenericType<List<TableResource>> list = new GenericType<List<TableResource>>() {};
    List<TableResource> tables = r.accept(MediaType.TEXT_XML, MediaType.TEXT_PLAIN).get(list);
    return tables;
  }

  @Override
  public TableResource getTable(String tableId) {
    WebResource r = c.resource(ub.clone().path(tableId).build());
    TableResource table = r.accept(MediaType.TEXT_XML, MediaType.TEXT_PLAIN).get(
        TableResource.class);
    return table;
  }

  @Override
  public TableResource createTable(String tableId, List<Column> columns) {
    WebResource r = c.resource(ub.clone().path(tableId).build());
    GenericEntity<List<Column>> entity = new GenericEntity<List<Column>>(columns) {};
    TableResource table = r.accept(MediaType.TEXT_XML, MediaType.TEXT_PLAIN)
        .type(MediaType.TEXT_XML).put(TableResource.class, entity);
    return table;
  }

  @Override
  public void deleteTable(String tableId) {
    WebResource r = c.resource(ub.clone().path(tableId).build());
    r.delete();
  }

  @Override
  public ColumnService getColumns(String tableId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClientDataService getData(String tableId) {
    UriBuilder ub2 = ub.clone().path(tableId);
    return new ClientDataService(ub2, c);
  }

}
