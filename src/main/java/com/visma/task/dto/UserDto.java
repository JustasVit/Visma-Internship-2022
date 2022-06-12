package com.visma.task.dto;

import com.visma.task.models.User;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {

    private long id;

    @NonNull
    private String username;

    @NonNull
    private String password;

    public UserDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
    }
}