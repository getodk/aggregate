package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * TableEntries is a relation containing all the {@link TableEntry} entities
 * stored in the datastore. TableEntries keeps track of all the tables created
 * through the odktables API.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class TableEntries
        extends
        TypedEntityRelation<org.opendatakit.aggregate.odktables.entity.TableEntry>
{
    // Field names
    /**
     * The name of the ownerUUID field.
     */
    public static final String OWNER_UUID = "OWNER_UUID";

    /**
     * The name of the tableName field.
     */
    public static final String TABLE_NAME = "TABLE_NAME";

    /**
     * The name of the modificationNumber field.
     */
    public static final String MODIFICATION_NUMBER = "MODIFICATION_NUMBER";

    // Relation name:
    /**
     * The name of the TableEntries relation.
     */
    private static final String RELATION_NAME = "TABLES";

    // The following defines the actual fields that will be in the datastore:
    // The tableUUID field is the entity UUID, so is created automatically
    /**
     * The ownerUUID field.
     */
    private static final DataField ownerUUID = new DataField(OWNER_UUID,
            DataType.URI, false);
    /**
     * The tableName field.
     */
    private static final DataField tableName = new DataField(TABLE_NAME,
            DataType.STRING, false);
    /**
     * The modificationNumber field.
     */
    private static final DataField modificationNumber = new DataField(
            MODIFICATION_NUMBER, DataType.INTEGER, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(ownerUUID);
        fields.add(tableName);
        fields.add(modificationNumber);
    }
    /**
     * The singleton instance of the TableEntries.
     */
    private static TableEntries instance;

    /**
     * Constructs an instance which can be used to manipulate the TableEntries
     * relation. If the TableEntries relation does not already exist in the
     * datastore it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private TableEntries(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    @Override
    public TableEntry initialize(Entity entity) throws ODKDatastoreException
    {
        return new TableEntry(entity, getCC());
    }

    /**
     * Returns the singleton instance of the TableEntries.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this TableEntries. If the instance does
     *         not exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static TableEntries getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || instance.getCC() != cc)
        {
            instance = new TableEntries(cc);
        }
        return instance;
    }
}
