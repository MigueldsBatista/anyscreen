package com.anyscreen.server.handlers;

import java.io.IOException;

import com.anyscreen.constants.HTMLConstants;
import com.sun.net.httpserver.HttpExchange;


public class TestPageHandler extends AbstractHandler {

    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        String html = generateTestPage();
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        sendResponse(exchange, 200, html);
    }

    private String generateTestPage() {
        return HTMLConstants.TEST_PAGE;
    }
}
