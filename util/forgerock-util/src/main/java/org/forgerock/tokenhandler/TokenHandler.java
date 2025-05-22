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

package org.forgerock.tokenhandler;

import org.forgerock.json.JsonValue;

/**
 * Responsible for the validation, generation and parsing of tokens used for keying a JsonValue
 * representative of some state.  Implementers must catch implementation-specific exceptions
 * and re-throw as {@link TokenHandlerException}.
 */
public interface TokenHandler {

    /**
     * Generates a new token using the state.
     *
     * @param state the state
     * @return token
     * @throws TokenHandlerException on failure to generate token
     */
    String generate(JsonValue state) throws TokenHandlerException;

    /**
     * Validates the passed token.
     *
     * @param token the token to be validated
     * @throws InvalidTokenException on invalid token
     * @throws ExpiredTokenException on expired token
     * @throws TokenHandlerException on other failure to validate token
     */
    void validate(String token) throws TokenHandlerException;

    /**
     * Validates and parses the token, extracting any encapsulated state.
     *
     * @param token the token to be validated and parsed
     * @return the state
     * @throws InvalidTokenException on invalid token
     * @throws ExpiredTokenException on expired token
     * @throws TokenHandlerException on other failure to validate or extract token
     */
    JsonValue validateAndExtractState(String token) throws TokenHandlerException;
}
