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

import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProvidersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeProvider;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeProviders;

@Produces({"application/xml", "application/json"})
public class V3OpenstackVolumeProvidersServer extends V3Server<OpenstackVolumeProvidersResource> {
    public V3OpenstackVolumeProvidersServer(OpenstackVolumeProvidersResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3OpenStackVolumeProvider provider) {
        return adaptAdd(getDelegate()::add, provider);
    }

    @GET
    public V3OpenStackVolumeProviders list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OpenstackVolumeProviderServer getProviderResource(@PathParam("id") String id) {
        return new V3OpenstackVolumeProviderServer(getDelegate().getProviderResource(id));
    }
}
