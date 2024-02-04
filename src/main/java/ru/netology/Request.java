package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Request {
    private String requestMethod;
    private String path;
    private String requestHeaders;

    private InputStream requestBody;


    public void parse(InputStream in) throws IOException, BadRequestException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        String[] parts1 = line.split(" ");

        if (parts1.length != 3) {
            throw new BadRequestException(line);
        }

        requestMethod = parts1[0];
        path = parts1[1];

        StringBuilder requestLine = new StringBuilder();
        while (true) {
            int ch = br.read();
            requestLine.append((char) ch);
            if (requestLine.toString().contains("\r\n\r\n"))
                break;
        }

        requestBody = in;
        requestHeaders = requestLine.toString();
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public InputStream getRequestBody() {
        return requestBody;
    }
}
