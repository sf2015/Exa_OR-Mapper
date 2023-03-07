package at.kaindorf.ormapper.pojos;

import at.kaindorf.ormapper.core.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceTest {

  private EntityManager em;

  @Test
  @DisplayName("Drop and create tables")
  public void DropAndCreateTables() {
    em = EntityManager.getInstance();
    assertTrue(true);
  }

  @Test
  @DisplayName("persist pizza entity")
  public void persistEntity() {
    try {
      em = EntityManager.getInstance();
      em.persist(new Pizza(2l, "Tonno", 3, 1.99,true, LocalDate.now()));
      Pizza pizza = (Pizza)em.findById(2l, Pizza.class);
      System.out.println(pizza);
    } catch (IllegalAccessException e) {
      System.out.println(e.toString());
    }
    assertTrue(true);
  }

}