package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import models.Dashboard;
import models.Role;
import models.User;
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


public class DashboardControllerTest extends WithApplication {
    @Inject
    IMongoDB mongoDB;

    String token;
    Dashboard toBeUpdated;
    Dashboard toBeDeleted;

    @Override
    @Before
    public void startPlay() {
        super.startPlay();
        mongoDB = app.injector().instanceOf(InMemoryMongoDB.class);
        //Getting the token from a user
        token = getToken(new User("Muhamet", "Muhamet", singletonList(new Role("muhametId", "USER"))));

        //Setup two dashboards that will be updated and deleted while testing
        MongoCollection<Dashboard> dashboardCollection = mongoDB.getMongoDatabase().getCollection("dashboards", Dashboard.class);
        toBeUpdated = new Dashboard("To be updated dashboard!", "This dashboard should be updated!",
                emptyList(), singletonList("muhametId")); // Muhamet can write this resource
        toBeDeleted = new Dashboard("To be deleted dashboard", "This dashboard should be deleted!",
                singletonList("muhametId"), emptyList()); // Muhamet can only read this resource

        Result updateResult = getResult(POST, toBeUpdated,"/api/dashboard/", "Bearer " + token);
        Result deleteResult = getResult(POST, toBeDeleted,"/api/dashboard/", "Bearer " + token);


        String toBeUpdatedId = Json.parse(contentAsString(updateResult)).get("id").asText();
        String toBeDeletedId = Json.parse(contentAsString(deleteResult)).get("id").asText();

        toBeUpdated.setId(new ObjectId(toBeUpdatedId));
        toBeDeleted.setId(new ObjectId(toBeDeletedId));
        //Logger.of(Constants.class).debug(toBeDeleted.toString());



    }

    private String getToken(User user) {
        MongoCollection<User> userMongoCollection = mongoDB.getMongoDatabase().getCollection("users", User.class);
        //userMongoCollection.insertOne(new User("Muhamet", "Muhamet", singletonList(new Role("muhametId", "USER"))));
        userMongoCollection.insertOne(user);
        ObjectNode node = Json.newObject();
        node.put("username", user.getUsername());
        node.put("password", user.getPassword());
        Http.RequestBuilder builder = new Http.RequestBuilder().method("POST").bodyJson(node).uri("/api/authenticate/");
        Result userResult = route(app, builder);
        JsonNode tokenNode = Json.parse(contentAsString(userResult));
        Logger.of(Constants.class).debug(contentAsString(userResult));
        Logger.of(Constants.class).debug(tokenNode.get("token").asText());
        return tokenNode.get("token").asText();
    }

    @After
    @Override
    public void stopPlay() {
        super.stopPlay();
        mongoDB.getMongoDatabase().drop();
        //mongoDB.disconnect();
    }


    @Test
    public void testSaveDashboard() {

        //OK
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Main dashboard!");
        dashboard.setDescription("Main dashboard for all content!");
        //Logger.of(Constants.class).debug("Dashboard, {}", dashboard);
        Result result = getResult(POST, dashboard, "/api/dashboard/", "Bearer " + token);
        Logger.of(Constants.class).debug(contentAsString(result));
        assertEquals(OK, result.status());
    }
    @Test
    public void testSaveBadDashboard() {
        //BAD REQUEST
        Dashboard badDashboard = new Dashboard();
        badDashboard.setDescription("This dashboard doesn't have a name!!");
        Result badDashboardResult = getResult(POST, badDashboard, "/api/dashboard/", "Bearer " + token);
        assertEquals(BAD_REQUEST, badDashboardResult.status());
        assertTrue(contentAsString(badDashboardResult).contains("Name must not be empty"));
    }
    @Test
    public void testSaveBadTokenDashboard() {
        //BAD TOKEN
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Main dashboard!");
        dashboard.setDescription("Main dashboard for all content!");
        String badToken = token.substring(0, 1).toUpperCase() + token.substring(1);
        Result badTokenResult = getResult(POST, dashboard, "/api/dashboard/", "Bearer " + badToken);
        assertEquals(UNAUTHORIZED, badTokenResult.status());
        assertTrue(contentAsString(badTokenResult).contains("You are not authorized"));
    }

    @Test
    public void testUpdateDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Updated dashboard");
        dashboard.setDescription("New description of dashboard"); //updated
        String dashboardId = toBeUpdated.getId().toString();
        //Logger.of(this.getClass()).debug(token);
        Result okResult = getResult(PUT, dashboard, "/api/dashboard/" + dashboardId, "Bearer " + token);
        //Logger.of(Constants.class).debug(contentAsString(okResult));
        assertEquals(OK, okResult.status());
    }
    @Test
    public void testUpdateBadDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.setName("This dashboard does not have a description");
        String dashboardId = toBeUpdated.getId().toString();
        //BAD Request Dashboard
        //Logger.of(this.getClass()).debug(dashboard.toString());
        Result badRequestResult = getResult(PUT, dashboard, "/api/dashboard/" + dashboardId, "Bearer " + token);
        //Logger.of(Constants.class).debug(contentAsString(badRequestResult));
        assertEquals(BAD_REQUEST, badRequestResult.status());
    }
    @Test
    public void testUpdateDashboardBadToken() {
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Main dashboard!");
        dashboard.setDescription("Main dashboard for all content!");
        //BAD TOKEN
        String badToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result badTokenResult = getResult(POST, dashboard, "/api/dashboard/", "Bearer " + badToken);
        assertEquals(UNAUTHORIZED, badTokenResult.status());
        assertTrue(contentAsString(badTokenResult).contains("You are not authorized"));

    }

    @Test
    public void testDeleteDashboardByIdUnauthorized() {

        //NOT AUTHORIZED
        String anotherToken = token.substring(0, 1).toUpperCase() + token.substring(1);
        Result notAuthorizedResult = getResult(DELETE, anotherToken, toBeDeleted.getId().toString());
        //Logger.of(this.getClass()).debug(contentAsString(notAuthorizedResult));
        assertEquals(UNAUTHORIZED, notAuthorizedResult.status());
    }
    @Test
    public void testDeleteDashboardByIdNotFound() {
        //Dashboard not found!
        String dummyDashboardId = "7acfad657bffd200b7bb63fe";
        Result notFoundResult = getResult(DELETE, token, dummyDashboardId);
        assertEquals(NOT_FOUND, notFoundResult.status());
    }
    @Test
    public  void testDeleteDashboardByIdForbidden() {
        //The user has read only access
        Result forbiddenResult = getResult(DELETE, token, toBeDeleted.getId().toString());
        Logger.of(this.getClass()).debug(toBeDeleted.toString());
        Logger.of(this.getClass()).debug(contentAsString(forbiddenResult));
        assertEquals(FORBIDDEN, forbiddenResult.status());
    }
    @Test
    public void ztestDeleteDashboardById(){
        //OK
        Result okResult = getResult(DELETE, token, toBeUpdated.getId().toString());
        assertEquals(OK, okResult.status());

    }

    @Test
    public void findDashboardById() {
        //OK
        Result okResult = getResult(GET, token, toBeUpdated.getId().toString());
        assertEquals(OK, okResult.status());
    }
    @Test
    public void findDashboardByIdUnauthorized() {
        //NOT AUTHORIZED
        String anotherToken = token.substring(0, 1).toUpperCase() + token.substring(1);
        Result notAuthorizedResult = getResult(GET, anotherToken, toBeUpdated.getId().toString());
        assertEquals(UNAUTHORIZED, notAuthorizedResult.status());
    }
    @Test
    public void findDashboardByIdNotFound(){
        //Dashboard not found!
        String dummyDashboardId = "7acfad657bffd200b7bb63fe";
        Result notFoundResult = getResult(GET, token, dummyDashboardId);
        assertEquals(NOT_FOUND, notFoundResult.status());

    }

    private Result getResult(String method,String token, String id) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(method)
                .uri("/api/dashboard/" + id)
                .header("Authorization", "Bearer " + token)
                .build();
        return route(app, builder);
    }


    private Result getResult(String method, Dashboard dashboard, String uri, String authHeader) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(method)
                .uri(uri)
                .bodyJson(Json.toJson(dashboard))
                .header("Authorization", authHeader)
                .build();
        return route(app, builder);
    }

}
