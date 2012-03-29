package org.opendatakit.aggregate.odktables.client.entity;

/**
 * <p>
 * A Permission represents the permissions of a specific odktables API user on a
 * specific table.
 * </p>
 * 
 * <p>
 * A Permission has the following attributes:
 * <ul>
 * <li>aggregateUserIdentifier: the Aggregate Identifier of the user who the
 * permission is for</li>
 * <li>aggregateTableIdentifier: the Aggregate Identifier of the table who the
 * permission is on</li>
 * <li>read: true if the user is allowed to read from the table</li>
 * <li>write: true if the user is allowed to write to the table</li>
 * <li>delete: true if the user is allowed to delete from or delete the table</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Permission is immutable.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Permission {
    private String aggregateUserIdentifier;
    private String aggregateTableIdentifier;
    private boolean read;
    private boolean write;
    private boolean delete;

    /**
     * For Gson serialization.
     */
    @SuppressWarnings("unused")
    private Permission() {
	this.aggregateUserIdentifier = null;
	this.aggregateTableIdentifier = null;
	this.read = false;
	this.write = false;
	this.delete = false;
    }

    /**
     * @param aggregateUserIdentifier
     *            the Aggregate Identifier of the user who the permission is for
     * @param aggregateTableIdentifier
     *            the Aggregate Identifier of the table
     * @param aggregateTableIdentifier
     *            the Aggregate Identifier of the table who the permission is on
     * @param read
     *            true if the user is allowed to read from the table
     * @param write
     *            true if the user is allowed to write to the table
     * @param delete
     *            true if the user is allowed to delete from or delete the table
     */
    public Permission(String aggregateUserIdentifier,
	    String aggregateTableIdentifier, boolean read, boolean write,
	    boolean delete) {
	this.aggregateUserIdentifier = aggregateUserIdentifier;
	this.aggregateTableIdentifier = aggregateTableIdentifier;
	this.read = read;
	this.write = write;
	this.delete = delete;
    }

    /**
     * @return the aggregateUserIdentifier
     */
    public String getAggregateUserIdentifier() {
	return aggregateUserIdentifier;
    }

    /**
     * @return the aggregateTableIdentifier
     */
    public String getAggregateTableIdentifier() {
	return aggregateTableIdentifier;
    }

    /**
     * @return the read
     */
    public boolean read() {
	return read;
    }

    /**
     * @return the write
     */
    public boolean write() {
	return write;
    }

    /**
     * @return the delete
     */
    public boolean delete() {
	return delete;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String
		.format("Permission [aggregateUserIdentifier=%s, aggregateTableIdentifier=%s, read=%s, write=%s, delete=%s]",
			aggregateUserIdentifier, aggregateTableIdentifier,
			read, write, delete);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (delete ? 1231 : 1237);
	result = prime * result + (read ? 1231 : 1237);
	result = prime
		* result
		+ ((aggregateTableIdentifier == null) ? 0
			: aggregateTableIdentifier.hashCode());
	result = prime
		* result
		+ ((aggregateUserIdentifier == null) ? 0
			: aggregateUserIdentifier.hashCode());
	result = prime * result + (write ? 1231 : 1237);
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
	if (obj == null)
	    return false;
	if (!(obj instanceof Permission))
	    return false;
	Permission other = (Permission) obj;
	if (delete != other.delete)
	    return false;
	if (read != other.read)
	    return false;
	if (aggregateTableIdentifier == null) {
	    if (other.aggregateTableIdentifier != null)
		return false;
	} else if (!aggregateTableIdentifier
		.equals(other.aggregateTableIdentifier))
	    return false;
	if (aggregateUserIdentifier == null) {
	    if (other.aggregateUserIdentifier != null)
		return false;
	} else if (!aggregateUserIdentifier
		.equals(other.aggregateUserIdentifier))
	    return false;
	if (write != other.write)
	    return false;
	return true;
    }
}
