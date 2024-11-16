package server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import server.HttpHandlers.BaseHttpHandler;
import service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final Gson gson = new Gson();

    private static FileBackedTaskManager manager;
    static HttpServer httpServer;

    public HttpTaskServer(FileBackedTaskManager manager) {
        HttpTaskServer.manager = manager;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(FileBackedTaskManager.loadFromFile(new File("saveFile.txt")));
        start();
    }







    public static void start() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(8080), 0);
        httpServer.createContext("/TaskManager", new BaseHttpHandler(manager, gson));
        httpServer.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Сервер остановлен");
    }

    public static Gson getGson() {
        return gson;
    }
}
