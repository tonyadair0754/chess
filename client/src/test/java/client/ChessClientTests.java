package client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class ChessClientTests {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUpStreams() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void helpCommandSignedOut() {
        String simulatedInput = "help\nquit\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        ChessClient client = new ChessClient(0); // port doesn't matter for quit/help
        assertDoesNotThrow(client::run);

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Welcome to the best chess program"));
        assertTrue(consoleOutput.contains("register <USERNAME>"));
    }

    @Test
    void unknownCommandSignedOut() {
        String simulatedInput = "badcommand\nquit\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        ChessClient client = new ChessClient(0);
        assertDoesNotThrow(client::run);

        String consoleOutput = outputStream.toString();
        assertTrue(consoleOutput.contains("Unknown command"));
    }
}