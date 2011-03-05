package org.opendatakit.aggregate.client.filter;

import org.opendatakit.aggregate.constants.common.ColumnVisibility;
import org.opendatakit.aggregate.constants.common.FilterOperation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class CreateNewFilterPopup extends PopupPanel{
	
	public CreateNewFilterPopup() {
		
	}
	
	public CreateNewFilterPopup(FlexTable data, 
			final FilterGroup def) {
		super(false); //do not close popup when user clicks out of it
		FlexTable create = new FlexTable();
		//keep or remove
		final ListBox keepRemove = new ListBox();
		keepRemove.addItem(ColumnVisibility.KEEP.toString());
		keepRemove.addItem(ColumnVisibility.REMOVE.toString());
		//column selection
		final ListBox column = new ListBox(true);
		for(int i = 0; i < data.getCellCount(0); i++) {
			column.addItem(data.getText(0, i));
		}
		//where rows
		final Label whereRows = new Label("where my rows are");
		//comparison operator
		final ListBox comp = new ListBox();
		comp.addItem(FilterOperation.LESS_THAN.toString());
		comp.addItem(FilterOperation.LESS_THAN_OR_EQUAL.toString());
		comp.addItem(FilterOperation.EQUAL.toString());
		comp.addItem(FilterOperation.GREATER_THAN_OR_EQUAL.toString());
		comp.addItem(FilterOperation.GREATER_THAN_OR_EQUAL.toString());
		//value input
		final TextBox var = new TextBox();
		Button exit = new Button("Save Filter");
		exit.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				ColumnVisibility kr;
				FilterOperation op;
				
				String korr = keepRemove.getValue(keepRemove.getSelectedIndex());
				if(korr.compareTo(ColumnVisibility.KEEP.toString()) == 0) {
					kr = ColumnVisibility.KEEP;
				} else {
					kr = ColumnVisibility.REMOVE;
				}
				
				String col = column.getValue(column.getSelectedIndex());
				for (int i = column.getSelectedIndex(); i < column.getItemCount(); i++) {
					if(column.isItemSelected(i)) {
						col += column.getValue(i);
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
				Filter newFilter = new Filter(kr, col, op, 
						variable, (long) def.getFilters().size());
				def.addFilter(newFilter);
				hide();
			}
		
		});
		create.setWidget(0, 0, keepRemove);
		create.setWidget(0, 1, column);
		create.setWidget(0, 2, whereRows);
		create.setWidget(0, 3, comp);
		create.setWidget(1, 0, exit);
		create.setWidget(0, 4, var);
		setWidget(create);
	}
}
