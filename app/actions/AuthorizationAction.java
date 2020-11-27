package actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import jwt.JwtValidator;
import models.User;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import services.UserService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthorizationAction extends Action.Simple{
    public static class Attrs {
        public static final TypedKey<User> USER = TypedKey.create("user");
    }
    @Inject
    JwtValidator jwtValidator;
    @Inject
    UserService userService;

    @Override
    public CompletionStage<Result> call(Http.Request req) {
        ObjectNode objectNode = jwtValidator.validateJwt(req);
        if(!objectNode.has("userId")){
            return CompletableFuture.completedFuture(unauthorized(objectNode));
        }
        User user = userService.findUserById(objectNode.get("userId").asText()).join();
        return delegate.call(req.addAttr(Attrs.USER, user));
    }


}
