package services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import exceptions.RequestException;
import models.User;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import sun.nio.cs.US_ASCII;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Singleton
public class UserService {

    @Inject
    HttpExecutionContext ec;
    private final String USERS_COLLECTION = "users";

    @Inject
    private BaseService<User> userService;

    public CompletableFuture<List<User>> findAllUsers() {
        return CompletableFuture.supplyAsync(() -> userService.findAll(USERS_COLLECTION, User.class),
                ec.current());
    }

    public CompletableFuture<User> findUserById(String id) {
        return CompletableFuture.supplyAsync(() -> userService.findById(USERS_COLLECTION, User.class, id),
                ec.current());
    }

    public CompletableFuture<User> findUserByUsername (String username) {
        return CompletableFuture.supplyAsync(() -> {
            List<User> allUsers = findAllUsers().join();
            Optional<User> optionalUser = allUsers
                    .stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findAny();
            return optionalUser.orElse(null);
        }, ec.current());
    }

    public CompletableFuture<User> deleteUserById(String id) {
        return CompletableFuture.supplyAsync(() -> userService.deleteById(USERS_COLLECTION, User.class, id),
                ec.current());
    }

    public CompletableFuture<User> saveUser(User user) {
        Logger.of(this.getClass()).error(user.toString());
        User foundUser = findUserByUsername(user.getUsername()).join();
        //Logger.of(this.getClass()).error(foundUser.toString());
        if(foundUser != null) {
            throw new RequestException(Http.Status.CONFLICT, "This username is not free");
        }

        return CompletableFuture.supplyAsync(() -> userService.save(USERS_COLLECTION, User.class, user),
                ec.current());
    }

    public CompletableFuture<User> updateUser (String id, User user) {
        return CompletableFuture.supplyAsync(() -> userService.update(USERS_COLLECTION, User.class, id, user),
                ec.current());
    }




}
