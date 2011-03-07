package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.Visibility;

public class Filter implements Serializable {

	private static final long serialVersionUID = -5453093733004634508L;
	private String uri; // unique identifier
	private Visibility kr;
	private RowOrCol rc;
	private String title;
	private FilterOperation operation;
	private String input;
	private Long ordinal; // order to display in filter group
	
	public Filter() {
		
	}
	  
	public Filter(Visibility keepRemove, RowOrCol rowcol, String title, 
			FilterOperation compare, String inputParam, long ordinal) {
	    this.uri = UIConsts.URI_DEFAULT;
	    this.kr = keepRemove;
	    this.setRc(rowcol);
	    this.title = title;
	    this.operation = compare;
	    this.input = inputParam;
	    this.ordinal = ordinal;
	}

	/**
     * This constructor should only be used by the server
     * 
	 * @param uri
	 */
	public Filter(String uri) {
	    this.uri = uri;
	}

	public String getUri() {
	    return uri;
	}
	
	public Visibility getVisibility() {
	    return kr;
	}

	public void setVisibility(Visibility kr) {
	    this.kr = kr;
	}

	public RowOrCol getRc() {
		return rc;
	}
	
	public void setRc(RowOrCol rc) {
		this.rc = rc;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public FilterOperation getOperation() {
		return operation;
	}
	
	public void setOperation(FilterOperation operation) {
	    this.operation = operation;
	}
	
	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Long getOrdinal() {
	    return ordinal;
	}

	public void setOrdinal(Long ordinal) {
	    this.ordinal = ordinal;
	}

}