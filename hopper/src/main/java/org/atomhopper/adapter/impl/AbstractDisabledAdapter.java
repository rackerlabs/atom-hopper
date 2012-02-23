package org.atomhopper.adapter.impl;

import org.atomhopper.adapter.AtomHopperAdapter;

import java.util.Map;

public abstract class AbstractDisabledAdapter implements AtomHopperAdapter {

    @Override
    public void setParameters(Map<String, String> params) {
    }    
}
