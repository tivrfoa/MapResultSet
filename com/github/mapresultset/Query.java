package com.github.mapresultset;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Query {
}


/*

References:

https://reflectoring.io/java-annotation-processing/

https://stackoverflow.com/questions/3285652/how-can-i-create-an-annotation-processor-that-processes-a-local-variable

*/
