package at.kurumi;

public interface TailListener {

    /**
     * Receive new tailing content.
     *
     * @param content the content
     */
    void receive(String content);
}
