/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.form;

/**
 * Helper class holding the details of a 
 * specific version of a form.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public final class XFormParameters implements Comparable<Object> {

	public final String formId;
	public final Long modelVersion;
	public final Long uiVersion;

	public XFormParameters(String formId, Long modelVersion, Long uiVersion) {
		if ( formId == null ) {
			throw new IllegalArgumentException("formId cannot be null");
		}
		this.formId = formId;
		this.modelVersion = modelVersion;
		this.uiVersion = uiVersion;
	}
	
	@Override
	public String toString() {
		return "XFormParameters[formId=" + formId + " and version=" +
					(modelVersion == null ? "null" : Long.toString(modelVersion)) +
					" and uiVersion=" +
					(uiVersion == null ? "null" : Long.toString(uiVersion)) + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj == null || !(obj instanceof XFormParameters) ) return false;
		XFormParameters p = (XFormParameters) obj;
		return formId.equals(p.formId) &&
			((modelVersion == null && p.modelVersion == null) ||
			 (modelVersion != null && modelVersion.equals(p.modelVersion))) &&
			((uiVersion == null && p.uiVersion == null) ||
			 (uiVersion != null && uiVersion.equals(p.uiVersion)));
	}

	@Override
	public int hashCode() {
		return Long.valueOf(formId.hashCode() + 
				((modelVersion == null) ? 20480L : 37*modelVersion) +
				((uiVersion == null) ? 40965L : 91*uiVersion) ).hashCode();
	}

	@Override
	public int compareTo(Object obj) {
		if ( obj == null || !(obj instanceof XFormParameters) ) return -1;
		XFormParameters p = (XFormParameters) obj;
		int cmp = formId.compareTo(p.formId);
		if ( cmp != 0 ) return cmp;
		if ( (modelVersion == null && p.modelVersion == null) ||
			 (modelVersion != null && modelVersion.equals(p.modelVersion)) ) {
			if ((uiVersion == null && p.uiVersion == null) ||
				(uiVersion != null && uiVersion.equals(p.uiVersion))) {
				return 0;
			} else if ( uiVersion == null ) {
				return 1;
			} else if ( p.uiVersion == null ) {
				return -1;
			} else {
				return uiVersion.compareTo(p.uiVersion);
			}
		} else if ( modelVersion == null ) {
			return 1;
		} else if ( p.modelVersion == null ) {
			return -1;
		} else {
			return modelVersion.compareTo(p.modelVersion);
		}
	}
}