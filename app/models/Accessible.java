package models;

import java.util.List;

public interface Accessible {
    List<String> getReadACL();
    List<String> getWriteACL();

}
