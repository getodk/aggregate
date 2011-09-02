package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.BookHelpConsts;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HelpBookPopup extends PopupPanel {
	private VerticalPanel panel;

	public HelpBookPopup() {
		super(false);
		panel = new VerticalPanel();

		// populate the panel
		panel.add(new ClosePopupButton(this));

		BookHelpConsts[] consts = BookHelpConsts.values();
		
		for(int i = 0; i < consts.length; i++) {
			panel.add(new HTML("<b>" + consts[i].getTitle() + "</b>"));
			panel.add(new HTML(consts[i].getConcept() + "<br><br>"));
			panel.add(new HTML(consts[i].getProcedures() + "<br><br>"));
		}

		ScrollPanel scroll = new ScrollPanel(panel);
		scroll.setPixelSize((Window.getClientWidth() / 2),(Window.getClientHeight() / 2));
		setWidget(scroll);
	}
}
