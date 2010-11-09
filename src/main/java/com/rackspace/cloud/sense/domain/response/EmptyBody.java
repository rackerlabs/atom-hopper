package com.rackspace.cloud.sense.domain.response;

public final class EmptyBody {

    private static final EmptyBody INSTANCE = new EmptyBody();

    public static EmptyBody getInstance() {
        return INSTANCE;
    }

    private EmptyBody() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyBody;
    }
}
