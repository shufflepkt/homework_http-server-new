package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Request {
    private String requestMethod;
    private String path;
    private List<String> requestHeaders;

    private List<String> requestBody;

    private List<NameValuePair> queryParams;

    public void parse(InputStream in) throws IOException, BadRequestException {
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            throw new BadRequestException(Arrays.toString(buffer));
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            throw new BadRequestException(Arrays.toString(requestLine));
        }

        requestMethod = requestLine[0];
        path = requestLine[1].substring(0, requestLine[1].indexOf('?'));

        try {
            queryParams = URLEncodedUtils.parse(new URI(requestLine[1]), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

        final var headersByte = Arrays.copyOfRange(buffer, headersStart, headersEnd);
        requestHeaders = Arrays.asList(new String(headersByte).split("\r\n"));

        final var bodyByte = Arrays.copyOfRange(buffer, headersEnd + headersDelimiter.length, read);
        requestBody = Arrays.asList(new String(bodyByte).split("\r\n"));
    }

    private Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    public List<String> getRequestHeaders() {
        return requestHeaders;
    }

    public List<String> getRequestBody() {
        return requestBody;
    }

    public Optional<String> getQueryParam(String name) {
        return queryParams.stream()
                .filter(o -> o.getName().equals(name))
                .map(NameValuePair::getValue)
                .findFirst();
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }
}
