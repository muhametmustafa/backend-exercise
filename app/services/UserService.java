package services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.RequestException;
import models.User;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
public class UserService {

    @Inject
    HttpExecutionContext ec;
    private final String USERS_COLLECTION = "users";

    @Inject
    private MongoRepository<User> userService;

    public CompletableFuture<List<User>> findAllUsers() {
        return CompletableFuture.supplyAsync(() -> userService.findAll(USERS_COLLECTION, User.class),
                ec.current());
    }

    public CompletableFuture<User> findUserById(String id) {
        return CompletableFuture.supplyAsync(() -> userService.findById(USERS_COLLECTION, User.class, id),
                ec.current());
    }

    public CompletableFuture<User> findUserByUsername (String username) {
        return findAllUsers().thenApply(users -> {
            Optional<User> optionalUser = users
                    .stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findAny();
            return optionalUser.orElse(null);
        });
    }

    public CompletableFuture<User> deleteUserById(String id) {
        return CompletableFuture.supplyAsync(() -> userService.deleteById(USERS_COLLECTION, User.class, id),
                ec.current());
    }

    public CompletableFuture<User> saveUser(User user) {
        return findUserByUsername(user.getUsername()).thenApply(foundUser -> {
            if(foundUser != null) {
                throw new RequestException(Http.Status.CONFLICT, "This username is not free");
            }
            return userService.save(USERS_COLLECTION, User.class, user);
        });
    }

    public CompletableFuture<User> updateUser (String id, User user) {
        return CompletableFuture.supplyAsync(() -> userService.update(USERS_COLLECTION, User.class, id, user),
                ec.current());
    }




}
