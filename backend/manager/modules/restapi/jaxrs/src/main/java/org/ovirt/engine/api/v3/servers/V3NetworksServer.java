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

import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Network;
import org.ovirt.engine.api.v3.types.V3Networks;

@Produces({"application/xml", "application/json"})
public class V3NetworksServer extends V3Server<NetworksResource> {
    public V3NetworksServer(NetworksResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Network network) {
        return adaptAdd(getDelegate()::add, network);
    }

    @GET
    public V3Networks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3NetworkServer getNetworkResource(@PathParam("id") String id) {
        return new V3NetworkServer(getDelegate().getNetworkResource(id));
    }
}
