package com.modusbox.client.processor;

import com.google.gson.Gson;
import com.modusbox.client.model.TransferRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.logging.Logger;

public class StandardizeBody implements Processor {

    private static final Logger LOGGER = Logger.getLogger(StandardizeBody.class.getName());

    @Override
    public void process(Exchange exchange) throws Exception {

//        LOGGER.info(exchange.getIn().getBody(String.class));

        if (exchange.getIn().getBody() instanceof TransferRequest) {

            TransferRequest req = exchange.getIn().getBody(TransferRequest.class);
            if (req.getFrom().getExtensionList().isEmpty()) req.getFrom().setExtensionList(null);
            if (req.getTo().getExtensionList().isEmpty()) req.getTo().setExtensionList(null);
            if (req.getQuoteRequestExtensions().isEmpty()) req.setQuoteRequestExtensions(null);
            if (req.getTransferRequestExtensions().isEmpty()) req.setTransferRequestExtensions(null);

            exchange.getIn().setBody(new Gson().toJson(req));
        }

//        LOGGER.info(exchange.getIn().getBody(String.class));
    }
}
