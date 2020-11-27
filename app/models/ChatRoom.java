package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatRoom implements Accessible{
    private String roomId;
    private List<String> writeACL = new ArrayList<>();
    private List<String> readACL = new ArrayList<>();
}
