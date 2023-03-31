package com.joutvhu.dynamic.r2dbc;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * Annotation to declare finder dynamic queries directly on repository methods.
 *
 * @author Giao Ho
 * @since 1.5.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface DynamicQuery {
    /**
     * Provides a query template method name, which is used to find external query templates.
     * The default is {@code entityName:methodName}, entityName is entity class name, methodName is query method name.
     *
     * @return the query template method name
     * @since x.x.8
     */
    String name() default "";

    /**
     * The SQL statement to execute when the annotated method gets invoked.
     */
    String value() default "";
}
