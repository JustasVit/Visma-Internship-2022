package com.visma.task.models;

import com.visma.task.dto.UserDto;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class User {

    @NonNull
    private long id;

    @NonNull
    private String username;

    @NonNull
    private String password;

    public User(UserDto userDto){
        this.id = userDto.getId();
        this.username = userDto.getUsername();
        this.password = userDto.getPassword();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && username.equals(user.username) && password.equals(user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password);
    }
}

