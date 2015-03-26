/*
 * Copyright 2012 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.cdi.weld.impl;

import java.util.Arrays;
import java.util.Collection;
import javax.enterprise.inject.spi.BeanManager;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerType;
import org.ops4j.pax.cdi.weld.core.AbstractWeldCdiContainer;
import org.ops4j.pax.cdi.weld.core.bda.BundleDeployment;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CdiContainer} implementation wrapping a JBoss Weld container, represented by a {@link WeldBootstrap}.
 *
 * @author Harald Wellmann
 *
 */
public class WeldCdiContainer extends AbstractWeldCdiContainer {

    private final Logger log = LoggerFactory.getLogger(WeldCdiContainer.class);

    private WeldBootstrap bootstrap;

    /**
     * Construct a CDI container for the given extended bundle.
     *
     * @param ownBundle bundle containing this class
     * @param bundle bundle to be extended with CDI container
     * @param extensionBundles CDI extension bundles to be loaded by OpenWebBeans
     */
    public WeldCdiContainer(CdiContainerType containerType, Bundle ownBundle, Bundle bundle,
            Collection<Bundle> extensionBundles) {
        super(containerType, bundle, extensionBundles, Arrays.asList(ownBundle,
                FrameworkUtil.getBundle(Bootstrap.class)));
        log.debug("creating Weld CDI container for bundle {}", bundle);
    }

    @Override
    protected BeanManager createBeanManager(String contextId) {
        bootstrap = new WeldBootstrap();
        BundleDeployment deployment = new BundleDeployment(getBundle(), bootstrap, getContextClassLoader());
        BeanDeploymentArchive beanDeploymentArchive = deployment
                .getBeanDeploymentArchive();

        bootstrap.startContainer(contextId, OsgiEnvironment.getInstance(), deployment);
        bootstrap.startInitialization();
        bootstrap.deployBeans();
        bootstrap.validateBeans();
        bootstrap.endInitialization();
        return bootstrap.getManager(beanDeploymentArchive);
    }

    @Override
    protected void shutdown() {
        bootstrap.shutdown();
    }

    @Override
    public <T> T unwrap(Class<T> wrappedClass) {
        if (wrappedClass.isAssignableFrom(WeldBootstrap.class)) {
            return wrappedClass.cast(bootstrap);
        } else {
            return super.unwrap(wrappedClass);
        }
    }

}
