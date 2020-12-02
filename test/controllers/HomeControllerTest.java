package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import models.Role;
import models.User;
import mongo.IMongoDB;
import mongo.InMemoryMongoDB;
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
import sun.rmi.runtime.Log;
import utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class HomeControllerTest extends WithApplication {

  /*  @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }
*/
    @Inject
    IMongoDB mongoDB;


    private User user;

    @Override
    @Before
    public void startPlay() {
        super.startPlay();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);

        //Add the user that will be deleted
        User user = new User("Muhamet", "Mustafa", new ArrayList<>());
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder();
        requestBuilder.method(POST)
                .bodyJson(Json.toJson(user))
                .uri("/api/users/")
                .build();
        Result result = route(app, requestBuilder);
        String content = contentAsString(result);
        this.user = Json.fromJson(Json.parse(content), User.class);
        Logger.of(Constants.class).debug(content);
        Logger.of(Constants.class).debug("THE USER, {}", this.user);

    }
    @After
    @Override
    public void stopPlay() {
        super.stopPlay();
        mongoDB.getMongoDatabase().drop();
        //mongoDB.disconnect();
    }

    @Test
    public void testIndex() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void addUserTest(){
        User user = new User("Agon", "Lohaj", new ArrayList<>());
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder();
        requestBuilder.method(POST)
                .bodyJson(Json.toJson(user))
                .uri("/api/users/")
                .build();
        Result result = route(app, requestBuilder);
        String content = contentAsString(result);
        Logger.of(Constants.class).debug(content);
        assertEquals(OK, result.status());
    }

    @Test
    public void findUserTest() {
        Logger.of(Constants.class).debug(user.toString());
        String id = this.user.getId().toString();
        Logger.of(Constants.class).debug("THE ID: " + id);
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(GET)
                .uri("/api/users/" + id)
                .build();
        Result result = route(app, builder);
        Logger.of(Constants.class).debug(contentAsString(result));
        assertEquals(OK, result.status());
    }



}
