package handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import service.FileBackedTaskManager;
import service.HistoryManager;
import service.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaseHttpHandlerTest {
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private HttpTaskServer taskServer;


    @BeforeEach
    public void setUp() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(historyManager, tempFile);
        taskServer = new HttpTaskServer(manager);

        taskServer.start();
    }

    @Test
    void testSend404Error() throws IOException {
        URL url = new URL("http://localhost:8080/");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        assertEquals(404, responseCode);
        taskServer.stop();
    }

    @Test
    void testSend405Error() throws IOException {
        URL url = new URL("http://localhost:8080/tasks");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");

        int responseCode = connection.getResponseCode();

        assertEquals(405, responseCode);
        taskServer.stop();
    }

    @Test
    void testSend500Error() throws IOException {
        URL url = new URL("http://localhost:8080/tasks");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        int responseCode = connection.getResponseCode();

        assertEquals(500, responseCode);
        taskServer.stop();
    }
}

