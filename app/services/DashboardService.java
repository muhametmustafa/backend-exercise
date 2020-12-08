package services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.RequestException;
import models.Dashboard;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static play.mvc.Http.Status.FORBIDDEN;
import static utils.AccessUtils.isReadable;
import static utils.AccessUtils.isWritable;

@Singleton
public class DashboardService {
    @Inject
    HttpExecutionContext ec;
    private final String DASHBOARD_COLLECTION = "dashboards";
    @Inject
    private MongoRepository<Dashboard> dashboardRepository;


    public CompletableFuture<List<Dashboard>> findAllDashboards (User user) {
        return CompletableFuture.supplyAsync(() -> {
            List<Dashboard> allDashboards = dashboardRepository.findAll(DASHBOARD_COLLECTION, Dashboard.class);
            return allDashboards
                    .stream()
                    .filter(dashboard -> isReadable(user, dashboard))
                    .collect(Collectors.toList());
        }, ec.current());
    }

    public CompletableFuture<Dashboard> findHierarchicDashboardById(String id, User user) {
            Dashboard dashboard = dashboardRepository.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(!isReadable(user, dashboard)) {
                throw new CompletionException(new RequestException(FORBIDDEN,
                        "This user doesn't have read access on this dashboard!"));
            }
            return findAllDashboards(user)
                    .thenApply(allDashboards -> {
                        buildHierarchyTree(dashboard, allDashboards, user);
                        return dashboard;
                    });
    }

    public CompletableFuture<Dashboard> findDashboardById(String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard =  dashboardRepository.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(!isReadable(user, dashboard)){
                throw new CompletionException(new RequestException(FORBIDDEN,
                        "This user doesn't have read access on this dashboard!"));
            }
            return dashboard;
        }, ec.current());
    }

    public CompletableFuture<Dashboard> deleteDashboardById(String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard = dashboardRepository.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(!isWritable(user, dashboard)) {
                throw new CompletionException(new RequestException(Http.Status.FORBIDDEN,
                        "This user doesn't have write access on this dashboard!"));
            }
            return dashboardRepository.deleteById(DASHBOARD_COLLECTION, Dashboard.class, id);
        }, ec.current());
    }

    public CompletableFuture<Dashboard> saveDashboard(Dashboard dashboard, User user) {
        return CompletableFuture.supplyAsync(() -> {
            dashboard.setTimestamp(new Date().getTime());
            return dashboardRepository.save(DASHBOARD_COLLECTION, Dashboard.class, dashboard);
        }, ec.current());
    }


    public CompletableFuture<Dashboard> updateDashboard(String id, Dashboard dashboardToBeUpdated, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Dashboard dashboard = dashboardRepository.findById(DASHBOARD_COLLECTION, Dashboard.class, id);
            if(!isWritable(user, dashboard)) {
                throw new CompletionException(new RequestException(Http.Status.FORBIDDEN,
                        "This user doesn't have write access on this dashboard!"));
            }
            return dashboardRepository.update(DASHBOARD_COLLECTION, Dashboard.class, id, dashboardToBeUpdated);
        }, ec.current());

    }

    private void buildHierarchyTree(Dashboard dashboard, List<Dashboard> allDashboards, User user) {
        List<Dashboard> children = allDashboards
                .stream()
                .filter(next -> dashboard.getId().toString().equals(next.getParentId()))
                .filter(next -> isReadable(user, next))
                .collect(Collectors.toList());
        dashboard.setChildren(children);
        if(!children.isEmpty()) {
            for (Dashboard child : children) {
                buildHierarchyTree(child, allDashboards, user);
            }
        }
    }

    /**
     * Gets the hierarchy for all dashboards
     * @return nested list of top level parent dashboards.
     */
    public CompletableFuture<List<Dashboard>> findHierarchicDashboards(User user) {
        return findAllDashboards(user).thenApply(allDashboards -> {
            List<Dashboard> parentDashboards = allDashboards
                    .stream()
                    .filter(next -> next.getParentId() == null)
                    .collect(Collectors.toList());
            for (Dashboard dashboard : parentDashboards){
                buildHierarchyTree(dashboard, allDashboards, user);
            }
            return parentDashboards;
        });
    }

}
