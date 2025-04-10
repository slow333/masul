package kr.masul.user;

import jakarta.validation.Valid;
import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import kr.masul.system.converter.UserToDto;
import kr.masul.system.converter.UserToEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.base-url}/users")
public class UserController {

   private final UserService userService;
   private final UserToDto userToDto;
   private final UserToEntity userToEntity;

   @GetMapping("/{userId}")
   public Result findById(@PathVariable Integer userId) {
      MaUser user = userService.findById(userId);
      UserDto dto = userToDto.convert(user);
      return new Result(true, StatusCode.SUCCESS, "Find Success", dto);
   }

   @GetMapping
   public Result findAll() {
      List<MaUser> users = userService.findAll();
      List<UserDto> dtos = users.stream().map(userToDto::convert).toList();
      return new Result(true, StatusCode.SUCCESS, "Find all Success", dtos);
   }

   @PostMapping
   public Result add(@Valid @RequestBody MaUser maUser){
      MaUser addedUser = userService.add(maUser);
      UserDto dto = userToDto.convert(addedUser);
      return new Result(true, StatusCode.SUCCESS, "Add Success", dto);
   }

   @PutMapping("/{userId}")
   public Result update(@PathVariable Integer userId,@Valid @RequestBody UserDto userDto) {
      MaUser user = userToEntity.convert(userDto);
      MaUser update = userService.update(userId, user);
      UserDto dto = userToDto.convert(update);
      return new Result(true, StatusCode.SUCCESS, "Update Success", dto);
   }

   @DeleteMapping("/{userId}")
   public Result delete(@PathVariable Integer userId) {
      userService.delete(userId);
      return new Result(true, StatusCode.SUCCESS, "Delete Success");
   }

   @PatchMapping("/{userId}/password")
   public Result changePassword(@PathVariable Integer userId,
                                @RequestBody Map<String, String> requestMap) {
      String oldPassword = requestMap.get("oldPassword");
      String newPassword = requestMap.get("newPassword");
      String confirmNewPassword = requestMap.get("confirmNewPassword");
      userService.changePassword(userId, oldPassword, newPassword, confirmNewPassword);
      return new Result(true, StatusCode.SUCCESS, "Change Password Success");
   }
}
