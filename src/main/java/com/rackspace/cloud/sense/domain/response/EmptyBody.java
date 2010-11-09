/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.domain.response;

/**
 *
 * @author zinic
 */
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
