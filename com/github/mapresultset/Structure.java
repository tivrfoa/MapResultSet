package com.github.mapresultset;

import java.util.List;

/**
 * 
 */
public class Structure {
   public static enum Type {
       CLASS,
       RECORD,
   }

   String fullName;
   Type type;
   List<Field> fields;
}
