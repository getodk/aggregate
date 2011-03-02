package org.opendatakit.aggregate.client.filter;

import org.opendatakit.aggregate.constants.common.ColumnVisibility;
import org.opendatakit.aggregate.constants.common.FilterOperation;

public class Filter {
  
	private ColumnVisibility kr;
	private String col;
	private FilterOperation operation;
	private String input;
	
	public Filter() {
		
	}
	
	public Filter (ColumnVisibility keepRemove, String colName, 
	    FilterOperation compare, String inputParam) {
		this.kr = keepRemove;
		this.col = colName;
		this.operation = compare;
		this.input = inputParam;
	}
	
	public ColumnVisibility getKR() {
		return kr;
	}
	
	public String getCol() {
		return col;
	}
	
	public FilterOperation getOperation() {
		return operation;
	}
	
	public String getInput() {
		return input;
	}
	
}
