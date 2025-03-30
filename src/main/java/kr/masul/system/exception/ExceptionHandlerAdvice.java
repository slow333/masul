package kr.masul.system.exception;


import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.ReflectionUtils.getField;

@RestControllerAdvice
public class ExceptionHandlerAdvice extends RuntimeException{

   @ExceptionHandler(ObjectNotFoundException.class)
   @ResponseStatus(HttpStatus.NOT_FOUND)
   public Result artifactNotFoundExceptionHandler(ObjectNotFoundException ex) {
      return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
   }

   @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
   @ResponseStatus(HttpStatus.UNAUTHORIZED)
   public Result usernameNotFoundExceptionHandler(Exception ex) {
      return new Result(false, StatusCode.UNAUTHORIZED,
              "username or password is incorrect",ex.getMessage());
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   public Result methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
      List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
      Map<String, String> errorMap = new HashMap<>();
      allErrors.forEach(error -> {
         String key = ((FieldError) error).getField();
         String value = error.getDefaultMessage();
         errorMap.put(key, value);
      });
      return new Result(false, StatusCode.BAD_REQUEST, "입력 값 중 빠진게 있습니다.(모두 입력하세요)", errorMap);
   }

   @ExceptionHandler(Exception.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public Result otherExceptionHandler(Exception ex) {
         return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, ex.getMessage());
   }

}
