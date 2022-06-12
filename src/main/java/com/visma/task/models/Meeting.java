package com.visma.task.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.visma.task.dto.MeetingDto;
import com.visma.task.enums.Category;
import com.visma.task.enums.Type;
import com.visma.task.utils.JsonDateDeserializer;
import com.visma.task.utils.JsonDateSerializer;
import lombok.*;

import java.time.LocalDateTime;


@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Meeting {
    @NonNull
    private long id;

    @NonNull
    private String name;

    @NonNull
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

    public Meeting(MeetingDto meetingDto){
        this.id = meetingDto.getId();
        this.name = meetingDto.getName();
        this.responsiblePerson = meetingDto.getResponsiblePerson();
        this.description = meetingDto.getDescription();
        this.category = meetingDto.getCategory();
        this.type = meetingDto.getType();
        this.startDate = meetingDto.getStartDate();
        this.endDate = meetingDto.getEndDate();
    }
}
