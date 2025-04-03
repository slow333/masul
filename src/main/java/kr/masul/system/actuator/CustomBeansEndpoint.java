package kr.masul.system.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 사용자 정의 endpoint 만들기
 */
@Component
@Endpoint(id = "custom-beans-count")
public class CustomBeansEndpoint {

   private final ApplicationContext applicationContext;

   public CustomBeansEndpoint(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
   }

   @ReadOperation
   public int beanCount() {
      return applicationContext.getBeanDefinitionCount();
   }
}
