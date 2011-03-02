package com.opendatakit.aggregate.client.filter;

public class Filter {

	public enum KeepRemove {
		KEEP, REMOVE
	}
	
	public enum Comparison {
		LESS_THAN, LESS_THAN_OR_EQUAL_TO,
		EQUAL_TO, GREATHER_THAN_OR_EQUAL_TO,
		GREATHER_THAN, NOT_EQUAL_TO
	}
	
	private KeepRemove kr;
	private String col;
	private Comparison comp;
	private String input;
	
	public Filter() {
		
	}
	
	public Filter (KeepRemove keepRemove, String colName, 
			Comparison compare, String inputParam) {
		this.kr = keepRemove;
		this.col = colName;
		this.comp = compare;
		this.input = inputParam;
	}
	
	public KeepRemove getKR() {
		return kr;
	}
	
	public String getCol() {
		return col;
	}
	
	public Comparison getComp() {
		return comp;
	}
	
	public String getInput() {
		return input;
	}
	
}
