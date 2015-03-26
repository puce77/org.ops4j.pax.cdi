/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ops4j.pax.cdi.weld.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ops4j.pax.cdi.spi.CdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerFactory;
import org.ops4j.pax.cdi.spi.CdiContainerListener;
import org.osgi.framework.Bundle;

/**
 *
 * @author puce
 */
public abstract class AbstractWeldCdiContainerFactory implements CdiContainerFactory {

    private final Map<Long, CdiContainer> containers = new HashMap<Long, CdiContainer>();
    private final List<CdiContainerListener> listeners = new CopyOnWriteArrayList<CdiContainerListener>();

    @Override
    public CdiContainer getContainer(Bundle bundle) {
        return containers.get(bundle.getBundleId());
    }

    @Override
    public Collection<CdiContainer> getContainers() {
        return Collections.unmodifiableCollection(containers.values());
    }

    @Override
    public void removeContainer(Bundle bundle) {
        CdiContainer container = containers.remove(bundle.getBundleId());
        for (CdiContainerListener listener : listeners) {
            listener.preDestroy(container);
        }
    }

    @Override
    public void addListener(CdiContainerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(CdiContainerListener listener) {
        listeners.remove(listener);
    }

    protected void registerContainer(Bundle bundle, CdiContainer container) {
        containers.put(bundle.getBundleId(), container);
        for (CdiContainerListener listener : listeners) {
            listener.postCreate(container);
        }
    }
}
