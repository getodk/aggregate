package org.opendatakit.aggregate.server;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.PropertiesResourceClient;
import org.opendatakit.aggregate.client.odktables.ServerPropertiesService;
import org.opendatakit.aggregate.client.odktables.TablePropertiesClient;
import org.opendatakit.aggregate.odktables.AuthFilter;
import org.opendatakit.aggregate.odktables.PropertiesManager;
import org.opendatakit.aggregate.odktables.api.PropertiesService;
import org.opendatakit.aggregate.odktables.api.TableService;
import org.opendatakit.aggregate.odktables.entity.TableProperties;
import org.opendatakit.aggregate.odktables.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.entity.api.PropertiesResource;
import org.opendatakit.aggregate.odktables.exception.EtagMismatchException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ServerPropertiesServiceImpl extends RemoteServiceServlet implements
		ServerPropertiesService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -109897492777781438L;

	@Override
	public PropertiesResourceClient getProperties(String tableId, UriInfo info)
			throws AccessDeniedException, RequestFailureException,
			DatastoreFailureException, PermissionDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try {
		    PropertiesManager pm = new PropertiesManager(tableId, cc);
			AuthFilter af = new AuthFilter(tableId, cc);
		    af.checkPermission(TablePermission.READ_PROPERTIES);
		    TableProperties properties = pm.getProperties();
			return getResource(properties, pm, info);
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }
	}

	@Override
	public PropertiesResourceClient setProperties(TablePropertiesClient properties,
			String tableId, UriInfo info) throws AccessDeniedException,
			RequestFailureException, DatastoreFailureException,
			ODKTaskLockException, EtagMismatchException, PermissionDeniedException {
	    HttpServletRequest req = this.getThreadLocalRequest();
	    CallingContext cc = ContextFactory.getCallingContext(this, req);
	    try {
			AuthFilter af = new AuthFilter(tableId, cc);
		    PropertiesManager pm = new PropertiesManager(tableId, cc);
		    af.checkPermission(TablePermission.WRITE_PROPERTIES);
		    // this seems fishy. Originally was passing in the parameter of type
		    // TablePropertiesClient, but it isn't clear why I need to, when 
		    // nothing is being explicitly set. Fixed by changing the type of
		    // the variable, but should be wary of this.
		    // first make the type TableProperties object
		    TableProperties tableProperties = new TableProperties(properties.getPropertiesEtag(),
		    		properties.getTableName(), properties.getMetadata());
		    tableProperties = pm.setProperties(tableProperties);
		    return getResource(tableProperties, pm, info);
	    } catch (ODKDatastoreException e) {
	    	e.printStackTrace();
	    	throw new DatastoreFailureException(e);
	    }
	}
	
	private PropertiesResourceClient getResource(TableProperties properties, PropertiesManager pm, UriInfo info) {
		PropertiesResource propertiesResource = new PropertiesResource(properties);
		
		String tableId = pm.getTableId();
		UriBuilder ub = info.getBaseUriBuilder();
		ub.path(TableService.class);
		URI self = ub.clone().path(TableService.class, "getProperties")
			        .path(PropertiesService.class, "getProperties").build(tableId);
		URI table = ub.clone().path(TableService.class, "getTable").build(tableId);
		
		propertiesResource.setSelfUri(self.toASCIIString());
		propertiesResource.setTableUri(table.toASCIIString());
		
		return propertiesResource.transform();
	}	

}
