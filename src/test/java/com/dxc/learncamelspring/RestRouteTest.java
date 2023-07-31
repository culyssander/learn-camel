package com.dxc.learncamelspring;

import lombok.Data;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.spi.RestApiConsumerFactory;
import org.apache.camel.spi.RestConfiguration;
import org.apache.camel.spi.RestConsumerFactory;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.support.ClassicUuidGenerator;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@EnableAutoConfiguration
@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                RestRouteTest.class,
                RestRouteTest.Config.class
        },
        properties = {
                "debug=false",
                "camel.springboot.xml-rests=false",
                "camel.springboot.xml-routes=false",
                "camel.rest.enabled=true",
                "camel.rest.component=dummy-rest",
                "camel.rest.host=localhost",
                "camel.rest.data-format-property.prettyPrint=true"
//                "camel.rest.data-format-pro",

        }
)
class RestRouteTest  {

    @Autowired
    private CamelContext context;

    @Test
    public void test() {
        ProducerTemplate template = context.createProducerTemplate();
        System.out.println(context.getEndpoints());
        String result = template.requestBody("direct:get-say-hello", "test",String.class);
        System.out.println("RESULT: " + result);
        Assertions.assertEquals("Hello World", result);
    }

    @Configuration
    static class Config {
        @Bean(name = "dummy-rest")
        public RestConsumerFactory dummyRestConsumerFactory() {
            return new TestConsumerFactory();
        }

        @Bean
        public RouteBuilder routeBuilder() {
            return new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    rest("/say/hello")
                            .get().to("direct:hello");

                    from("direct:hello")
                            .transform().constant("Hello World");
                }
            };
        }
    }

    @Data
    private static final class TestConsumerFactory implements RestConsumerFactory, RestApiConsumerFactory {

        private Object dummy;
        @Override
        public Consumer createApiConsumer(CamelContext camelContext, Processor processor, String contextPath, RestConfiguration configuration, Map<String, Object> parameters) throws Exception {
            String id = ClassicUuidGenerator.generateSanitizedId(contextPath);

            if (id.startsWith("-")) {
                id = id.substring(1);
            }

            DirectEndpoint direct = camelContext.getEndpoint("direct:api:"+"-"+id, DirectEndpoint.class);
            return direct.createConsumer(processor);
        }

        @Override
        public Consumer createConsumer(CamelContext camelContext, Processor processor, String verb, String basePath, String uriTemplate, String consumes, String produces, RestConfiguration configuration, Map<String, Object> parameters) throws Exception {
            String id;

            if (uriTemplate != null) {
                id = ClassicUuidGenerator.generateSanitizedId(basePath + uriTemplate);
            } else {
                id = ClassicUuidGenerator.generateSanitizedId(basePath);
            }

            if (id.startsWith("-")) {
                id = id.substring(1);
            }

            if (configuration.getConsumerProperties() != null) {
                String ref = (String) configuration.getConsumerProperties().get("dummy");

                if (ref != null) {
                    dummy = CamelContextHelper.mandatoryLookup(camelContext, ref.substring(1));
                }
            }

            DirectEndpoint direct = camelContext.getEndpoint("direct:" + verb + "-" + id, DirectEndpoint.class);
            return direct.createConsumer(processor);
        }
    }
}

