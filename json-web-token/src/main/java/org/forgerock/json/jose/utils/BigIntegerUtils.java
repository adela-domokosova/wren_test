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

import java.math.BigInteger;

import org.forgerock.util.encode.Base64url;

/** Utils to complement bit operations not covered by the BigInteger functions. */
public final class BigIntegerUtils {

    private BigIntegerUtils() {
    }

    /**
     * Returns the magnitude big-endian byte array of a big integer.
     * @param x a big integer
     * @return the magnitude big-endian byte array of x
     */
    public static byte[] toBytesUnsigned(final BigInteger x) {
        /*
            we got a binary number represented in multiple octets, and we want to take only the k first bits, with here
            k = x.bitLength();
            As it is a positive number, the last byte will be completed by 0 bits.

            x = 00000011 10111101 10011111 and k = 18
            Let's write x with references: x = A(00000011) B(10111101) C(10011111)
            A,B,C are bytes.

            With k=18, it means the unsigned big endian representation would be the first 18 bits,
            so 11 10111101 10011111
            As we have to store it in byte array, we will have to complete with zero anyway, resulting to:
            00000011 10111101 10011111 which is x in this case but not always:

            The limit case when k % 8 = 0, which is called byte-aligned, will have a different result. As x contains the
            sign too, it will need to add an extra byte to represent the sign. For example:
            For the positive number 11011000 11100000, so k = 16, x would be 00000000 11011000 11100000
            The extra 0 bits are here to indicates it's indeed a positive number.
            For our problem, we need to skip the extra 0 and returning 11011000 11100000

         */
        final byte[] xBytes = x.abs().toByteArray();

        //As explained earlier, we can return the xbytes if this is not the byte-align case
        if (xBytes.length == 0 || xBytes[0] != 0x00) {
            return xBytes;
        }
        final byte[] unsignedBigEndian = new byte[xBytes.length - 1];
        System.arraycopy(xBytes, 1, unsignedBigEndian, 0, xBytes.length - 1);
        return unsignedBigEndian;
    }

    /**
     * Decode a big-endian base64 url encoding of a magnitude big integer and transform it as a positive big integer.
     * @param magnitudeBase64UrlEncoded  big-endian base64 url encoding of a big integer magnitude
     * @return a positive big integer with the magnitude decoded from thhe
     */
    public static BigInteger base64UrlDecode(String magnitudeBase64UrlEncoded) {
        final int positive = 1;
        return new BigInteger(positive, Base64url.decode(magnitudeBase64UrlEncoded));
    }
}
