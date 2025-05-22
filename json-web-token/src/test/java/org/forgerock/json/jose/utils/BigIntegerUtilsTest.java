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
 * Copyright 2017 ForgeRock AS
 */

package org.forgerock.json.jose.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BigIntegerUtilsTest {

    @DataProvider
    public Object[][] unsignBigEndianTestData() {
        return new Object[][]{
                {BigInteger.valueOf(0)},
                {BigInteger.valueOf(33L)},
                {BigInteger.valueOf(922337203L)},
                {BigInteger.valueOf(-33L)},
                {BigInteger.valueOf(-922337203L)},
                {BigInteger.valueOf(1).shiftLeft(7)},
                {BigInteger.valueOf(1).shiftLeft(15)},
                {BigInteger.valueOf(1).shiftLeft(23)},
                {BigInteger.valueOf(1).shiftLeft(64)},
                {BigInteger.valueOf(1).shiftLeft(128)},
                {BigInteger.valueOf(1).shiftLeft(256)},
        };
    }

    @Test(dataProvider = "unsignBigEndianTestData")
    public void testToBytesUnsigned(final BigInteger bigIntOrigin) {
        byte[] bytes = BigIntegerUtils.toBytesUnsigned(bigIntOrigin);
        BigInteger bigIntResult = new BigInteger(bigIntOrigin.signum(), bytes);
        assertThat(bigIntResult).isEqualTo(bigIntOrigin);
    }
}
