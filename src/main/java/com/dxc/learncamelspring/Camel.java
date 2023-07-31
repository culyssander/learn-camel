package com.dxc.learncamelspring;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

public class Camel extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:greeting").id("greeting")
//                .log(LoggingLevel.INFO, "Hello ${body}")
                .choice()
                .when().simple("${body} contains 'Team'")
                    .log(LoggingLevel.INFO, "I like to working with Teams")
                .otherwise()
                    .log(LoggingLevel.INFO, "Solo fighter");
//                .to("direct:finishGreeting");

//        from("direct:finishGreeting").log(LoggingLevel.INFO, "Bye ${body}");
    }
}
