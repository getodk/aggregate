package org.opendatakit.aggregate.odktables.client.entity;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A Modification represents a set of changes to a table.
 * </p>
 * 
 * <p>
 * A Modification has the following attributes:
 * <ul>
 * <li>modificationNumber: the modificationNumber which a client's table will be
 * at after applying the modifications</li>
 * <li>rows: a collection of rows which represent the difference between the
 * rows the client's table has now and the rows in Aggregate's version of the
 * table</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Modification is immutable.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Modification {
    private final int modificationNumber;
    private final List<SynchronizedRow> rows;

    public Modification(int modificationNumber, List<SynchronizedRow> rows) {
	this.modificationNumber = modificationNumber;
	this.rows = rows;
    }

    public int getModificationNumber() {
	return this.modificationNumber;
    }

    public List<SynchronizedRow> getRows() {
	return Collections.unmodifiableList(this.rows);
    }
}
