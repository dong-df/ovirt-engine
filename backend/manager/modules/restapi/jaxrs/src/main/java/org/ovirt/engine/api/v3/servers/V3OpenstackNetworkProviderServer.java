/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProviderResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworkProvider;

@Produces({"application/xml", "application/json"})
public class V3OpenstackNetworkProviderServer extends V3Server<OpenstackNetworkProviderResource> {
    public V3OpenstackNetworkProviderServer(OpenstackNetworkProviderResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackNetworkProvider get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3OpenStackNetworkProvider update(V3OpenStackNetworkProvider provider) {
        return adaptUpdate(getDelegate()::update, provider);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("networks")
    public V3OpenstackNetworksServer getNetworksResource() {
        return new V3OpenstackNetworksServer(getDelegate().getNetworksResource());
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("importcertificates")
    public Response importCertificates(V3Action action) {
        return adaptAction(getDelegate()::importCertificates, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("testconnectivity")
    public Response testConnectivity(V3Action action) {
        return adaptAction(getDelegate()::testConnectivity, action);
    }

    @Path("certificates")
    public V3ExternalProviderCertificatesServer getCertificatesResource() {
        return new V3ExternalProviderCertificatesServer(getDelegate().getCertificatesResource());
    }

    @Path("{action: (?:importcertificates|testconnectivity)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
