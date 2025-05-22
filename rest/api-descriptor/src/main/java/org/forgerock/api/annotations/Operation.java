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

package org.forgerock.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.forgerock.api.enums.Stability;

/**
 * The common details of an operation.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Operation {
    /** The list of supported locales for the operation. */
    String[] locales() default {};
    /** Operation specific error definitions. Use this when standard error descriptions are not specific enough. */
    ApiError[] errors() default {};
    /**
     * JSON references to previously declared errors (e.g., {@code frapi:common#/errors/badRequest}).
     *
     * @see org.forgerock.api.commons.CommonsApi#COMMONS_API_DESCRIPTION
     */
    String[] errorRefs() default {};
    /** Parameters on operation paths and/or endpoints. */
    Parameter[] parameters() default {};
    /** The stability state for the operation. Defaults to {@code STABLE}. */
    Stability stability() default Stability.STABLE;
    /** A description of the operation. */
    String description() default "";
}
