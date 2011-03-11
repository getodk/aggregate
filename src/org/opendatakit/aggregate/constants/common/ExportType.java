package org.opendatakit.aggregate.constants.common;

public enum ExportType {
 	CSV,
 	KML;
 	
 	public String toString() {
 		if ( this == CSV ) {
 			return "Csv file";
 		} else {
 			return "Kml file";
 		}
 	}
 }