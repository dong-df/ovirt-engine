/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.VnicProfiles;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VnicProfiles;

public class V3VnicProfilesOutAdapter implements V3Adapter<VnicProfiles, V3VnicProfiles> {
    @Override
    public V3VnicProfiles adapt(VnicProfiles from) {
        V3VnicProfiles to = new V3VnicProfiles();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getVnicProfiles().addAll(adaptOut(from.getVnicProfiles()));
        return to;
    }
}
