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

import java.util.Collection;
import org.jboss.weld.bootstrap.api.SingletonProvider;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerFactory;
import org.ops4j.pax.cdi.spi.CdiContainerType;
import org.ops4j.pax.cdi.weld.core.AbstractWeldCdiContainerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CdiContainerFactory} implementation based on Weld.
 *
 * @author Harald Wellmann
 *
 */
@Component
public class WeldCdiContainerFactory extends AbstractWeldCdiContainerFactory implements CdiContainerFactory {

    private final Logger log = LoggerFactory.getLogger(WeldCdiContainerFactory.class);

    private BundleContext bundleContext;

    @Activate    
    public void activate(BundleContext bc) {
        this.bundleContext = bc;
        SingletonProvider.initialize(new RegistrySingletonProvider());
    }

    @Deactivate
    public void deactivate() {
        SingletonProvider.reset();
    }

    @Override
    public String getProviderName() {
        return "Weld";
    }

    @Override
    public CdiContainer createContainer(Bundle bundle, Collection<Bundle> extensions, CdiContainerType containerType) {
        WeldCdiContainer container = new WeldCdiContainer(containerType, bundleContext.getBundle(), bundle, extensions);
        registerContainer(bundle, container);
        log.debug("Weld Container created");
        return container;
    }

   

}
