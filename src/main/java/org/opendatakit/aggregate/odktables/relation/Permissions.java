package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.Permission;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Permissions is a relation containing all the {@link Permission} entities
 * stored in the datastore. Permissions keeps track of read, write, and delete
 * permissions for all tables created through the odktables API.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Permissions extends TypedEntityRelation<Permission>
{
    // Field names
    /**
     * The name of the tableUUID field.
     */
    public static final String TABLE_UUID = "TABLE_UUID";

    /**
     * The name of the userUUID field.
     */
    public static final String USER_UUID = "USER_UUID";

    /**
     * The name of the read field.
     */
    public static final String READ = "READ";

    /**
     * The name of the write field.
     */
    public static final String WRITE = "WRITE";

    /**
     * The name of the delete field.
     */
    public static final String DELETE = "DELETE";

    // Relation name
    /**
     * The name of the Permissions relation.
     */
    private static final String RELATION_NAME = "PERMISSIONS";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The tableUUID field.
     */
    private static final DataField tableUUID = new DataField(TABLE_UUID,
            DataType.URI, false);
    /**
     * The userUUID field.
     */
    private static final DataField userUUID = new DataField(USER_UUID,
            DataType.URI, false);
    /**
     * The read field.
     */
    private static final DataField read = new DataField(READ, DataType.BOOLEAN,
            false);
    /**
     * The write field.
     */
    private static final DataField write = new DataField(WRITE,
            DataType.BOOLEAN, false);
    /**
     * The delete field.
     */
    private static final DataField delete = new DataField(DELETE,
            DataType.BOOLEAN, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(tableUUID);
        fields.add(userUUID);
        fields.add(read);
        fields.add(write);
        fields.add(delete);
    }
    /**
     * The singleton instance of the Permissions relation.
     */
    private static Permissions instance;

    /**
     * Constructs an instance which can be used to manipulate the Permissions
     * relation. If the Permissions relation does not already exist in the
     * datastore it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Permissions(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    @Override
    public Permission initialize(Entity entity) throws ODKDatastoreException
    {
        return new Permission(entity, getCC());
    }

    /**
     * Returns the singleton instance of the Permissions relation.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Permissions relation. If the
     *         instance does not exist or if the CallingContext has changed
     *         since it was constructed, then constructs and returns a new
     *         instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Permissions getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || instance.getCC() != cc)
        {
            instance = new Permissions(cc);
        }
        return instance;
    }
}
