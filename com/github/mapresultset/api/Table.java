package com.github.mapresultset.api;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Table {
	String name() default "";
}


/*

References:

https://reflectoring.io/java-annotation-processing/

https://www.oracle.com/technical-resources/articles/hunter-meta1.html

*/
