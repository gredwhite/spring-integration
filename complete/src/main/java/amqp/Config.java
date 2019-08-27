package amqp;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.messaging.MessageChannel;


@Configuration
@EnableIntegration
@IntegrationComponentScan
public class Config {

    @Autowired
    private AmqpTemplate amqpTemplate;

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
    public IntegrationFlow fromConsoleToRabbitFlow() {
        return IntegrationFlows.from(consoleSource(), c -> c.id("consoleInput")
                .poller(Pollers.fixedRate(1000).maxMessagesPerPoll(5))
                .autoStartup(true)
        ).channel("consoleOutputChannel")
                .handle(Amqp.outboundAdapter(amqpTemplate)
                        .exchangeNameFunction(message -> {
                            if ("nack".equals(message.getPayload())) {
                                return "bad_exchange";
                            } else {
                                return "console_exchange";
                            }
                        }).routingKeyFunction(message -> {
                            if ("fail".equals(message.getPayload())) {
                                return "bad_key";
                            } else {
                                return "console_queue";
                            }
                        })
                        .confirmCorrelationExpression("payload")
                        .confirmAckChannel(ackChannel())
                        .confirmNackChannel(nackChannel())
                        .returnChannel(returnChannel()))
                .get();
    }

    @Bean
    public MessageChannel ackChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow ackChannelListener() {
        return IntegrationFlows.from(ackChannel())
                .handle(m -> {
                    System.out.println("ACK:" + m);
                })
                .get();
    }

    @Bean
    public MessageChannel nackChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow nackChannelListener() {
        return IntegrationFlows.from(nackChannel())
                .handle(m -> {
                    System.out.println("NACK:" + m);
                }).get();
    }

    @Bean
    public MessageChannel returnChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow returnChannelListener() {
        return IntegrationFlows.from(returnChannel())
                .handle(m -> {
                    System.out.println("RETURN:" + m);
                }).get();
    }

    public MessageSource<String> consoleSource() {
        return CharacterStreamReadingMessageSource.stdin();
    }


    @Bean
    public IntegrationFlow fromRabbitToConsoleFlow() {
        return IntegrationFlows.from(Amqp.inboundGateway(rabbitConnectionFactory(), "console_queue"))
                .log()
                .transform(m -> " response: " + m)
                .handle(System.out::println)
                .get();
    }

}
