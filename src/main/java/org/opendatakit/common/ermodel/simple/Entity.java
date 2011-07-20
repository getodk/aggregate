package org.opendatakit.common.ermodel.simple;

public class Entity
{
    private ExtendedAbstractRelation relation;
    private org.opendatakit.common.ermodel.Entity entity;

    private Entity(ExtendedAbstractRelation relation, org.opendatakit.common.ermodel.Entity entity)
    {
        Check.notNull(relation, "relation");
        Check.notNull(entity, "entity");
        this.relation = relation;
        this.entity = entity;
    }

	public void setBoolean(String attributeName, Boolean value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setBoolean(field, value);
    }
	
	public Boolean getBoolean(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getBoolean(field);
    }
	
	public void setDate(String attributeName, Date value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setDate(field, value);
    }
	
	public Date getDate(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getDate(field);
    }

	public void setDouble(String attributeName, Double value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setDouble(field, value);
    }
	
    public Double getDouble(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getDouble(field);
    }
	
    public void setNumeric(String attributeName, BigDecimal value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setNumeric(field, value);
    }
	
    public BigDecimal getNumeric(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getNumeric(field);
    }

	public void setInteger(String attributeName, Integer value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setInteger(field, value);
    }
	
	public Integer getInteger(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getInteger(field);
    }

	public void setLong(String attributeName, Long value)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setLong(field, value);
    }
	
	public Long getLong(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getLong(field);
    }
	
	public void setString(String attributeName, String value )
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        this.entity.setString(field, value);
    }

	public String getString(String attributeName)
    {
        Check.notNullOrEmpty(attributeName, "attributeName");
        DataField field = relation.getDataField(attributeName);
        return this.entity.getString(field);
    }

    protected Entity fromEntity(ExtendedAbstractRelation relation, org.opendatakit.common.ermodel.Entity entity)
    {
        return new Entity(relation, entity);
    }
}
