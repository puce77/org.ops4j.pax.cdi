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
package org.ops4j.pax.cdi.web.impl;

import javax.servlet.ServletContainerInitializer;

import org.ops4j.pax.web.service.WebAppDependencyHolder;
import org.osgi.service.http.HttpService;

public class CdiWebAppDependencyHolder implements WebAppDependencyHolder {

    private HttpService httpService;
    private ServletContainerInitializer initializer;

    public CdiWebAppDependencyHolder(HttpService httpService,
        ServletContainerInitializer initializer) {
        this.httpService = httpService;
        this.initializer = initializer;
    }

    @Override
    public HttpService getHttpService() {
        return httpService;
    }

    @Override
    public ServletContainerInitializer getServletContainerInitializer() {
        return initializer;
    }
}