package com.occupify.notification.mapper;

import com.occupify.notification.dto.response.NotificationResponse;
import com.occupify.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
