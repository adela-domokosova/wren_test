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

package org.forgerock.selfservice.stages.user;

import static org.forgerock.selfservice.stages.CommonStateFields.ACCOUNTSTATUS_FIELD;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.selfservice.core.ProcessContext;
import org.forgerock.selfservice.core.ProgressStage;
import org.forgerock.selfservice.core.StageResponse;
import org.forgerock.selfservice.core.util.RequirementsBuilder;
import org.forgerock.util.Reject;

/**
 * Stage is responsible for validating account status.
 *
 * @since 22.0.0
 */
public final class ValidateActiveAccountStage implements ProgressStage<ValidateActiveAccountConfig> {
    @Override
    public JsonValue gatherInitialRequirements(ProcessContext context, ValidateActiveAccountConfig config)
            throws ResourceException {
        Reject.ifFalse(context.containsState(ACCOUNTSTATUS_FIELD),
                "Validate active account stage expects identity account status");

        return RequirementsBuilder.newEmptyRequirements();
    }

    @Override
    public StageResponse advance(ProcessContext context, ValidateActiveAccountConfig config)
            throws ResourceException {
        JsonValue contextStatus = context.getState(ACCOUNTSTATUS_FIELD);
        if (contextStatus == null) {
            throw new InternalServerErrorException("Account status is null");
        }
        String status = contextStatus.asString();
        String active = config.getValidStatusValue();
        if (!status.toLowerCase().equals(active)) {
            throw new BadRequestException("You do not have permissions "
                    + "to reset password. Please contact administrator");
        }

        return StageResponse.newBuilder().build();
    }
}
