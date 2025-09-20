package org.tikito.exception;

import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Getter
public class ResourceNotFoundException extends Exception {
    private final String resourceId;
    private final Collection<String> resourceIds;

    public ResourceNotFoundException(final String resourceId) {
        super("Resource " + resourceId + " not found");
        this.resourceId = resourceId;
        this.resourceIds = new HashSet<>();
    }

    public ResourceNotFoundException(final Collection<String> resourceIds) {
        super("Resources " + String.join(",", resourceIds) + " not found");
        this.resourceId = null;
        this.resourceIds = resourceIds;
    }

    public ResourceNotFoundException() {
        resourceId = null;
        resourceIds = List.of();
    }

    public ResourceNotFoundException(final Long resourceId) {
        this(Long.toString(resourceId));
    }
}
