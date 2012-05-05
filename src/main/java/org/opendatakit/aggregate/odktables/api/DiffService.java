package org.opendatakit.aggregate.odktables.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.opendatakit.aggregate.odktables.entity.api.RowResource;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

@Produces(MediaType.TEXT_XML)
public interface DiffService {

  public static final String QUERY_DATA_ETAG = "data_etag";

  @GET
  public List<RowResource> getRowsSince(@QueryParam(QUERY_DATA_ETAG) String dataEtag)
      throws ODKDatastoreException;
}
