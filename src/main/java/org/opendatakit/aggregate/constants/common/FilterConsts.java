package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

public enum FilterConsts implements Serializable {
	FETCH("Fetch Form with Filter", "This allows you to view the data for a form, " +
			"or the part of the data you wish to view using a filter.<br>" +
			"1.  Use the left drop down list to select a form.<br>" +
			"2.  Use the right drop down list to select a filter. (optional)<br>" +
	"3.  Press the button."),
	ADD("Add Filter", "This adds a filter to the data.  Unless you save it, it will be temporary.<br>" +
			"1.  Display/Hide - Will you be selecting data to show or hide?<br>" +
			"2.  Rows/Columns - Choose whether you will be working with the rows or columns of the table.<br>" +
			"3a.  If you selected Rows:<br>" +
			"	a.  Pick the column that you want to evaluate for all rows.<br>" +
			"	b.  Pick the operation you would like to use.<br>" +
			"	c.  Pick a value to use for the evaluation.<br>" +
			"3b.  If you selected Columns:<br>" +
			"	a.  Pick the columns you want to either display or hide.<br>" +
	"4.  Click \"Apply Filter\"."),
	SAVE("Save", "This will save a filter for future use.  You must have at least one filter in order to save.<br>" +
			"1.  Please fill in the desired name.<br>" +
	"2.  Press \"OK\"."),
	VISUALIZE("Visualize", "This feature allows you to view some of your data in a pie chard, bar graph, or a map.<br>" +
			"1.  Choose whether you want to view a Pie Chart, Bar Graph, or a Map.<br>" +
			"2a.  If you choose Pie Chart:<br>" +
			"	a.  Select the column that you want to work with on the x axis.<br>" +
			"	b.  Select the column that you want to work with on the y axis, or choose \"Number of Occurrences\" to simply to get a " +
			"count of column entries in your x axis.<br>" +
			"	c.  Press \"Pie It\".<br>" +
			"2b.  If you choose Bar Graph:<br>" +
			"	a.  Select the column that you want to work with on the x axis.<br>" +
			"	b.  Select the column that you want to work with on the y axis, or choose \"Number of Occurrences\" to simply to get a " +
			"count of column entries in your x axis.<br>" +
			"	c.  Press \"Bar It\".<br>" +
			"2c.  If you choose Map:<br>" +
			"	a.  Select the column that you want to map.<br>" +
			"	b.  Select the type of geographical data you are using.<br>" +
	"	c.  Press \"Map It\"."),
	EXPORT("Export", "This allows you to view your data in either Microsoft Excel, or in a Google Map.<br>" +
			"1.  Choose whether you want to export to a .csv file, or a .kml file.<br>" +
			"2a.  If you choose CSV, just press \"Export\".<br>" +
			"2b.  If you choose KML:<br>" +
			"	a.  Select the type of geographical data you are using.<br>" +
			"	b.  Select the column that you want to map.<br>" +
			"	c.  Choose the type of picture for your map.  This will be displayed in the balloon on the map.<br>" +
	"	d.  Press \"Export\"."),
	PUBLISH("Publish", "This allows you to view your data in a Google Fusion Table or a Google Spreadsheet.<br>" +
			"1.  Choose whether you want your data in a Google Fusion Table or a Google Spreadsheet.<br>" +
			"2a.  If you chose Google Fusion Table:<br>" +
			"	a.  Choose whether you would like to upload only, stream only, or both.<br>" +
			"		1.  Upload only - This will take the current table and send it to the service.  No new data will be sent.<br>" +
			"		2.  Stream only - This will only send new data after the service is created.  No old data will be sent." +
			"		3.  Both will send both old and new data.<br>" +
			"	b.  Press \"Publish\".<br>" +
			"	c.  Press \"Grant Access\" so that ODK Aggregate is allowed to make the file.<br>" +
			"	d.  You can view your published document in Google Fusion Tables.<br>" +
			"2b.  If you chose Google Spreadsheet:<br>" +
			"	a.  Enter the desired name of the spreadsheet.<br>" +
			"	b.  Choose whether you would like to upload only, stream only, or both.<br>" +
			"		1.  Upload only - This will take the current table and send it to the service.  No new data will be sent.<br>" +
			"		2.  Stream only - This will only send new data after the service is created.  No old data will be sent.<br>" +
			"		3.  Both will send both old and new data.<br>" +
			"	c.  Press \"Publish\".<br>" +
			"	d.  Press \"Grant Access\" so that ODK Aggregate is allowed to make the file.<br>" +
	"	e.  You can view your published document in Google Docs.");

	private String title;
	private String content;

	private FilterConsts(String titleString, String contentString) {
		title = titleString;
		content = contentString;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}
}