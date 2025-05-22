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

package org.forgerock.json.jose.jwk.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.json.jose.jwe.JweAlgorithm;
import org.forgerock.json.jose.jwk.JWK;
import org.forgerock.json.jose.jwk.JWKSet;
import org.forgerock.json.jose.jwk.JWKSetParser;
import org.forgerock.json.jose.jwk.KeyUse;
import org.forgerock.json.jose.jwk.RsaJWK;
import org.forgerock.util.time.Duration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JwksStoreTestWithEncryption {

    private JWKSetParser jwkSetParser;
    private URL jwkUrl = new URL("http://example.com");
    private String jwksStoreID = "toto";

    private JWKSet jwkSet;
    private JWK jwkForEnc;

    public JwksStoreTestWithEncryption() throws MalformedURLException {
    }

    @BeforeMethod
    public void generateJwksForEncWithOneKey() throws NoSuchAlgorithmException, FailedToLoadJWKException {
        jwkSetParser = mock(JWKSetParser.class);
        jwkForEnc = rotateJwkEncryptionKey("jwkForEncBeforeRotation");
    }

    @Test
    public void testFindJWKForEncryptionBeforeCacheTimeout() throws NoSuchAlgorithmException, FailedToLoadJWKException {
        //Given

        //Create a jwksStore pointing to this jwkSet
        JwksStore jwksStore = new JwksStore(jwksStoreID, JwksStoreService.JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS,
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS, jwkUrl, jwkSetParser);

        //When
        JWK jwkForEncAfterRotation = rotateJwkEncryptionKey("jwkForEncAfterRotation");
        JWK jwkFoundForEnc = jwksStore.findJwk(JweAlgorithm.RSA_OAEP_256, KeyUse.ENC);

        //then
        //We check that we are still using the old JWK, as the cache is not timeout yet.
        assertThat(jwkFoundForEnc).isNotNull();
        assertThat(jwkFoundForEnc.getKeyId()).isEqualTo(jwkForEnc.getKeyId());
        assertThat(jwkFoundForEnc.getKeyId()).isNotEqualTo(jwkForEncAfterRotation.getKeyId());
    }

    @Test
    public void testFindJWKForEncryptionAfterCacheTimeout() throws NoSuchAlgorithmException, FailedToLoadJWKException {
        //Given

        //Create a jwksStore pointing to this jwkSet
        JwksStore jwksStore = new JwksStore(jwksStoreID, Duration.duration(0L, TimeUnit.MILLISECONDS),
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS, jwkUrl, jwkSetParser);

        //When
        JWK jwkForEncAfterRotation = rotateJwkEncryptionKey("jwkForEncAfterRotation");
        JWK jwkFoundForEnc = jwksStore.findJwk(JweAlgorithm.RSA_OAEP_256, KeyUse.ENC);

        //then
        //This time, the cache was timeout so we should use the new JWK
        assertThat(jwkFoundForEnc).isNotNull();
        assertThat(jwkFoundForEnc.getKeyId()).isNotEqualTo(jwkForEnc.getKeyId());
        assertThat(jwkFoundForEnc.getKeyId()).isEqualTo(jwkForEncAfterRotation.getKeyId());
    }

    private JWK rotateJwkEncryptionKey(String kid) throws NoSuchAlgorithmException, FailedToLoadJWKException {
        //Generate a JWK for enc
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        JWK jwkForEnc = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.ENC,
                JweAlgorithm.RSA_OAEP_256.getJwaAlgorithmName(),
                kid, null, null, null);

        //Transform it as a JwkSet
        List<JWK> jwks = new ArrayList<>();
        jwks.add(jwkForEnc);
        jwkSet = new JWKSet(jwks);
        given(jwkSetParser.jwkSet(jwkUrl)).willReturn(jwkSet);
        return jwkForEnc;
    }
}
