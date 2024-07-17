package com.yes25.yes255orderpaymentserver.common.appender;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.util.StatusPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpAppenderTest {

    @Mock
    private MockWebServer mockWebServer;
    private Logger logger;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        HttpAppender httpAppender = new HttpAppender();
        httpAppender.setContext(context);
        httpAppender.setUrl(mockWebServer.url("/log").toString());
        httpAppender.setProjectName("TestProject");
        httpAppender.setProjectVersion("1.0.0");
        httpAppender.setLogVersion("1.0");
        httpAppender.setLogSource("test");
        httpAppender.setLogType("testType");
        httpAppender.setHost("localhost");
        httpAppender.setSecretKey("secret");
        httpAppender.setPlatform("testPlatform");

        httpAppender.start();

        logger = (Logger) LoggerFactory.getLogger(HttpAppenderTest.class);
        logger.addAppender(httpAppender);
        context.start();
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DisplayName("로그를 찍는지 확인한다.")
    @Test
    void testHttpAppender() throws InterruptedException, IOException {
        // given
        MockResponse mockResponse = new MockResponse().setResponseCode(200);
        mockWebServer.enqueue(mockResponse);

        // when
        logger.info("Test log message");

        // then
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals("/log", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());

        ObjectMapper objectMapper = new ObjectMapper();
        HttpAppender.LogEvent logEvent = objectMapper.readValue(recordedRequest.getBody().readUtf8(), HttpAppender.LogEvent.class);

        assertEquals("TestProject", logEvent.projectName);
        assertEquals("1.0.0", logEvent.projectVersion);
        assertEquals("1.0", logEvent.logVersion);
        assertEquals("Test log message", logEvent.body);
        assertEquals("test", logEvent.logSource);
        assertEquals("testType", logEvent.logType);
        assertEquals("localhost", logEvent.host);
        assertEquals("secret", logEvent.secretKey);
        assertEquals("INFO", logEvent.logLevel);
        assertEquals("testPlatform", logEvent.platform);
    }
}
