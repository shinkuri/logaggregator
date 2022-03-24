package at.kurumi.endpoints;

import at.kurumi.Sources;
import at.kurumi.TailListener;
import at.kurumi.Tailer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/log/{source}")
public class LogWebsocket implements TailListener {

    private static final Logger LOG = LogManager.getLogger("Log-Websocket");
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @SuppressWarnings("unused")
    @Inject private Sources sources;

    @Override
    public void receive(String content) {
        sessions.forEach(session -> session.getAsyncRemote().sendText(content));
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("source") String source) {
        sources.getSourceByName(source).ifPresentOrElse(source_ -> {
            sessions.add(session);
            Tailer.startTail(new File(source_.getPath()), this);
        }, () -> {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Source doesn't exist."));
            } catch (IOException e) {
                LOG.error("Tried to close Websocket requesting invalid log source, but the target did not respond");
                LOG.debug(e.getMessage());
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("source") String source) {
        sources.getSourceByName(source).ifPresent(source_ -> {
            sessions.remove(session);
            Tailer.stopTail(new File(source_.getPath()), this);
        });
    }
}
