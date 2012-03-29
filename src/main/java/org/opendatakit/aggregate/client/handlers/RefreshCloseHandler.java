package org.opendatakit.aggregate.client.handlers;

import org.opendatakit.aggregate.client.AggregateUI;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;

public class RefreshCloseHandler<T> implements CloseHandler<T> {
	@Override
	public void onClose(CloseEvent<T> event) {
		AggregateUI.resize();
	}
	
}
