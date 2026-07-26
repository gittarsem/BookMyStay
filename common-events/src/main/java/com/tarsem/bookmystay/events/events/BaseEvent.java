package com.tarsem.bookmystay.events.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class BaseEvent {

    private UUID eventId;
    private LocalDateTime occurredAt;
}
