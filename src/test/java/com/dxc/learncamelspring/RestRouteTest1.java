package com.dxc.learncamelspring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.camel.spi.RestApiConsumerFactory;
import org.apache.camel.spi.RestConfiguration;
import org.apache.camel.spi.RestConsumerFactory;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.support.ClassicUuidGenerator;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

@EnableAutoConfiguration
@DirtiesContext
@CamelSpringBootTest
@SpringBootTest(
        classes = {
                RestRouteTest1.class,
                RestRouteTest1.Config.class
        }
//        properties = {
//                "debug=false",
//                "camel.springboot.xml-rests=false",
//                "camel.springboot.xml-routes=false",
//                "camel.rest.enabled=true",
//                "camel.rest.component=dummy-rest",
//                "camel.rest.host=localhost",
//                "camel.rest.data-format-property.prettyPrint=true"
////                "camel.rest.data-format-pro",
//
//        }
)
class RestRouteTest1 {

    @Autowired
    private CamelContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test() throws Exception {
        AdviceWith.adviceWith(context, "direct-route-id-send-to-queue", false, route -> {
           route.weaveById("id-rabbitmq")
                   .replace()
                   .log("${body}");
        });

        MyBean bean = new MyBean();
        bean.setName("000000000000A");
        String json = objectMapper.writer().writeValueAsString(bean);
        ProducerTemplate template = context.createProducerTemplate();
        System.out.println(context.getEndpoints());
        System.out.println(json);
        String result = template.requestBody("direct:post-api-bean", json, String.class);
        System.out.println("RESULT: " + result);

    }

    @Configuration
    static class Config {
        @Bean(name = "servlet")
        public RestConsumerFactory dummyRestConsumerFactory() {
            return new TestConsumerFactory();
        }

        @Bean
        public RouteBuilder routeRest() {
            return new RestRoute();
        }

        @Bean
        public RouteBuilder routeRabbitMQ() {
            return new RabbitMQRoute();
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

