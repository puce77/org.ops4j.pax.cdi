/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ops4j.pax.cdi.weld.core;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.concurrent.Callable;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import org.jboss.weld.manager.BeanManagerImpl;
import org.ops4j.lang.Ops4jException;
import org.ops4j.pax.cdi.spi.AbstractCdiContainer;
import org.ops4j.pax.cdi.spi.CdiContainerType;
import org.ops4j.pax.cdi.weld.core.impl.InstanceManager;
import static org.ops4j.pax.swissbox.core.ContextClassLoaderUtils.doWithClassLoader;
import org.osgi.framework.Bundle;

/**
 *
 * @author Florian Brunner
 */
public abstract class AbstractWeldCdiContainer extends AbstractCdiContainer {

    /**
     * Helper for accessing Instance and Event of CDI container.
     */
    private InstanceManager instanceManager;

    private BeanManager manager;

    protected AbstractWeldCdiContainer(CdiContainerType containerType, Bundle bundle,
            Collection<Bundle> extensionBundles, Collection<Bundle> additionalBundles) {
        super(containerType, bundle, extensionBundles, additionalBundles);
    }

    @Override
    protected void doStart(Object environment) {
        buildContextClassLoader();
        try {
            final String contextId = getBundle().getSymbolicName() + ":"
                    + getBundle().getBundleId();
            manager = doWithClassLoader(getContextClassLoader(),
                    new Callable<BeanManager>() {
                        @Override
                        public BeanManager call() throws Exception {
                            return createBeanManager(contextId);
                        }
                    });
        } // CHECKSTYLE:SKIP
        catch (Exception exc) {
            throw new Ops4jException(exc);
        }
    }

    @Override
    public void doStop() {
        try {
            doWithClassLoader(getContextClassLoader(), new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    shutdown();
                    return null;
                }
            });
        } // CHECKSTYLE:SKIP
        catch (Exception exc) {
            throw new Ops4jException(exc);
        }
    }

    protected abstract BeanManager createBeanManager(String contextId);

    protected abstract void shutdown();

    @Override
    public BeanManager getBeanManager() {
        return manager;
    }

    @Override
    public Event<Object> getEvent() {
        return getInstanceManager().getEvent();
    }

    @Override
    public Instance<Object> getInstance() {
        return getInstanceManager().getInstance();
    }

    private InstanceManager getInstanceManager() {
        if (instanceManager == null) {
            BeanManager beanManager = getBeanManager();
            instanceManager = new InstanceManager();
            AnnotatedType<InstanceManager> annotatedType = beanManager.createAnnotatedType(InstanceManager.class);
            InjectionTarget<InstanceManager> target = beanManager.createInjectionTarget(annotatedType);
            CreationalContext<InstanceManager> cc = beanManager.createCreationalContext(null);
            target.inject(instanceManager, cc);
        }
        return instanceManager;
    }

    @Override
    public <T> T unwrap(Class<T> wrappedClass) {
        if (wrappedClass.isAssignableFrom(BeanManagerImpl.class)) {
            return wrappedClass.cast(manager);
        }
        return null;
    }

    @Override
    public void startContext(Class<? extends Annotation> scope) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void stopContext(Class<? extends Annotation> scope) {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
