package microservices.book.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
class GatewayApplicationTests {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private GlobalCorsProperties globalCorsProperties;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("loads configured gateway routes")
    void loadsConfiguredRoutes() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();

        assertThat(routes).isNotNull();
        assertThat(routes)
                .extracting(RouteDefinition::getId)
                .containsExactlyInAnyOrder("multiplication", "gamification");
        assertThat(routes)
                .extracting(RouteDefinition::getUri)
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("lb://multiplication/", "lb://gamification/");
    }

    @Test
    @DisplayName("binds default retry filter settings")
    void bindsRetryFilterSettings() {
        assertThat(environment.getProperty("spring.cloud.gateway.server.webflux.default-filters[0].name"))
                .isEqualTo("Retry");
        assertThat(environment.getProperty("spring.cloud.gateway.server.webflux.default-filters[0].args.retries"))
                .isEqualTo("3");
        assertThat(environment.getProperty("spring.cloud.gateway.server.webflux.default-filters[0].args.methods"))
                .isEqualTo("GET,POST");
    }

    @Test
    @DisplayName("binds default CORS configuration for frontend origin")
    void bindsCorsConfiguration() {
        var corsConfiguration = globalCorsProperties.getCorsConfigurations().get("/**");

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.getAllowedOrigins()).containsExactly("http://localhost:3000");
        assertThat(corsConfiguration.getAllowedMethods()).containsExactly("GET", "POST", "OPTIONS");
        assertThat(corsConfiguration.getAllowedHeaders()).containsExactly("*");
    }

}
