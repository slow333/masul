package kr.masul.system.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice extends RuntimeException{

   @ExceptionHandler(ObjectNotFoundException.class)
   @ResponseStatus(HttpStatus.NOT_FOUND)
   public Result artifactNotFoundExceptionHandler(ObjectNotFoundException ex) {
      return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
   }

   // rest client 관련 애러 처리(OpenAiChatClient에서 발생)
   @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
   public ResponseEntity<Result> noHandlerFoundExceptionHandler(HttpStatusCodeException ex) throws JsonProcessingException { // 일반적인 http error 처리

      // 애러 메세지 포멧 정리하기
      String exceptionMessage = ex.getMessage();

      // Replace <EOL> with actual newlines.
      exceptionMessage = exceptionMessage.replace("<EOL>", "\n");
      // Extract the JSON part from the string.
      String jsonPart = exceptionMessage.substring(exceptionMessage.indexOf("{"), exceptionMessage.lastIndexOf("}") + 1);
      // Create an ObjectMapper instance.
      ObjectMapper mapper = new ObjectMapper();
      // Parse the JSON string to a JsonNode.
      JsonNode rootNode = mapper.readTree(jsonPart);
      // Extract the message.
      String formattedExceptionMessage = rootNode.path("error").path("message").asText();


      return new ResponseEntity<>(
              new Result(false, ex.getStatusCode().value(),
                      "OpenAi occur unauthorized, etc.", formattedExceptionMessage)
              ,ex.getStatusCode());
   }

   @ExceptionHandler(RestClientException.class)
   @ResponseStatus(HttpStatus.NOT_FOUND)
   public Result restClientExceptionHandler(RestClientException ex) {
      return new Result(false, StatusCode.NOT_FOUND,"The API endpoint not found", ex.getMessage());
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

   @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
   @ResponseStatus(HttpStatus.UNAUTHORIZED)
   public Result usernameNotFoundExceptionHandler(Exception ex) {
      return new Result(false, StatusCode.UNAUTHORIZED,
              "username or password is incorrect",ex.getMessage());
   }

   @ExceptionHandler(InsufficientAuthenticationException.class)
   @ResponseStatus(HttpStatus.UNAUTHORIZED)
   public Result insufficientAuthenticationExceptionHandler(InsufficientAuthenticationException ex) {
      return new Result(false, StatusCode.UNAUTHORIZED,
              "username or password is missing",ex.getMessage());
   }

   @ExceptionHandler(AccountStatusException.class)
   @ResponseStatus(HttpStatus.UNAUTHORIZED)
   public Result accountStatusExceptionExceptionHandler(AccountStatusException ex) {
      return new Result(false, StatusCode.UNAUTHORIZED,
              "user is abnormal",ex.getMessage());
   }

   @ExceptionHandler(InvalidBearerTokenException.class)
   @ResponseStatus(HttpStatus.UNAUTHORIZED)
   public Result invalidBearerTokenExceptionExceptionHandler(InvalidBearerTokenException ex) {
      return new Result(false, StatusCode.UNAUTHORIZED,
              "The access token provide is not normal",ex.getMessage());
   }

   @ExceptionHandler(AccessDeniedException.class)
   @ResponseStatus(HttpStatus.FORBIDDEN)
   public Result accessDeniedExceptionExceptionHandler(AccessDeniedException ex) {
      return new Result(false, StatusCode.FORBIDDEN,
              "No Permission",ex.getMessage());
   }

   @ExceptionHandler(CustomBlobStorageException.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public Result customBlobStorageExceptionHandler(CustomBlobStorageException ex){
      return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCause().getMessage());
   }

   @ExceptionHandler(PasswordChangeIllegalArgumentException.class)
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   public Result passwordChangeIllegalArgumentExceptionHandler(PasswordChangeIllegalArgumentException ex){
      return new Result(false, StatusCode.BAD_REQUEST, ex.getMessage());
   }


   @ExceptionHandler(Exception.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public Result otherExceptionHandler(Exception ex) {
         return new Result(false, StatusCode.INTERNAL_SERVER_ERROR,"A server internal error occurs.", ex.getMessage());
   }

}
