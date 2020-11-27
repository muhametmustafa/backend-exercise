package models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class Role {
    @NotEmpty
    private String id;

    @NotEmpty
    private String name; // can be used enum instead

}
