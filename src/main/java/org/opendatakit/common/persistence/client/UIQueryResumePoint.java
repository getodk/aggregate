package org.opendatakit.common.persistence.client;

import java.io.Serializable;

public class UIQueryResumePoint implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7500161085826059616L;
	private String attributeName;
	private String filterOperation;
	private String value;
	private String uriLastReturnedValue;
	
	public UIQueryResumePoint() {}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getFilterOperation() {
		return filterOperation;
	}

	public void setFilterOperation(String filterOperation) {
		this.filterOperation = filterOperation;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUriLastReturnedValue() {
		return uriLastReturnedValue;
	}

	public void setUriLastReturnedValue(String uriLastReturnedValue) {
		this.uriLastReturnedValue = uriLastReturnedValue;
	};
	
}
