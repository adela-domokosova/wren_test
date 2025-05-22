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
 * Copyright 2016-2017 ForgeRock AS.
 */
package org.forgerock.api.commons;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.forgerock.api.models.Errors;
import org.forgerock.util.i18n.PreferredLocales;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class CommonsApiTest {

    @Test
    public void testCommonsApiTranslationWorks() throws Exception {
        PreferredLocales locales = new PreferredLocales();
        Errors errors = CommonsApi.COMMONS_API_DESCRIPTION.getErrors();
        for (String name : errors.getNames()) {
            assertThat(errors.get(name).getDescription().toTranslatedString(locales)).isNotNull();
        }
    }

    @Test
    public void testCheckForMissingErrorRefs() {
        final Map<CommonsApi.Errors, String> map = new HashMap<>();
        map.put(CommonsApi.Errors.BAD_GATEWAY, CommonsApi.BAD_GATEWAY_REF);
        map.put(CommonsApi.Errors.BAD_REQUEST, CommonsApi.BAD_REQUEST_REF);
        map.put(CommonsApi.Errors.CONFLICT, CommonsApi.CONFLICT_REF);
        map.put(CommonsApi.Errors.EXPECTATION_FAILED, CommonsApi.EXPECTATION_FAILED_REF);
        map.put(CommonsApi.Errors.FORBIDDEN, CommonsApi.FORBIDDEN_REF);
        map.put(CommonsApi.Errors.GATEWAY_TIMEOUT, CommonsApi.GATEWAY_TIMEOUT_REF);
        map.put(CommonsApi.Errors.GONE, CommonsApi.GONE_REF);
        map.put(CommonsApi.Errors.HTTP_VERSION_NOT_SUPPORTED, CommonsApi.HTTP_VERSION_NOT_SUPPORTED_REF);
        map.put(CommonsApi.Errors.INTERNAL_SERVER_ERROR, CommonsApi.INTERNAL_SERVER_ERROR_REF);
        map.put(CommonsApi.Errors.LENGTH_REQUIRED, CommonsApi.LENGTH_REQUIRED_REF);
        map.put(CommonsApi.Errors.METHOD_NOT_ALLOWED, CommonsApi.METHOD_NOT_ALLOWED_REF);
        map.put(CommonsApi.Errors.NOT_ACCEPTABLE, CommonsApi.NOT_ACCEPTABLE_REF);
        map.put(CommonsApi.Errors.NOT_FOUND, CommonsApi.NOT_FOUND_REF);
        map.put(CommonsApi.Errors.NOT_SUPPORTED, CommonsApi.NOT_SUPPORTED_REF);
        map.put(CommonsApi.Errors.PAYMENT_REQUIRED, CommonsApi.PAYMENT_REQUIRED_REF);
        map.put(CommonsApi.Errors.PRECONDITION_FAILED, CommonsApi.PRECONDITION_FAILED_REF);
        map.put(CommonsApi.Errors.PRECONDITION_REQUIRED, CommonsApi.PRECONDITION_REQUIRED_REF);
        map.put(CommonsApi.Errors.PROXY_AUTH_REQUIRED, CommonsApi.PROXY_AUTH_REQUIRED_REF);
        map.put(CommonsApi.Errors.RANGE_NOT_SATISFIABLE, CommonsApi.RANGE_NOT_SATISFIABLE_REF);
        map.put(CommonsApi.Errors.REQUEST_ENTITY_TOO_LARGE, CommonsApi.REQUEST_ENTITY_TOO_LARGE_REF);
        map.put(CommonsApi.Errors.REQUEST_TIMEOUT, CommonsApi.REQUEST_TIMEOUT_REF);
        map.put(CommonsApi.Errors.REQUEST_URI_TOO_LARGE, CommonsApi.REQUEST_URI_TOO_LARGE_REF);
        map.put(CommonsApi.Errors.UNAUTHORIZED, CommonsApi.UNAUTHORIZED_REF);
        map.put(CommonsApi.Errors.UNAVAILABLE, CommonsApi.UNAVAILABLE_REF);
        map.put(CommonsApi.Errors.UNSUPPORTED_MEDIA_TYPE, CommonsApi.UNSUPPORTED_MEDIA_TYPE_REF);
        map.put(CommonsApi.Errors.VERSION_MISMATCH, CommonsApi.VERSION_MISMATCH_REF);
        map.put(CommonsApi.Errors.VERSION_REQUIRED, CommonsApi.VERSION_REQUIRED_REF);

        for (final CommonsApi.Errors key : CommonsApi.Errors.values()) {
            assertThat(map).containsKey(key);
            assertThat(map.get(key)).isEqualTo(key.getReference());
        }
    }
}
