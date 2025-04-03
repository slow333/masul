package kr.masul.system.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UsableDiskHealthIndicator implements HealthIndicator {
   @Override
   public Health health() {
      File path = new File(".");
      long freeSpace = path.getUsableSpace();
      boolean isOk = freeSpace > 100*1024*1024;
      Status status = isOk ? Status.UP: Status.DOWN;
      return Health
              .status(status)
              .withDetail("usable disk", freeSpace)
              .withDetail("threshold", 10*1024*1024)
              .build();
   }
}
