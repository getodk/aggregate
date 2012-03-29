package org.opendatakit.aggregate.client.handlers;

import org.opendatakit.aggregate.client.AggregateUI;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;

public class RefreshOpenHandler<T> implements OpenHandler<T> {
	@Override
	public void onOpen(OpenEvent<T> event) {
		AggregateUI.resize();
	}
	
}
