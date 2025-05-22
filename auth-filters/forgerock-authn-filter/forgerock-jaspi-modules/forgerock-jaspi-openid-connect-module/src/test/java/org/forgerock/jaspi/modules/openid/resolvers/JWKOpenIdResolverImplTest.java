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
* Copyright 2014-2017 ForgeRock AS.
*/
package org.forgerock.jaspi.modules.openid.resolvers;

import org.forgerock.json.jose.builders.JwtBuilderFactory;
import org.forgerock.json.jose.common.JwtReconstruction;
import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.jaspi.modules.openid.exceptions.InvalidIssException;
import org.forgerock.jaspi.modules.openid.exceptions.InvalidSignatureException;
import org.forgerock.jaspi.modules.openid.exceptions.JwtExpiredException;
import org.forgerock.json.jose.jwk.KeyUse;
import org.forgerock.json.jose.jwk.RsaJWK;
import org.forgerock.json.jose.jwk.store.JwksStore;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jws.JwsHeader;
import org.forgerock.json.jose.jws.SignedJwt;
import org.forgerock.json.jose.jws.SigningManager;
import org.forgerock.json.jose.jws.handlers.SigningHandler;
import org.forgerock.json.jose.jwt.JwtClaimsSet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class JWKOpenIdResolverImplTest {

    JWKOpenIdResolverImpl testResolver;
    SigningHandler signingHandler;
    JwksStore jwksStore;

    @BeforeMethod
    public void setUp() throws FailedToLoadJWKException, MalformedURLException {
        signingHandler = mock(SigningHandler.class);
        jwksStore = mock(JwksStore.class);
        given(jwksStore.getUid()).willReturn("test");
        testResolver = new JWKOpenIdResolverImpl(jwksStore);
    }

    @Test
    public void testValidSignature() throws NoSuchAlgorithmException, FailedToLoadJWKException,
            InvalidSignatureException {
        //given
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RsaJWK rsaJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG,
                null, "rsaJwk", null, null, null);

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("test", "test");
        SigningHandler rsaSigningHandler =
                 new SigningManager().newRsaSigningHandler(keyPair.getPrivate());
        String jwt = new JwtBuilderFactory()
                .jws(rsaSigningHandler)
                .headers()
                .alg(JwsAlgorithm.RS256)
                .kid(rsaJwk.getKeyId())
                .done()
                .claims(new JwtClaimsSet(claims)).build();
        SignedJwt signedJwt = new JwtReconstruction().reconstructJwt(jwt, SignedJwt.class);

        given(jwksStore.findJwk(signedJwt.getHeader().getKeyId())).willReturn(rsaJwk);

        //when
        testResolver.verifySignature(signedJwt);

        //Then expect no InvalidSignatureException exception
    }

    @Test(expectedExceptions = InvalidSignatureException.class)
    public void testInvalidSignatureThrowsException()
            throws InvalidSignatureException, FailedToLoadJWKException {
        //given
        SignedJwt mockJwt = mock(SignedJwt.class);
        JwsHeader mockHeader = mock(JwsHeader.class);

        given(mockJwt.getHeader()).willReturn(mockHeader);
        given(mockHeader.getKeyId()).willReturn("keyId");

        given(mockJwt.verify(signingHandler)).willReturn(false);
        given(jwksStore.findJwk(mockJwt.getHeader().getKeyId())).willReturn(null);

        //when
        testResolver.verifySignature(mockJwt);

        //then checked by exception

    }

    @Test(expectedExceptions = JwtExpiredException.class)
    public void testExpiredTokenThrowsException() throws JwtExpiredException {
        //given
        Date pastDate = new Date();
        pastDate.setTime(1);

        //when
        testResolver.verifyExpiration(pastDate);

        //then checked by exception
    }

    @Test(expectedExceptions = InvalidIssException.class)
    public void testInvalidIssuerThrowsException() throws InvalidIssException {
        //given
        String wrongIssuer = "One";

        //when
        testResolver.verifyIssuer(wrongIssuer);

        //then checked by exception
    }
}
