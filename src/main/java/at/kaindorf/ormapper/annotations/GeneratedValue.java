package at.kaindorf.ormapper.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Project: Exa_OR-Mapper
 * Created by: SF
 * Date: 10.02.2023
 * Time: 09:17
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {
}
