package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import models.Role;
import models.User;
import mongo.IMongoDB;
import mongo.InMemoryMongoDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class AuthorizationControllerTest extends WithApplication {

    @Inject
    IMongoDB mongoDB;

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);

        MongoCollection<User> userMongoCollection = mongoDB.getMongoDatabase().getCollection("users", User.class);
        userMongoCollection.insertOne(new User("Muhamet", "Muhamet", Arrays.asList(new Role("muhametId", "USER"))));

    }

    @After
    @Override
    public void stopPlay() {
        super.stopPlay();
        mongoDB.getMongoDatabase().drop();
        //mongoDB.disconnect();
    }


    @Test
    public void testAuthenticate() {
        ObjectNode node = getJsonNode("Muhamet", "Muhamet");

        Result result = getResult(node, POST);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("token"));
    }

    @Test
    public void testAuthenticateNotFound() {
        ObjectNode notFoundNode = getJsonNode("Muhamet", "Mustafa");//password mismatch
        Result notFoundResult = getResult(notFoundNode, POST);

        assertEquals(NOT_FOUND, notFoundResult.status());
        assertTrue(contentAsString(notFoundResult).contains("error"));
    }



    private ObjectNode getJsonNode(String username, String password) {
        ObjectNode node = Json.newObject();
        node.put("username", username);
        node.put("password", password);
        return node;
    }

    private Result getResult(JsonNode node, String method) {
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder();
        requestBuilder.method(method)
                .bodyJson(node)
                .uri("/api/authenticate/")
                .build();
        return route(app, requestBuilder);
    }


}
