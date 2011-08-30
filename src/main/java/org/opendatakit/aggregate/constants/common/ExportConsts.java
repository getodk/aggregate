package org.opendatakit.aggregate.constants.common;


public enum ExportConsts implements HelpSliderConsts {
	EXPORT("After running \"Export\" on Submissions -> Filter Submissions, you should have a list of files you have exported.",
			"Understanding the table:<br>" +
			"1.  File Type - CSV or KML file.<br>" +
			"2.  Status - This will state whether the file being made is in progress, or is now available for viewing.<br>" +
			"3.  Time Requested - this shows the time when you finished filling out the \"Export\" form.<br>" +
			"4.  Time Completed - this shows the time when the \"Export\" task is complete and the file is ready.<br>" +
			"5.  Last Retry - this shows the time when the file was last attempted to be made.<br>" +
	"6.  Download File - click the link to see your exported file.");

	private String title;
	private String content;

	private ExportConsts(String titleString, String contentString) {
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
