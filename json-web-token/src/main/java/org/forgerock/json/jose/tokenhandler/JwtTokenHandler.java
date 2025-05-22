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
 * Copyright 2015-2017 ForgeRock AS.
 */

package org.forgerock.json.jose.tokenhandler;

import static org.forgerock.json.JsonValue.json;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Date;
import java.util.Map;

import org.wrensecurity.guava.common.base.Optional;
import org.forgerock.json.JsonValue;
import org.forgerock.json.jose.builders.JwtBuilderFactory;
import org.forgerock.json.jose.builders.JwtClaimsSetBuilder;
import org.forgerock.json.jose.jwe.EncryptionMethod;
import org.forgerock.json.jose.jwe.JweAlgorithm;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.SignedEncryptedJwt;
import org.forgerock.json.jose.jws.handlers.SigningHandler;
import org.forgerock.json.jose.jwt.JwtClaimsSet;
import org.forgerock.tokenhandler.ExpiredTokenException;
import org.forgerock.tokenhandler.InvalidTokenException;
import org.forgerock.tokenhandler.TokenHandler;
import org.forgerock.tokenhandler.TokenHandlerException;
import org.forgerock.util.Reject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Token handler for creating tokens using a JWT as the store.
 */
public final class JwtTokenHandler implements TokenHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JwtBuilderFactory jwtBuilderFactory;
    private final JweAlgorithm jweAlgorithm;
    private final EncryptionMethod jweMethod;
    private final KeyPair jweKeyPair;
    private final JwsAlgorithm jwsAlgorithm;
    private final SigningHandler jwsHandler;
    private final Optional<Long> tokenLifeTimeInSeconds;

    /**
     * Constructs a new JWT token handler that never expires.
     *
     * @param jweAlgorithm
     *         the JWE algorithm use to construct the key pair
     * @param jweMethod
     *         the encryption method to use
     * @param jweKeyPair
     *         key pair for the purpose of encryption
     * @param jwsAlgorithm
     *         the JWS algorithm to use
     * @param jwsHandler
     *         the signing handler
     */
    public JwtTokenHandler(JweAlgorithm jweAlgorithm, EncryptionMethod jweMethod, KeyPair jweKeyPair,
            JwsAlgorithm jwsAlgorithm, SigningHandler jwsHandler) {
        this(jweAlgorithm, jweMethod, jweKeyPair, jwsAlgorithm, jwsHandler, Optional.<Long>absent());
    }

    /**
     * Constructs a new JWT token handler.
     *
     * @param jweAlgorithm
     *         the JWE algorithm use to construct the key pair
     * @param jweMethod
     *         the encryption method to use
     * @param jweKeyPair
     *         key pair for the purpose of encryption
     * @param jwsAlgorithm
     *         the JWS algorithm to use
     * @param jwsHandler
     *         the signing handler
     * @param tokenLifeTimeInSeconds
     *         token life time in seconds
     */
    public JwtTokenHandler(JweAlgorithm jweAlgorithm, EncryptionMethod jweMethod, KeyPair jweKeyPair,
            JwsAlgorithm jwsAlgorithm, SigningHandler jwsHandler, Optional<Long> tokenLifeTimeInSeconds) {
        Reject.ifNull(jweAlgorithm, jweMethod, jweKeyPair, jwsAlgorithm, jwsHandler);
        Reject.ifTrue(tokenLifeTimeInSeconds.isPresent() && tokenLifeTimeInSeconds.get() <= 0);
        jwtBuilderFactory = new JwtBuilderFactory();
        this.jweAlgorithm = jweAlgorithm;
        this.jweMethod = jweMethod;
        this.jweKeyPair = jweKeyPair;
        this.jwsAlgorithm = jwsAlgorithm;
        this.jwsHandler = jwsHandler;
        this.tokenLifeTimeInSeconds = tokenLifeTimeInSeconds;
    }

    @Override
    public String generate(JsonValue state) throws TokenHandlerException {
        Reject.ifNull(state);

        try {
            JwtClaimsSetBuilder claimsSetBuilder = jwtBuilderFactory
                    .claims()
                    .claim("state", MAPPER.writeValueAsString(state.getObject()));

            final JwtClaimsSet claimsSet;
            if (tokenLifeTimeInSeconds.isPresent()) {
                claimsSet = claimsSetBuilder
                        .exp(new Date(System.currentTimeMillis() + (tokenLifeTimeInSeconds.get() * 1000L)))
                        .build();
            } else {
                claimsSet = claimsSetBuilder.build();
            }

            return jwtBuilderFactory
                    .jwe(jweKeyPair.getPublic())
                    .headers()
                        .alg(jweAlgorithm)
                        .enc(jweMethod)
                        .done()
                    .claims(claimsSet)
                    .sign(jwsHandler, jwsAlgorithm)
                    .build();
        } catch (IOException e) {
            throw new TokenHandlerException("Error serializing token state", e);
        } catch (RuntimeException e) {
            throw new TokenHandlerException("Error constructing token", e);
        }
    }

    @Override
    public void validate(String snapshotToken) throws TokenHandlerException {
        validateAndExtractClaims(snapshotToken);
    }

    @Override
    public JsonValue validateAndExtractState(String snapshotToken) throws TokenHandlerException {
        Reject.ifNull(snapshotToken);

        try {
            JwtClaimsSet claimsSet = validateAndExtractClaims(snapshotToken);
            return json(MAPPER.readValue(claimsSet.getClaim("state").toString(), Map.class));
        } catch (IOException e) {
            throw new InvalidTokenException("Failed to parse token state as JSON", e);
        }
    }

    private JwtClaimsSet validateAndExtractClaims(String snapshotToken) throws TokenHandlerException {
        try {
            SignedEncryptedJwt signedEncryptedJwt = jwtBuilderFactory
                    .reconstruct(snapshotToken, SignedEncryptedJwt.class);

            if (!signedEncryptedJwt.verify(jwsHandler)) {
                throw new InvalidTokenException("Invalid token");
            }

            signedEncryptedJwt.decrypt(jweKeyPair.getPrivate());

            JwtClaimsSet claimsSet = signedEncryptedJwt.getClaimsSet();
            Date expirationTime = claimsSet.getExpirationTime();

            if (expirationTime != null && expirationTime.before(new Date())) {
                throw new ExpiredTokenException("Token has expired");
            }

            return claimsSet;
        } catch (RuntimeException e) {
            throw new InvalidTokenException("Invalid token", e);
        }
    }

}
