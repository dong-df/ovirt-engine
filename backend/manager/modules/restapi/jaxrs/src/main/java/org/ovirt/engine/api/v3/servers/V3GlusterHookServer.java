/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.gluster.GlusterHookResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3GlusterHook;

@Produces({"application/xml", "application/json"})
public class V3GlusterHookServer extends V3Server<GlusterHookResource> {
    public V3GlusterHookServer(GlusterHookResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("disable")
    public Response disable(V3Action action) {
        return adaptAction(getDelegate()::disable, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("enable")
    public Response enable(V3Action action) {
        return adaptAction(getDelegate()::enable, action);
    }

    @GET
    public V3GlusterHook get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("resolve")
    public Response resolve(V3Action action) {
        return adaptAction(getDelegate()::resolve, action);
    }

    @Path("{action: (?:disable|enable|resolve)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
