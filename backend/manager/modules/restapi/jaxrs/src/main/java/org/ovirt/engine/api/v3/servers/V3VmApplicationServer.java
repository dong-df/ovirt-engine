/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.VmApplicationResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Application;

@Produces({"application/xml", "application/json"})
public class V3VmApplicationServer extends V3Server<VmApplicationResource> {
    public V3VmApplicationServer(VmApplicationResource delegate) {
        super(delegate);
    }

    @GET
    public V3Application get() {
        return adaptGet(getDelegate()::get);
    }
}
