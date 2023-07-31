package com.dxc.learncamelspring;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootApplication
@MockEndpoints("direct:greeting")
class CamelTest {

    @Autowired
    private ProducerTemplate template;

    @EndpointInject("mock:direct:greeting")
    private MockEndpoint mock;


    @Test
    public void shouldSendMessageWithSuccess() throws InterruptedException {
//        MockEndpoint mock = getMockEndpoint("direct:greeting");
//        mock.expectedMessageCount(2);

        System.out.println("Sending 1");
        template.sendBody("direct:greeting", "Team");
        mock.expectedBodiesReceived("I like to working with Teams");


        System.out.println("Sending 2");
        template.sendBody("direct:greeting", "Me");
        mock.expectedBodiesReceived("Solo fighter");

        mock.assertIsSatisfied();
    }
}