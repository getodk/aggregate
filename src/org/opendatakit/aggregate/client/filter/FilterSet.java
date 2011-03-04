package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilterSet implements Serializable {

	private static final long serialVersionUID = -6646690488192856868L;
	private List<FilterGroup> groups;
	
	public FilterSet() {
		this.groups = new ArrayList<FilterGroup>();
	}
	
	public FilterSet(List<FilterGroup> allFilterGroups) {
		this.groups = allFilterGroups;
	}
	
	public List<FilterGroup> getGroups() {
		return groups;
	}
	
	/**
	 * This should add a new filter group to the database
	 * @param group the new group
	 */
	public void addFilterGroup(FilterGroup group) {
		groups.add(group);
	}
	
	/**
	 * This should remove the filter group altogether
	 * @param group the group to be removed
	 */
	public void removeFilterGroup(FilterGroup group) {
		
	}
	
	/**
	 * This should run the filter group filters on the data
	 * i.e. they load a filter group, press "x" to remove it
	 * then say "oops... undo"
	 * @param group the group the person added in
	 */
	public void maskAddFilterGroup(FilterGroup group) {
		
	}
	
	/**
	 * This should remove the filter group filters from the data
	 * This does not remove the filter group altogether
	 * i.e. they want to "redo" their removal of a filter group
	 * @param group the group the person removed
	 */
	public void maskRemoveFilterGroup(FilterGroup group) {
		
	}
	
}
