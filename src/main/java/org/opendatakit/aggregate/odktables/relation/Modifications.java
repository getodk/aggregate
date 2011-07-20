package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Modifications is a relation containing all the {@link InternalModification} entities
 * stored in the datastore. Thus Modifications keeps track of all the registered
 * users of the odktables API.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Modifications extends TypedEntityRelation<InternalModification>
{
    // Field names
    /**
     * The name of the aggregateTableIdentifier field.
     */
    public static final String AGGREGATE_TABLE_IDENTIFIER = "AGGREGATE_TABLE_IDENTIFIER";

    /**
     * The name of the modificationNumber field.
     */
    public static final String MODIFICATION_NUMBER = "MODIFICATION_NUMBER";

    /**
     * The name of the aggregateRowIdentifier field.
     */
    public static final String AGGREGATE_ROW_IDENTIFIER = "AGGREGATE_ROW_IDENTIFIER";

    // Relation name
    /**
     * The name of the Modifications relation.
     */
    private static final String RELATION_NAME = "MODIFICATIONS";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The field for the aggregateTableIdentifier.
     */
    private static final DataField aggregateTableIdentifier = new DataField(AGGREGATE_TABLE_IDENTIFIER,
            DataType.URI, false);
    /**
     * The field for the modificationNumber.
     */
    private static final DataField modificationNumber = new DataField(
            MODIFICATION_NUMBER, DataType.INTEGER, false);

    private static final DataField aggregateRowIdentifier = new DataField(AGGREGATE_ROW_IDENTIFIER,
            DataType.URI, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(aggregateTableIdentifier);
        fields.add(modificationNumber);
        fields.add(aggregateRowIdentifier);
    }

    /**
     * The singleton instance of the Modifications.
     */
    private static Modifications instance;

    /**
     * Constructs an instance which can be used to manipulate the Modifications
     * relation. If the Modifications relation does not already exist in the
     * datastore it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Modifications(CallingContext cc) throws ODKDatastoreException
    {
        super(Table.NAMESPACE, RELATION_NAME, fields, cc);
    }

    public InternalModification initialize(Entity entity) throws ODKDatastoreException
    {
        return new InternalModification(entity, super.getCC());
    }

    /**
     * Returns the singleton instance of the Modifications.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Modifications. If the instance
     *         does not exist or if the CallingContext has changed since it was
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
