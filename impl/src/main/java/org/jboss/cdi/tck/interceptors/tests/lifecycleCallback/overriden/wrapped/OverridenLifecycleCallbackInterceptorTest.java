/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.cdi.tck.interceptors.tests.lifecycleCallback.overriden.wrapped;

import static org.testng.Assert.assertEquals;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.cdi.tck.AbstractTest;
import org.jboss.cdi.tck.shrinkwrap.WebArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

@SpecVersion(spec = "int", version = "3.1.PFD")
public class OverridenLifecycleCallbackInterceptorTest extends AbstractTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return new WebArchiveBuilder().withTestClassPackage(OverridenLifecycleCallbackInterceptorTest.class)
                .withExtension(WrappingExtension.class).build();
    }

    @Test
    @SpecAssertion(section = "5.1", id = "g")
    public void testCallbackOverridenByCallback() {

        Bird.reset();
        Eagle.reset();

        Bean<Eagle> fooBean = getUniqueBean(Eagle.class);
        CreationalContext<Eagle> ctx = getCurrentManager().createCreationalContext(fooBean);
        Eagle foo = fooBean.create(ctx);

        foo.ping();
        fooBean.destroy(foo, ctx);

        assertEquals(Bird.getInitBirdCalled().get(), 0);
        assertEquals(Eagle.getInitEagleCalled().get(), 1);
        assertEquals(Bird.getDestroyBirdCalled().get(), 0);
        assertEquals(Eagle.getDestroyEagleCalled().get(), 1);
    }

    @Test
    @SpecAssertion(section = "5.1", id = "g")
    public void testCallbackOverridenByNonCallback() {

        Bird.reset();
        Falcon.reset();

        Bean<Falcon> bazBean = getUniqueBean(Falcon.class);
        CreationalContext<Falcon> ctx = getCurrentManager().createCreationalContext(bazBean);
        Falcon baz = bazBean.create(ctx);

        baz.ping();
        bazBean.destroy(baz, ctx);

        assertEquals(Bird.getInitBirdCalled().get(), 0);
        assertEquals(Falcon.getInitFalconCalled().get(), 0);
        assertEquals(Bird.getDestroyBirdCalled().get(), 0);
        assertEquals(Falcon.getDestroyFalconCalled().get(), 0);
    }
}