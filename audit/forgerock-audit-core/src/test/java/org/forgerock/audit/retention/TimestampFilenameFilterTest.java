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
package org.forgerock.audit.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.format.DateTimeFormat.forPattern;

import java.io.File;

import org.joda.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

public class TimestampFilenameFilterTest {
    private static final DateTimeFormatter DATE_FORMAT = forPattern("-yyyy.MM.dd-HH.mm.ss.SSS");
    private static final File UNUSED = new File("/tmp");

    @Test
    public void testFilterRetainsOnlyMatchingFiles() {
        final TimestampFilenameFilter filter = new TimestampFilenameFilter(new File("test"), "prefix.", DATE_FORMAT);

        assertThat(filter.accept(UNUSED, "test")).isFalse();
        assertThat(filter.accept(UNUSED, "test-2017.03.02-11.15.00.000")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.test")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.test-invalidDateFormat")).isFalse();
        assertThat(filter.accept(UNUSED, "wrong-prefix.test-2017.03.02-11.15.00.000")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.wrong-file-name-2017.03.02-11.15.00.000")).isFalse();

        assertThat(filter.accept(UNUSED, "prefix.test-2017.03.02-11.15.00.000")).isTrue();
    }

    @Test
    public void testFilterWithNullPrefixRetainsOnlyMatchingFiles() {
        final TimestampFilenameFilter filter = new TimestampFilenameFilter(new File("test"), null, DATE_FORMAT);

        assertThat(filter.accept(UNUSED, "test")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.test")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.test-invalidDateFormat")).isFalse();
        assertThat(filter.accept(UNUSED, "wrong-prefix.test-2017.03.02-11.15.00.000")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.wrong-file-name-2017.03.02-11.15.00.000")).isFalse();
        assertThat(filter.accept(UNUSED, "prefix.test-2017.03.02-11.15.00.000")).isFalse();

        assertThat(filter.accept(UNUSED, "test-2017.03.02-11.15.00.000")).isTrue();
    }
}
