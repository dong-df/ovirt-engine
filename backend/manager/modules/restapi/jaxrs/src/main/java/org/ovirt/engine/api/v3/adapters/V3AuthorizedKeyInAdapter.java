/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.AuthorizedKey;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3AuthorizedKey;

public class V3AuthorizedKeyInAdapter implements V3Adapter<V3AuthorizedKey, AuthorizedKey> {
    @Override
    public AuthorizedKey adapt(V3AuthorizedKey from) {
        AuthorizedKey to = new AuthorizedKey();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetKey()) {
            to.setKey(from.getKey());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetUser()) {
            to.setUser(adaptIn(from.getUser()));
        }
        return to;
    }
}
