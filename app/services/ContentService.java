package services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import exceptions.RequestException;
import models.Content;
import models.User;
import mongo.IMongoDB;
import play.libs.concurrent.HttpExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static play.mvc.Http.Status.FORBIDDEN;
import static utils.AccessUtils.isReadable;
import static utils.AccessUtils.isWritable;

@Singleton
public class ContentService {
    @Inject
    HttpExecutionContext ec;
    @Inject
    IMongoDB mongoDB;
    @Inject
    private MongoRepository<Content> contentRepository;
    @Inject
    private DashboardService dashboardService;

    private final String CONTENT_COLLECTION = "content";

    private MongoCollection<Content> getContentCollection() {
        return mongoDB.getMongoDatabase().getCollection(CONTENT_COLLECTION, Content.class);
    }

    public CompletableFuture<List<Content>> findContentByDashboardId(String dashboardId, User user) {
            return dashboardService.findDashboardById(dashboardId, user)
                    .thenApply(dashboard -> {
                        MongoCollection<Content> collection = getContentCollection();
                        return collection.find()
                                .filter(Filters.eq("dashboardId", dashboardId))
                                .into(new ArrayList<>())
                                .stream()
                                .filter(content -> isReadable(user, content))
                                .collect(Collectors.toList());
                    });
    }
    public CompletableFuture<Content> saveContent(String dashboardId, Content content, User user) {
            return dashboardService.findDashboardById(dashboardId, user)
                    .thenApply(dashboard -> {
                        content.setDashboardId(dashboardId);
                        return contentRepository.save(CONTENT_COLLECTION, Content.class, content);
                    });
    }

    public CompletableFuture<Content> updateContent(String id, Content content, User user) {
        return CompletableFuture.supplyAsync(() -> {
            if(!isWritable(user, content)) {
                throw new CompletionException(new RequestException(FORBIDDEN,
                        "This user doesn't have read access on this dashboard!"));
            }
            return contentRepository.update(CONTENT_COLLECTION, Content.class, id, content);
        }, ec.current());
    }

    public CompletableFuture<Content> deleteContent (String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            Content content = contentRepository.findById(CONTENT_COLLECTION, Content.class, id);
            if(!isWritable(user, content)) {
                throw new CompletionException(new RequestException(FORBIDDEN,
                        "This user doesn't have read access on this dashboard!"));
            }
            return contentRepository.deleteById(CONTENT_COLLECTION, Content.class, id);
        }, ec.current());
    }
}
