package at.kurumi.json;

import java.util.ArrayList;
import java.util.List;

public class SourcesJSON {

    private final List<Source> sources = new ArrayList<>();

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources.addAll(sources);
    }
}
