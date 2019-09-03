package enricher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplication(MyApplication.class).run(args);
        UserService userService = ctx.getBean(UserService.class);
        User user = new User("some_name", null, null);
        System.out.println("Main:" + userService.findUser(user));
        System.out.println("Main:" + userService.findUserByUsername(user));
        Map<String, Object> map = new HashMap<>();
        map.put("username", "vasya");
        System.out.println("Main:" + userService.findUserWithUsernameInMap(map));
    }
}
