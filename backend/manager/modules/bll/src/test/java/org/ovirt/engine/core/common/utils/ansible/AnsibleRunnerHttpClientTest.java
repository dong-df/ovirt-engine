package org.ovirt.engine.core.common.utils.ansible;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnsibleRunnerHttpClientTest {

    @Mock
    HttpClient httpClient;

    @InjectMocks
    AnsibleRunnerHTTPClient client;

    private static Stream<Arguments> statusesForPlaybook() {
        return Stream.of(
            Arguments.of(
                new AnsibleRunnerHTTPClient.PlaybookStatus("ok", "running"),
                createHttpResponse("{\"status\": \"OK\", \"msg\": \"running\"}")
            ),
            Arguments.of(
                new AnsibleRunnerHTTPClient.PlaybookStatus("notfound", "not found"),
                createHttpResponse("{\"status\": \"NOTFOUND\", \"msg\": \"not found\"}")
            ),
            Arguments.of(
                new AnsibleRunnerHTTPClient.PlaybookStatus("unknown", "the artifacts directory is incomplete!"),
                createHttpResponse("{\"status\": \"UNKNOWN\", \"msg\": \"The artifacts directory is incomplete!\"}")
            )
        );
    }

    private static HttpResponse createHttpResponse(String content) {
        return createHttpResponse(content, HttpStatus.SC_OK);
    }

    private static HttpResponse createHttpResponse(String content, int httpStatus) {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = factory.newHttpResponse(
            new BasicStatusLine(HttpVersion.HTTP_1_1, httpStatus, null), null
        );
        response.setEntity(EntityBuilder.create().setText(content).build());
        return response;
    }

    // Run playbook tests:
    @Test
    public void checkThatCorrectPlayUuidIsReturned() throws IOException {
        AnsibleCommandConfig command = mock(AnsibleCommandConfig.class);

        when(command.hosts()).thenReturn(new ArrayList<>());
        when(httpClient.execute(any(HttpPost.class))).thenReturn(
            createHttpResponse("{\"data\": {\"play_uuid\": \"123-456\"}}", HttpStatus.SC_ACCEPTED)
        );

        assertThat(client.runPlaybook(command), is("123-456"));
    }

    @Test
    public void checkErrorIsThrownWhenIncorrectHttpState() throws IOException {
        AnsibleCommandConfig command = mock(AnsibleCommandConfig.class);

        when(command.hosts()).thenReturn(new ArrayList<>());
        when(httpClient.execute(any(HttpPost.class))).thenReturn(
            createHttpResponse(
                "{\"status\": \"FAILED\", \"msg\": \"Runner thread failed to start\"}",
                HttpStatus.SC_INTERNAL_SERVER_ERROR
            )
        );

        assertThrows(AnsibleRunnerCallException.class, () -> {
            client.runPlaybook(command);
        });
    }

    // Get playbook status tests:
    @ParameterizedTest(name = "Test playbook statuses: {0}")
    @MethodSource("statusesForPlaybook")
    public void getPlaybookStatus(
        AnsibleRunnerHTTPClient.PlaybookStatus playbookStatus,
        HttpResponse response
    ) throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(response);

        assertThat(client.getPlaybookStatus(any(String.class)), is(playbookStatus));
    }

    @Test
    public void checkThatTotalEventsIsReturned() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(
            createHttpResponse("{\"data\": {\"total_events\": 10}}")
        );

        assertThat(client.getTotalEvents(any(String.class)), is(10));
    }

    @Test
    public void checkThatTotalEventsThrowsException() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(
            createHttpResponse("{\"status\": \"NOTFOUND\", \"msg\": \"not exists\"}", HttpStatus.SC_NOT_FOUND)
        );

        assertThrows(AnsibleRunnerCallException.class, () -> {
            client.getTotalEvents("123");
        });
    }

    @Test
    public void testProcessEventsWithoutOkEvent() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(
            createHttpResponse(
                String.join(
                    "",
                    "{",
                    "\"status\": \"OK\",",
                    "\"msg\": \"\",",
                    "\"data\": {",
                    "\"events\": {",
                    "\"1-123\": {\"event\": \"playbook_on_start\"}",
                    "},",
                    "\"total_events\": 1",
                    "}",
                    "}"
                ),
                HttpStatus.SC_OK
            )
        );

        assertThat(client.processEvents("123", 0, null), is(1));
    }

    @Test
    public void testProcessEventsWithError() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(
            createHttpResponse("{\"status\": \"NOTFOUND\", \"msg\": \"not exists\"}", HttpStatus.SC_NOT_FOUND)
        );

        assertThrows(AnsibleRunnerCallException.class, () -> {
            client.processEvents("123", 0, null);
        });
    }

    @Test
    public void checkBothInstalledAndUpdatedAreReturned() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(
            createHttpResponse(
                String.join(
                    "",
                    "{",
                    "\"status\": \"OK\",",
                    "\"msg\": \"\",",
                    "\"data\": {",
                    "\"event_data\": {",
                    "\"res\": {",
                    "\"changed\": true,",
                    "\"changes\": {",
                    "\"installed\": [[\"mypackage2\", \"version1\"]],",
                    "\"updated\": [[\"mypackage1\", \"version1\"]]",
                    "}",
                    "}",
                    "}",
                    "}",
                    "}"
                ),
                HttpStatus.SC_OK
            )
        );

        assertThat(
            client.getYumPackages(any(String.class)),
            containsInAnyOrder("mypackage1", "mypackage2")
        );
    }

    @Test
    public void checkThatStdoutIsReturned() throws IOException {
        when(httpClient.execute(any(HttpGet.class))).thenReturn(
            createHttpResponse(
                String.join(
                    "",
                    "{",
                    "\"status\": \"OK\",",
                    "\"msg\": \"\",",
                    "\"data\": {",
                    "\"event_data\": {",
                    "\"res\": {",
                    "\"stdout\": \"output\"",
                    "}",
                    "}",
                    "}",
                    "}"
                ),
                HttpStatus.SC_OK
            )
        );

        assertThat(
            client.getCommandStdout(any(String.class)),
            is("output")
        );
    }
}
