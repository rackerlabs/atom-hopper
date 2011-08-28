package org.atomhopper.adapter.impl;

import java.util.Map;
import org.atomhopper.adapter.AtomHopperAdapter;

public abstract class AbstractDisabledAdapter implements AtomHopperAdapter {

    @Override
    public void setParameters(Map<String, String> params) {
    }    
}
