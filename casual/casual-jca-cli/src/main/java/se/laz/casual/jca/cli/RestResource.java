package se.laz.casual.jca.cli;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/configuration")
public class RestResource
{
    @Inject
    private CLIService CLIService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get()
    {
        return Response.ok(CLIService.getConfiguration()).build();
    }
}
