package eu.isygoit.model.timeline;

public interface ITimelineEventSource {
    String resolveTenant();

    String resolveModifiedBy();

    String resolveElementId();
}
