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
 * Copyright 2018 Wren Security. All rights reserved.
 */
package org.forgerock.api.util;

import java.math.BigDecimal;

/**
 * Common utility methods for dealing with {@code BigDecimal} values.
 */
public final class BigDecimalUtil {
    /**
     * Private constructor for singleton static class.
     */
    private BigDecimalUtil() { }

    /**
     * Convert a nullable {@code Double} into a {@code BigDecimal} value.
     *
     * <p>If the value is {@code null}, then {@code null} is returned.
     *
     * @param value
     *   The value to convert to a {@code BigDecimal}.
     *
     * @return
     *   If {@code value} is not {@code null}, the {@code BigDecimal} value of the double value;
     *   otherwise, {@code null}.
     */
    public static BigDecimal safeValueOf(final Double value) {
        if (value == null) {
            return null;
        } else {
            return BigDecimal.valueOf(value);
        }
    }
}
