package com.yes25.yes255orderpaymentserver.common.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpAppender extends AppenderBase<ILoggingEvent> {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String url;
    private String projectName;
    private String projectVersion;
    private String logVersion;
    private String logSource;
    private String logType;
    private String host;
    private String secretKey;

    public HttpAppender() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            LogEvent logEvent = new LogEvent(
                projectName, projectVersion, logVersion, eventObject.getFormattedMessage(),
                logSource, logType, host, secretKey
            );
            String json = objectMapper.writeValueAsString(logEvent);
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    addError("Failed to send log event", e);
                    // 필요시 실패한 로그를 파일에 저장하거나 재시도 로직 추가
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        addError("Failed to send log event, response code: " + response.code());
                    }
                    response.close();
                }
            });

        } catch (IOException e) {
            addError("Failed to send log event", e);
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public void setLogVersion(String logVersion) {
        this.logVersion = logVersion;
    }

    public void setLogSource(String logSource) {
        this.logSource = logSource;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    static class LogEvent {
        public String projectName;
        public String projectVersion;
        public String logVersion;
        public String body;
        public String logSource;
        public String logType;
        public String host;
        public String secretKey;

        public LogEvent(String projectName, String projectVersion, String logVersion, String body,
            String logSource, String logType, String host, String secretKey) {
            this.projectName = projectName;
            this.projectVersion = projectVersion;
            this.logVersion = logVersion;
            this.body = body;
            this.logSource = logSource;
            this.logType = logType;
            this.host = host;
            this.secretKey = secretKey;
        }
    }
}