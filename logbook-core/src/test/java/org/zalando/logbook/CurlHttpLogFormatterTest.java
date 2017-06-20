package org.zalando.logbook;

import org.junit.Test;
import org.zalando.logbook.DefaultLogbook.SimpleCorrelation;
import org.zalando.logbook.DefaultLogbook.SimplePrecorrelation;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CurlHttpLogFormatterTest {

    @Test
    public void shouldLogRequest() throws IOException {
        final String correlationId = "c9408eaa-677d-11e5-9457-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withProtocolVersion("HTTP/1.0")
                .withOrigin(Origin.REMOTE)
                .withPath("/test")
                .withQuery("limit=1")
                .withHeaders(MockHeaders.of(
                        "Accept", "application/json",
                        "Content-Type", "text/plain"))
                .withBodyAsString("Hello, world!");

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(curl, is("curl -v -X GET -H 'Accept: application/json' -H 'Content-Type: text/plain' -d 'Hello, world!' 'http://localhost/test?limit=1'"));
    }

    @Test
    public void shouldLogRequestWithoutBody() throws IOException {
        final String correlationId = "0eae9f6c-6824-11e5-8b0a-10ddb1ee7671";
        final HttpRequest request = MockHttpRequest.create()
                .withPath("/test")
                .withHeaders(MockHeaders.of("Accept", "application/json"));

        final HttpLogFormatter unit = new CurlHttpLogFormatter();
        final String curl = unit.format(new SimplePrecorrelation<>(correlationId, request));

        assertThat(curl, is("curl -v -X GET -H 'Accept: application/json' 'http://localhost/test'"));
    }

    @Test
    public void shouldDelegateLogResponse() throws IOException {
        final HttpLogFormatter fallback = mock(HttpLogFormatter.class);
        final HttpLogFormatter unit = new CurlHttpLogFormatter(fallback);

        final Correlation<HttpRequest, HttpResponse> correlation = new SimpleCorrelation<>(
                "3881ae92-6824-11e5-921b-10ddb1ee7671", MockHttpRequest.create(), MockHttpResponse.create());

        unit.format(correlation);

        verify(fallback).format(correlation);
    }

}
