package at.kurumi.endpoints;

import at.kurumi.Sources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/reload")
public class ReloadSourcesEndpoint {

    @Inject private Sources sources;

    @GET
    public Response reloadSources() {
        final var success = sources.load();
        return success ? Response.ok().build() : Response.serverError().build();
    }
}
