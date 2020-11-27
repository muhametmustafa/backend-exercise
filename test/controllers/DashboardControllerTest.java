package controllers;

import models.Dashboard;
import org.bson.types.ObjectId;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class DashboardControllerTest extends WithApplication {
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNWZiYjhlMzY5YThlYzNiMTQ3NTY5Y2UyIiwiZXhwIjoxNjA2NDI5MjQ2fQ.f34F5rQxweRmaiObU6UFvrbL8WyqMTZ5dpV7NDUxqRY";
    //Muhamet's token!

    @Test
    public void testSaveDashboard() {
        //OK
        Dashboard dashboard = new Dashboard();
        dashboard.setName("Main dashboard!");
        dashboard.setDescription("Main dashboard for all content!");
        Result result = getResult(POST, dashboard,"/api/dashboards/", "Bearer " + token);
        assertEquals(OK, result.status());

        //BAD REQUEST
        Dashboard badDashboard = new Dashboard();
        badDashboard.setDescription("This dashboard doesn't have a name!!");
        Result badDashboardResult = getResult(POST, badDashboard, "/api/dashboards/", "Bearer " + token);
        assertEquals(BAD_REQUEST, badDashboardResult.status());
        assertTrue(contentAsString(badDashboardResult).contains("Name must not be empty"));

        //BAD TOKEN
        String badToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result badTokenResult = getResult(POST, dashboard, "/api/dashboards/", "Bearer " + badToken);
        assertEquals(UNAUTHORIZED, badTokenResult.status());
        assertTrue(contentAsString(badTokenResult).contains("You are not authorized"));

        //FORBIDDEN
        dashboard.setReadACL(Arrays.asList("5fbb8ea89a8ec3b147569d1d")); // The id is not of the user who got authenticated!
        Result forbiddenResult = getResult(POST, dashboard,"/api/dashboards/", "Bearer " + token);
        assertEquals(FORBIDDEN, forbiddenResult.status());
    }

    @Test
    public void updateDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.setName("PRIME Main Dashboard updated!" );
        dashboard.setDescription("PRIME Main dashboard description!"); //updated
        String dashboardId = "5fbfabac7bffd200b7bb6306";

        Result okResult = getResult(PUT, dashboard, "/api/dashboards/" + dashboardId, "Bearer " + token);
        assertEquals(OK, okResult.status());

        //BAD Request Dashboard
        dashboard.setDescription(null);
        Result badRequestResult = getResult(PUT, dashboard, "/api/dashboards/" + dashboardId, "Bearer " + token);
        assertEquals(BAD_REQUEST, badRequestResult.status());

        //BAD TOKEN
        String badToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result badTokenResult = getResult(POST, dashboard, "/api/dashboards/", "Bearer " + badToken);
        assertEquals(UNAUTHORIZED, badTokenResult.status());
        assertTrue(contentAsString(badTokenResult).contains("You are not authorized"));

        //DASHBOARD NOT ACCESSIBLE BY THAT USER
        String frontendDashboardId = "5fbfad657bffd200b7bb63fd";
        dashboard.setId(new ObjectId(frontendDashboardId));
        Result forbiddenResult = getResult(PUT, dashboard, "/api/dashboards/" + frontendDashboardId, "Bearer " + token);
        assertEquals(FORBIDDEN, forbiddenResult.status());
    }

    @Test
    public void findDashboardById() {

        String backendDashboardId = "5fbfae387bffd200b7bb647d";
        //OK
        Result okResult = getResult(token, backendDashboardId);
        assertEquals(OK, okResult.status());

        //NOT AUTHORIZED
        String anotherToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result notAuthorizedResult = getResult(anotherToken, backendDashboardId);
        assertEquals(UNAUTHORIZED, notAuthorizedResult.status());

        //DASHBOARD NOT ACCESSIBLE BY THAT USER
        String frontendDashboardId = "5fbfad657bffd200b7bb63fd";
        Result forbiddenResult = getResult(token, frontendDashboardId);
        assertEquals(FORBIDDEN, forbiddenResult.status());

        //Dashboard not found!
        String dummyDashboardId = "7acfad657bffd200b7bb63fe";
        Result notFoundResult = getResult(token, dummyDashboardId);
        assertEquals(NOT_FOUND, notFoundResult.status());



    }

    @Test
    public void deleteDashboardById(){
        String id = "5fbfb48fb847984e25b78a93";// the id of the inserted dashboard by saveDashboardTest
        //change this when you re test

        //NOT AUTHORIZED
        String anotherToken = token.substring(0,1).toUpperCase() + token.substring(1);
        Result notAuthorizedResult = getResult(anotherToken, id);
        assertEquals(UNAUTHORIZED, notAuthorizedResult.status());

        //Dashboard not found!
        String dummyDashboardId = "7acfad657bffd200b7bb63fe";
        Result notFoundResult = getResult(token, dummyDashboardId);
        assertEquals(NOT_FOUND, notFoundResult.status());

        //DASHBOARD NOT ACCESSIBLE BY THAT USER
        String frontendDashboardId = "5fbfad657bffd200b7bb63fd";
        Result forbiddenResult = getResult(token, frontendDashboardId);
        assertEquals(FORBIDDEN, forbiddenResult.status());

        //OK
        Result okResult = getResult(token, id);
        assertEquals(OK, okResult.status());



    }

    private Result getResult(String token, String backendDashboardId) {
        Http.RequestBuilder builder = new Http.RequestBuilder();
        builder.method(GET)
                .uri("/api/dashboards/" + backendDashboardId)
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
