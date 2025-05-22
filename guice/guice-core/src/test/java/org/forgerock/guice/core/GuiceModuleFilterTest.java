/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2021 Wren Security.
 */
package org.forgerock.guice.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GuiceModuleFilterTest {

    private GuiceModuleFilter moduleFilter = new GuiceModuleFilter();

    @DataProvider
    public Object[][] validConfig() {
        return new Object[][] {
            { "SecondOverride", new Module[] { new RootModule(), new FirstOverride(), new SecondOverride() } },
            { "SecondOverride", new Module[] { new FirstOverride(), new SecondOverride(), new RootModule() } },
            { "FirstOverride", new Module[] { new SecondOverride(), new RootModule(), new FirstOverride() } },
            { "OverrideOverride", new Module[] { new OverrideOverride(), new RootModule(), new FirstOverride() } }
        };
    }

    @Test(dataProvider = "validConfig")
    public void shouldFilterOverridenModules(String expected, Module[] modules) {
        // Given

        // When
        Collection<Module> filtered = moduleFilter.filter(Arrays.asList(modules));
        Injector injector = Guice.createInjector(filtered);

        // Then
        assertEquals(filtered.size(), 1);
        assertEquals(injector.getInstance(String.class), expected);
        assertEquals(injector.getInstance(Integer.class), Integer.valueOf(42));
    }

    @Test
    public void shouldNotFilterWithoutOverride() {
        // Given
        Collection<Module> modules = Arrays.asList(new StringModule() {}, new StringModule() {});

        // When
        Collection<Module> filtered = moduleFilter.filter(modules);

        //Then
        assertEquals(filtered, modules);
    }

    @DataProvider
    public Object[][] cycleConfig() {
        return new Object[][] {
            new Module[] { new RootModule(), new CycleOverrideA(), new CycleOverrideB() },
            new Module[] { new RootModule(), new CycleOverrideB(), new CycleOverrideA() },
            new Module[] { new CycleOverrideA(), new CycleOverrideB(), new RootModule() },
        };
    }

    @Test(dataProvider = "cycleConfig")
    public void shouldIgnoreOverrideOnCycle(Module[] modules) {
        // Given

        // When
        Collection<Module> filtered = moduleFilter.filter(Arrays.asList(modules));

        //Then
        assertEquals(filtered.size(), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, timeOut = 1000)
    public void shouldThrowExceptionOnInvalidReference() {
        // Given
        Collection<Module> modules = Arrays.asList(new FirstOverride());

        // When
        moduleFilter.filter(modules);

        //Then
        fail("Invalid reference not detected");
    }

    static abstract class StringModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance(getClass().getSimpleName());
        }
    }

    static final class RootModule extends StringModule {
        @Override
        protected void configure() {
            super.configure();
            bind(Integer.class).toInstance(42);
        }
    }

    @GuiceOverride(RootModule.class)
    static final class FirstOverride extends StringModule {
    }

    @GuiceOverride(RootModule.class)
    static final class SecondOverride extends StringModule {
    }

    @GuiceOverride(FirstOverride.class)
    static final class OverrideOverride extends StringModule {
    }

    @GuiceOverride(CycleOverrideB.class)
    static final class CycleOverrideA extends StringModule {
    }

    @GuiceOverride(CycleOverrideA.class)
    static final class CycleOverrideB extends StringModule {
    }

}
