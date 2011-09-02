package org.opendatakit.common.persistence;

import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.client.UIQueryResumePoint;

/**
 * Tracks the information needed to resume a query.
 * In order for this to work, the query is expected 
 * to have a final ascending sort of the URI key
 * applied to it.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class QueryResumePoint {
	
	private final String attributeName;
	private final FilterOperation op;
	private final String value;
	private final String uriLastReturnedValue;
	
	public QueryResumePoint(String attributeName, FilterOperation op, String value, String uriLast) {
		this.attributeName = attributeName;
		this.op = op;
		this.value = value;
		this.uriLastReturnedValue = uriLast;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public FilterOperation getOp() {
		return op;
	}

	public String getValue() {
		return value;
	}

	public String getUriLastReturnedValue() {
		return uriLastReturnedValue;
	}
	
	public UIQueryResumePoint transform() {
		UIQueryResumePoint qrp = new UIQueryResumePoint();
		qrp.setAttributeName(attributeName);
		qrp.setFilterOperation(op.name());
		qrp.setValue(value);
		qrp.setUriLastReturnedValue(uriLastReturnedValue);
		return qrp;
	}
	
	public static final QueryResumePoint transform(UIQueryResumePoint qrp) {
		FilterOperation op = FilterOperation.valueOf(qrp.getFilterOperation());
		return new QueryResumePoint(qrp.getAttributeName(), op, qrp.getValue(), qrp.getUriLastReturnedValue());
	}
}
