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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.Module;
import com.google.inject.util.Modules;

/**
 * <p>Filtering Guice module instances before they are handed over to Injector.</p>
 *
 * <p>
 * Filter reads {@link GuiceOverride} annotations and applies module overrides.
 * This allows for simple external overrides of core application components.
 * </p>
 *
 * @since 3.0.0
 */
public class GuiceModuleFilter {

    /**
     * Filter module instances and apply overrides where requested.
     * @param modules Original module instances.
     * @return Filtered module instances.
     */
    Collection<Module> filter(Collection<Module> modules) {
        // Make this method deterministic by honoring module order
        Map<Class<?>, Module> modulesByClass = new LinkedHashMap<>();
        for (Module module : modules) {
            modulesByClass.put(module.getClass(), module);
        }
        // Build override graph edges
        Map<Class<?>, Class<?>> classOverrides = new HashMap<>();
        for (Module module : modules) {
            Class<?> moduleClass = module.getClass();
            GuiceOverride annotation = moduleClass.getAnnotation(GuiceOverride.class);
            if (annotation == null) {
                continue;
            }
            Class<?> overridesClass = annotation.value();
            if (overridesClass == null || overridesClass == moduleClass) {
                continue;
            }
            // Check if the target module exists
            if (!modulesByClass.containsKey(overridesClass)) {
                throw new IllegalArgumentException("Unknown module override for " + moduleClass);
            }
            // Switch target class if it is already being overridden
            if (classOverrides.containsKey(overridesClass)) {
                overridesClass = classOverrides.get(overridesClass);
            }
            classOverrides.put(overridesClass, moduleClass);
        }
        // Build the result module collection
        Collection<Module> finalModules = new ArrayList<>();
        for (Class<?> moduleClass : modulesByClass.keySet()) {
            if (moduleClass.getDeclaredAnnotation(GuiceOverride.class) != null) {
                continue; // Override modules are not part of the final set
            }
            Module finalModule = modulesByClass.get(moduleClass);
            while (true) { // Starting from leaf node so cycles are not possible
                Class<?> overrideClass = classOverrides.get(moduleClass);
                if (overrideClass == null) {
                    break; // No more overrides
                }
                moduleClass = overrideClass;
                finalModule = Modules.override(finalModule).with(modulesByClass.get(overrideClass));
            }
            finalModules.add(finalModule);
        }
        return finalModules;
    }

}
