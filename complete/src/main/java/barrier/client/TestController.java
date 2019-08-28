package barrier.client;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @PostMapping("/my_post")
    public String test(@RequestBody String arg) {
        return "response " + arg;
    }
}
