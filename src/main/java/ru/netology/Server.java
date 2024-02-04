package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int NUMBER_OF_THREADS = 64;

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();

                threadPool.execute(() -> {
                    try (
                            socket;
                            final var in = socket.getInputStream();
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        connectionHandler(in, out);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String requestMethod, String path, Handler handler) {
        if (!handlers.containsKey(requestMethod)) {
            handlers.put(requestMethod, new ConcurrentHashMap<>());
        }
        handlers.get(requestMethod).putIfAbsent(path, handler);
    }

    private void connectionHandler(InputStream in, BufferedOutputStream out) throws IOException {
        Request request = new Request();
        try {
            request.parse(in);
        } catch (BadRequestException e) {
            sendMsg(out, compose400BadRequest());
            return;
        }

        if (handlers.containsKey(request.getRequestMethod())) {
            ConcurrentHashMap<String, Handler> pathAndHandler = handlers.get(request.getRequestMethod());
            if (pathAndHandler.containsKey(request.getPath())) {
                pathAndHandler.get(request.getPath()).handle(request, out);
                return;
            }
            sendMsg(out, compose404NotFound());
            return;
        }
        sendMsg(out, compose400BadRequest());
    }

    public String compose200Ok(String mimeType, long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String compose404NotFound() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private String compose400BadRequest() {
        return "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    private void sendMsg(BufferedOutputStream out, String msg) throws IOException {
        out.write(msg.getBytes());
        out.flush();
    }

    // Тестовая функция, для вывода в консоль текущего списка обработчиков
    public void showHandlers() {
        for (Map.Entry<String, ConcurrentHashMap<String, Handler>> entry1 : handlers.entrySet()) {
            String method = entry1.getKey();
            System.out.println("\nДля метода " + method + ":");

            for (Map.Entry<String, Handler> entry2 : entry1.getValue().entrySet()) {
                String path = entry2.getKey();
                Handler handler = entry2.getValue();
                System.out.println("Путь: " + path + ", обработчик: " + handler.toString());
            }
        }
    }
}
