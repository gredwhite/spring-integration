package barrier.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.http.dsl.HttpMessageHandlerSpec;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;

import java.util.function.Consumer;


@Configuration
@EnableIntegration
@IntegrationComponentScan
public class Config {

    @Bean
    public IntegrationFlow integrationFlow() {
        return IntegrationFlows.from(consoleSource(), consoleConsumer())
                .handle(httpOutboundGateway())
                .log()
                .channel("httpRequestChannel")
                .handle(s -> {
                    System.out.println("We got response: " + s);
                })
                .get();
    }

    private HttpMessageHandlerSpec httpOutboundGateway() {
        return Http.outboundGateway("http://localhost:8080/spring_integration_post") //http://localhost:8080/my_post
                .httpMethod(HttpMethod.POST)
                .expectedResponseType(String.class);
    }

    private Consumer<SourcePollingChannelAdapterSpec> consoleConsumer() {
        return c -> c.poller(Pollers.fixedRate(1000)
                .maxMessagesPerPoll(1));
    }

    public MessageSource<String> consoleSource() {
        return CharacterStreamReadingMessageSource.stdin();
    }
}
