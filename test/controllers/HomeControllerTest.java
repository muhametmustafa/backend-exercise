package controllers;

import models.Role;
import models.User;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.Logger;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import utils.DatabaseUtils;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class HomeControllerTest extends WithApplication {

  /*  @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }
*/
    private User user;

    @Test
    public void testIndex() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    //@Before
    @Test
    public void addUserTest(){
        user = new User("Aaggon", "Lohaj", new ArrayList<>());
        Logger.of(this.getClass()).error(user.toString());
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder();
        requestBuilder.method(POST)
                .bodyJson(Json.toJson(user))
                .uri("/api/users/")
                .build();
        Result result = route(app, requestBuilder);
        String content = contentAsString(result);
        Logger.of(this.getClass()).error(content);
        assertEquals(OK, result.status());
    }

    @Test
    public void findUserTest() {
        Logger.of(this.getClass()).error(user.toString());
        String id = user.getId().toString();
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(GET)
                .uri("api/users/" + id)
                .build();
        Result result = route(app, builder);
        assertEquals(OK, result.status());
    }



}
