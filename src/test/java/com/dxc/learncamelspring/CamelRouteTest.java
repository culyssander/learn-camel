package com.dxc.learncamelspring;

import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CamelRouteTest {

    @Autowired
    private ProducerTemplate template;

    @Test
    public void testMockAreValid() {
        template.sendBody("direct:greeting",
                "Working");
    }
}