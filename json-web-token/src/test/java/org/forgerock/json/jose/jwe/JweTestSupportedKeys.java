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
package org.forgerock.json.jose.jwe;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.forgerock.json.jose.builders.JweHeaderBuilder;
import org.forgerock.json.jose.builders.JwtBuilderFactory;
import org.forgerock.json.jose.builders.SignedJwtBuilderImpl;
import org.forgerock.json.jose.builders.SignedThenEncryptedJwtBuilder;
import org.forgerock.json.jose.common.JwtReconstruction;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.SignedJwt;
import org.forgerock.json.jose.jws.SigningManager;
import org.forgerock.json.jose.jws.handlers.SigningHandler;
import org.forgerock.json.jose.jwt.Jwt;
import org.forgerock.json.jose.jwt.JwtClaimsSet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JweTestSupportedKeys {
    private KeyPair rsaKeyPair;

    private KeyPair p256KeyPair;
    private KeyPair p384KeyPair;
    private KeyPair p521KeyPair;

    @BeforeClass
    public void generateKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        rsaKeyPair = keyPairGenerator.generateKeyPair();

        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC");
        ecKeyPairGenerator.initialize(256);
        p256KeyPair = ecKeyPairGenerator.generateKeyPair();

        ecKeyPairGenerator.initialize(384);
        p384KeyPair = ecKeyPairGenerator.generateKeyPair();

        ecKeyPairGenerator.initialize(521);
        p521KeyPair = ecKeyPairGenerator.generateKeyPair();
    }

    @DataProvider
    private Object[][] algorithms() throws NoSuchAlgorithmException {
        final List<Object[]> results = new ArrayList<>();
        for (JwsAlgorithm jwsAlgorithm : JwsAlgorithm.values()) {
            if (jwsAlgorithm == JwsAlgorithm.NONE) {
                continue;
            }
            results.add(new Object[]{jwsAlgorithm, null, null}); // Signing only
            for (JweAlgorithm jweAlgorithm : JweAlgorithm.values()) {
                if (jweAlgorithm.getAlgorithmType() == JweAlgorithmType.AES_KEYWRAP) {
                    if (jweAlgorithm != JweAlgorithm.A128KW && Cipher.getMaxAllowedKeyLength("AES") < 192) {
                        // Key size not supported on this platform
                        continue;
                    }
                }
                for (EncryptionMethod encryptionMethod : EncryptionMethod.values()) {
                    if (encryptionMethod.getKeyOffset() * 8 > Cipher.getMaxAllowedKeyLength("AES")) {
                        // Key size not supported
                        continue;
                    }

                    try {
                        Cipher.getInstance(encryptionMethod.getTransformation());
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                        // AES-GCM not supported
                        continue;
                    }

                    results.add(new Object[]{jwsAlgorithm, jweAlgorithm, encryptionMethod});
                }
            }
        }
        return results.toArray(new Object[0][]);
    }

    @Test(dataProvider = "algorithms")
    public void shouldSupportAllSigningAndEncryptionModes(JwsAlgorithm signingAlgorithm,
            JweAlgorithm encryptionAlgorithm, EncryptionMethod encryptionMethod) throws Exception {
        // Given
        Key encryptionKey = getEncryptionKey(encryptionAlgorithm, encryptionMethod);
        Key decryptionKey = getDecryptionKey(encryptionAlgorithm, encryptionMethod);
        KeyPair signingKeyPair = getSigningKeyPair(signingAlgorithm);
        JwtReconstruction jwtReconstruction = new JwtReconstruction();
        String kid = "toto";
        JwtClaimsSet claimsSet = new JwtClaimsSet();

        // When
        SignedJwtBuilderImpl jws = signJwt(claimsSet, kid, signingAlgorithm, signingKeyPair);
        String jwt;
        if (encryptionAlgorithm == null) {
            //no encryption needed
            jwt = jws.build();
        } else {
            jwt = encryptJWT(jws, kid, encryptionKey, encryptionAlgorithm, encryptionMethod).build();
        }

        // Then
        if (encryptionAlgorithm == null) {
            SignedJwt signedJwt = jwtReconstruction.reconstructJwt(jwt, SignedJwt.class);
            assertThat(signedJwt.getHeader().getParameters()).containsEntry("alg", signingAlgorithm.toString());
        } else {
            SignedThenEncryptedJwt signedThenEncryptedJwt = jwtReconstruction.reconstructJwt(jwt,
                    SignedThenEncryptedJwt.class);
            assertThat(signedThenEncryptedJwt.getHeader().getParameters())
                    .containsEntry("alg", encryptionAlgorithm.toString())
                    .containsEntry("enc", encryptionMethod.toString());

            signedThenEncryptedJwt.decrypt(decryptionKey);
            assertThat(signedThenEncryptedJwt.getClaimsSet()).isNotNull();
        }
    }

    private SigningHandler getSigningHandler(JwsAlgorithm jwsAlgorithm, KeyPair signingKeyPair) {
        switch (jwsAlgorithm.getAlgorithmType()) {
        case HMAC:
            return new SigningManager().newHmacSigningHandler("test".getBytes());
        case RSA:
            return new SigningManager().newRsaSigningHandler(signingKeyPair.getPrivate());
        case ECDSA:
            if (!(signingKeyPair.getPrivate() instanceof ECPrivateKey)) {
                throw new IllegalArgumentException("Expecting private key to be a ECPrivateKey");
            }
            return new SigningManager().newEcdsaSigningHandler((ECPrivateKey) signingKeyPair.getPrivate());
        case NONE:
        default:
            throw new IllegalArgumentException("Type of algorithm '" + jwsAlgorithm.getAlgorithmType()
                    + "' not supported yet.");
        }
    }

    private SignedJwtBuilderImpl signJwt(JwtClaimsSet claimsSet, String kid, JwsAlgorithm jwsAlgorithm,
            KeyPair signingKeyPair) {
        SigningHandler signingHandler = getSigningHandler(jwsAlgorithm, signingKeyPair);
        JwtBuilderFactory jwtBuilderFactory = new JwtBuilderFactory();
        return jwtBuilderFactory
                .jws(signingHandler)
                .headers()
                .alg(jwsAlgorithm)
                .kid(kid)
                .done()
                .claims(claimsSet);
    }

    private Jwt encryptJWT(SignedJwtBuilderImpl signedJwtBuilderImpl, String kid, Key publicKey,
            JweAlgorithm jweAlgorithm, EncryptionMethod encryptionMethod) {
        JweHeaderBuilder<SignedThenEncryptedJwtBuilder> builder = signedJwtBuilderImpl.encrypt(publicKey)
                .headers()
                .alg(jweAlgorithm)
                .enc(encryptionMethod)
                .cty("JWT")
                .kid(kid);

        return builder.done().asJwt();
    }

    private KeyPair getSigningKeyPair(JwsAlgorithm algorithm) {
        switch (algorithm) {
        case RS256:
            return rsaKeyPair;
        case ES256:
            return p256KeyPair;
        case ES384:
            return p384KeyPair;
        case ES512:
            return p521KeyPair;
        default:
            return null;
        }
    }

    private Key getEncryptionKey(JweAlgorithm algorithm, EncryptionMethod encryptionMethod) {
        return getKey(algorithm, encryptionMethod, true);
    }

    private Key getDecryptionKey(JweAlgorithm algorithm, EncryptionMethod encryptionMethod) {
        return getKey(algorithm, encryptionMethod, false);
    }

    private Key getKey(JweAlgorithm algorithm, EncryptionMethod encryptionMethod, boolean isPublic) {
        if (algorithm == null) {
            return null;
        }
        switch (algorithm) {
        case RSA_OAEP:
        case RSA_OAEP_256:
        case RSAES_PKCS1_V1_5:
            return isPublic ? rsaKeyPair.getPublic() : rsaKeyPair.getPrivate();
        case DIRECT:
            return new SecretKeySpec(new byte[encryptionMethod.getKeySize() / 8], "AES");
        case A128KW:
            return new SecretKeySpec(new byte[16], "AES");
        case A192KW:
            return new SecretKeySpec(new byte[24], "AES");
        case A256KW:
            return new SecretKeySpec(new byte[32], "AES");
        default:
            return null;
        }
    }
}
