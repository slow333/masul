package kr.masul.system.actuator;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * httpexchanges 를 위해서 필요한 설정
 */

@Configuration
public class ActuatorConfiguration {

   @Bean
   public HttpExchangeRepository httpExchangeRepository() {
      InMemoryHttpExchangeRepository repository = new InMemoryHttpExchangeRepository();
      repository.setCapacity(1000);
      return repository;
   }
}
