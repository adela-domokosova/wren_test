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
 * Copyright 2013-2017 ForgeRock AS.
 */

package org.forgerock.json.jose.jwk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.forgerock.json.JsonValue;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.SupportedEllipticCurve;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EcJWKTest {

    private String expectedKty = "EC";
    private SupportedEllipticCurve expectedCurve;
    private BigInteger expectedX;
    private BigInteger expectedY;
    private BigInteger expectedD;
    private KeyUse expectedUse = KeyUse.SIG;
    private String expectedKid = "test";

    private ECPublicKey ecPublicKey;
    private ECPrivateKey ecPrivateKey;

    private String ecJwkAsJsonString;
    private JsonValue ecJwkAsJsonValue;

    @BeforeClass
    public void setup() throws NoSuchAlgorithmException {
        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC");
        ecKeyPairGenerator.initialize(256);
        KeyPair keyPair = ecKeyPairGenerator.generateKeyPair();
        ecPublicKey = (ECPublicKey) keyPair.getPublic();
        ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
        expectedCurve = SupportedEllipticCurve.forKey(ecPublicKey);
        expectedX = ecPublicKey.getW().getAffineX();
        expectedY = ecPublicKey.getW().getAffineY();
        expectedD = ecPrivateKey.getS();

        ecJwkAsJsonValue = json(object(
                field("kty", expectedKty),
                field("crv", expectedCurve.getStandardName()),
                field("alg", JwsAlgorithm.ES256.name()),
                field("x", EcJWK.encodeCoordinate(ecPublicKey.getParams().getCurve().getField().getFieldSize(),
                        ecPublicKey.getW().getAffineX())),
                field("y", EcJWK.encodeCoordinate(ecPublicKey.getParams().getCurve().getField().getFieldSize(),
                        ecPublicKey.getW().getAffineY())),
                field("d", EcJWK.encodeCoordinate(ecPublicKey.getParams().getCurve().getField().getFieldSize(),
                        ecPrivateKey.getS())),
                field("use", expectedUse.toString()),
                field("kid", expectedKid)
        ));
        ecJwkAsJsonString = ecJwkAsJsonValue.toString();
    }

    @Test
    public void testCreateJWKFromAString() throws IOException {
        //When
        EcJWK ecJwk = EcJWK.parse(ecJwkAsJsonString);

        //Then
        assertEcJwkIsEqualToOriginal(ecJwk);
    }

    @Test
    public void testCreateJWKFromAJsonValue() throws IOException {
        //When
        EcJWK ecJwk = EcJWK.parse(ecJwkAsJsonValue);

        //Then
        assertEcJwkIsEqualToOriginal(ecJwk);
    }

    @Test
    public void testCreateEcJWKFromECKey() throws NoSuchAlgorithmException, IOException {
        //When
        EcJWK ecJwk = new EcJWK(ecPublicKey, ecPrivateKey, KeyUse.SIG, expectedKid);

        //Then
        assertEcJwkIsEqualToOriginal(ecJwk);
    }

    private void assertEcJwkIsEqualToOriginal(EcJWK ecJwk) throws IOException {
        BigInteger x = EcJWK.decodeCoordinate(ecJwk.getX());
        BigInteger y = EcJWK.decodeCoordinate(ecJwk.getY());
        BigInteger d = EcJWK.decodeCoordinate(ecJwk.getD());

        assertThat(SupportedEllipticCurve.forKey(ecPrivateKey)).isEqualTo(expectedCurve);
        assertThat(x).isEqualTo(expectedX);
        assertThat(y).isEqualTo(expectedY);
        assertThat(d).isEqualTo(expectedD);


        assertThat(expectedX.signum()).isEqualTo(1);
        assertThat(expectedY.signum()).isEqualTo(1);
        assertThat(expectedD.signum()).isEqualTo(1);

        assertThat(ecJwk.toECPublicKey()).isEqualTo(ecPublicKey);
        assertThat(ecJwk.toECPrivateKey()).isEqualTo(ecPrivateKey);
    }

    @Test
    public void testExportToJWK() {
        //Given
        EcJWK jwk = EcJWK.parse(ecJwkAsJsonValue);
        String x = EcJWK.encodeCoordinate(ecPublicKey.getParams().getCurve().getField().getFieldSize(), expectedX);
        String y = EcJWK.encodeCoordinate(ecPublicKey.getParams().getCurve().getField().getFieldSize(), expectedY);
        String d = EcJWK.encodeCoordinate(ecPublicKey.getParams().getCurve().getField().getFieldSize(), expectedD);

        //When
        JsonValue jwkAsJson = jwk.toJsonValue();

        //Then
        assertEquals(jwkAsJson.get("kty").asString(), expectedKty);
        assertEquals(jwkAsJson.get("crv").asString(), expectedCurve.getStandardName());
        assertEquals(jwkAsJson.get("x").asString(), x);
        assertEquals(jwkAsJson.get("y").asString(), y);
        assertEquals(jwkAsJson.get("d").asString(), d);
        assertEquals(jwkAsJson.get("use").asString(), expectedUse.toString());
        assertEquals(jwkAsJson.get("kid").asString(), expectedKid);
    }
}
