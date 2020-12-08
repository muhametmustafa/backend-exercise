package controllers;

import actions.AuthorizationAction;
import com.google.inject.Inject;
import models.Dashboard;
import models.User;
import play.mvc.*;
import services.DashboardService;
import services.SerializationService;
import utils.DatabaseUtils;

import java.util.concurrent.CompletableFuture;

@With(AuthorizationAction.class)
public class DashboardController extends Controller {

    @Inject
    private DashboardService dashboardService;

    @Inject
    private SerializationService serializationService;


    public CompletableFuture<Result> findDashboardById (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.findDashboardById(id, user)
                .thenCompose(serializationService::toJsonNode)
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> findHierarchicDashboardById (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.findHierarchicDashboardById(id, user)
                .thenCompose(serializationService::toJsonNode)
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> deleteDashboard (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.deleteDashboardById(id, user)
                .thenCompose(dashboard -> serializationService.toJsonNode(dashboard))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> saveDashboard (Http.Request request) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> dashboardService.saveDashboard(dashboard, user))
                .thenCompose(dashboard -> serializationService.toJsonNode(dashboard))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> updateDashboard (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> dashboardService.updateDashboard(id, dashboard, user))
                .thenCompose(serializationService::toJsonNode)
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    public CompletableFuture<Result> findHierarchicDashboards (Http.Request request) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.findHierarchicDashboards(user)
                .thenCompose(dashboard -> serializationService.toJsonNode(dashboard))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
        }

    public CompletableFuture<Result> findAllDashboards(Http.Request request) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.findAllDashboards(user)
                .thenCompose(dashboards -> serializationService.toJsonNode(dashboards))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
