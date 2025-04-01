package kr.masul.security;

import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base-url}/users")
@RequiredArgsConstructor
public class AuthController {

   private final AuthService authService;
   private Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

   @PostMapping("/login")
   public Result getLoginInfo(Authentication authentication) {
      LOGGER.info("Authentication get name : {}", authentication.getName());
      return new Result(true, StatusCode.SUCCESS, "username, password get jwt.",
              authService.createLoginInfo(authentication));
   }
}
