package org.opendatakit.aggregate.odktables.client.exception;

public class RowOutOfSynchException extends ODKTablesClientException {
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 1582204586795767787L;

    private final String aggregateRowIdentifier;

    public RowOutOfSynchException(String aggregateRowIdentifier) {
	super(String.format("Row %s has wrong revisionTag!",
		aggregateRowIdentifier));
	this.aggregateRowIdentifier = aggregateRowIdentifier;
    }

    /**
     * @return the aggregateRowIdentifier
     */
    public String getAggregateRowIdentifier() {
	return aggregateRowIdentifier;
    }
}
