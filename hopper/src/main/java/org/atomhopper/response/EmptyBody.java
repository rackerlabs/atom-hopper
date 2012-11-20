package org.atomhopper.response;

public final class EmptyBody {

    private static final EmptyBody INSTANCE = new EmptyBody();

    public static EmptyBody getInstance() {
        return INSTANCE;
    }

    private EmptyBody() {
    }

    @Override
    public int hashCode() {
        return INSTANCE.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            return obj.hashCode() == hashCode();
        } else {
            return false;
        }
    }
}
