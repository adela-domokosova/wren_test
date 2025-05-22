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

import static org.forgerock.caf.authentication.framework.AuthenticationFramework.LOG;

import java.security.PublicKey;


import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.jaspi.modules.openid.exceptions.InvalidSignatureException;
import org.forgerock.jaspi.modules.openid.exceptions.OpenIdConnectVerificationException;
import org.forgerock.util.SimpleHTTPClient;
import org.forgerock.json.jose.jwk.store.JwksStore;
import org.forgerock.json.jose.jwk.EcJWK;
import org.forgerock.json.jose.jwk.RsaJWK;
import org.forgerock.json.jose.jws.SignedJwt;
import org.forgerock.json.jose.jws.SigningManager;
import org.forgerock.json.jose.jwk.JWK;

/**
 * This class exists to allow Open Id Providers to supply or promote a JWK exposure point for
 * their public keys. We convert the exposed keys they provide according to the algorithm
 * defined by their JWK and offer their keys in a map key'd on their keyId.
 *
 * The map of keys is loaded on construction, and reloaded each time an Open Id token is
 * passed in to this resolver whose keyId does not exist within the list that we currently have.
 *
 * This means that we will cache the keys for as long as they are valid, and as soon as we
 * receive a request to verify using a key which we don't have we discard our current keys and
 * re-fill our map.
 */
public class JWKOpenIdResolverImpl extends BaseOpenIdResolver {

    private final SigningManager signingManager;

    private final JwksStore jwksStore;

    /**
     * Constructor using provided timeout values to generate the
     * {@link SimpleHTTPClient} used for communicating over HTTP.
     *
     * @param jwksStore The jwks store

     * @throws FailedToLoadJWKException if there were issues resolving or parsing the JWK
     */
    public JWKOpenIdResolverImpl(JwksStore jwksStore) throws FailedToLoadJWKException {
        super(jwksStore.getUid());
        this.jwksStore = jwksStore;
        this.signingManager = new SigningManager();
    }

    @Override
    public void validateIdentity(final SignedJwt idClaim) throws OpenIdConnectVerificationException {
        super.validateIdentity(idClaim);
        try {
            verifySignature(idClaim);
        } catch (FailedToLoadJWKException e) {
            throw new OpenIdConnectVerificationException(e);
        }
    }

    /**
     * Verifies that the JWS was signed by the supplied key. Throws an exception otherwise.
     *
     * @param idClaim The JWS to verify
     * @throws InvalidSignatureException If the JWS supplied does not match the key for this resolver
     * @throws FailedToLoadJWKException If the JWK supplied cannot be loaded from its remote location
     */
    public void verifySignature(final SignedJwt idClaim) throws InvalidSignatureException,
            FailedToLoadJWKException {
        final JWK jwk = jwksStore.findJwk(idClaim.getHeader().getKeyId());
        if (jwk == null || !idClaim.verify(createSigningHandlerForKey(signingManager, getPublicKeyFromJWK(jwk)))) {
            LOG.debug("JWS unable to be verified");
            throw new InvalidSignatureException("JWS unable to be verified");
        }
    }

    private PublicKey getPublicKeyFromJWK(org.forgerock.json.jose.jwk.JWK jwk) {
        switch (jwk.getKeyType()) {
        case RSA:
            RsaJWK rsaJWK = (RsaJWK) jwk;
            return rsaJWK.toRSAPublicKey();
        case EC:
            EcJWK ecJWK = (EcJWK) jwk;
            return ecJWK.toECPublicKey();
        default:
            throw new IllegalArgumentException("Key type '" + jwk.getKeyType() + "' not supported");
        }
    }
}
