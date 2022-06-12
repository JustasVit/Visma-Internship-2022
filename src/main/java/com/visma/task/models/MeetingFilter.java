package com.visma.task.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.visma.task.enums.Category;
import com.visma.task.enums.Type;
import com.visma.task.utils.JsonDateDeserializer;
import com.visma.task.utils.JsonDateSerializer;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
@Builder
public class MeetingFilter {
    String description;

    long responsiblePersonId;

    Category category;

    Type type;

    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    LocalDateTime startingDate;

    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    LocalDateTime endingDate;

    int minimumAttendeeCount;
}
