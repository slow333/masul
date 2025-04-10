package kr.masul.system;

import kr.masul.security.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


// 이걸 적용해야 controller로 드나드난 패킷에 대해 갭쳐해서 적용됨(실재로 적용됨_)
@Configuration
public class WebConfig implements WebMvcConfigurer {

   private final JwtInterceptor jwtInterceptor;

   public WebConfig(JwtInterceptor jwtInterceptor) {
      this.jwtInterceptor = jwtInterceptor;
   }

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(jwtInterceptor).addPathPatterns("/**");
   }

   // cors를 여기에 적용해도 되나 이름을 갖기 위해 별도 파일로 정의함
//   @Override
//   public void addCorsMappings(CorsRegistry registry) {
//      registry.addMapping("/**");
//   }
}
