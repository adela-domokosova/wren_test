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
 * Copyright 2017 ForgeRock AS.
 */

package org.forgerock.selfservice.stages.utils;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.forgerock.util.i18n.PreferredLocales;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LocaleUtils}.
 *
 * @since 0.2.1
 */
public class LocaleUtilsTest {

    @BeforeClass
    public void init() {
        System.setProperty("org.forgerock.selfservice.defaultLocale", Locale.ITALIAN.toLanguageTag());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailWhenNoTranslationFound() {
        //given
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.FRENCH, "Bonjour");
        PreferredLocales preferredLocales = new PreferredLocales(Arrays.asList(Locale.GERMAN));

        //when
        LocaleUtils.getTranslationFromLocaleMap(preferredLocales, translations);
    }

    @Test
    public void shouldFallBackToSelfServiceDefaultTranslation() {
        //given
        PreferredLocales preferredLocales = new PreferredLocales(Arrays.asList(Locale.GERMAN));
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.FRENCH, "Bonjour");
        translations.put(Locale.ITALIAN, "Ciao");

        //when
        String translation = LocaleUtils.getTranslationFromLocaleMap(preferredLocales, translations);

        //then
        Locale ussDefaultLocale = Locale.forLanguageTag(System.getProperty("org.forgerock.selfservice.defaultLocale"));
        assertThat(translation).isEqualTo(translations.get(ussDefaultLocale));
    }

    @Test
    public void shouldFallBackToSelfServiceDefaultTranslationWithRootPreferredLocale() {
        //given
        PreferredLocales preferredLocales =
                new PreferredLocales(Arrays.asList(Locale.GERMAN, Locale.ROOT, Locale.FRENCH));
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.FRENCH, "Bonjour");
        translations.put(Locale.ITALIAN, "Ciao");

        //when
        String translation = LocaleUtils.getTranslationFromLocaleMap(preferredLocales, translations);

        //then
        Locale ussDefaultLocale = Locale.forLanguageTag(System.getProperty("org.forgerock.selfservice.defaultLocale"));
        assertThat(translation).isEqualTo(translations.get(ussDefaultLocale));
    }

    @Test
    public void shouldFallBackToJVMDefaultTranslation() {
        //given
        PreferredLocales preferredLocales = new PreferredLocales(Arrays.asList(Locale.GERMAN));
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.FRENCH, "Bonjour");
        translations.put(Locale.getDefault(), "Hello");

        //when
        String translation = LocaleUtils.getTranslationFromLocaleMap(preferredLocales, translations);

        //then
        assertThat(translation).isEqualTo(translations.get(Locale.getDefault()));
    }

    @Test
    public void shouldFallBackToJVMDefaultTranslationWithRootPreferredLocale() {
        //given
        PreferredLocales preferredLocales =
                new PreferredLocales(Arrays.asList(Locale.GERMAN, Locale.ROOT, Locale.FRENCH));
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.FRENCH, "Bonjour");
        translations.put(Locale.getDefault(), "Hello");

        //when
        String translation = LocaleUtils.getTranslationFromLocaleMap(preferredLocales, translations);

        //then
        assertThat(translation).isEqualTo(translations.get(Locale.getDefault()));
    }

    @Test
    public void shouldReturnTranslationForPreferredLocale() {
        //given
        PreferredLocales preferredLocales = new PreferredLocales(Arrays.asList(Locale.FRENCH));
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.FRENCH, "Bonjour");

        //when
        String translation = LocaleUtils.getTranslationFromLocaleMap(preferredLocales, translations);

        //then
        assertThat(translation).isEqualTo(translations.get(Locale.FRENCH));
    }

    @AfterClass
    public void tearDown() {
        System.clearProperty("org.forgerock.selfservice.defaultLocale");
    }
}
