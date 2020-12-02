package controllers;

import com.google.inject.Inject;
import models.User;
import mongo.IMongoDB;
import org.bson.Document;
import play.libs.Json;
import play.mvc.*;
import services.SerializationService;
import services.UserService;
import utils.DatabaseUtils;

import java.util.concurrent.CompletableFuture;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    @Inject
    IMongoDB mongoDB;

    @Inject
    UserService userService;

    @Inject
    SerializationService serializationService;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    public CompletableFuture<Result> findAllUsers(Http.Request request) {
        return userService.findAllUsers()
                .thenCompose(users -> serializationService.toJsonNode(users))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> findUserById(Http.Request request, String id) {
        return userService.findUserById(id)
                .thenCompose(user -> serializationService.toJsonNode(user))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> deleteUserById (Http.Request request, String id) {
        return userService.deleteUserById(id)
                .thenCompose(user -> serializationService.toJsonNode(user))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> saveUser (Http.Request request) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(user -> userService.saveUser(user))
                .thenCompose(user -> serializationService.toJsonNode(user))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> updateUser (Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(user -> userService.updateUser(id, user))
                .thenCompose(user -> serializationService.toJsonNode(user))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }






}
