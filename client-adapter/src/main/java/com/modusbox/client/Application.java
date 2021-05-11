package com.modusbox.client;

import com.modusbox.client.exporter.HTTPServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath:spring/application.xml"})
public class Application {

    public static void main(String... args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        new HTTPServer(Integer.parseInt(ctx.getEnvironment().getProperty("server.metrics.port")));
    }
}
