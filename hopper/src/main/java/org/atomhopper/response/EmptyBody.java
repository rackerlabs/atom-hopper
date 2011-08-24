package net.jps.atom.hopper.response;

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
        return obj.hashCode() == hashCode();
    }
}
