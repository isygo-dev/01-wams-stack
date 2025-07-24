package eu.isygoit.multitenancy.model;

import eu.isygoit.multitenancy.service.TimelineEventListener;
import jakarta.persistence.EntityListeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EntityListeners(TimelineEventListener.class)
public @interface TrackTimelineEvents {
}