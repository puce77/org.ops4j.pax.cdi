/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ops4j.pax.cdi.weld.se.impl;

import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.ops4j.pax.cdi.weld.core.bda.BundleDeployment;
import org.osgi.framework.Bundle;

/**
 *
 * @author Florian Brunner
 */
public class OsgiWeld extends Weld {
    private final Bundle bundle;
    private final ClassLoader extensionClassLoader;

    public OsgiWeld(String containerId, Bundle bundle, ClassLoader extensionClassLoader) {
        super(containerId);
        this.bundle = bundle;
        this.extensionClassLoader = extensionClassLoader;
    }

    @Override
    protected Deployment createDeployment(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
        return new BundleDeployment(bundle, bootstrap, extensionClassLoader);
    }

}
