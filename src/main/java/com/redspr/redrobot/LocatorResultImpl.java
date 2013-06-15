package com.redspr.redrobot;

/**
 * Simple implementation that wraps a native object.
 */
class LocatorResultImpl implements LocatorResult {
    private final Object wrapped;

    LocatorResultImpl(Object wrapped2) {
        this.wrapped = wrapped2;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return (T) wrapped;
    }
}
