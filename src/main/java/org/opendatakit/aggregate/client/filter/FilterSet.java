/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class FilterSet implements Serializable {

	private static final long serialVersionUID = -7646690488192856868L;
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
		groups.remove(group);
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
