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

package org.forgerock.json.jose.tokenhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.*;

import java.security.Key;
import java.security.KeyPair;

import org.wrensecurity.guava.common.base.Optional;
import org.forgerock.json.JsonValue;
import org.forgerock.json.jose.builders.JwtBuilderFactory;
import org.forgerock.json.jose.helper.KeysHelper;
import org.forgerock.json.jose.jwe.EncryptionMethod;
import org.forgerock.json.jose.jwe.JweAlgorithm;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.SignedEncryptedJwt;
import org.forgerock.json.jose.jws.SigningManager;
import org.forgerock.json.jose.jws.handlers.SigningHandler;
import org.forgerock.json.jose.jwt.JwtClaimsSet;
import org.forgerock.tokenhandler.ExpiredTokenException;
import org.forgerock.tokenhandler.InvalidTokenException;
import org.forgerock.tokenhandler.TokenHandler;
import org.forgerock.tokenhandler.TokenHandlerException;
import org.forgerock.util.encode.Base64;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class JwtTokenHandlerTest {

    private static final Key KEY = KeysHelper.getRSAPublicKey();
    private static final KeyPair KEY_PAIR = new KeyPair(KeysHelper.getRSAPublicKey(), KeysHelper.getRSAPrivateKey());
    private static final String SHARED_KEY = Base64.encode(KEY.getEncoded());
    private static final SigningHandler SIGNING_HANDLER =
            new SigningManager().newHmacSigningHandler(SHARED_KEY.getBytes());

    @Test
    public void shouldNotBeExpird() throws TokenHandlerException {
        final JsonValue state = jsonState();
        final TokenHandler handler = tokenHandler(Optional.of(5L));
        final String generated = handler.generate(state);
        handler.validate(generated);
        final JsonValue value = handler.validateAndExtractState(generated);
        assertThat(value.isEqualTo(state)).isTrue();
    }

    @Test(expectedExceptions = ExpiredTokenException.class)
    public void shouldThrowExpiredTokenExceptionWhenExpired() throws TokenHandlerException, InterruptedException {
        final TokenHandler handler = tokenHandler(Optional.of(1L));
        final String generated = handler.generate(jsonState());
        Thread.sleep(1000);
        // should be expired now
        handler.validate(generated);
    }

    @Test
    public void shouldNeverExpire() throws TokenHandlerException {
        final JsonValue state = jsonState();
        final TokenHandler handler = tokenHandler(Optional.<Long>absent());
        final String generated = handler.generate(state);

        // decrypt claimset locally as method is private
        SignedEncryptedJwt signedEncryptedJwt = new JwtBuilderFactory()
                .reconstruct(generated, SignedEncryptedJwt.class);

        if (!signedEncryptedJwt.verify(SIGNING_HANDLER)) {
            throw new InvalidTokenException("Invalid token");
        }

        signedEncryptedJwt.decrypt(KEY_PAIR.getPrivate());

        JwtClaimsSet claimsSet = signedEncryptedJwt.getClaimsSet();
        assertThat(claimsSet.getExpirationTime()).isNull();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnBadExpiry() {
        tokenHandler(Optional.of(0L));
    }

    private TokenHandler tokenHandler(Optional<Long> expiry) {
        return new JwtTokenHandler(
                JweAlgorithm.RSAES_PKCS1_V1_5,
                EncryptionMethod.A128CBC_HS256,
                KEY_PAIR,
                JwsAlgorithm.HS256,
                SIGNING_HANDLER,
                expiry);
    }

    private JsonValue jsonState() {
        return json(object(field("state", "ok")));
    }

}
