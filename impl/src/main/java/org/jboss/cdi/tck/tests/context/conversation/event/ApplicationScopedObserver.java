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
package org.jboss.cdi.tck.tests.context.conversation.event;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.servlet.ServletRequestEvent;

@ApplicationScoped
public class ApplicationScopedObserver {

    private final AtomicBoolean destroyedCalled = new AtomicBoolean();

    void observeRequestDestroyed(@Observes @Destroyed(ConversationScoped.class) ServletRequestEvent event) {
        if (event.getServletRequest().getAttribute("foo") != null) {
            destroyedCalled.set(true);
        }
    }

    boolean isDestroyedCalled() {
        return destroyedCalled.get();
    }

    public void reset() {
        destroyedCalled.set(false);
    }
}
