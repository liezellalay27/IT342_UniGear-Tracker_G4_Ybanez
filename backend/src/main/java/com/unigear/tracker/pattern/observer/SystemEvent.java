package com.unigear.tracker.pattern.observer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Observer Pattern: Event Data Object
 * 
 * Encapsulates event information that is broadcast to all observers.
 * Contains information about what happened, when, and by whom.
 */
public class SystemEvent {
    
    public enum EventType {
        REQUEST_CREATED,
        REQUEST_APPROVED,
        REQUEST_REJECTED,
        REQUEST_RETURNED,
        EQUIPMENT_ADDED,
        EQUIPMENT_UPDATED,
        EQUIPMENT_REMOVED,
        USER_REGISTERED,
        USER_LOGIN,
        ADMIN_ACTION
    }
    
    private final EventType eventType;
    private final String source;           // Component that triggered the event
    private final Long targetId;           // ID of entity affected (Request ID, Equipment ID, etc)
    private final String targetType;       // Type of entity (REQUEST, EQUIPMENT, USER)
    private final String actor;            // Who triggered the event (user email)
    private final String description;      // Human-readable description
    private final LocalDateTime timestamp;
    private final Object data;             // Optional additional data
    private final Map<String, Object> metadata;  // Custom metadata
    
    /**
     * Builder class for fluent construction
     */
    public static class Builder {
        private EventType eventType;
        private String source;
        private Long targetId;
        private String targetType;
        private String actor;
        private String description;
        private Object data;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata;
        
        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        public Builder targetId(Long targetId) {
            this.targetId = targetId;
            return this;
        }
        
        public Builder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }
        
        public Builder actor(String actor) {
            this.actor = actor;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder data(Object data) {
            this.data = data;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public SystemEvent build() {
            if (this.timestamp == null) {
                this.timestamp = LocalDateTime.now();
            }
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            return new SystemEvent(
                eventType, source, targetId, targetType, actor, 
                description, data, timestamp, metadata
            );
        }
    }
    
    /**
     * Static builder method
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public SystemEvent(
            EventType eventType,
            String source,
            Long targetId,
            String targetType,
            String actor,
            String description) {
        this(eventType, source, targetId, targetType, actor, description, null);
    }
    
    public SystemEvent(
            EventType eventType,
            String source,
            Long targetId,
            String targetType,
            String actor,
            String description,
            Object data) {
        this(eventType, source, targetId, targetType, actor, description, data, 
             LocalDateTime.now(), new HashMap<>());
    }
    
    public SystemEvent(
            EventType eventType,
            String source,
            Long targetId,
            String targetType,
            String actor,
            String description,
            Object data,
            LocalDateTime timestamp,
            Map<String, Object> metadata) {
        this.eventType = eventType;
        this.source = source;
        this.targetId = targetId;
        this.targetType = targetType;
        this.actor = actor;
        this.description = description;
        this.data = data;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
    
    /**
     * Add custom metadata to this event
     */
    public SystemEvent addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    // ============ Getters ============
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getSource() {
        return source;
    }
    
    public Long getTargetId() {
        return targetId;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public String getActor() {
        return actor;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public String toString() {
        return String.format(
            "SystemEvent{type=%s, target=%s:%d, actor=%s, timestamp=%s}",
            eventType, targetType, targetId, actor, timestamp
        );
    }
}
