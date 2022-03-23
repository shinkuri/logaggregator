package at.kurumi;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.inject.Produces;
import javax.websocket.Session;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Warps a BufferedReader that hopefully doesn't block the file from being written to.
 */
@Stateful
public class Tail {

    private final BufferedReader bufferedReader;

    private Tail(String uri) throws IOException, URISyntaxException {
        final var path = Path.of(new URI(uri));
        final var is = Files.newInputStream(path, StandardOpenOption.READ);
        final var reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        bufferedReader = new BufferedReader(reader);
    }

    @Produces
    public Tail start() throws IOException, URISyntaxException {
        return new Tail("F:/Development/logaggregator/dummy/OwnCloud.log");
    }

    @PreDestroy
    public void close() throws Exception {
        bufferedReader.close();
    }

    public BufferedReader getReader() {
        return bufferedReader;
    }

    public static void update(Session session) {

        session.getAsyncRemote().sendText("");
    }
}
