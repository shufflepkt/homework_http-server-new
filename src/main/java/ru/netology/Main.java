package ru.netology;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static final int PORT = 9999;

    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/classic.html", ((request, out) -> {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);

            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((server.compose200Ok(mimeType, content.length)).getBytes());
            out.write(content);
            out.flush();
        }));

        server.addHandler("GET", "/spring.svg", ((request, out) -> {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);

            final var length = Files.size(filePath);
            out.write((server.compose200Ok(mimeType, length)).getBytes());
            Files.copy(filePath, out);
            out.flush();
        }));

        server.addHandler("GET", "/spring.png", ((request, out) -> {
            final var filePath = Path.of(".", "public", request.getPath());
            final var mimeType = Files.probeContentType(filePath);

            final var length = Files.size(filePath);
            out.write((server.compose200Ok(mimeType, length)).getBytes());
            Files.copy(filePath, out);
            out.flush();
        }));

        server.addHandler("POST", "/messages", ((request, responseStream) -> {
            // TODO: handlers code
        }));

        server.listen(PORT);
    }
}
