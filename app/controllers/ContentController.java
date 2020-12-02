package controllers;

import actions.AuthorizationAction;
import com.google.inject.Inject;
import models.Content;
import models.User;
import play.mvc.*;
import services.ContentService;
import services.SerializationService;
import utils.DatabaseUtils;

import java.util.concurrent.CompletableFuture;

public class ContentController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    ContentService contentService;

    @With(AuthorizationAction.class)
    public CompletableFuture<Result> findAllContentByDashboardId(Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return contentService.findContentByDashboardId(id, user)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @With(AuthorizationAction.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> saveContent (Http.Request request, String dashboardId) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose(object -> contentService.saveContent(dashboardId, object, user))
                .thenCompose(content -> serializationService.toJsonNode(content))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @With(AuthorizationAction.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> updateContent(Http.Request request, String contentId) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose(content -> contentService.updateContent(contentId, content, user))
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @With(AuthorizationAction.class)
    public CompletableFuture<Result> deleteContent(Http.Request request, String contentId) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return contentService.deleteContent(contentId, user)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

}
