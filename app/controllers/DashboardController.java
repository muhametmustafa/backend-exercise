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


public class DashboardController extends Controller {

    @Inject
    private DashboardService dashboardService;

    @Inject
    private SerializationService serializationService;

    @With(AuthorizationAction.class)
    public CompletableFuture<Result> findDashboardById (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.findDashboardById(id, user)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @With(AuthorizationAction.class)
    public CompletableFuture<Result> findHierarchicDashboardById (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.findHierarchicDashboardById(id, user)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @With(AuthorizationAction.class)
    public CompletableFuture<Result> deleteDashboard (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return dashboardService.deleteDashboardById(id, user)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @With(AuthorizationAction.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> saveDashboard (Http.Request request) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> dashboardService.saveDashboard(dashboard, user))
                .thenCompose(dashboard -> serializationService.toJsonNode(dashboard))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @With(AuthorizationAction.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> updateDashboard (Http.Request request, String id) {
        User user = request.attrs().get(AuthorizationAction.Attrs.USER);
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(dashboard -> dashboardService.updateDashboard(id, dashboard, user))
                .exceptionally(DatabaseUtils::throwableToResult);
    }

        public CompletableFuture<Result> findHierarchicDashboards (Http.Request request) {
        return dashboardService.findHierarchicDashboards()
                .thenCompose(dashboard -> serializationService.toJsonNode(dashboard))
                .thenApply(Results::ok)
                 .exceptionally(DatabaseUtils::throwableToResult);
        }

    public CompletableFuture<Result> findAllDashboards(Http.Request request) {
        return dashboardService.findAllDashboards()
                .thenCompose(dashboards -> serializationService.toJsonNode(dashboards))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
