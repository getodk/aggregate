package org.opendatakit.aggregate.odktables.api;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.odktables.rest.entity.Scope;
import org.opendatakit.aggregate.odktables.rest.entity.TableAcl;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole;
import org.opendatakit.aggregate.odktables.rest.entity.TableAclResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TableAclServiceTest extends AbstractServiceTest {

  @Before
  public void setUp() throws Exception {
    super.createTable();
    TableResource resource = rt.getForObject(baseUri.resolve(T.tableId), TableResource.class);
    baseUri = URI.create(resource.getAclUri());
  }

  @Test
  public void testGetAclsOnlyOwner() {
    List<?> acls = rt.getForObject(baseUri, List.class);
    assertEquals(1, acls.size());
  }

  @Test
  public void testSetUserAcl() {
    Scope.Type type = Scope.Type.USER;
    String userId = "someone@somewhere.com";
    String uri = Util.buildUri(baseUri.toASCIIString(), type.name().toLowerCase(), userId);
    TableAcl expected = new TableAcl(TableRole.READER);

    HttpEntity<TableAcl> entity = super.entity(expected);
    ResponseEntity<TableAclResource> resp = rt.exchange(uri, HttpMethod.PUT, entity,
        TableAclResource.class);
    TableAclResource actual = resp.getBody();
    assertEquals(expected.getRole(), actual.getRole());
    assertEquals(new Scope(type, userId), actual.getScope());
  }

  @Test
  public void testSetDefaultAcl() {
    Scope.Type type = Scope.Type.DEFAULT;
    String uri = Util.buildUri(baseUri.toASCIIString(), type.name().toLowerCase());
    TableAcl expected = new TableAcl(TableRole.READER);

    HttpEntity<TableAcl> entity = super.entity(expected);
    ResponseEntity<TableAclResource> resp = rt.exchange(uri, HttpMethod.PUT, entity,
        TableAclResource.class);
    TableAclResource actual = resp.getBody();
    assertEquals(expected.getRole(), actual.getRole());
    assertEquals(new Scope(Scope.Type.DEFAULT, null), actual.getScope());
  }

  @Test
  public void testDeleteDefaultAcl() {
    Scope.Type type = Scope.Type.DEFAULT;
    String uri = Util.buildUri(baseUri.toASCIIString(), type.name().toLowerCase());
    rt.delete(uri);
  }

  @Test
  public void testDeleteNonExistentAcl() {
    Scope.Type type = Scope.Type.USER;
    String userId = "someone@somewhere.com";
    String uri = Util.buildUri(baseUri.toASCIIString(), type.name().toLowerCase(), userId);
    rt.delete(uri);
  }

}
