package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import models.*;
import mongo.IMongoDB;
import mongo.InMemoryMongoDB;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;
import static play.test.Helpers.contentAsString;

public class ContentControllerTest extends WithApplication {
    @Inject
    IMongoDB mongoDB;

    String token;
    Dashboard dashboard;
    Content toBeUpdated;
    Content toBeDeleted;

    @Override
    @Before
    public void startPlay() {
        super.startPlay();
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

        //Setup a dashboard and two content that will be updated and deleted while testing
        MongoCollection<Dashboard> dashboardCollection = mongoDB.getMongoDatabase().getCollection("dashboards", Dashboard.class);
        dashboard = new Dashboard("To be updated dashboard!", "This dashboard should be updated!",
                emptyList(), singletonList("muhametId")); // Muhamet can write this resource

        Result dashboardResult = getDashboardResult(POST, dashboard, "Bearer " + token);
        String dashboardId = Json.parse(contentAsString(dashboardResult)).get("id").asText();
        dashboard.setId(new ObjectId(dashboardId));
        toBeUpdated = new Image(dashboardId, "prime-logo",
                singletonList("muhametId"), emptyList());// Muhamet has read only access on this resource
        toBeDeleted = new Text(dashboardId, "Prime introduction text",
                emptyList(), singletonList("muhametId"));// Muhamet has write access on this resource

        Result updateResult = getResult(POST, toBeUpdated,"/api/dashboard/"+ dashboardId +
                "/content/", "Bearer " + token);
        Result deleteResult = getResult(POST, toBeDeleted,"/api/dashboard/"+ dashboardId +
                "/content/", "Bearer " + token);
        //Logger.of(Constants.class).debug(contentAsString(updateResult));
        String toBeUpdatedId = Json.parse(contentAsString(updateResult)).get("id").asText();
        String toBeDeletedId = Json.parse(contentAsString(deleteResult)).get("id").asText();

        toBeUpdated.setId(new ObjectId(toBeUpdatedId));
        toBeDeleted.setId(new ObjectId(toBeDeletedId));
        //Logger.of(Constants.class).debug(toBeDeleted.toString());

    }

    @After
    @Override
    public void stopPlay() {
        super.stopPlay();
        mongoDB.getMongoDatabase().drop();
        //mongoDB.disconnect();
    }

    @Test
    public void saveContentTest() {
        Content email = new Email( "muhamet@prime.io", "PRIME Email subject",
                "PRIME Email text", singletonList("muhametId"), emptyList() );
        //OK
        Result okResult = getResult(POST, email, "/api/dashboard/"+ dashboard.getId().toString() +
                "/content/", "Bearer " + token);
        //Logger.of(Constants.class).debug(contentAsString(okResult));
        assertEquals(OK, okResult.status());

        //BAD REQUEST
        Email  badEmail = new Email();
        badEmail.setText("This email doesn't have a subject and an email!");
        Result badEmailResult = getResult(POST, badEmail, "/api/dashboard/"+ dashboard.getId().toString() +
                "/content/", "Bearer " + token);
        Logger.of(Constants.class).debug(contentAsString(badEmailResult));
        assertEquals(BAD_REQUEST, badEmailResult.status());

        //BAD TOKEN
        String badToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result unAuthorizedResult = getResult(POST, email, "/api/dashboard/"+ dashboard.getId().toString() +
                "/content/", "Bearer " + badToken);
        assertEquals(UNAUTHORIZED, unAuthorizedResult.status());
        assertTrue(contentAsString(unAuthorizedResult).contains("You are not authorized"));
    }
    @Test
    public void updateContentTest() {
        Content image = new Image(dashboard.getId().toString(), "prime-logo-UPDATED",
                singletonList("muhametId"), emptyList());
        //FORBIDDEN
        Result forbiddenResult = getResult(PUT, image, "/api/dashboard/content/"+
                toBeUpdated.getId().toString(), "Bearer " + token);
        //Logger.of(Constants.class).debug(contentAsString(okResult));
        assertEquals(FORBIDDEN, forbiddenResult.status());

        //BAD REQUEST
        Image  badImage = new Image();
        badImage.setDashboardId(dashboard.getId().toString());
        //This image doesn't have an url
        Result badImageResult = getResult(PUT, badImage, "/api/dashboard/content/"+
                toBeUpdated.getId().toString(), "Bearer " + token);
        assertEquals(BAD_REQUEST, badImageResult.status());

        //BAD TOKEN
        String badToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result unAuthorizedResult = getResult(PUT, badImage, "/api/dashboard/content/"+
                toBeUpdated.getId().toString(), "Bearer " + badToken);
        assertEquals(UNAUTHORIZED, unAuthorizedResult.status());
        assertTrue(contentAsString(unAuthorizedResult).contains("You are not authorized"));
    }

    @Test
    public void deleteContent() {

        //NOT AUTHORIZED
        String anotherToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result notAuthorizedResult = getResult(DELETE, anotherToken, toBeDeleted.getId().toString());
        //Logger.of(this.getClass()).debug(contentAsString(notAuthorizedResult));
        assertEquals(UNAUTHORIZED, notAuthorizedResult.status());

        //Dashboard not found!
        String contentId = "7acfad657bffd200b7bb63fe";
        Result notFoundResult = getResult(DELETE, token, contentId);
        Logger.of(Constants.class).debug(contentAsString(notFoundResult));
        assertEquals(NOT_FOUND, notFoundResult.status());

        //The user write access access
        Result okResult = getResult(DELETE, token, toBeDeleted.getId().toString());
        assertEquals(OK, okResult.status());
    }

    @Test
    public void findByDashboardId() {
        //OK
        Result okResult = getResultByDashboardId(token, dashboard.getId().toString());
        assertEquals(OK, okResult.status());

        //NOT AUTHORIZED
        String anotherToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result notAuthorizedResult = getResultByDashboardId(anotherToken, dashboard.getId().toString());
        assertEquals(UNAUTHORIZED, notAuthorizedResult.status());

        //Dashboard not found!
        String dummyDashboardId = "7acfad657bffd200b7bb63fe";
        Result notFoundResult = getResultByDashboardId(token, dummyDashboardId);
        assertEquals(NOT_FOUND, notFoundResult.status());

    }


    private Result getResult(String method, Content content, String uri, String authHeader) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(method)
                .uri(uri)
                .bodyJson(Json.toJson(content))
                .header("Authorization", authHeader)
                .build();
        return route(app, builder);
    }

    private Result getResult(String method, String token, String id) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(method)
                .uri("/api/dashboard/content/" + id)
                .header("Authorization", "Bearer " + token)
                .build();
        return route(app, builder);
    }
    private Result getResultByDashboardId(String token, String id) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(GET)
                .uri("/api/dashboard/" + id + "/content/")
                .header("Authorization", "Bearer " + token)
                .build();
        return route(app, builder);
    }



    private Result getDashboardResult(String method, Dashboard dashboard, String authHeader) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(method)
                .uri("/api/dashboard/")
                .bodyJson(Json.toJson(dashboard))
                .header("Authorization", authHeader)
                .build();
        return route(app, builder);
    }

}
