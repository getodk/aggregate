package org.opendatakit.aggregate.client.widgets;

import java.util.List;

import org.opendatakit.aggregate.client.odktables.TableEntryClient;

/**
 * This will just provide a list box of String values. It is modeled after
 * ColumnListBox.
 *
 * NB: not currently used, b/c can't do updates and whatnot.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableEntryClientListBox extends AggregateListBox {

	  private final List<TableEntryClient> tables;

	  public TableEntryClientListBox(List<TableEntryClient> tables, boolean multipleValueSelection,
	      boolean onlyIncludeTableName, String tooltipText) {
	    this(tables, multipleValueSelection, onlyIncludeTableName, tooltipText, null);
	  }

	  public TableEntryClientListBox(List<TableEntryClient> tables, boolean multipleValueSelection,
	      boolean onlyIncludeTableName, String tooltipText, String helpBalloonTxt) {
	    super(tooltipText, multipleValueSelection, helpBalloonTxt);
	    this.tables = tables;

	    for (TableEntryClient table : tables) {
	    	// TODO: fix this direction stuff. and figure out wtf it is.
	    	if (onlyIncludeTableName) {
	    		addItem(table.getTableKey(), "what the hell is direction?");
	    	} else { // display both
	    		addItem(table.getTableKey() + "--" + table.getTableId(), "direction 2?");
	    	}
	    }
	  }

}
