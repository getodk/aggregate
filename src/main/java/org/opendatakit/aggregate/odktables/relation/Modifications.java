
package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.User;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Modifications is a relation containing all the {@link Modification} entities stored in the
 * datastore. Thus Modifications keeps track of all the registered users of the
 * odktables API.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Modifications extends TypedEntityRelation<Modification>
{
    // Field names
    /**
     * The name of the tableUUID field.
     */
    public static final String TABLE_UUID = "TABLE_UUID";

    /**
     * The name of the modificationNumber field.
     */
    public static final String MODIFICATION_NUMBER = "MODIFICATION_NUMBER";

    /**
     * The name of the rowUUID field.
     */
    public static final String ROW_UUID = "ROW_UUID";

    // Relation name
    /**
     * The name of the Modifications relation.
     */
    private static final String RELATION_NAME = "MODIFICATIONS";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The field for the tableUUID.
     */
    private static final DataField tableUUID = new DataField(tableUUID,
            DataType.URI, false);
    /**
     * The field for the modificationNumber.
     */
    private static final DataField modificationNumber = new DataField(MODIFICATION_NUMBER,
            DataType.INTEGER, false);

    private static final DataField rowUUID = new DataField(ROW_UUID,
            DataType.URI, false);

    private static final List<DataField> fields;
    static
    {
        userID.setIndexable(IndexType.HASH);

        fields = new ArrayList<DataField>();
        fields.add(tableUUID);
        fields.add(modificationNumber);
        fields.add(rowUUID);
    }

    /**
     * The singleton instance of the Modifications.
     */
    private static Modifications instance;

    /**
     * Constructs an instance which can be used to manipulate the Modifications
     * relation. If the Modifications relation does not already exist in the datastore
     * it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Modifications(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    public Modification initialize(Entity entity) throws ODKDatastoreException
    {
        return new Modification(entity, super.getCC());
    }

    /**
     * Returns the singleton instance of the Modifications.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Modifications. If the instance does not
     *         exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Modifications getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || instance.getCC() != cc)
        {
            instance = new Modifications(cc);
        }
        return instance;
    }
}
