/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.cdi.extension.impl.component;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.ops4j.pax.cdi.extension.impl.context.ServiceContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cdi.Component;
import org.osgi.service.cdi.ComponentProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 * 
 */
@ApplicationScoped
public class ComponentLifecycleManager implements ComponentDependencyListener {

    private static Logger log = LoggerFactory.getLogger(ComponentLifecycleManager.class);

    @Inject
    private BeanManager beanManager;

    /**
     * Registry for all service components of the current bean bundle.
     */
    @Inject
    private ComponentRegistry componentRegistry;

    /**
     * Bundle context of the current bean bundle.
     */
    @Inject
    private BundleContext bundleContext;

    /**
     * Creational context for service components contextual instances.
     */
    private CreationalContext<Object> cc;

    /**
     * Service context for OSGi component {@code @ServiceScoped} contextual instances.
     */
    @Inject
    private ServiceContext context;

    /**
     * Starts the component lifecycle for this bean bundle.
     * <p>
     * Registers all satisfied components and starts a service tracker for unsatisfied dependencies.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void start() {
        cc = beanManager.createCreationalContext(null);
        componentRegistry.setBundleContext(bundleContext);

        // register services for all components that are satisfied already
        for (Bean bean : componentRegistry.getComponents()) {
            if (isBeanFromCurrentBundle(bean)) {
                ComponentDescriptor descriptor = componentRegistry.getDescriptor(bean);
                descriptor.setBundleContext(bundleContext);
                descriptor.setListener(this);
                if (descriptor.isSatisfied()) {
                    log.info("component {} is available", bean);
                    Object service = context.get(bean, cc);
                    registerService(bean, service, descriptor);
                }
                descriptor.start();
            }
        }
    }

    /**
     * Stops the component lifecycle for the current bean bundle.
     * <p>
     * Closes the service tracker and unregisters all services for OSGi components.
     */
    @SuppressWarnings("rawtypes")
    public void stop() {
        ComponentDependencyListener noop = new DefaultComponentDependencyListener();
        for (Bean bean : componentRegistry.getComponents()) {
            ComponentDescriptor descriptor = componentRegistry.getDescriptor(bean);
            descriptor.setListener(noop);
            descriptor.stop();
        }
    }

    private boolean isBeanFromCurrentBundle(Bean<?> bean) {
        long extendedBundleId = bundleContext.getBundle().getBundleId();
        Class<?> klass = bean.getBeanClass();
        long serviceBundleId = FrameworkUtil.getBundle(klass).getBundleId();
        return serviceBundleId == extendedBundleId;
    }

    /**
     * Registers the OSGi service for the given OSGi component bean
     * 
     * @param bean
     *            OSGi component bean
     * @param service
     *            contextual instance of the given bean type
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <S> void registerService(Bean<S> bean, S service, ComponentDescriptor descriptor) {
        // only register services for classes located in the extended bundle
        if (!isBeanFromCurrentBundle(bean)) {
            return;
        }

        Class<?> klass = bean.getBeanClass();
        AnnotatedType<?> annotatedType = beanManager.createAnnotatedType(klass);
        Component provider = annotatedType.getAnnotation(Component.class);

        String[] typeNames;
        if (provider.interfaces().length == 0) {
            typeNames = getTypeNamesForBeanTypes(bean);
        }
        else {
            typeNames = getTypeNamesForClasses(provider.interfaces());
        }

        Dictionary<String, Object> props = createProperties(annotatedType);
        log.debug("publishing service {}, props = {}", typeNames[0], props);
        ServiceRegistration<?> reg = bundleContext.registerService(typeNames, service, props);
        descriptor.setServiceRegistration(reg);
    }

    private <S> void unregisterService(Bean<S> bean, Object service,
        ComponentDescriptor<S> descriptor) {
        ServiceRegistration<S> reg = descriptor.getServiceRegistration();
        if (reg != null) {
            log.debug("removing service {}", reg);
            try {
                reg.unregister();
            }
            catch (IllegalStateException exc) {
                // Ignore if the service has already been unregistered
            }
        }
    }

    private String[] getTypeNamesForBeanTypes(Bean<?> bean) {
        Set<Type> closure = bean.getTypes();
        String[] typeNames = new String[closure.size()];
        int i = 0;
        for (Type type : closure) {
            Class<?> c = (Class<?>) type;
            if (c.isInterface()) {
                typeNames[i++] = c.getName();
            }
        }
        if (i == 0) {
            typeNames[i++] = bean.getBeanClass().getName();
        }
        return Arrays.copyOf(typeNames, i);
    }

    private String[] getTypeNamesForClasses(Class<?>[] classes) {
        String[] typeNames = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            typeNames[i] = classes[i].getName();
        }
        return typeNames;
    }

    private Dictionary<String, Object> createProperties(AnnotatedType type) {
        Hashtable<String, Object> dict = new Hashtable<String, Object>();
        Component comp = type.getAnnotation(Component.class);
        if (comp != null) {
            for (ComponentProperty prop : comp.properties()) {
                dict.put(prop.key(), prop.value());
            }
        }
        return dict;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <S> void onComponentSatisfied(ComponentDescriptor<S> descriptor) {
        Bean bean = descriptor.getBean();
        log.info("component {} is available", bean);
        Object service = context.get(bean, cc);
        registerService(bean, service, descriptor);
    }

    @Override
    public <S> void onComponentUnsatisfied(ComponentDescriptor<S> descriptor) {
        Bean<S> bean = descriptor.getBean();
        S service = context.get(bean);
        if (service != null) {
            unregisterService(bean, service, descriptor);
            context.destroy(bean);
        }
    }
}
