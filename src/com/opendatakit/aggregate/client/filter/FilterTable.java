package com.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.List;

public class FilterTable implements Serializable {

	private static final long serialVersionUID = -6646690488192856868L;
	private List<FilterGroup> groups;
	
	public FilterTable() {
		
	}
	
	public FilterTable(List<FilterGroup> allFilterGroups) {
		this.groups = allFilterGroups;
	}
	
	public List<FilterGroup> getGroups() {
		return groups;
	}
	
	/**
	 * This should add a new filter group to the database
	 * @param group the new group
	 */
	public void createFilterGroup(FilterGroup group) {
		
	}
	
	/**
	 * This should remove the filter group altogether
	 * @param group the group to be removed
	 */
	public void removeFilterGroup(FilterGroup group) {
		
	}
	
}
