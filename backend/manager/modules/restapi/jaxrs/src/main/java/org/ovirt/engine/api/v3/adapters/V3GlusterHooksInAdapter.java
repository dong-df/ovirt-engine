/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterHooks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterHooks;

public class V3GlusterHooksInAdapter implements V3Adapter<V3GlusterHooks, GlusterHooks> {
    @Override
    public GlusterHooks adapt(V3GlusterHooks from) {
        GlusterHooks to = new GlusterHooks();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getGlusterHooks().addAll(adaptIn(from.getGlusterHooks()));
        return to;
    }
}
