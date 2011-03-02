package com.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.List;

public class FilterGroup implements Serializable {
	
	private static final long serialVersionUID = 3317433416889397657L;
	private String name;
	private List<Filter> filters;
	
	public FilterGroup() {
		
	}
	
	public FilterGroup(String groupName, List<Filter> groupFilters) {
		this.name = groupName;
		this.filters = groupFilters;
	}

	public String getName() {
		return name;
	}

	public List<Filter> getFilters() {
		return filters;
	}
	
	/**
	 * This should add the filter to the group
	 * @param filter the filter to be added
	 */
	public void addFilter(Filter filter) {
		
	}
	
	/**
	 * This should remove the filter from the group
	 * @param filter the filter to be removed
	 */
	public void removeFilter(Filter filter) {
		
	}
	
}
