/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.MacPoolsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3MacPool;
import org.ovirt.engine.api.v3.types.V3MacPools;

@Produces({"application/xml", "application/json"})
public class V3MacPoolsServer extends V3Server<MacPoolsResource> {
    public V3MacPoolsServer(MacPoolsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3MacPool pool) {
        return adaptAdd(getDelegate()::add, pool);
    }

    @GET
    public V3MacPools list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3MacPoolServer getMacPoolResource(@PathParam("id") String id) {
        return new V3MacPoolServer(getDelegate().getMacPoolResource(id));
    }
}
