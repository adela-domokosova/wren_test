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

package org.forgerock.json.resource;

/**
 * A specific exception for when Create is not supported, but Upsert might be being attempted so distinguish from
 * other {@link BadRequestException}s.
 */
public class CreateNotSupportedException extends BadRequestException {

    /**
     * Constructor with default message.
     */
    public CreateNotSupportedException() {
        super("Create not supported");
    }

    /**
     * Constructor with custom message.
     * @param message The message.
     */
    public CreateNotSupportedException(String message) {
        super(message);
    }
}
