package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Rows is a set of relations containing all the {@link InternalRow} entities
 * for all tables stored in the datastore. An instance of Rows contains all the
 * Row entities for a specific table.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Table extends TypedEntityRelation<InternalRow> {
    /**
     * The name of the revisionTag field.
     */
    public static final String REVISION_TAG = "REVISION_TAG";

    /**
     * The revisionTag field.
     */
    private static final Attribute revisionTag = new Attribute(REVISION_TAG,
	    AttributeType.STRING, false);

    /**
     * The namespace for all relations.
     */
    public static final String NAMESPACE = "ODKTABLES";
    /**
     * The namespace for Table relations
     */
    private static final String TABLE_NAMESPACE = NAMESPACE + "_TABLE";

    private List<Attribute> attributes;

    /**
     * Constructs a Table. If the constructed Table does not already exist in
     * the datastore it will be created.
     * 
     * @param namespace
     *            the namespace the table should be created under.
     * @param aggregateTableIdentifier
     *            the globally unique identifier of the Table.
     * @param attributes
     *            a list of Attributes representing the attributes of the Table
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Table(String namespace, String aggregateTableIdentifier,
	    List<Attribute> attributes, CallingContext cc)
	    throws ODKDatastoreException {
	super(namespace, aggregateTableIdentifier, attributes, cc);
	this.attributes = attributes;
    }

    @Override
    public InternalRow initialize(Entity entity) throws ODKDatastoreException {
	return InternalRow.fromEntity(entity);
    }

    /**
     * @return a list of Attributes representing the columns of this table.
     */
    public List<Attribute> getAttributes() {
	return Collections.unmodifiableList(this.attributes);
    }

    public static Table getInstance(String aggregateTableIdentifier,
	    CallingContext cc) throws ODKDatastoreException {
	List<InternalColumn> columns = Columns
		.getInstance(cc)
		.query("[odktables]Table.getInstance")
		.equal(Columns.AGGREGATE_TABLE_IDENTIFIER,
			aggregateTableIdentifier).execute();
	List<Attribute> attributes = new ArrayList<Attribute>();

	for (InternalColumn column : columns) {
	    String name = convertIdentifier(column.getAggregateIdentifier());
	    AttributeType type = column.getType();
	    boolean nullable = column.getNullable();
	    Attribute attribute = new Attribute(name, type, nullable);
	    attributes.add(attribute);
	}

	attributes.add(revisionTag);
	String tableName = convertIdentifier(aggregateTableIdentifier);

	return new Table(TABLE_NAMESPACE, tableName, attributes, cc);
    }

    public static String convertIdentifier(String aggregateIdentifier) {
	return aggregateIdentifier.replace('-', '_').replace(':', '_')
		.toUpperCase();
    }
}
