package exception;

import java.io.IOException;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message, IOException exception) {
        super(message);
    }
}
