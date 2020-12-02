package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import models.ChatRoom;
import models.Role;
import models.User;
import mongo.IMongoDB;
import mongo.InMemoryMongoDB;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithServer;

import java.util.OptionalInt;
import java.util.concurrent.CompletionStage;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class ChatControllerTest extends WithServer {
    @Inject
    IMongoDB mongoDB;
    String token;

    @Before
    @Override
    public void startServer() {
        super.startServer();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);
        //Getting the token from a user
        MongoCollection<User> userMongoCollection = mongoDB.getMongoDatabase().getCollection("users", User.class);
        userMongoCollection.insertOne(new User("Muhamet", "Muhamet", singletonList(new Role("muhametId", "USER"))));
        ObjectNode node = Json.newObject();
        node.put("username", "Muhamet");
        node.put("password", "Muhamet");
        Http.RequestBuilder builder = new Http.RequestBuilder().method("POST").bodyJson(node).uri("/api/authenticate/");
        Result userResult = route(app, builder);
        JsonNode tokenNode = Json.parse(contentAsString(userResult));
        Logger.of(Constants.class).debug(contentAsString(userResult));
        Logger.of(Constants.class).debug(tokenNode.get("token").asText());
        token = tokenNode.get("token").asText();
    }

    @After
    @Override
    public void stopServer() {
        super.stopServer();
        mongoDB.getMongoDatabase().drop();
    }

    @Test
    public void testInServer() throws Exception {
        ChatRoom chatRoom = new ChatRoom("backend", singletonList("muhametId"), emptyList());
        OptionalInt optHttpsPort = testServer.getRunningHttpsPort();
        String route = "/api/chat/" + chatRoom.getRoomId() + "/?token=" + token;
        String url;
        int port;
        if (optHttpsPort.isPresent()) {
            port = optHttpsPort.getAsInt();
            url = "ws:/localhost:" + port + route;
        } else {
            port = testServer.getRunningHttpPort().getAsInt();
            url = "ws:/localhost:" + port + route;
        }
        Logger.of(Constants.class).debug(url);
        try (WSClient ws = play.test.WSTestClient.newClient(port)) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(OK, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}