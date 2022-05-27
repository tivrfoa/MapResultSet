package com.github.mapresultset;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface Query {
}


/*

References:

https://reflectoring.io/java-annotation-processing/

*/
