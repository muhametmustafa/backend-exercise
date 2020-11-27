package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Role;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;

public class AuthorizationControllerTest extends WithApplication {
    User user;



    @Test
    public void testAuthenticate() {
        ObjectNode node = Json.newObject();
        node.put("username", "Agon");
        node.put("password", "Lohaj");

        Result result = getResult(node, "/api/authenticate/", POST);
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("token"));

        ObjectNode notFoundNode = Json.newObject();
        notFoundNode.put("username", "Agon");
        notFoundNode.put("password", "Agon"); // password mismatch!
        Result notFoundResult = getResult(notFoundNode, "/api/authenticate/", POST);
        assertEquals(NOT_FOUND, notFoundResult.status());
        assertTrue(contentAsString(notFoundResult).contains("error"));


    }

    private Result getResult(JsonNode node, String uri, String method) {
        Http.RequestBuilder requestBuilder = new Http.RequestBuilder();
        requestBuilder.method(method)
                .bodyJson(node)
                .uri(uri)
                .build();
        return route(app, requestBuilder);
    }


}
