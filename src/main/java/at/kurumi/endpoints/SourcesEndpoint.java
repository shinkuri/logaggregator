package at.kurumi.endpoints;

import at.kurumi.Sources;

import javax.inject.Inject;
import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/sources")
public class SourcesEndpoint {

    @SuppressWarnings("unused")
    @Inject private Sources sources;

    @GET
    public Response getSources() {
        final var json = Json.createArrayBuilder(sources.getSourceHierarchiesWithNames()).build();
        return Response.ok(json, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
