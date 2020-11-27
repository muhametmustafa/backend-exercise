package utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Accessible;
import models.Role;
import models.User;
import play.libs.Json;

import java.util.List;
import java.util.stream.Collectors;

public class AccessUtils {
    private static final String WRITE_ERROR = "You don't have access to write this resource!";
    private static final String READ_ERROR = "You don't have access to read this resource!";

    /**
     * Determines if the user can write on this resource. He can write on the resource if
     * dashboard is public or his role or id is tagged on writeACL and readACL arrays
     * @param user that is signed in
     * @param accessible resource
     * @return true if resource is writable by this user, false if it's not
     */
    public static boolean isWritable(User user, Accessible accessible) {
        return isPublic(accessible) || isTagged(user, accessible.getWriteACL()) ;
    }
    public static boolean isReadable(User user, Accessible accessible) {
        return isPublic(accessible) || isTagged(user, accessible.getReadACL())
                || isTagged(user, accessible.getWriteACL()) ;
    }

    private static boolean isPublic(Accessible accessible) {
        return accessible.getWriteACL().isEmpty() && accessible.getReadACL().isEmpty();
    }

    private static boolean isTagged(User user, List<String> accessTags) {
        List<String> userRoles = user.getRoles()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toList());;
        return accessTags
                .stream()
                .anyMatch(tag -> userRoles.contains(tag) || tag.equals(user.getId().toString()));
        /*for (String tag : accessTags){
            if(userRoles.contains(tag) || tag.equals(user.getId().toString())){
                return true;
            }
        }
        return false;*/
    }


    public static ObjectNode writeErrorNode() {
        ObjectNode node = Json.newObject();
        return node.put("error", WRITE_ERROR);
    }

    public static ObjectNode readErrorNode() {
        ObjectNode node = Json.newObject();
        return node.put("error", READ_ERROR);
    }
}
