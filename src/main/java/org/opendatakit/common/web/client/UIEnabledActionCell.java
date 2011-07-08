/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.common.web.client;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Implements a button that can be made not visible, enabled or disabled.
 * 
 * @author mitchellsundt@gmail.com
 *
 * @param <T>
 */
public class UIEnabledActionCell<T>  extends ActionCell<T>  {
	
	@Override
	protected void onEnterKeyDown(Context context, Element parent,
			T value, NativeEvent event,
			ValueUpdater<T> valueUpdater) {
		if ( isEnabledPredicate == null || isEnabledPredicate.isEnabled(value) ) {
			super.onEnterKeyDown(context, parent, value, event, valueUpdater);
		}
	}

	@Override
	public void render(Context context, T object,
			SafeHtmlBuilder sb) {
		if ( isVisiblePredicate == null || isVisiblePredicate.isVisible(object) ) {
			if ( isEnabledPredicate == null || isEnabledPredicate.isEnabled(object) ) {
				super.render(context, object, sb);
			} else {
				sb.append(htmlDisabled);
			}
		}
	}

	private final SafeHtml htmlDisabled;
	private final UIEnabledPredicate<T> isEnabledPredicate;
	private final UIVisiblePredicate<T> isVisiblePredicate;
	
	public UIEnabledActionCell(
			String text,
			UIEnabledPredicate<T> isEnabledPredicate,
			com.google.gwt.cell.client.ActionCell.Delegate<T> delegate) {
		this(SafeHtmlUtils.fromString(text), null, isEnabledPredicate, delegate);
	}
	
	public UIEnabledActionCell(
			String text,
			UIVisiblePredicate<T> isVisiblePredicate,
			com.google.gwt.cell.client.ActionCell.Delegate<T> delegate) {
		this(SafeHtmlUtils.fromString(text), isVisiblePredicate, null, delegate);
	}
	
	public UIEnabledActionCell(
			String text,
			UIVisiblePredicate<T> isVisiblePredicate,
			UIEnabledPredicate<T> isEnabledPredicate,
			com.google.gwt.cell.client.ActionCell.Delegate<T> delegate) {
		this(SafeHtmlUtils.fromString(text), isVisiblePredicate, isEnabledPredicate, delegate);
	}

	public UIEnabledActionCell(
			SafeHtml text,
			UIVisiblePredicate<T> isVisiblePredicate,
			UIEnabledPredicate<T> isEnabledPredicate,
			com.google.gwt.cell.client.ActionCell.Delegate<T> delegate) {
		super(text, delegate);
		this.isEnabledPredicate = isEnabledPredicate;
		this.isVisiblePredicate = isVisiblePredicate;
		htmlDisabled = new SafeHtmlBuilder().appendHtmlConstant(
		"<button type=\"button\" tabindex=\"-1\" disabled>").append(text).appendHtmlConstant(
	          "</button>").toSafeHtml();
	}
}
