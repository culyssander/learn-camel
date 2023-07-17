package com.dxc.learncamelspring.component;

import org.apache.camel.Message;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.DefaultMessage;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component
public class TimerComponent extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:hello?period=5000")
                .process(e -> {
                    Message message = new DefaultMessage(e);
                    message.setBody(new Date());
                    e.setMessage(message);
                })
                .to("direct:route-timer");

        from("direct:route-timer")
                .log("${body}");
    }
}
