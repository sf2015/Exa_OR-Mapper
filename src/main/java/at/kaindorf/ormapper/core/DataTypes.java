package at.kaindorf.ormapper.core;

import java.util.Map;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 10.02.2023
 * Time: 08:03
 */
public class DataTypes {
  public static final Map<String,String> SQL_TYPES = Map.ofEntries(
      Map.entry("int","INTEGER"),
      Map.entry("java.lang.Integer","INTEGER"),
      Map.entry("long","BIGINT"),
      Map.entry("java.lang.Long","BIGINT"),
      Map.entry("float","REAL"),
      Map.entry("java.lang.FLOAT","REAL"),
      Map.entry("double","DOUBLE PRECISION"),
      Map.entry("java.lang.Double","DOUBLE PRECISION"),
      Map.entry("boolean","BOOLEAN"),
      Map.entry("java.lang.Boolean","BOOLEAN"),
      Map.entry("java.lang.String","VARCHAR(255)"),
      Map.entry("java.time.LocalTime","TIME"),
      Map.entry("java.time.LocalDate","DATE"),
      Map.entry("java.time.LocalDateTime","TIMESTAMP")
  );
}
