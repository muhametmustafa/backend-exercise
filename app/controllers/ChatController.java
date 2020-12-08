package controllers;

import actors.ChatActor;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import jwt.JwtValidator;
import models.ChatRoom;
import models.User;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.MongoRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static utils.AccessUtils.isReadable;

@Singleton
public class ChatController extends Controller {
    private static final String USERS_COLLECTION = "users";
    @Inject
    private ActorSystem actorSystem;

    @Inject
    private Materializer materializer;

    @com.google.inject.Inject
    private JwtValidator jwtValidator;

    @com.google.inject.Inject
    private MongoRepository<User> userService;

    private final List<ChatRoom> chatRooms = Arrays.asList(
            //Our frontend Developers
      new ChatRoom("frontend", Arrays.asList("5fbb8e0a9a8ec3b147569cc3", "5fbb8e809a8ec3b147569d0a",
              "5fbb8e939a8ec3b147569d11", "5fbb8e9e9a8ec3b147569d18", "5fbb8ea89a8ec3b147569d1d"), new ArrayList<>()),
            //Our backend developers
      new ChatRoom("backend", Arrays.asList("5fbb8e0a9a8ec3b147569cc3",
              "5fbb8e4a9a8ec3b147569cef", "5fbb8e709a8ec3b147569cff"), Arrays.asList("5fbb8e369a8ec3b147569ce2"))
    );

    public WebSocket chat(String roomId, String token) {
        return WebSocket.Text.acceptOrResult(requestHeader  -> {
            Result result;
            ObjectNode objectNode = jwtValidator.verify(token);
            if(!objectNode.has("userId")) {
                result = unauthorized(objectNode);
                return CompletableFuture.completedFuture(F.Either.Left(result));
            }
            Optional<ChatRoom> optionalChatRoom = chatRooms.stream().filter(next -> next.getRoomId().equals(roomId)).findAny();
            if(!optionalChatRoom.isPresent()) {
                result = notFound(Json.toJson("Cannot find the room with id: " + roomId));
                return CompletableFuture.completedFuture(F.Either.Left(result));
            }

            User user = userService.findById(USERS_COLLECTION, User.class, objectNode.get("userId").asText());

            if(!isReadable(user, optionalChatRoom.get())){
                result = forbidden(Json.toJson("You don't have acces to the chat!"));
                return CompletableFuture.completedFuture(F.Either.Left(result));
            }

            return CompletableFuture.completedFuture(F.Either.Right(
                    ActorFlow.actorRef((out) -> ChatActor.props(out, optionalChatRoom.get(), user), actorSystem, materializer)));
        });
    }

}
