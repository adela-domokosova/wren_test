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

package org.forgerock.json.jose.jwk.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.json.jose.jwk.EcJWK;
import org.forgerock.json.jose.jwk.JWK;
import org.forgerock.json.jose.jwk.JWKSet;
import org.forgerock.json.jose.jwk.JWKSetParser;
import org.forgerock.json.jose.jwk.KeyUse;
import org.forgerock.json.jose.jwk.RsaJWK;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.util.time.Duration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JwksStoreTest {

    private JWKSetParser jwkSetParser;
    private URL jwkUrl = new URL("http://example.com");
    private String jwksStoreID = "toto";

    private JWKSet jwkSet;
    private Map<String, JWK> jwksMapByKid;
    private JWK extraJWK;

    public JwksStoreTest() throws MalformedURLException {
    }

    @BeforeMethod
    public void generateRandomJwks() throws NoSuchAlgorithmException {
        jwkSetParser = mock(JWKSetParser.class);
        jwksMapByKid = new HashMap<>();

        //Generate some RSA JWKs
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RsaJWK rsaJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG,
                    JwsAlgorithm.RS256.getJwaAlgorithmName(), "rsaJwk" + i, null, null, null);
            jwksMapByKid.put(rsaJwk.getKeyId(), rsaJwk);
        }

        //Generate some EC JWKs
        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC");
        ecKeyPairGenerator.initialize(256);
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = ecKeyPairGenerator.generateKeyPair();
            EcJWK ecJwk = new EcJWK((ECPublicKey) keyPair.getPublic(), KeyUse.SIG, "ecJwk" + i);
            jwksMapByKid.put(ecJwk.getKeyId(), ecJwk);
        }

        jwkSet = new JWKSet(new ArrayList<>(jwksMapByKid.values()));

        //Extra JWK for test
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        extraJWK = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG, JwsAlgorithm.RS256.getJwaAlgorithmName(),
                "extraJWK", null, null, null);
    }

    @Test
    public void testLoadingJwksUri() throws FailedToLoadJWKException {
        //Given
        given(jwkSetParser.jwkSet(jwkUrl)).willReturn(jwkSet);

        //When
        JwksStore jwksStore = new JwksStore(jwksStoreID, JwksStoreService.JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS,
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS, jwkUrl, jwkSetParser);

        //then
        for (JWK expectedJwk : jwksMapByKid.values()) {
            assertEquals(jwksStore.findJwk(expectedJwk.getKeyId()).getKeyId(), expectedJwk.getKeyId());
        }
    }

    @Test
    public void testChangingJwksUri() throws Exception {
        //Given
        URL originalJwkUrl = new URL("http://different.com");
        JwksStore jwksStore = new JwksStore(jwksStoreID, JwksStoreService.JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS,
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS, originalJwkUrl, jwkSetParser);
        given(jwkSetParser.jwkSet(this.jwkUrl)).willReturn(jwkSet);

        //When
        jwksStore.setJwkUrl(this.jwkUrl);

        //then
        for (JWK expectedJwk : jwksMapByKid.values()) {
            assertEquals(jwksStore.findJwk(expectedJwk.getKeyId()).getKeyId(), expectedJwk.getKeyId());
        }
        verify(jwkSetParser).jwkSet(originalJwkUrl);
        verify(jwkSetParser).jwkSet(this.jwkUrl);
    }

    @Test
    public void testFindUnknownKidBeforeCacheMissCacheTime() throws FailedToLoadJWKException, NoSuchAlgorithmException {
        //Given
        given(jwkSetParser.jwkSet(jwkUrl)).willReturn(jwkSet);
        JwksStore jwksStore = new JwksStore(jwksStoreID, JwksStoreService.JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS,
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS, jwkUrl, jwkSetParser);
        verify(jwkSetParser, times(1)).jwkSet(jwkUrl);

        //When
        //We update the jwkSet by adding the extraJWK in it
        List<JWK> jwks = new ArrayList<>(jwksMapByKid.values());
        jwks.add(extraJWK);
        jwkSet = new JWKSet(jwks);
        given(jwkSetParser.jwkSet(jwkUrl)).willReturn(jwkSet);

        //We try to find this JWK in the JwksStore
        JWK jwkFound = jwksStore.findJwk(extraJWK.getKeyId());

        //then
        assertThat(jwkFound).isNull();
        //We check that the cache hasn't be reloaded, as expected
        verify(jwkSetParser, times(1)).jwkSet(jwkUrl);
    }

    @Test
    public void testFindUnknownKidAfterCacheMissCacheTime() throws FailedToLoadJWKException, NoSuchAlgorithmException {
        //Given
        given(jwkSetParser.jwkSet(jwkUrl)).willReturn(jwkSet);
        JwksStore jwksStore = new JwksStore(jwksStoreID, JwksStoreService.JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS,
                Duration.duration(0L, TimeUnit.MILLISECONDS), jwkUrl, jwkSetParser);

        //When
        //We update the jwkSet by adding the extraJWK in it
        List<JWK> jwks = new ArrayList<>(jwksMapByKid.values());
        jwks.add(extraJWK);
        jwkSet = new JWKSet(jwks);
        given(jwkSetParser.jwkSet(jwkUrl)).willReturn(jwkSet);

        //We try to find this JWK in the JwksStore
        JWK jwkFound = jwksStore.findJwk(extraJWK.getKeyId());

        //then
        assertThat(jwkFound).isNotNull();
        verify(jwkSetParser, times(2)).jwkSet(jwkUrl);
    }
}
