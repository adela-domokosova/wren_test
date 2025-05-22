/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015-2017 ForgeRock AS.
 * Portions Copyright 2023 Wren Security.
 */

package org.forgerock.selfservice.stages.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.forgerock.util.i18n.PreferredLocales;

/**
 * Utility class for Locales.
 *
 * @since 0.8.0
 */
public final class LocaleUtils {

    private LocaleUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Using the user's preferred locales (for example, from the "Accept-Language" header in the HTTP context),
     * select the most optimal (string) translation from the map.  If there is nothing acceptable, throw an exception.
     *
     * @param preferredLocales
     *         the preferred locales
     * @param translations
     *         Map of locales to strings
     *
     * @return the most appropriate string given the above
     *
     * @throws IllegalArgumentException
     *         If an acceptable string cannot be found
     */
    public static String getTranslationFromLocaleMap(PreferredLocales preferredLocales, Map<Locale,
            String> translations) {
        List<Locale> locales = getTargetLocales(preferredLocales);
        Set<Locale> candidates = translations.keySet();
        int numberLocales = locales.size();
        for (int i = 0; i < numberLocales; i++) {
            Locale locale = locales.get(i);
            List<Locale> remainingLocales = locales.subList(i + 1, numberLocales);
            for (Locale candidate : candidates) {
                if (PreferredLocales.matches(locale, candidate, remainingLocales)) {
                    return translations.get(candidate);
                }
            }
        }
        throw new IllegalArgumentException("Cannot find suitable translation from given choices");
    }

    /**
     * Add the SELF_SERVICE_DEFAULT_LOCALE and JVM Default locale to the list.
     */
    private static List<Locale> getTargetLocales(PreferredLocales preferredLocales) {
        Locale defaultLocale =
                Locale.forLanguageTag(System.getProperty("org.forgerock.selfservice.defaultLocale", "en-US"));
        boolean defaultsAdded = false;
        List<Locale> locales = new ArrayList<>();
        if (preferredLocales != null) {
            for (Locale locale : preferredLocales.getLocales()) {
                if (locale.equals(Locale.ROOT) && !defaultsAdded) {
                    locales.add(defaultLocale);
                    locales.add(Locale.getDefault());
                    defaultsAdded = true;
                } else if (!locale.equals(Locale.ROOT)) {
                    locales.add(locale);
                }
            }
        }
        if (!defaultsAdded) {
            locales.add(defaultLocale);
            locales.add(Locale.getDefault());
        }
        return locales;
    }
}
