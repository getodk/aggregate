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
 * <li>userUUID: the UUID of the user who the permission is for</li>
 * <li>tableUUID: the UUID of the table who the permission is on</li>
 * <li>read: true if the user is allowed to read from the table</li>
 * <li>write: true if the user is allowed to write to the table</li>
 * <li>delete: true if the user is allowed to delete from or delete the table</li>
 * </ul>
 * </p>
 * 
 * Permission is immutable.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class Permission
{
    private String userUUID;
    private String tableUUID;
    private boolean read;
    private boolean write;
    private boolean delete;

    /**
     * For Gson serialization.
     */
    @SuppressWarnings("unused")
    private Permission()
    {
        this.userUUID = null;
        this.tableUUID = null;
        this.read = false;
        this.write = false;
        this.delete = false;
    }

    /**
     * @param userUUID
     *            the UUID of the user who the permission is for
     * @param tableUUID
     *            the UUID of the table
     * @param tableUUID
     *            the UUID of the table who the permission is on
     * @param read
     *            true if the user is allowed to read from the table
     * @param write
     *            true if the user is allowed to write to the table
     * @param delete
     *            true if the user is allowed to delete from or delete the table
     */
    public Permission(String userUUID, String tableUUID, boolean read,
            boolean write, boolean delete)
    {
        this.userUUID = userUUID;
        this.tableUUID = tableUUID;
        this.read = read;
        this.write = write;
        this.delete = delete;
    }

    /**
     * @return the userUUID
     */
    public String getUserUUID()
    {
        return userUUID;
    }

    /**
     * @return the tableUUID
     */
    public String getTableUUID()
    {
        return tableUUID;
    }

    /**
     * @return the read
     */
    public boolean read()
    {
        return read;
    }

    /**
     * @return the write
     */
    public boolean write()
    {
        return write;
    }

    /**
     * @return the delete
     */
    public boolean delete()
    {
        return delete;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String
                .format("Permission [userUUID=%s, tableUUID=%s, read=%s, write=%s, delete=%s]",
                        userUUID, tableUUID, read, write, delete);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (delete ? 1231 : 1237);
        result = prime * result + (read ? 1231 : 1237);
        result = prime * result
                + ((tableUUID == null) ? 0 : tableUUID.hashCode());
        result = prime * result
                + ((userUUID == null) ? 0 : userUUID.hashCode());
        result = prime * result + (write ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
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
        if (tableUUID == null)
        {
            if (other.tableUUID != null)
                return false;
        } else if (!tableUUID.equals(other.tableUUID))
            return false;
        if (userUUID == null)
        {
            if (other.userUUID != null)
                return false;
        } else if (!userUUID.equals(other.userUUID))
            return false;
        if (write != other.write)
            return false;
        return true;
    }
}
