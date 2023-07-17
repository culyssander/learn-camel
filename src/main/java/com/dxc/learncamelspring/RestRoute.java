package com.dxc.learncamelspring;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.DefaultMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RestRoute extends RouteBuilder {

    @Value("${claro.api.path}")
    String contextPath;

    @Override
    public void configure() throws Exception {

        onException(CustomException.class)
                .handled(true)
                .bean(PrepareErrorResponse.class)
                .log("Error response processed");

        restConfiguration()
                .contextPath(contextPath)
                .port(8080)
                .enableCORS(true)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Test REST API")
                .apiProperty("api.version", "v1")
                .apiContextRouteId("doc-api")
                .component("servlet")
                .bindingMode(RestBindingMode.json);

        rest("/api/")
                .id("api-route")
                .consumes("application/json")
                .post("/bean")
                .bindingMode(RestBindingMode.json)
                .type(MyBean.class)
                .to("direct:remoteService");

        from("direct:remoteService")
                .routeId("direct-route")
                .tracing()
                .log(">>> ${body.id}")
                .log(">>> ${body.name}")
                .choice()
                .when(ValidacaoDoValorDeEntrada::test)
                .to("direct:handle-exception")
                .otherwise()
                .process(e -> {
                    MyBean bean = e.getMessage().getBody(MyBean.class);
                    bean.setId(UUID.randomUUID().toString());
                    Message message = new DefaultMessage(e);
                    message.setBody(bean);
                    e.setMessage(message);
                })
                .to("direct:send-to-queue?exchangePattern=InOnly");

        from("direct:handle-exception")
                .routeId("direct-route-handle-exception")
                .process(e -> {throw new CustomException(400, "Invalid input");});
    }
}
