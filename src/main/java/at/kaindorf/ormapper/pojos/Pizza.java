package at.kaindorf.ormapper.pojos;

import at.kaindorf.ormapper.annotations.Column;
import at.kaindorf.ormapper.annotations.Entity;
import at.kaindorf.ormapper.annotations.Id;
import at.kaindorf.ormapper.core.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 08.02.2023
 * Time: 10:26
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Pizza {
  @Id
  @Column(name = "pizza_id")
  private Long pizzaId;
  @Column(length = 100, nullable = false, unique = true)
  private String name;
  @Column(nullable = false)
  private Integer size;
  private Double price;
  @Column(name = "is_available")
  private Boolean isAvailable;
  private LocalDate delivery;

}

