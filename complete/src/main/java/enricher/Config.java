package enricher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.Message;

import java.util.Map;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class Config {

    @Bean
    public SystemService systemService() {
        return new SystemService();
    }

    //first flow
    @Bean
    public IntegrationFlow findUserEnricherFlow() {
        return IntegrationFlows.from("findUserEnricherChannel")
                .enrich(enricherSpec ->
                        enricherSpec.requestChannel("findUserServiceChannel")
                                .<User>propertyFunction("email", (message) ->
                                        (message.getPayload()).getEmail()
                                ).<User>propertyFunction("password", (message) ->
                                (message.getPayload()).getPassword()
                        ))
                .get();
    }

    @Bean
    public IntegrationFlow findUserServiceFlow(SystemService systemService) {
        return IntegrationFlows.
                from("findUserServiceChannel")
                .<User>handle((p, h) -> systemService.findUser(p))
                .get();
    }

    //second flow
    @Bean
    public IntegrationFlow findUserByUsernameEnricherFlow() {
        return IntegrationFlows.from("findUserByUsernameEnricherChannel")
                .enrich(enricherSpec ->
                        enricherSpec.requestChannel("findUserByUsernameRequestChannel")
                                .<User>requestPayload(userMessage -> userMessage.getPayload().getUsername())
                                .<User>propertyFunction("email", (message) ->
                                        (message.getPayload()).getEmail()
                                ).<User>propertyFunction("password", (message) ->
                                (message.getPayload()).getPassword()
                        ))
                .get();

    }

    @Bean
    public IntegrationFlow findUserByUsernameServiceFlow(SystemService systemService) {
        return IntegrationFlows.from("findUserByUsernameRequestChannel")
                .<String>handle((p, h) -> systemService.findUserByUsername(p))
                .get();
    }

    //third flow
    @Bean
    public IntegrationFlow findUserWithUsernameInMapEnricherFlow() {
        return IntegrationFlows.from("findUserWithMapEnricherChannel")
                .enrich(enricherSpec ->
                        enricherSpec.requestChannel("findUserWithMapRequestChannel")
                                .<Map<String, User>>requestPayload(userMessage -> userMessage.getPayload().get("username"))
                                .<User>propertyFunction("user", Message::getPayload)
                ).get();
    }

    @Bean
    public IntegrationFlow findUserWithUsernameInMapServiceFlow(SystemService systemService) {
        return IntegrationFlows.from("findUserWithMapRequestChannel")
                .<String>handle((p, h) -> systemService.findUserByUsername(p))
                .get();
    }
}
