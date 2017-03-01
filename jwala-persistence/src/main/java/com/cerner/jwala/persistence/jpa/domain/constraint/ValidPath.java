package com.cerner.jwala.persistence.jpa.domain.constraint;

import com.cerner.jwala.persistence.jpa.domain.constraint.validator.PathValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint validator that validates a path
 *
 * Created by JC043760 on 12/13/2016
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = PathValidator.class)
@Documented
public @interface ValidPath {

    String message() default "the path is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String [] allowableFileExtensions() default {};

    boolean checkIfExists() default false;

    /**
     * Defines several {@link ValidPath} annotations on the same element.
     *
     * @see ValidPath
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        ValidPath[] value();
    }

}
