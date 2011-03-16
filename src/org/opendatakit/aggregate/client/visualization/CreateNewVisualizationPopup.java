package org.opendatakit.aggregate.client.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUI;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CreateNewVisualizationPopup extends PopupPanel {
	private static final String UI_PIE_CHART = "Pie Chart";
	private static final String UI_BAR_GRAPH = "Bar Graph";
	private static final String UI_SCATTER_PLOT = "Scatter Plot";
	
	private static final String GOOGLE_PIE_CHART = "p3";
	private static final String GOOGLE_BAR_GRAPH = "bvg";
	private static final String GOOGLE_SCATTER_PLOT = "s";
	
	private static final HashMap<String, String> UI_TO_GOOGLE = new HashMap<String, String>();
	static {
		UI_TO_GOOGLE.put(UI_PIE_CHART, GOOGLE_PIE_CHART);
		UI_TO_GOOGLE.put(UI_BAR_GRAPH, GOOGLE_BAR_GRAPH);
		UI_TO_GOOGLE.put(UI_SCATTER_PLOT, GOOGLE_SCATTER_PLOT);
	}
	
	private final List<Column> head;
	private final List<SubmissionUI> sub;
	private final ListBox chartType = new ListBox();
	private final ListBox firstData = new ListBox();
	private final ListBox secondData = new ListBox();
	private final Image chart = new Image();
	
	private String getImageUrl() {
		StringBuffer chartUrl = new StringBuffer("https://chart.googleapis.com/chart?cht=");
		chartUrl.append(UI_TO_GOOGLE.get(chartType.getItemText(chartType.getSelectedIndex())));
		chartUrl.append("&chs=600x500");
		
		int firstIndex = 0;
		int secondIndex = 0;
		String firstDataValue = firstData.getItemText(firstData.getSelectedIndex());
		String secondDataValue = secondData.getItemText(secondData.getSelectedIndex());
      chartUrl.append("&chtt=" + secondDataValue);
      chartUrl.append("&chxt=x,y");
		int index = 0;
		for (Column c : head) {
			if (c.getDisplayHeader().equals(firstDataValue))
				firstIndex = index;
			if (c.getDisplayHeader().equals(secondDataValue))
				secondIndex = index;
			index++;
		}
		StringBuffer firstValues = new StringBuffer();
		StringBuffer secondValues = new StringBuffer();
		for (SubmissionUI s : sub) {
			firstValues.append(s.getValues().get(firstIndex));
			firstValues.append("|");
			secondValues.append(s.getValues().get(secondIndex));
			secondValues.append(",");
		}
		if (firstValues.length() > 0)
			firstValues.delete(firstValues.length() - 1, firstValues.length());
		if (secondValues.length() > 0)
			secondValues.delete(secondValues.length() - 1, secondValues.length());
		chartUrl.append("&chd=t:");
		chartUrl.append(secondValues.toString());
		chartUrl.append("&chdl=");
		chartUrl.append(firstValues.toString());
		
		
		return chartUrl.toString();
	}
	
	public CreateNewVisualizationPopup(List<Column> headers,
									   List<SubmissionUI> submissions) {
		super(true);
		this.head = headers;
		this.sub = submissions;
		
		VerticalPanel layoutPanel = new VerticalPanel();
		final FlexTable dropDownsTable = new FlexTable();
		
		for (Column c : head) {
			firstData.addItem(c.getDisplayHeader());
			secondData.addItem(c.getDisplayHeader());
		}
		
		final Button executeButton = new Button("<img src=\"images/pie_chart.png\" /> Pie It");
		executeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				chart.setUrl(getImageUrl());
			}
		});
		
		dropDownsTable.setWidget(0, 1, firstData);
		dropDownsTable.setWidget(0, 2, secondData);
		
		chartType.addItem(UI_PIE_CHART);
		chartType.addItem(UI_BAR_GRAPH);
      /*
		chartType.addItem(UI_SCATTER_PLOT);
		*/
		chartType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String selected = chartType.getValue(chartType.getSelectedIndex());
				if (selected.equals(UI_PIE_CHART)) {
					executeButton.setHTML("<img src=\"images/pie_chart.png\" /> Pie It");
				} else if (selected.equals(UI_BAR_GRAPH)) {
					executeButton.setHTML("<img src=\"images/bar_chart.png\" /> Bar It");
				} else { // selected.equals(UI_SCATTER_PLOT)
					executeButton.setHTML("<img src=\"scatter_plot.png\" /> Plot It");
				}
			}
		});
		dropDownsTable.setWidget(0, 0, chartType);
		dropDownsTable.setWidget(0, 1, firstData);
		dropDownsTable.setWidget(0, 2, secondData);
		dropDownsTable.setWidget(0, 3, executeButton);
		
		layoutPanel.add(dropDownsTable);
		chart.getElement().setId("chart_image");
		chartType.setItemSelected(0, true);
		layoutPanel.add(chart);
		setWidget(layoutPanel);
	}
}
