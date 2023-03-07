package at.kaindorf.ormapper.io;
import at.kaindorf.ormapper.annotations.Entity;
import at.kaindorf.ormapper.xml.Persistence;
import jakarta.xml.bind.JAXB;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 08.02.2023
 * Time: 08:33
 */
public class IO_Access {
  public static List<Class> findEntityClassesInProject() throws IOException {
    Path projectPath = Path.of(System.getProperty("user.dir"),"src","main","java");
    return Files.walk(projectPath)
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".java"))
        .map(p -> projectPath.relativize(p))
        .map(p -> p.toString().replace(File.separatorChar,'.').substring(0,p.toString().lastIndexOf(".")))
        .map(IO_Access::getClass)
        .filter(c -> c.isAnnotationPresent(Entity.class))
        .toList();
  }

  public static Class getClass(String s) {
    try {
      return Class.forName(s);
    } catch (ClassNotFoundException e) {
      return Object.class;
    }
  }

  public static Map<String, String> getDatabaseProperties() {
    InputStream is = IO_Access.class.getResourceAsStream("/persistence.xml");
    Persistence pu = JAXB.unmarshal(is, Persistence.class);
    try {
      return pu.getDatabaseProperties();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
