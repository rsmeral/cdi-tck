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
package org.jboss.jsr299.tck.tests.definition.bean.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.jsr299.tck.literals.DefaultLiteral;

public class CustomInjectionPoint implements InjectionPoint {

    private Type type;

    private Bean<?> bean;

    private boolean isTransient;

    private boolean isTransientCalled = false;

    private AnnotatedField<?> annotatedField;

    public CustomInjectionPoint(Type type, Bean<?> bean, boolean isTransient, AnnotatedField<?> annotatedField) {
        this.type = type;
        this.bean = bean;
        this.isTransient = isTransient;
        this.annotatedField = annotatedField;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Annotation defaultQualifier = new DefaultLiteral();
        return Collections.singleton(defaultQualifier);
    }

    @Override
    public Bean<?> getBean() {
        return bean;
    }

    @Override
    public Member getMember() {
        return annotatedField.getJavaMember();
    }

    @Override
    public Annotated getAnnotated() {
        return annotatedField;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public boolean isTransient() {
        isTransientCalled = true;
        return isTransient;
    }

    public boolean isTransientCalled() {
        return isTransientCalled;
    }

}
