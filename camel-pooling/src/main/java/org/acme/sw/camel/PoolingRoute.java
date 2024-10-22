package org.acme.sw.camel;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.ContentType;
import io.cloudevents.core.provider.EventFormatProvider;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PoolingRoute extends RouteBuilder {

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "org.acme.sw.camel.target.workflow.url")
    String targetWorkflow;

    @Override
    public void configure() throws Exception {

        // Poll the REST endpoint every 30 seconds
        from("timer://myTimer?period=30000")
                .routeId("pool-rest-endpoint")
                .log("Pooling REST endpoint")
                // Call the REST endpoint - it's public mock server
                .to("rest:get:typicode/demo/db?host=https://my-json-server.typicode.com")
                // Process the response and emit a CloudEvent
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);

                    // Create a CloudEvent with the body as data
                    CloudEvent cloudEvent = CloudEventBuilder.v1()
                            .withId(UUID.randomUUID().toString())
                            // Type and Source matches the workflow `events` definition on `target-workflow` project
                            .withType("org.acme.sw.camel.pooling")
                            .withSource(URI.create("urn:camel-pooling"))
                            .withTime(OffsetDateTime.now())
                            .withData("application/json", body.getBytes())
                            .build();

                    // Set the CloudEvent as the outgoing message body
                    exchange.getIn().setBody(Objects.requireNonNull(EventFormatProvider.getInstance().resolveFormat(ContentType.JSON))
                            .serialize(cloudEvent));
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/cloudevents+json");
                    exchange.getIn().setHeader(Exchange.HTTP_METHOD, HttpMethods.POST);
                })
                //.marshal().json(JsonLibrary.Jackson)
                .log("CloudEvent created: ${body}")
                // Send the CloudEvent to the HTTP endpoint
                .to(targetWorkflow);
    }
}
