package controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import jwt.JwtValidator;
import models.User;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.UserService;

import javax.inject.Inject;
import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

public class AuthorizationController extends Controller {

    @Inject
    JwtValidator jwtValidator;

    @Inject
    private Config config;

    @Inject
    UserService userService;


    public CompletableFuture<Result> authenticate(Http.Request request) {
        User bodyUser = Json.fromJson(request.body().asJson(), User.class);
        return userService.findUserByUsername(bodyUser.getUsername()).thenApply(user -> {
            ObjectNode node = Json.newObject();
            if (user == null || !bodyUser.getPassword().equals(user.getPassword())) {
                node.put("error", "Credentials do not match!");
                return notFound(node);
            }
            node.put("token", getSignedToken(user.getId()));
            return ok(Json.toJson(node));
        });
    }

    private String getSignedToken(ObjectId userId) {
        String secret = config.getString("play.http.secret.key");
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withClaim("user_id", userId.toString())
                .withExpiresAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(60*8).toInstant())) //8 working hours
                .sign(algorithm);
    }

    public CompletableFuture<Result> validateJwtResult(Http.Request request) {
        return CompletableFuture.completedFuture(ok(jwtValidator.validateJwt(request)));
    }

}


