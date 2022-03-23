package at.kurumi;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/log/{source}")
public class Endpoint {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());


    @OnOpen
    public void onOpen(Session session, @PathParam("source") String source) {
        sessions.add(session);

    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnMessage
    public void onReceiveMessage(Session clientSession, String msg) {

        // broadcast
        for (Session session : sessions) {
            session.getAsyncRemote().sendText("msg for all clients: " + msg);
        }

        // reply directly
        clientSession.getAsyncRemote().sendText("your personal message from server");
        // echo directly
        clientSession.getAsyncRemote().sendText("your msg was: " + msg);
    }
}
