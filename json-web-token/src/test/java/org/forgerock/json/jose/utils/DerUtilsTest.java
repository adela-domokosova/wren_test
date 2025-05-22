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

package org.forgerock.json.jose.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.jose.utils.DerUtils.writeInteger;

import java.nio.ByteBuffer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DerUtilsTest {

    @DataProvider
    public Object[][] testData() {
        return new Object[][]{
                {new byte[] {0x0}, 3},
                {new byte[] {0x1}, 3},
                {new byte[] {0x1, 0x2}, 4},
                {new byte[] {0x0, 0x1, 0x2}, 4},
                {new byte[] {0x0, 0x0, 0x1, 0x2}, 4},
                {new byte[] {0x0, 0x0, 0x0, 0x1, 0x2}, 4},
                {new byte[] {0xf, 0xf, 0xf}, 5},
        };
    }

    @Test(dataProvider = "testData")
    public void trimsLeadingZeros(byte[] data, Integer length) {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        writeInteger(buffer, data);
        assertThat(buffer.position()).isEqualTo(length);
    }
}
