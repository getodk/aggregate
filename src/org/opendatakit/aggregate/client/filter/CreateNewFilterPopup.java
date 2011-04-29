package org.opendatakit.aggregate.client.filter;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionService;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class CreateNewFilterPopup extends PopupPanel{
	
	private SubmissionServiceAsync subSvc;
	private FilterGroup group;
	private List<Column> columns;
	
	public CreateNewFilterPopup() {
		subSvc = GWT.create(SubmissionService.class);
	}
	
	public CreateNewFilterPopup(FlexTable data, 
			final FilterGroup group, final AggregateUI aggregateUI) {
		super(false); //do not close popup when user clicks out of it
		this.group = group;
		getSubmissions();
		final FlexTable create = new FlexTable();
		//keep or remove
		final ListBox keepRemove = new ListBox();
		keepRemove.addItem(Visibility.KEEP.toString());
		keepRemove.addItem(Visibility.REMOVE.toString());
		
		//rows or columns
		final ListBox rowCol = new ListBox();
		rowCol.addItem(RowOrCol.ROW.toString());
		rowCol.addItem(RowOrCol.COLUMN.toString());
		
		//where columns
		final Label whereCols = new Label("where column");
		
		//column selection - for row filter
		final ListBox col = new ListBox();
		for(int i = 0; i < data.getCellCount(0); i++) {
			col.addItem(data.getText(0, i));
		}
		
		//columns selection - for column filter
		final ListBox cols = new ListBox(true);
		for(int i = 0; i < data.getCellCount(0); i++) {
			cols.addItem(data.getText(0, i));
		}
		
		//comparison operator
		final ListBox comp = new ListBox();
		comp.addItem(FilterOperation.LESS_THAN.toString());
		comp.addItem(FilterOperation.LESS_THAN_OR_EQUAL.toString());
		comp.addItem(FilterOperation.EQUAL.toString());
		comp.addItem(FilterOperation.GREATER_THAN_OR_EQUAL.toString());
		comp.addItem(FilterOperation.GREATER_THAN.toString());
		//value input
		final TextBox var = new TextBox();
		
		//on exit
		final Button submit = new Button("<img src=\"images/green_check.png\" /> Save Filter");
		submit.addStyleDependentName("positive");
		submit.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Visibility kr;
				FilterOperation op;
				
				String korr = keepRemove.getValue(keepRemove.getSelectedIndex());
				if(korr.compareTo(Visibility.KEEP.toString()) == 0) {
					kr = Visibility.KEEP;
				} else {
					kr = Visibility.REMOVE;
				}
				
				String rowcol = rowCol.getValue(rowCol.getSelectedIndex());
				if(rowcol.compareTo(RowOrCol.ROW.toString()) == 0) {
					String colname = col.getValue(col.getSelectedIndex());
					String colencode = "";
					
					for(Column column: columns) {
						if(colname.compareTo(column.getDisplayHeader()) == 0) {
							colencode = column.getColumnEncoding();
							break;
						}
					}
					
					String compare = comp.getValue(comp.getSelectedIndex());
					if(compare.compareTo(FilterOperation.LESS_THAN.toString()) == 0) {
						op = FilterOperation.LESS_THAN;
					} else if (compare.compareTo(FilterOperation.LESS_THAN_OR_EQUAL.toString()) == 0) {
						op = FilterOperation.LESS_THAN_OR_EQUAL;
					} else if (compare.compareTo(FilterOperation.EQUAL.toString()) == 0) {
						op = FilterOperation.EQUAL;
					} else if (compare.compareTo(FilterOperation.GREATER_THAN_OR_EQUAL.toString()) == 0) {
						op = FilterOperation.GREATER_THAN_OR_EQUAL;
					} else {
						op = FilterOperation.GREATER_THAN;
					}
					
					String variable = var.getValue();
					
					Filter newFilter = new RowFilter(kr, new Column(colname, colencode), op, 
							variable, (long) group.getFilters().size());
					group.addFilter(newFilter);
				} else {
					List<ColumnFilterHeader> columnfilterheaders = new ArrayList<ColumnFilterHeader>();
					for (int i = cols.getSelectedIndex(); i < cols.getItemCount(); i++) {
						String colname = "";
						String colencode = "";
						Long colgpsIndex = null;
						if(cols.isItemSelected(i)) {
							colname = cols.getValue(i);
							for(Column column: columns) {
								if(colname.compareTo(column.getDisplayHeader()) == 0) {
									colencode = column.getColumnEncoding();
									colgpsIndex = column.getGeopointColumnCode();
									//Waylon go here
									break;
								}
							}
							columnfilterheaders.add(new ColumnFilterHeader(colname, colencode, colgpsIndex));
						}
					}
					Filter newFilter = new ColumnFilter(kr, columnfilterheaders,(long) group.getFilters().size());
					group.addFilter(newFilter);
				}
				aggregateUI.getTimer().restartTimer(aggregateUI);
				aggregateUI.update(FormOrFilter.FORM, PageUpdates.SUBMISSIONDATA);
				hide();
			}
		
		});
		
		Button exit = new Button("<img src=\"images/red_x.png\" /> Cancel");
		exit.addStyleDependentName("negative");
		exit.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		create.setWidget(0, 0, keepRemove);
		create.setWidget(0, 1, rowCol);
		create.setWidget(1, 0, submit);
		create.setWidget(1, 1, exit);
		rowCol.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				if(rowCol.getValue(rowCol.getSelectedIndex()).compareTo(RowOrCol.ROW.toString()) == 0) {
					create.setWidget(0, 2, whereCols);
					col.setVisible(true);
					cols.setVisible(false);
					whereCols.setVisible(true);
					comp.setVisible(true);
					var.setVisible(true);
				} else {
					create.setWidget(0, 2, cols);
					col.setVisible(false);
					whereCols.setVisible(false);
					cols.setVisible(true);
					comp.setVisible(false);
					var.setVisible(false);
				}
			}
			
		});
		create.setWidget(0, 2, whereCols);
		create.setWidget(0, 3, col);
		create.setWidget(0, 4, comp);
		create.setWidget(0, 5, var);
		
		col.setVisible(true);
		cols.setVisible(false);
		whereCols.setVisible(true);
		comp.setVisible(true);
		var.setVisible(true);
		setWidget(create);
	}
	
	private void getSubmissions() {
	    // Initialize the service proxy.
	    if (subSvc == null) {
	      subSvc = GWT.create(SubmissionService.class);
	    }

	    // Set up the callback object.
	    AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
	      public void onFailure(Throwable caught) {
	         // TODO: deal with error
	      }

		@Override
		public void onSuccess(SubmissionUISummary result) {
			columns = result.getHeaders();
		}
	    };

	    // Make the call to the form service.
	    subSvc.getSubmissions(group, callback);
	  }
}
