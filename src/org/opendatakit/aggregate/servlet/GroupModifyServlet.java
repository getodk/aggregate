/*
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.SecurityBeanDefs;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.spring.GrantedAuthorityHierarchyTable;
import org.opendatakit.common.security.spring.GrantedAuthorityNames;
import org.opendatakit.common.security.spring.RoleHierarchyImpl;

/**
 * Displays the inheritance of a group and the roles directly granted
 * to a group.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class GroupModifyServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -613768029221696145L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "access/group-modify";
	
	public static final String TITLE_INFO = "Modify Permissions for: ";
	
	public static final String GROUPNAME = "_groupname";
	/**
	 * Handler for HTTP Get request that adds a new groupname to the user groups.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

		// get parameter
		String groupname = getParameter(req, GROUPNAME);
		if (groupname == null || groupname.length() == 0) {
			errorMissingParam(resp);
			return;
		}
		
		// ROLE_ values are reserved, as are RUN_AS_ values.
		if ( !GrantedAuthorityNames.permissionsCanBeAssigned(groupname) ) {
			errorBadParam(resp);
			return;
		}
		
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		GrantedAuthorityHierarchyTable relation;
		List<? extends CommonFieldsBase> groupsList;
		List<?> uniqueGroupsList;
		try {
			relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
			Query query = ds.createQuery(relation, user);
			query.addFilter(relation.dominatingGrantedAuthority, FilterOperation.EQUAL, groupname);
			groupsList = query.executeQuery(0);
			
			query = ds.createQuery(relation, user);
			uniqueGroupsList = query.executeDistinctValueForDataField(relation.dominatingGrantedAuthority);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		}

		// construct the set of groups that this group directly inherits from
		TreeSet<String> groups = new TreeSet<String>();
		for ( CommonFieldsBase b : groupsList ) {
			GrantedAuthorityHierarchyTable t = (GrantedAuthorityHierarchyTable) b;
			groups.add(t.getSubordinateGrantedAuthority().getAuthority());
		}
		
		// construct the sets of groups and roles that can be selected
		TreeSet<String> uniqueGroups = new TreeSet<String>();
		TreeSet<String> roles = new TreeSet<String>();

		// pull groups from the defined groups list
		for ( Object o : uniqueGroupsList ) {
			String groupName = (String) o;
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(groupName) ) continue;
			uniqueGroups.add(groupName);
		}
		
		// add the logged-in user's mailto domain in case it is different...
		String mailtoAuthority = GrantedAuthorityNames.getMailtoGrantedAuthorityName(user.getUriUser());
		if ( mailtoAuthority != null ) {
			uniqueGroups.add(mailtoAuthority);
		}
		
		// add the well-known granted authority names.
		for ( GrantedAuthorityNames name : GrantedAuthorityNames.values()) {
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(name.toString()) ) {
				roles.add(name.toString());
			} else {
				uniqueGroups.add(name.toString());
			}
		}
		
		// make sure the group itself is not in the list of selectable groups...
		uniqueGroups.remove(groupname);
		
		beginBasicHtmlResponse(TITLE_INFO + groupname, resp, true, cc); // header info

		PrintWriter out = resp.getWriter();
		
		out.write("<h2>Current Privileges</h2>");
		
		out.print(HtmlConsts.TABLE_OPEN);

		String[] headers = new String[] { 
							"Inherits some Access Rights<br/>from these User Groups", 
							"Directly Granted Access Rights", 
							"Full Set of Granted Access Rights" };

		for (String header : headers) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
	    }
		
		out.print(HtmlConsts.TABLE_ROW_OPEN);
		out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
				organizeGroups(groups, false, cc)));
		out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
				organizeGroups(groups, true, cc)));
		out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
				fetchFullSet(groupname, cc)));

		out.print(HtmlConsts.TABLE_ROW_CLOSE);

		out.print(HtmlConsts.TABLE_CLOSE);

		out.write("<h2>Update Privileges</h2>");

		out.write(HtmlUtil.createFormBeginTag(cc
				.getWebApplicationURL(GroupModifyServlet.ADDR), HtmlConsts.MULTIPART_FORM_DATA,
				HtmlConsts.POST));
		out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_HIDDEN, GroupModifyServlet.GROUPNAME, groupname));

		out.write("<h3>Select the user groups from which to inherit access rights</h3>");
		
		String[] ugHeaders = new String[] { 
				"User Group", 
				"Full Set of Granted Access Rights" };

		out.print(HtmlConsts.TABLE_OPEN);
		for (String header : ugHeaders) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
		}

		for ( String inheritablegroup : uniqueGroups ) {
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        								inheritablegroup, inheritablegroup,
	        								groups.contains(inheritablegroup)) +
	        								groupNameLink(inheritablegroup,cc)));
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
					fetchFullSet(inheritablegroup, cc)));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);

		out.write("<h3>Select the access rights directly given to this group</h3>");
		
		String[] rHeaders = new String[] { 
				"Access Right", 
				"Description" };

		out.print(HtmlConsts.TABLE_OPEN);
		for (String header : rHeaders) {
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_HEADER, header));
		}

		for ( String rolename : roles ) {
			GrantedAuthorityNames n = GrantedAuthorityNames.valueOf(rolename);
			out.print(HtmlConsts.TABLE_ROW_OPEN);
	        out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
	        		HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_CHECKBOX, 
	        				rolename, rolename, groups.contains(rolename)) + rolename));
			out.print(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
					n.getDescription()));
			out.print(HtmlConsts.TABLE_ROW_CLOSE);
		}
		out.print(HtmlConsts.TABLE_CLOSE);
		
		out.write(HtmlConsts.LINE_BREAK);
		out.write(HtmlUtil.createInput("submit", null, "Update"));
		out.write("</form>");
		finishBasicHtmlResponse(resp);
	}
	
	private String groupNameLink( String groupname, CallingContext cc ) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(GroupModifyServlet.GROUPNAME, groupname);
    
        return HtmlUtil.createHrefWithProperties(
        		cc.getWebApplicationURL(GroupModifyServlet.ADDR), properties,
        		groupname );
	}

	private String fetchFullSet(String key, CallingContext cc) {
		TreeSet<String> orderedSet = fetchGrantedAuthoritySet(key, cc);

		return buildList(orderedSet, cc);
	}

	private String organizeGroups(Collection<String> directGrants, boolean rolesOnly, CallingContext cc) {
		TreeSet<String> orderedSet = new TreeSet<String>();
		for ( String g : directGrants ) {
			boolean role = !GrantedAuthorityNames.permissionsCanBeAssigned(g);
			if ( rolesOnly ? role : !role ) {
				orderedSet.add(g);
			}
		}
		return buildList(orderedSet, cc);
	}

	private String buildList(TreeSet<String> orderedSet, CallingContext cc) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for ( String g : orderedSet ) {
			if ( !first ) {
				b.append("<br/>");
			}
			first = false;
			if ( !GrantedAuthorityNames.permissionsCanBeAssigned(g) ) {
				b.append(g);
			} else {
				b.append(groupNameLink(g,cc));
			}
		}
		return b.toString();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();

		try {
			GrantedAuthorityHierarchyTable relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
			
			MultiPartFormData mfd = new MultiPartFormData(req);
			String groupname = mfd.getFormDataByFieldName(GROUPNAME).getStream().toString();
			if (groupname == null || groupname.length() == 0) {
				errorMissingParam(resp);
				return;
			}
			TreeSet<String> groups = new TreeSet<String>();
			TreeSet<String> roles = new TreeSet<String>();
			for ( Map.Entry<String,MultiPartFormItem> e : mfd.getFieldNameEntrySet() ) {
				String key = e.getKey();
				if ( GROUPNAME.equals(key) ) continue;
				if ( groupname.equals(key) ) continue;
				if ( !GrantedAuthorityNames.permissionsCanBeAssigned(key) ) {
					roles.add(key);
				} else {
					groups.add(key);
				}
			}

			// get the hierarchy as currently defined for this group 
			List<? extends CommonFieldsBase> groupsList;
			relation = GrantedAuthorityHierarchyTable.assertRelation(ds, user);
			Query query = ds.createQuery(relation, user);
			query.addFilter(relation.dominatingGrantedAuthority, FilterOperation.EQUAL, groupname);
			groupsList = query.executeQuery(0);

			// OK we have the groups and roles to establish for this groupname.
			// AND we have the groupsList of groups and roles already established for groupname.
			List<EntityKey> deleted = new ArrayList<EntityKey>();
			for ( CommonFieldsBase b : groupsList ) {
				GrantedAuthorityHierarchyTable t = (GrantedAuthorityHierarchyTable) b;
				String authority = t.getSubordinateGrantedAuthority().getAuthority();
				if ( groups.contains(authority) ) {
					groups.remove(authority);
				} else if ( roles.contains(authority) ) {
					roles.remove(authority);
				} else {
					deleted.add(new EntityKey(t, t.getUri()));
				}
			}
			// we now have the list of groups and roles to insert, and the list of 
			// existing records to delete...
			List<GrantedAuthorityHierarchyTable> added = new ArrayList<GrantedAuthorityHierarchyTable>();
			for ( String group : groups ) {
				GrantedAuthorityHierarchyTable t = ds.createEntityUsingRelation(relation, user);
				t.setDominatingGrantedAuthority(groupname);
				t.setSubordinateGrantedAuthority(group);
				added.add(t);
			}
			
			for ( String role : roles ) {
				GrantedAuthorityHierarchyTable t = ds.createEntityUsingRelation(relation, user);
				t.setDominatingGrantedAuthority(groupname);
				t.setSubordinateGrantedAuthority(role);
				added.add(t);
			}
			
			// we now have the list of EntityKeys to delete, and the list of records to add -- do it.
			ds.putEntities(added, user);
			ds.deleteEntities(deleted, user);
		} catch (ODKDatastoreException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
					ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
			return;
		} catch (FileUploadException e) {
			e.printStackTrace();
			errorBadParam(resp);
			return;
		} finally {
			// finally, since we mucked with the group hierarchies, refresh the 
			// cache of those hierarchies.
			RoleHierarchyImpl rh = (RoleHierarchyImpl) cc.getBean(SecurityBeanDefs.ROLE_HIERARCHY_MANAGER);
			try {
				rh.refreshReachableGrantedAuthorities();
			} catch (ODKDatastoreException e) {
				e.printStackTrace();
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
				return;
			}
		}

		resp.sendRedirect(cc.getWebApplicationURL(GroupManagementServlet.ADDR));
	}
}
