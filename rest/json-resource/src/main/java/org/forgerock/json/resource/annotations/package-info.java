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
 * Copyright 2015 ForgeRock AS.
 * Partial Copyright 2021 Wren Security
 */

/**
 * The annotations are made available to allow a POJO annotated with
 * {@link org.forgerock.api.annotations.RequestHandler} to be used as
 * a CREST resource, with the necessary methods then being annotated to
 * indicate which operations are available, and the method that handles them.
 */
package org.forgerock.json.resource.annotations;
