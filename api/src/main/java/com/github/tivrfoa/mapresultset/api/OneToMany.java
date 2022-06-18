package com.github.tivrfoa.mapresultset.api;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface OneToMany {

    String[] createWith() default {};

    String[] addWith() default {};
}