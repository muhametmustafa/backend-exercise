package services;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.RequestException;
import models.Dashboard;
import models.User;
import models.validators.HibernateValidator;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.ok;
import static utils.AccessUtils.*;

@Singleton
public class DashboardService {
    @Inject
    HttpExecutionContext ec;
    private final String DASHBOARD_COLLECTION = "dashboards";
    @Inject
    private BaseService<Dashboard> dashboardService;


    public CompletableFuture<List<Dashboard>> findAllDashboards () {
        return CompletableFuture.supplyAsync(() -> dashboardService.findAll(DASHBOARD_COLLECTION, Dashboard.class), ec.current());
    }

    public CompletableFuture<Result> findHierarchicDashboardById(String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard = dashboardService.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(isWritable(user, dashboard)) {
                List<Dashboard> allDashboards = findAllDashboards().join();
                buildHierarchyTree(dashboard, allDashboards);
                return ok(Json.toJson(dashboard));
            }
            return forbidden(readErrorNode());
        }, ec.current());
    }


    public CompletableFuture<Result> findDashboardById(String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard =  dashboardService.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(isReadable(user, dashboard)){
                return ok(Json.toJson(dashboard));
            }
            return forbidden(readErrorNode());
        }, ec.current());
    }


    public CompletableFuture<Result> deleteDashboardById(String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard = dashboardService.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(isWritable(user, dashboard)) {
                Dashboard deletedDashboard = dashboardService.deleteById(DASHBOARD_COLLECTION, Dashboard.class, id);
                return ok(Json.toJson(deletedDashboard));
            }
            return forbidden(writeErrorNode());
        }, ec.current());
    }

    public CompletableFuture<Result> saveDashboard(Dashboard dashboard, User user) {
        return CompletableFuture.supplyAsync(() -> {
            dashboard.setTimestamp(new Date().getTime());
            if(isWritable(user, dashboard)) {
                Dashboard savedDashboard = dashboardService.save(DASHBOARD_COLLECTION, Dashboard.class, dashboard);
                return ok(Json.toJson(savedDashboard));
            }
            return forbidden(writeErrorNode());
        }, ec.current());
    }


    public CompletableFuture<Result> updateDashboard(String id, Dashboard dashboardToBeUpdated, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard = dashboardService.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(isWritable(user, dashboard)) {
                Dashboard updatedDashBoard = dashboardService.update(DASHBOARD_COLLECTION, Dashboard.class, id, dashboardToBeUpdated);
                return ok(Json.toJson(updatedDashBoard));
            }
            return forbidden(writeErrorNode());
        }, ec.current());

    }

    private void buildHierarchyTree(Dashboard dashboard, List<Dashboard> allDashboards) {
        List<Dashboard> children = allDashboards
                .stream()
                .filter(next -> dashboard.getId().toString().equals(next.getParentId()))
                .collect(Collectors.toList());
        dashboard.setChildren(children);
        if(!children.isEmpty()) {
            for (Dashboard child : children) {
                buildHierarchyTree(child, allDashboards);
            }
        }
    }

    /**
     * Gets the hierarchy for all dashboards
     * @return nested list of top level parent dashboards.
     */
    /*public CompletableFuture<List<Dashboard>> findHierarchicDashboards() {
        return CompletableFuture.supplyAsync(() -> {
            List<Dashboard> allDashboards = findAllDashboards().join();
            List<Dashboard> parentDashboards = allDashboards
                    .stream()
                    .filter(next -> next.getParentId() == null)
                    .collect(Collectors.toList());
            for (Dashboard dashboard : parentDashboards){
                buildHierarchyTree(dashboard, allDashboards);
            }
            return parentDashboards;
        }, ec.current());
    }*/

}
