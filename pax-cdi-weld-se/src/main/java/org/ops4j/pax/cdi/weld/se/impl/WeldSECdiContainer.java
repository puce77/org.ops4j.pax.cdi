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
package org.ops4j.pax.cdi.weld.se.impl;

import java.util.Arrays;
import java.util.Collection;
import javax.enterprise.inject.spi.BeanManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerType;
import org.ops4j.pax.cdi.weld.core.AbstractWeldCdiContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CdiContainer} implementation wrapping a JBoss Weld SE container, represented by {@link Weld}.
 *
 * @author Florian Brunner
 *
 */
public class WeldSECdiContainer extends AbstractWeldCdiContainer {

    private final Logger log = LoggerFactory.getLogger(WeldSECdiContainer.class);

    private Weld weld;

    /**
     * Construct a CDI container for the given extended bundle.
     *
     * @param ownBundle bundle containing this class
     * @param bundle bundle to be extended with CDI container
     * @param extensionBundles CDI extension bundles to be loaded by OpenWebBeans
     */
    public WeldSECdiContainer(CdiContainerType containerType, Bundle ownBundle, Bundle bundle,
            Collection<Bundle> extensionBundles) {
        super(containerType, bundle, extensionBundles, Arrays.asList(ownBundle,
                FrameworkUtil.getBundle(Weld.class)));
        log.debug("creating Weld SE CDI container for bundle {}", bundle);
    }

    @Override
    protected BeanManager createBeanManager(String contextId) {
        weld = new Weld(contextId);
        WeldContainer weldContainer = weld.initialize();
        return weldContainer.getBeanManager();
    }

    @Override
    protected void shutdown() {
        weld.shutdown();
    }

    @Override
    public <T> T unwrap(Class<T> wrappedClass) {
        if (wrappedClass.isAssignableFrom(Weld.class)) {
            return wrappedClass.cast(weld);
        } else {
            return super.unwrap(wrappedClass);
        }
    }

}
