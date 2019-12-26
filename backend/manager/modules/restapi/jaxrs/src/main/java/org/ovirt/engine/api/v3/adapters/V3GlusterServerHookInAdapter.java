/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.GlusterHookStatus;
import org.ovirt.engine.api.model.GlusterServerHook;
import org.ovirt.engine.api.model.HookContentType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3GlusterServerHook;

public class V3GlusterServerHookInAdapter implements V3Adapter<V3GlusterServerHook, GlusterServerHook> {
    @Override
    public GlusterServerHook adapt(V3GlusterServerHook from) {
        GlusterServerHook to = new GlusterServerHook();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetChecksum()) {
            to.setChecksum(from.getChecksum());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetContentType()) {
            to.setContentType(HookContentType.fromValue(from.getContentType()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(GlusterHookStatus.fromValue(from.getStatus().getState()));
        }
        return to;
    }
}
