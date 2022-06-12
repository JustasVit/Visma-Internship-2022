package com.visma.task.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.visma.task.enums.Category;
import com.visma.task.enums.Type;
import com.visma.task.models.Meeting;
import com.visma.task.utils.JsonDateDeserializer;
import com.visma.task.utils.JsonDateSerializer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingDto {
    private long id;

    @NonNull
    private String name;

    private long responsiblePerson;

    @NonNull
    private String description;

    @NonNull
    private Category category;

    @NonNull
    private Type type;

    @NonNull
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private LocalDateTime startDate;

    @NonNull
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private LocalDateTime endDate;

    public MeetingDto(Meeting meeting){
        this.id = meeting.getId();
        this.name = meeting.getName();
        this.responsiblePerson = meeting.getResponsiblePerson();
        this.description = meeting.getDescription();
        this.category = meeting.getCategory();
        this.type = meeting.getType();
        this.startDate = meeting.getStartDate();
        this.endDate = meeting.getEndDate();
    }
}