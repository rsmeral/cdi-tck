/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.cdi.tck.tests.decorators.definition;

import static org.jboss.cdi.tck.cdi.Sections.BEAN_DISCOVERY;
import static org.jboss.cdi.tck.cdi.Sections.BM_DECORATOR_RESOLUTION;
import static org.jboss.cdi.tck.cdi.Sections.DECORATED_TYPES;
import static org.jboss.cdi.tck.cdi.Sections.DECORATOR;
import static org.jboss.cdi.tck.cdi.Sections.DECORATOR_ANNOTATION;
import static org.jboss.cdi.tck.cdi.Sections.DECORATOR_BEAN;
import static org.jboss.cdi.tck.cdi.Sections.DECORATOR_RESOLUTION;
import static org.jboss.cdi.tck.cdi.Sections.DELEGATE_ATTRIBUTE;
import static org.jboss.cdi.tck.cdi.Sections.ENABLED_DECORATORS;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;

import javax.decorator.Delegate;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.cdi.tck.AbstractTest;
import org.jboss.cdi.tck.shrinkwrap.WebArchiveBuilder;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.beans10.BeansDescriptor;
import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecAssertions;
import org.jboss.test.audit.annotations.SpecVersion;
import org.testng.annotations.Test;

/**
 * @author pmuir
 * @author Martin Kouba
 */
@SpecVersion(spec = "cdi", version = "20091101")
public class DecoratorDefinitionTest extends AbstractTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return new WebArchiveBuilder()
                .withTestClassPackage(DecoratorDefinitionTest.class)
                .withBeansXml(
                        Descriptors
                                .create(BeansDescriptor.class)
                                .createDecorators()
                                .clazz(BazDecorator.class.getName(), BazDecorator1.class.getName(),
                                        FooDecorator.class.getName(), TimestampLogger.class.getName(),
                                        ChargeDecorator.class.getName()).up()).build();
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = DECORATOR_BEAN, id = "d"), @SpecAssertion(section = DECORATOR_ANNOTATION, id = "a"),
            @SpecAssertion(section = DECORATED_TYPES, id = "c"), @SpecAssertion(section = DECORATOR_RESOLUTION, id = "aa"),
            @SpecAssertion(section = DECORATOR, id = "a"), @SpecAssertion(section = BEAN_DISCOVERY, id = "kc") })
    public void testDecoratorIsManagedBean() {
        List<Decorator<?>> decorators = getCurrentManager().resolveDecorators(MockLogger.TYPES);
        assert decorators.size() == 1;
        boolean implementsInterface = false;
        for (Class<?> interfaze : decorators.get(0).getClass().getInterfaces()) {
            if (Decorator.class.isAssignableFrom(interfaze)) {
                implementsInterface = true;
                break;
            }
        }
        assert implementsInterface;
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = DECORATOR_BEAN, id = "b"), @SpecAssertion(section = DECORATOR_BEAN, id = "c"),
            @SpecAssertion(section = DECORATOR, id = "b"), @SpecAssertion(section = BM_DECORATOR_RESOLUTION, id = "a"),
            @SpecAssertion(section = BM_DECORATOR_RESOLUTION, id = "b") })
    public void testDecoratedTypes() {
        List<Decorator<?>> decorators = getCurrentManager().resolveDecorators(FooBar.TYPES);
        assert decorators.size() == 1;
        assert decorators.get(0).getDecoratedTypes().size() == 4;
        assert decorators.get(0).getDecoratedTypes().contains(Foo.class);
        assert decorators.get(0).getDecoratedTypes().contains(Bar.class);
        assert decorators.get(0).getDecoratedTypes().contains(Baz.class);
        assert decorators.get(0).getDecoratedTypes().contains(Boo.class);
        assert !decorators.get(0).getDecoratedTypes().contains(Serializable.class);
        assert !decorators.get(0).getDecoratedTypes().contains(FooDecorator.class);
        assert !decorators.get(0).getDecoratedTypes().contains(AbstractFooDecorator.class);
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = DELEGATE_ATTRIBUTE, id = "a"), @SpecAssertion(section = DECORATOR, id = "c") })
    public void testDelegateInjectionPoint() {
        List<Decorator<?>> decorators = getCurrentManager().resolveDecorators(Logger.TYPES);
        assert decorators.size() == 1;
        Decorator<?> decorator = decorators.get(0);
        assert decorator.getInjectionPoints().size() == 1;
        assert decorator.getInjectionPoints().iterator().next().getType().equals(Logger.class);
        assert decorator.getInjectionPoints().iterator().next().getAnnotated().isAnnotationPresent(Delegate.class);
        assert decorator.getDelegateType().equals(Logger.class);
        assert decorator.getDelegateQualifiers().size() == 1;
        assert annotationSetMatches(decorator.getDelegateQualifiers(), Default.class);
    }

    @Test
    @SpecAssertion(section = DECORATED_TYPES, id = "b")
    public void testDecoratorDoesNotImplementDelegateType() {
        List<Decorator<?>> decorators = getCurrentManager().resolveDecorators(Bazt.TYPES);
        assert decorators.size() == 2;
    }

    @Test
    @SpecAssertions({ @SpecAssertion(section = ENABLED_DECORATORS, id = "g"), @SpecAssertion(section = DECORATOR_RESOLUTION, id = "aa"),
            @SpecAssertion(section = BM_DECORATOR_RESOLUTION, id = "a") })
    public void testDecoratorOrdering() {
        List<Decorator<?>> decorators = getCurrentManager().resolveDecorators(Bazt.TYPES);
        assert decorators.size() == 2;
        assert decorators.get(0).getTypes().contains(BazDecorator.class);
        assert decorators.get(1).getTypes().contains(BazDecorator1.class);
    }

    @Test
    @SpecAssertion(section = ENABLED_DECORATORS, id = "ab")
    public void testNonEnabledDecoratorNotResolved() {
        List<Decorator<?>> decorators = getCurrentManager().resolveDecorators(Field.TYPES);
        assert decorators.size() == 0;
    }

    @Test
    @SpecAssertion(section = DECORATOR, id = "d")
    public void testInstanceOfDecoratorForEachEnabled() {
        assert !getCurrentManager().resolveDecorators(MockLogger.TYPES).isEmpty();
        assert !getCurrentManager().resolveDecorators(FooBar.TYPES).isEmpty();
        assert !getCurrentManager().resolveDecorators(Logger.TYPES).isEmpty();
        assert getCurrentManager().resolveDecorators(Bazt.TYPES).size() == 2;
        assert getCurrentManager().resolveDecorators(Field.TYPES).isEmpty();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SpecAssertion(section = BM_DECORATOR_RESOLUTION, id = "c")
    public void testDuplicateBindingsOnResolveDecoratorsFails() {
        Annotation binding = new AnnotationLiteral<Meta>() {
        };
        getCurrentManager().resolveDecorators(FooBar.TYPES, binding, binding);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SpecAssertion(section = BM_DECORATOR_RESOLUTION, id = "d")
    public void testNonBindingsOnResolveDecoratorsFails() {
        Annotation binding = new AnnotationLiteral<NonMeta>() {
        };
        getCurrentManager().resolveDecorators(FooBar.TYPES, binding);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SpecAssertion(section = BM_DECORATOR_RESOLUTION, id = "e")
    public void testEmptyTypeSetOnResolveDecoratorsFails() {
        Annotation binding = new AnnotationLiteral<NonMeta>() {
        };
        getCurrentManager().resolveDecorators(new HashSet<Type>(), binding);
    }

    /**
     * Test that if the decorator does not implement a method of the decorated type, the container will provide an implicit
     * implementation that calls the method on the delegate.
     * 
     * @param account
     */
    @Test(dataProvider = ARQUILLIAN_DATA_PROVIDER)
    @SpecAssertions({ @SpecAssertion(section = DECORATOR_BEAN, id = "d"), @SpecAssertion(section = DECORATED_TYPES, id = "c"),
            @SpecAssertion(section = DECORATED_TYPES, id = "ca") })
    public void testAbstractDecoratorNotImplementingMethodOfDecoratedType(BankAccount account) {
        ChargeDecorator.reset();
        account.deposit(100);
        assertEquals(ChargeDecorator.charged, 0);
        account.withdraw(50);
        assertEquals(ChargeDecorator.charged, 5);
        assertEquals(account.getBalance(), 45);
    }

}
