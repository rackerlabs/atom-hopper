package org.atomhopper.response;

/**
 *
 * 
 */
public enum ResponseParameter {
    PREVIOUS_MARKER,
    NEXT_MARKER;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
