package control_bus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class Config {

    @Bean
    public IntegrationFlow controlBusFlow() {
        return IntegrationFlows.from("operationChannel")
                .controlBus()
                .get();
    }

    @Bean
    public IntegrationFlow mainFlow() {
        return IntegrationFlows.from(inboundAdapter(), c -> c.id("myInboundAdapter")
                .autoStartup(false)
                .poller(Pollers.fixedRate(1000))
        ).channel("adapterOutputChanel")
                .get();
    }

    @Bean
    public AbstractMessageChannel adapterOutputChanel() {
        return new QueueChannel();
    }

    public MessageSource inboundAdapter() {
        return new MyMessageSource();
    }


    public static class MyMessageSource implements MessageSource<String> {
        @Override
        public Message receive() {
            return new Message() {
                @Override
                public String getPayload() {
                    return "some_output_message";
                }

                @Override
                public MessageHeaders getHeaders() {
                    return new MessageHeaders(new HashMap());
                }

                @Override
                public String toString() {
                    return getPayload() + ", " + getHeaders();
                }
            };
        }
    }
}
