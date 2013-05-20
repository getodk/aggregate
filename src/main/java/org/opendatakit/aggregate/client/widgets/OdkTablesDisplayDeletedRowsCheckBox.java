package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.OdkTablesViewTableSubTab;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * This was to be a checkbox that determined if deleted rows were
 * to be displayed to the user. This is currently not used, as another
 * tact was taken.
 * @author sudar.sam
 *
 */
public class OdkTablesDisplayDeletedRowsCheckBox extends AggregateCheckBox
		implements ValueChangeHandler<Boolean> {

	private OdkTablesViewTableSubTab tableSubTab;

	private static final String TOOLTIP_TXT = "Display or hide deleted rows";

	private static final String HELP_BALLOON_TXT = "Check this box if you want to display" +
			"deleted rows.";


	public OdkTablesDisplayDeletedRowsCheckBox(OdkTablesViewTableSubTab tableSubTab,
			Boolean accept) {
		super(TOOLTIP_TXT, HELP_BALLOON_TXT);
		this.tableSubTab = tableSubTab;
		setValue(accept);

	}

	@Override
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		super.onValueChange(event);
		//tableSubTab.setDisplayDeleted(event.getValue());
		tableSubTab.update();

	}

}
