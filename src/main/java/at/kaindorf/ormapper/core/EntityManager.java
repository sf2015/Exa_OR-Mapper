package at.kaindorf.ormapper.core;

import at.kaindorf.ormapper.annotations.Column;
import at.kaindorf.ormapper.annotations.Id;
import at.kaindorf.ormapper.annotations.Transient;
import at.kaindorf.ormapper.io.IO_Access;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static at.kaindorf.ormapper.core.DataTypes.SQL_TYPES;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 09.02.2023
 * Time: 19:41
 */
public class EntityManager {

  private List<Class> entityClasses;
  private DB_Access dbAccess = DB_Access.getInstance();
  private static EntityManager theInstance = null;

  public static EntityManager getInstance() {
    if (theInstance == null) {
      theInstance = new EntityManager();
    }
    return theInstance;
  }

  private EntityManager() {
    if (dbAccess.getDb_generation_schema().equals("drop-and-create")) {
      try {
        entityClasses = IO_Access.findEntityClassesInProject();
        entityClasses.forEach(this::dropTable);
        entityClasses.forEach(this::createTable);
      } catch (IOException e) {
        System.out.println("Failed loading entity-classes: " + e.toString());
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * drop table of an entity-class
   *
   * @param entityClass
   * @throws SQLException
   */
  public void dropTable(Class entityClass) {
    String tableName = entityClass.getSimpleName().toLowerCase();
    String sqlString = String.format("DROP TABLE IF EXISTS %s", tableName);
    try {
      dbAccess.getStatement().execute(sqlString);
      System.out.format("table '%s' dropped\n", tableName);
    } catch (SQLException e) {
      System.err.format("drop table failed: '%s'\n", tableName);
      System.err.println(e.toString());
    }
  }

  /**
   * create new Table for entity-class
   *
   * @param entityClass
   * @throws SQLException
   */
  public void createTable(Class entityClass) {
    String tableName = entityClass.getSimpleName().toLowerCase();
    String sqlString = String.format(" CREATE TABLE %s (", tableName);
    // loop over all fields of entity class
    for (Field field : entityClass.getDeclaredFields()) {
      // skip transient fields
      if (field.isAnnotationPresent(Transient.class)) {
        continue;
      }
      String fieldName = " " + field.getName().toLowerCase();
      String fieldType = SQL_TYPES.get(field.getType().getName());

      if (field.isAnnotationPresent(Column.class)) {
        if (!field.getAnnotation(Column.class).name().equals("")) {
          fieldName = field.getAnnotation(Column.class).name();
        }
        if (field.getType().equals(String.class)) {
          fieldType = String.format("VARCHAR(%d)", field.getAnnotation(Column.class).length());
        }
        fieldType += field.getAnnotation(Column.class).nullable() ? "" : " NOT NULL";
        fieldType += field.getAnnotation(Column.class).unique() ? " UNIQUE" : "";
      }

      String pk = field.isAnnotationPresent(Id.class) ? " PRIMARY KEY" : "";
      sqlString += String.format("%s %s%s,\n", fieldName, fieldType, pk);
    }
    sqlString = sqlString.substring(0, sqlString.lastIndexOf(',')) + ");";
    System.out.println(sqlString);
    try {
      dbAccess.getStatement().execute(sqlString);
      System.out.format("table '%s' created\n", tableName);
    } catch (SQLException e) {
      System.err.format("create table failed: '%s'\n", tableName);
      System.err.println(e.toString());
    }
  }

  /**
   * persist entity object to database
   *
   * @param entityObject
   * @throws IllegalAccessException
   */
  public void persist(Object entityObject) throws IllegalAccessException {
    Class entityClass = entityObject.getClass();
    String entityClassname = entityClass.getSimpleName().toLowerCase();
    String fieldNames = "";
    String fieldValues = "";

    for (Field field : entityClass.getDeclaredFields()) {
      if (field.isAnnotationPresent(Transient.class)) {
        continue;
      }
      if (field.isAnnotationPresent(Column.class) &&
          !field.getDeclaredAnnotation(Column.class).name().isEmpty()) {
        fieldNames += field.getDeclaredAnnotation(Column.class).name() + ", ";
      } else {
        fieldNames += field.getName().toLowerCase() + ", ";
      }
      field.setAccessible(true);
      String fieldValue = field.get(entityObject).toString();
      fieldValues += (isNumeric(field) ? fieldValue : String.format("'%s'", fieldValue)) + ", ";
    }

    String sqlString = String.format("INSERT INTO %s (%s) VALUES (%s)",
        entityClassname,
        fieldNames.substring(0, fieldNames.lastIndexOf(",")),
        fieldValues.substring(0, fieldValues.lastIndexOf(",")));
    System.out.println(sqlString);
    try {
      dbAccess.getStatement().executeUpdate(sqlString);
    } catch (SQLException e) {
      System.err.format("persists entity failed: '%s'\n", entityObject.getClass());
      System.out.println(e.toString());
    }
  }

  private boolean isNumeric(Field field) {
    return field.getType().equals(Integer.class)
        || field.getType().equals(Long.class)
        || field.getType().equals(Float.class)
        || field.getType().equals(Double.class);
  }

  /**
   * find object from database table by ist id
   *
   * @param primaryKey
   * @param entityClass
   * @return
   */
  public Object findById(Object primaryKey, Class entityClass)  {
    if(!entityClasses.contains(entityClass)) {
      throw new RuntimeException("This class is not supported");
    }
    // get primary key field
    Field primaryKeyField = Arrays.stream(entityClass.getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Id.class))
        .findFirst().get();

    // check if datatype of primary key matches
    boolean primaryKeyTypeMatch = primaryKeyField.getType().getClass().equals(primaryKey.getClass());
    if (primaryKeyTypeMatch) {
      throw new RuntimeException("primary key type does not match database primary key");
    }

    // setup sqlString for SELECT statement
    String sqlPrimaryKey = switch (primaryKey.getClass().getSimpleName()) {
      case "String" -> String.format("'%s'", primaryKey);
      case "LocalDate" -> String.format("TO_DATE('%s', 'YYYY-MM-DD'),", primaryKey);
      default -> String.format("%s", primaryKey);
    };

    ResultSet result;
    String sqlString = String.format("SELECT * FROM %s WHERE %s = %s", entityClass.getSimpleName().toLowerCase(),
        getNameOfField(primaryKeyField),
        sqlPrimaryKey);
    try {
      Statement statement = dbAccess.getStatement();
      result = statement.executeQuery(sqlString);

      if(!result.next()) {
        throw new RuntimeException("no object with this id");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    }

    // create object from entity class
    Object entityObject = null;
    try {
      Constructor constructor = entityClass.getConstructor(null);
      entityObject = constructor.newInstance(null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException("Object creation failed: " + e.getMessage());
    }

    // fill entity-object with data from database table
    for (Field field : entityClass.getDeclaredFields()) {
      if(field.isAnnotationPresent(Transient.class)) {
        continue;
      }
      try {
        field.setAccessible(true);
        Object resultSetValue = result.getObject(getNameOfField(field), field.getType());
        field.set(entityObject, resultSetValue);
      } catch (SQLException | IllegalAccessException e) {
        throw new RuntimeException("Setting values for entity object failed: " + e.getMessage());
      }
    }
    return entityObject;
  }

  private String getNameOfField(Field f) {
    if(!f.isAnnotationPresent(Column.class)) {
      return f.getName();
    }
    if(f.getAnnotation(Column.class).name().isEmpty()) {
      return f.getName();
    }
    return f.getAnnotation(Column.class).name();
  }

}
