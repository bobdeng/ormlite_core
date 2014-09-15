package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Persists an unknown Java Object that is {@link java.io.Serializable}.
 *
 * @author graywatson
 */
public class ObjectType extends BaseDataType {

	private static final ObjectType singleTon = new ObjectType();
    private static final ObjectMapper mapper = new ObjectMapper();
	public static ObjectType getSingleton() {
		return singleTon;
	}

	private ObjectType() {
		/*
		 * NOTE: Serializable class should _not_ be in the list because _everything_ is serializable and we want to
		 * force folks to use DataType.SERIALIZABLE -- especially for forwards compatibility.
		 */
		super(SqlType.JSONOBJECT, new Class<?>[]{JsonObject.class});
	}

	/**
	 * Here for others to subclass.
	 */
	protected ObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		throw new SQLException("Default values for serializable types are not supported");
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		String str = (String) sqlArg;
		try {
			if(str==null){
				return fieldType.getField().getType().newInstance();
			}
            return mapper.readValue(str, fieldType.getField().getType());
		} catch (Exception e) {
			try {
				return fieldType.getField().getType().newInstance();
			} catch (Exception e1) {
				throw new SQLException("field "+fieldType.getFieldName()+"no none arg constructor");
			} 
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException {
		try {
			if(obj==null)
			{
				return null;
			}
            return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw SqlExceptionUtil.create("Could not write JsonObject object : " + obj, e);
        }

	}

	@Override
	public boolean isValidForField(Field field) {
		boolean rlt= JsonObject.class.isAssignableFrom(field.getType());
		return rlt;
	}

	@Override
	public boolean isStreamType() {
		// can't do a getObject call beforehand so we have to check for nulls
		return true;
	}

	@Override
	public boolean isComparable() {
		return false;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isArgumentHolderRequired() {
		return true;
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
        try {
        	if(stringValue==null){
        		return fieldType.getField().getType().newInstance();
        	}
            return mapper.readValue(stringValue, fieldType.getField().getType());
        } catch (Exception e) {
        	try {
				return fieldType.getField().getType().newInstance();
			} catch (Exception e1) {
				throw new SQLException("field "+fieldType.getFieldName()+"no none arg constructor");
			} 
        }
        
	}

	@Override
	public Class<?> getPrimaryClass() {
		return JsonObject.class;
	}

	
	
}
