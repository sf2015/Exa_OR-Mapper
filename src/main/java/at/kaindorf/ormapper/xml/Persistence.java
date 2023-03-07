package at.kaindorf.ormapper.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 09.02.2023
 * Time: 20:09
 */
@XmlRootElement(name = "persistence-unit")
//@XmlAccessorType(XmlAccessType.PROPERTY)    // not required because default
public class Persistence {

  private Map<String, String> properties = new HashMap<>();

  @XmlElementWrapper(name = "properties")
  @XmlElement(name = "property")
  public MapEntry[] getProperties() {
    MapEntry[] mapEntries = new MapEntry[properties.size()];
    int index = 0;
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      mapEntries[index++] = new MapEntry(entry.getKey(), entry.getValue());
    }
    return mapEntries;
  }

  public void setProperties(MapEntry[] entryArray) {
    for (MapEntry entry : entryArray) {
      this.properties.put(entry.key, entry.value);
    }
  }

  @NoArgsConstructor
  @AllArgsConstructor
  static class MapEntry {
    @XmlAttribute
    private String key;
    @XmlAttribute
    private String value;
  }

  public Map<String,String> getDatabaseProperties() throws IOException {
    return properties;
  }
}

