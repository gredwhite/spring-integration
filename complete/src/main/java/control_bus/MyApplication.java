package control_bus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplication(MyApplication.class).run(args);

        MessageChannel controlChannel = ctx.getBean("operationChannel", MessageChannel.class);
        PollableChannel adapterOutputChanel = ctx.getBean("adapterOutputChanel", PollableChannel.class);

        System.out.println("Before start:" + adapterOutputChanel.receive(1000));

        controlChannel.send(new GenericMessage<String>("@myInboundAdapter.start()"));
        System.out.println("After start:" + adapterOutputChanel.receive(1000));

        controlChannel.send(new GenericMessage<String>("@myInboundAdapter.stop()"));
        System.out.println("After stop:" + adapterOutputChanel.receive(1000));


    }
}
