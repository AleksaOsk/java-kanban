package server.HttpHandlers;

import com.google.gson.Gson;
import service.TaskManager;

public class SubtaskHttpHandler extends TaskHttpHandler {

    public SubtaskHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }
}
