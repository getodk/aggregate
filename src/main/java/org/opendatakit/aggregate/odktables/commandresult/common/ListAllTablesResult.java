package org.opendatakit.aggregate.odktables.commandresult.common;

import java.util.List;

import org.opendatakit.aggregate.odktables.client.entity.TableEntry;
import org.opendatakit.aggregate.odktables.command.common.ListAllTables;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.common.utils.Check;

/**
 * A ListAllTablesResult represents the result of executing a QueryForTables
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class ListAllTablesResult extends CommandResult<ListAllTables> {
    private final List<TableEntry> entries;

    /**
     * Need a no-arg constructor for serialization by Gson.
     */
    private ListAllTablesResult() {
	super(true, null);
	this.entries = null;
    }

    /**
     * The success constructor. See {@link #success} for param info.
     */
    private ListAllTablesResult(List<TableEntry> entries) {
	super(true, null);
	Check.notNull(entries, "entries");
	this.entries = entries;
    }

    /**
     * @return the list of table entries representing the results of the
     *         QueryForTables command.
     */
    public List<TableEntry> getEntries() {
	return this.entries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String.format("QueryForTablesResult [entries=%s]", entries);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((entries == null) ? 0 : entries.hashCode());
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (!(obj instanceof ListAllTablesResult))
	    return false;
	ListAllTablesResult other = (ListAllTablesResult) obj;
	if (entries == null) {
	    if (other.entries != null)
		return false;
	} else if (!entries.equals(other.entries))
	    return false;
	return true;
    }

    /**
     * @param entries
     *            a list of TableEntries containing an entry for each table in
     *            the results of the QueryForTables command.
     * @return a new QueryForTablesResult representing the successful completion
     *         of a QueryForTables command.
     */
    public static ListAllTablesResult success(List<TableEntry> entries) {
	return new ListAllTablesResult(entries);
    }
}
