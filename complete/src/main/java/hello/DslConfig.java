package hello;

import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.feed.dsl.FeedEntryMessageSourceSpec;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class DslConfig {

    @Bean
    public IntegrationFlow feedFlow() throws MalformedURLException {
        return IntegrationFlows.from(inBoundFeedDataAdapter(), configurer -> {
            PollerSpec pollerMetadataSpec = Pollers.fixedDelay(1000);
            pollerMetadataSpec.maxMessagesPerPoll(100L);
            configurer.poller(pollerMetadataSpec);
        })
                .channel(newsChannel())
                .transform(source -> {
                    SyndEntry e = ((SyndEntry) source);
                    return e.getTitle() + " " + e.getLink();
                })
                .publishSubscribeChannel(s -> s
                        .applySequence(true)
                        .subscribe(f -> {
                            f.handle(messageHandler1());
                        })

                        .subscribe(f -> f.handle(messageHandler2()))
                ).get();
    }

    @Bean
    public FeedEntryMessageSourceSpec inBoundFeedDataAdapter() throws MalformedURLException {
        return Feed.inboundAdapter(new URL("https://spring.io/blog.atom"), "some_key");
    }

    @Bean
    public MessageChannel newsChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageHandler messageHandler1() {
        return x -> System.out.println("handler_1: " + x);
    }

    @Bean
    public MessageHandler messageHandler2() {
        return x -> System.out.println("handler_2: " + x);
    }
}
