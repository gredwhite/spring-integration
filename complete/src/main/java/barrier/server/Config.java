package barrier.server;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.Message;


@Configuration
@EnableIntegration
public class Config {

    @Bean
    public IntegrationFlow integrationFlow() {
        return IntegrationFlows.from(Http.inboundGateway("/spring_integration_post")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .requestPayloadType(String.class))
                .log()
                .enrich(enricherSpec -> {
                    enricherSpec.headerFunction("ackCorrelationId", message -> message.getHeaders().getId()); //ackCorrelationId
                })
                .split(s -> s.applySequence(true).get().getT2().setDelimiters(","))
                .log()
                .publishSubscribeChannel(publishSubscribeSpec -> {
                            //publishSubscribeSpec.applySequence(true);
                            publishSubscribeSpec.subscribe(f -> Amqp.outboundAdapter(amqpTemplate())
                                    .exchangeName("barrierExchange")
                                    .routingKey("barrierKey")
                                    .confirmAckChannel(confirmAckChannel())
                                    .confirmCorrelationFunction(Message::getPayload));
                            publishSubscribeSpec.subscribe(flow -> {
                                flow.handle((p, h) -> "from server: " + p);
                            });
                        }
                )
                .handle((payload, headers) -> {
                    System.out.println("Before aggregation");
                    return true;
                })
                .aggregate()
                .handle((payload, headers) -> {
                    System.out.println("After aggregation");
                    return true;
                })
                .get();
    }


    @Bean
    public CachingConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost("localhost");
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setPublisherConfirms(true);
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }

    @Bean
    public AmqpTemplate amqpTemplate() {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactory());
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public DirectChannel confirmAckChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow ackChannelListener() {
        return IntegrationFlows.from(confirmAckChannel())
                .handle(m -> {
                    System.out.println("ACK:" + m);
                })
                .get();
    }

}

