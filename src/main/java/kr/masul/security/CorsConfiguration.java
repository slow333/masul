package kr.masul.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration{

// implements WebMvcConfigurer 적용할 때 사용 방법
//   @Override
//   public void addCorsMappings(CorsRegistry registry) {
//      registry.addMapping("/**");
//   }
   
    @Bean
    public WebMvcConfigurer corsConfigurer() {
       return new WebMvcConfigurer() {
          public void addCorsMappings(CorsRegistry registry) {
             registry.addMapping("/**");
             //                    .allowedOrigins("*")
             //                    .allowedMethods("GET","POST","PUT","DELETE","OPTIONS","HEAD","PATCH")
             //                    .allowedHeaders("*");
          }
       };
    }
 }
 
