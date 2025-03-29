package kr.masul.system.exception;

import org.springframework.stereotype.Component;

public class ObjectNotFoundException extends RuntimeException{

   public ObjectNotFoundException(String name, String id) {
      super("Could not find " + name + " with id " + id);
   }
   public ObjectNotFoundException(String name, Integer id) {
      super("Could not find " + name + " with id " + id);
   }
}
