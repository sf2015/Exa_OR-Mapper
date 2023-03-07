package at.kaindorf.ormapper.core;

import at.kaindorf.ormapper.io.IO_Access;
import lombok.Getter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 10.02.2023
 * Time: 08:40
 */
@Getter
public class DB_Access {
  private static DB_Access theInstance = null;
  private Map<String, String> databaseProperties = null;
  private Connection connection;

  public static DB_Access getInstance() {
    if (theInstance == null) {
      theInstance = new DB_Access();
    }
    return theInstance;
  }

  private DB_Access() {
    try {
      setDatabaseProperties();
      Class.forName(db_driver);
      connect();
    } catch (ClassNotFoundException e) {
      System.out.println("Loading database driver failed");
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.out.println("Loading persistence-unit failed: " + e.toString());
      throw new RuntimeException(e);
    } catch (SQLException e) {
      System.out.println("Connection to database " + db_url + " failed failed");
      throw new RuntimeException(e);
    } catch (RuntimeException e) {
      System.out.println("Loading persistence-unit failed: " + e.toString());
      throw new RuntimeException(e);
    }
  }

  public void connect() throws SQLException {
    connection = DriverManager.getConnection(db_url,db_username, db_password);
    System.out.println("Connected to " + db_url);
  }

  public Statement getStatement() throws SQLException {
    return connection.createStatement();
  }

  private void setDatabaseProperties() throws IOException {
    databaseProperties = IO_Access.getDatabaseProperties();
    db_driver = databaseProperties.get("driver");
    db_url = databaseProperties.get("url");
    db_username = databaseProperties.get("username");
    db_password = databaseProperties.get("password");
    db_generation_schema = databaseProperties.get("generation_schema");
    if (db_driver == null || db_driver.isBlank() ||
        db_url == null || db_url.isBlank() ||
        db_username == null || db_username.isBlank() ||
        db_password == null || db_password.isBlank()) {
      throw new RuntimeException("Insufficient database properties in persistence.xml");
    }
  }

  private String db_driver;
  private String db_url;
  private String db_username;
  private String db_password;

  private String db_generation_schema = "none";

}
