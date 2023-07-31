package com.dxc.learncamelspring;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RabbitMQRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
//        from("rabbitmq:amq.direct?queue=test.queue&autoDelete=false&declare=false")
//                .log("Before enrichment: ${body}")
//                .unmarshal().json(JsonLibrary.Jackson, MyBean.class)
//                .process(this::enrichBeanDto)
//                .log("After enrichment: ${body}");

        from("direct:send-to-queue")
                .routeId("direct-route-id-send-to-queue")
                .log("LOG: ${body}")
                .marshal().json(JsonLibrary.Jackson, MyBean.class)
                .log("LOG AFTER: ${body}")
                .to("rabbitmq:amq.direct?queue=test&autoDelete=false")
                .id("id-rabbitmq")
                .log("DDDDDDDDDDDDD ${body}")
                .unmarshal().json(JsonLibrary.Jackson, MyBean.class)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }


    public void enrichBeanDto(Exchange exchange) {
        MyBean bean = exchange.getMessage().getBody(MyBean.class);
        bean.setName(LocalDateTime.now().toString());
    }
}
