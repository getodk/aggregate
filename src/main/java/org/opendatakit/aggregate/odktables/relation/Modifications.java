package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalModification;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Modifications is a relation containing all the {@link InternalModification}
 * entities stored in the datastore. Thus Modifications keeps track of all the
 * registered users of the odktables API.
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

    // The following defines the actual attributes that will be in the datastore:
    /**
     * The field for the aggregateTableIdentifier.
     */
    private static final Attribute aggregateTableIdentifier = new Attribute(
            AGGREGATE_TABLE_IDENTIFIER, AttributeType.STRING, false);
    /**
     * The field for the modificationNumber.
     */
    private static final Attribute modificationNumber = new Attribute(
            MODIFICATION_NUMBER, AttributeType.INTEGER, false);

    private static final Attribute aggregateRowIdentifier = new Attribute(
            AGGREGATE_ROW_IDENTIFIER, AttributeType.STRING, false);

    private static final List<Attribute> attributes;
    static
    {
        attributes = new ArrayList<Attribute>();
        attributes.add(aggregateTableIdentifier);
        attributes.add(modificationNumber);
        attributes.add(aggregateRowIdentifier);
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
        super(Table.NAMESPACE, RELATION_NAME, attributes, cc);
    }

    public InternalModification initialize(Entity entity)
            throws ODKDatastoreException
    {
        return InternalModification.fromEntity(entity);
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
