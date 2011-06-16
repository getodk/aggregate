package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entities.Table;
import org.opendatakit.common.ermodel.AbstractRelationAdapter;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * TableIndex is an index of all the Table stored in the datastore.
 * </p>
 * 
 * <p>
 * TableIndex is a set of (tableUri, ownerUri, tableName, modificationNumber)
 * tuples, aka 'entities' where
 * <ul>
 * <li>tableUri: the globally unique identifer of the table</li>
 * <li>ownerUri: the globally unique identifier of the user who owns the table</li>
 * <li>tableName: the human readable name of the table</li>
 * <li>modificationNumber: the current modification number. This is incremented
 * every time the table is edited.</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Tables extends TypedEntityRelation<org.opendatakit.aggregate.odktables.entities.Table>
{
    // Field names
    /**
     * The name of the ownerUri field.
     */
    public static final String OWNER_URI = "OWNER_URI";
    
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
     * The name of the TableIndex relation.
     */
    private static final String RELATION_NAME = "TABLE_INDEX";

    // The following defines the actual fields that will be in the datastore:
    // The tableUri field is the entity URI, so is created automatically
    /**
     * The ownerUri field.
     */
    private static final DataField ownerUri = new DataField(
            OWNER_URI, DataType.URI, false);
    /**
     * The tableName field.
     */
    private static final DataField tableName = new DataField(
            TABLE_NAME, DataType.STRING, false);
    /**
     * The modificationNumber field.
     */
    private static final DataField modificationNumber = new DataField(
            MODIFICATION_NUMBER, DataType.INTEGER, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(ownerUri);
        fields.add(tableName);
        fields.add(modificationNumber);
    }
    /**
     * The singleton instance of the TableIndex.
     */
    private static Tables instance;

    /**
     * Constructs an instance which can be used to manipulate the TableIndex
     * relation. If the TableIndex relation does not already exist in the
     * datastore it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Tables(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    @Override
    public Table initialize(Entity entity) throws ODKDatastoreException
    {
        return new Table(entity, getCC());
    }    
    
    /**
     * Returns the singleton instance of the TableIndex.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this TableIndex. If the instance does
     *         not exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Tables getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || AbstractRelationAdapter.getCC() != cc)
        {
            instance = new Tables(cc);
        }
        return instance;
    }
}
