package services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import models.Content;
import models.Dashboard;
import models.User;
import mongo.IMongoDB;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static play.mvc.Results.forbidden;
import static play.mvc.Results.ok;
import static utils.AccessUtils.*;

@Singleton
public class ContentService {
    @Inject
    HttpExecutionContext ec;
    @Inject
    IMongoDB mongoDB;
    @Inject
    private BaseService<Content> contentService;
    @Inject
    private BaseService<Dashboard> dashboardService;

    private final String CONTENT_COLLECTION = "content";
    private final String DASHBOARD_COLLECTION = "dashboards";

    private MongoCollection<Content> getContentCollection() {
        return mongoDB.getMongoDatabase().getCollection(CONTENT_COLLECTION, Content.class);
    }

    public CompletableFuture<Result> findContentByDashboardId(String dashboardId, User user) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<Content> collection = getContentCollection();
            List<Content> contentList = collection.find()
                    .filter(Filters.eq("dashboardId", dashboardId))
                    .into(new ArrayList<>());
            Dashboard dashboard = dashboardService.findById(DASHBOARD_COLLECTION, Dashboard.class, dashboardId);
            if(isReadable(user, dashboard)){
                List<Content> readableContent = contentList
                        .stream()
                        .filter(content -> isReadable(user, content))
                        .collect(Collectors.toList());
                return ok(Json.toJson(readableContent));
            }
            return forbidden(readErrorNode());
        }, ec.current());
    }

    public CompletableFuture<Content> saveContent(String dashboardId, Content content, User user) {
        return CompletableFuture.supplyAsync(()-> {
            dashboardService.findById(DASHBOARD_COLLECTION, Dashboard.class, dashboardId);
            //Above object is created only to check if dashboard exists or it's id is valid!
            content.setDashboardId(dashboardId);
            return contentService.save(CONTENT_COLLECTION, Content.class, content);
        }, ec.current());
    }

    public CompletableFuture<Result> updateContent (String id, Content content, User user) {
        return CompletableFuture.supplyAsync(() -> {
            if(isWritable(user, content)) {
                Content updatedContent = contentService.update(CONTENT_COLLECTION, Content.class, id, content);
                return ok(Json.toJson(updatedContent));
            }
            return forbidden(writeErrorNode());
        }, ec.current());
    }

    public CompletableFuture<Result> deleteContent (String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Content content = contentService.findById(CONTENT_COLLECTION, Content.class, id);
            if(isWritable(user, content)) {
                Content deletedContent = contentService.deleteById(CONTENT_COLLECTION, Content.class, id);
                return ok(Json.toJson(deletedContent));
            }
            return forbidden(writeErrorNode());
        }, ec.current());
    }
}
