package org.opendatakit.aggregate.odktables.entity;

import java.util.List;

import org.opendatakit.aggregate.odktables.relation.Columns;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.Check;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * An InternalColumn is a (aggregateTableIdentifier, columnName, columnType,
 * nullable, properties) tuple, where
 * <ul>
 * <li>aggregateTableIdentifier: the globally unique identifier of the table
 * this column belongs to.</li>
 * <li>columnName: the name of the column.</li>
 * <li>columnType: the type of the column. This is a DataField.DataType.</li>
 * <li>nullable: whether the column is allowed to contain a null value.</li>
 * <li>properties: arbitrary metadata the client wants to store on the column</li>
 * </ul>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class InternalColumn extends TypedEntity {

    public InternalColumn(String aggregateTableIdentifier, String columnName,
	    AttributeType columnType, boolean nullable, String properties,
	    CallingContext cc) throws ODKDatastoreException {
	super(Columns.getInstance(cc).newEntity());
	setAggregateTableIdentifier(aggregateTableIdentifier);
	setName(columnName);
	setType(columnType);
	setNullable(nullable);
	setProperties(properties);
    }

    private InternalColumn(Entity entity) throws ODKDatastoreException {
	super(entity);
    }

    public String getAggregateTableIdentifier() {
	return entity.getString(Columns.AGGREGATE_TABLE_IDENTIFIER);
    }

    public void setAggregateTableIdentifier(String value) {
	entity.set(Columns.AGGREGATE_TABLE_IDENTIFIER, value);
    }

    public String getName() {
	return entity.getString(Columns.COLUMN_NAME);
    }

    public void setName(String value) {
	entity.set(Columns.COLUMN_NAME, value);
    }

    public AttributeType getType() {
	return AttributeType.valueOf(entity.getString(Columns.COLUMN_TYPE));
    }

    public void setType(AttributeType value) {
	entity.set(Columns.COLUMN_TYPE, value.toString());
    }

    public boolean getNullable() {
	return entity.getBoolean(Columns.NULLABLE);
    }

    public void setNullable(boolean value) {
	entity.set(Columns.NULLABLE, value);
    }

    public String getProperties() {
	return entity.getString(Columns.PROPERTIES);
    }

    public void setProperties(String value) {
	entity.set(Columns.PROPERTIES, value);
    }

    @Override
    public String toString() {
	return String.format(
		"InternalColumn[name=%s, type=%s, nullable=%s, properties=%s]",
		getName(), getType(), getNullable(), getProperties());
    }

    public static InternalColumn fromEntity(Entity entity)
	    throws ODKDatastoreException {
	return new InternalColumn(entity);
    }

    /**
     * Searches list for an InternalColumn with the name columnName. If found,
     * returns that InternalColumn, otherwise returns null.
     * 
     * @param list
     *            a list of InternalColumns. Must not be null.
     * @param columnName
     *            the column name to search for. Must not be null or empty.
     * @return the column if found, else null.
     */
    public static InternalColumn search(List<InternalColumn> list,
	    String columnName) {
	Check.notNull(list, "list");
	Check.notNullOrEmpty(columnName, "columnName");

	for (InternalColumn column : list) {
	    if (column.getName().equals(columnName))
		return column;
	}
	return null;
    }
}
