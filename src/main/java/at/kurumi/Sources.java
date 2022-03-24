package at.kurumi;

import at.kurumi.json.Source;
import at.kurumi.json.SourcesJSON;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Startup
@Singleton
public class Sources {

    private static final Logger LOG = LogManager.getLogger();

    private final Set<Source> sources = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    public void init() {
        load();
    }

    public boolean load() {
        LOG.info("Loading sources");
        try (final var is = new FileReader("sources.json")) {
            final var gson = new Gson();
            final var sourcesJson = gson.fromJson(is, SourcesJSON.class);
            sources.addAll(sourcesJson.getSources());
            LOG.info("Loaded sources");
            return true;
        } catch (FileNotFoundException e) {
            LOG.error("sources.json not found");
            LOG.debug(e.getMessage());
            return false;
        } catch (IOException e) {
            LOG.error("Failed to read sources.json");
            LOG.debug(e.getMessage());
            return false;
        }
    }

    public Optional<Source> getSourceByName(String name) {
        return sources.stream()
                .filter(source -> source.getName().equals(name))
                .findFirst();
    }
}
