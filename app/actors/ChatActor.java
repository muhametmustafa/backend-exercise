package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import models.ChatRoom;
import models.User;

import static utils.AccessUtils.isWritable;

/**
 * Chat Actor - Representing a user in a room!
 */
public class ChatActor extends AbstractActor {
	/**
	 * For logging purposes
	 */
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	/**
	 * String messages as constants
	 */
	private static final String JOINED_ROOM = " joined the Room!";
	private static final String LEFT_ROOM = " left the Room!";
	private static final String PING = "PING";
	private static final String PONG = "PONG";

	/**
	 * Mediator
	 */
	private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
	/**
	 * Room ID to pub/sub
	 */
	private ChatRoom chatRoom;
	/**
	 * Web socket represented from the front end
	 */
	private ActorRef out;
	/**
	 * The user represented by the actor
	 */
	private User user;

	public static Props props(ActorRef out, ChatRoom roomId, User user) {
		return Props.create(ChatActor.class, () -> new ChatActor(out, roomId, user));
	}

	private ChatActor(ActorRef out, ChatRoom roomId, User user) {
		this.chatRoom = roomId;
		this.out = out;
		this.user = user;
		mediator.tell(new DistributedPubSubMediator.Subscribe(chatRoom.getRoomId(), getSelf()), getSelf());
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(String.class, this::onMessageReceived)
				.match(ChatActorProtocol.ChatMessage.class, this::onChatMessageReceived)
				.match(DistributedPubSubMediator.SubscribeAck.class, this::onSubscribe)
				.match(DistributedPubSubMediator.UnsubscribeAck.class, this::onUnsubscribe)
				.build();
	}

	/**
	 * Receiver of socket messages comming from the front end
	 * @param message
	 */
	public void onMessageReceived (String message) {
		if (message.equals(PING)) {
			out.tell(PONG, getSelf());
			return;
		}
		if(isWritable(user, chatRoom)){
			broadcast(String.format("%s is saying: %s", user.getUsername(), message));
		}
		else {
			String notWritable = "You cannot send messages in this chat room!";
			out.tell(notWritable, getSelf());
		}

	}

	/**
	 * Chat Message Protocol message receiver
	 * @param what
	 */
	public void onChatMessageReceived (ChatActorProtocol.ChatMessage what) {
		// Don't send messages back that came from this socket
		if (getSender().equals(getSelf())) {
			return;
		}
		String message = what.getMessage();
		out.tell(message, getSelf());
	}

	/**
	 * When a subscribe message is received, this method gets called
	 * @param message
	 */
	public void onSubscribe (DistributedPubSubMediator.SubscribeAck message) {
			this.joinTheRoom();
	}

	/**
	 * When an unsubscribe message is received, this method gets called
	 * @param message
	 */
	public void onUnsubscribe (DistributedPubSubMediator.UnsubscribeAck message) {
		this.leaveTheRoom();
	}

	/**
	 * When the actor is shutting down, let the others know that I've left the room!
	 */
	@Override
	public void postStop() {
		this.leaveTheRoom();
	}


	/**
	 * Sends a simple JOINED_ROOM message
	 */
	private void joinTheRoom () {
		this.broadcast(user.getUsername() + JOINED_ROOM);
	}

	/**
	 * Sends a simple LEFT_ROOM message
	 */
	private void leaveTheRoom () {
		this.broadcast(user.getUsername() + LEFT_ROOM);
	}

	/**
	 * Publish message to the current room
	 * @param message
	 */
	private void broadcast (String message) {
		mediator.tell(
			new DistributedPubSubMediator.Publish(chatRoom.getRoomId(), new ChatActorProtocol.ChatMessage(message)),
			getSelf()
		);
	}

}
