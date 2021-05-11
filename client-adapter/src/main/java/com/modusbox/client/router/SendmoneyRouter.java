package com.modusbox.client.router;

import com.modusbox.client.exception.RouteExceptionHandlingConfigurer;
import com.modusbox.client.processor.StandardizeBody;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class SendmoneyRouter extends RouteBuilder {

    private final RouteExceptionHandlingConfigurer exception = new RouteExceptionHandlingConfigurer();

    private static final String ROUTE_ID_POST = "com.modusbox.postSendmoney";
    private static final String COUNTER_NAME_POST = "counter_post_sendmoney_requests";
    private static final String TIMER_NAME_POST = "histogram_post_sendmoney_timer";
    private static final String HISTOGRAM_NAME_POST = "histogram_post_sendmoney_requests_latency";

    public static final Counter requestCounterPost = Counter.build()
            .name(COUNTER_NAME_POST)
            .help("Total requests for POST /sendmoney.")
            .register();

    private static final Histogram requestLatencyPost = Histogram.build()
            .name(HISTOGRAM_NAME_POST)
            .help("Request latency in seconds for POST /sendmoney.")
            .register();

    private static final String ROUTE_ID_PUT = "com.modusbox.putSendmoneyById";
    private static final String COUNTER_NAME_PUT = "counter_put_sendmoney_by_id_requests";
    private static final String TIMER_NAME_PUT = "histogram_put_sendmoney_by_id_timer";
    private static final String HISTOGRAM_NAME_PUT = "histogram_put_sendmoney_by_id_requests_latency";

    public static final Counter requestCounterPut = Counter.build()
        .name(COUNTER_NAME_PUT)
        .help("Total requests for PUT /sendmoney.")
        .register();

    private static final Histogram requestLatencyPut = Histogram.build()
        .name(HISTOGRAM_NAME_PUT)
        .help("Request latency in seconds for PUT /sendmoney.")
        .register();

    public void configure() {

        // Add custom global exception handling strategy
        exception.configureExceptionHandling(this);
        StandardizeBody stdBody = new StandardizeBody();

        from("direct:postSendmoney").routeId(ROUTE_ID_POST).doTry()
            .process(exchange -> {
                requestCounterPost.inc(1); // increment Prometheus Counter metric
                exchange.setProperty(TIMER_NAME_POST, requestLatencyPost.startTimer()); // initiate Prometheus Histogram metric
            })
            .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                    "'Request received, " + ROUTE_ID_POST + "', null, null, 'Input Payload: ${body}')") // default logging
            /*
             * BEGIN processing
             */
            .setProperty("origPayload", simple("${body}"))
            .removeHeaders("CamelHttp*")
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader("Content-Type", constant("application/json"))
            .process(stdBody)
            .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                "'Calling outbound API, postTransfers, " +
                "POST {{outbound.endpoint}}', " +
                "'Tracking the request', 'Track the response', 'Input Payload: ${body}')")
            .toD("{{outbound.endpoint}}/transfers?bridgeEndpoint=true&throwExceptionOnFailure=false")
            .unmarshal().json(JsonLibrary.Gson)
            .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                    "'Response from outbound API, postTransfers: ${body}', " +
                    "'Tracking the response', 'Verify the response', null)")
            /*
             * END processing
             */
            .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                    "'Send response, " + ROUTE_ID_POST + "', null, null, 'Output Payload: ${body}')") // default logging
            .doFinally().process(exchange -> {
                ((Histogram.Timer) exchange.getProperty(TIMER_NAME_POST)).observeDuration(); // stop Prometheus Histogram metric
            }).end()
        ;

        from("direct:putSendmoneyById").routeId(ROUTE_ID_PUT).doTry()
                .process(exchange -> {
                    requestCounterPut.inc(1); // increment Prometheus Counter metric
                    exchange.setProperty(TIMER_NAME_POST, requestLatencyPut.startTimer()); // initiate Prometheus Histogram metric
                })
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Request received, " + ROUTE_ID_PUT + "', null, null, 'Input Payload: ${body}')") // default logging
                /*
                 * BEGIN processing
                 */
                .setProperty("origPayload", simple("${body}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader("Content-Type", constant("application/json"))
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Calling outbound API, putTransfersById', " +
                        "'Tracking the request', 'Track the response', " +
                        "'Request sent to PUT https://{{outbound.endpoint}}/transfers/${header.transferId}')")
                .marshal().json(JsonLibrary.Gson)
                .toD("{{outbound.endpoint}}/transfers/${header.transferId}?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .unmarshal().json(JsonLibrary.Gson)
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Response from outbound API, putTransfersById: ${body}', " +
                        "'Tracking the response', 'Verify the response', null)")
                /*
                 * END processing
                 */
                .to("bean:customJsonMessage?method=logJsonMessage('info', ${header.X-CorrelationId}, " +
                        "'Send response, " + ROUTE_ID_PUT + "', null, null, 'Output Payload: ${body}')") // default logging
                .doFinally().process(exchange -> {
                    ((Histogram.Timer) exchange.getProperty(TIMER_NAME_PUT)).observeDuration(); // stop Prometheus Histogram metric
                }).end()
        ;
    }
}
