package nu.te4.services;

import javax.ejb.EJB;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import nu.te4.beans.LoginBean;
import nu.te4.beans.RecipeBean;

@Path("/")
public class RecipeService
{
    @EJB
    RecipeBean recipeBean;
    
    @EJB
    LoginBean loginBean;
    
    @GET
    @Path("Recipe/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRecipes()
    {
        JsonArray recipes = recipeBean.getAllRecipes();
        return Response.ok(recipes).build();
    }
    
    @GET
    @Path("Recipe/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecipe(@PathParam("id") int id)
    {
        JsonObject recipe = recipeBean.getRecipe(id);
        return Response.ok(recipe).build();
    }
    
    @POST
    @Path("Recipe")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addRecipe(String body, @Context HttpHeaders header)
    {
        String basic_auth = header.getHeaderString("Authorization");
        if(basic_auth == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        else if(loginBean.checkCredentials(basic_auth))
        {
            int statuscode = recipeBean.addRecipe(body);
            return Response.status(statuscode).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @DELETE
    @Path("Recipe/{id}")
    public Response deleteRecipe(@PathParam("id") int id, @Context HttpHeaders header)
    {
        String basic_auth = header.getHeaderString("Authorization");
        if(loginBean.checkCredentials(basic_auth))
        {
            int statuscode = recipeBean.deleteRecipe(id);
            return Response.status(statuscode).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @PUT
    @Path("Recipe/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRecipe(@PathParam("id") int id, @Context HttpHeaders header, String body)
    {
        String basic_auth = header.getHeaderString("Authorization");
        if(basic_auth == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        else if(loginBean.checkCredentials(basic_auth))
        {
            int statuscode = recipeBean.updateRecipe(id, body);
            return Response.status(statuscode).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @GET
    @Path("categories")
    public Response getCategories()
    {
        JsonArray categories = recipeBean.getCategories();
        return Response.ok(categories).build();
    }
    
    @GET
    @Path("login")
    public Response login(@Context HttpHeaders header)
    {
        String basic_auth = header.getHeaderString("Authorization");
        if(loginBean.checkCredentials(basic_auth))
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
