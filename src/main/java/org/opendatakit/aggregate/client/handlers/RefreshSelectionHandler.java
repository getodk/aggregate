package org.opendatakit.aggregate.client.handlers;

import org.opendatakit.aggregate.client.AggregateUI;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

public class RefreshSelectionHandler<T> implements SelectionHandler<T> {
	@Override
	public void onSelection(SelectionEvent<T> event) {
		AggregateUI.resize();
	}
	
}
