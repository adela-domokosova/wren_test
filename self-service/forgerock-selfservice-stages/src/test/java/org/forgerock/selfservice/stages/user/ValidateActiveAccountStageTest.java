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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.selfservice.stages.CommonStateFields.ACCOUNTSTATUS_FIELD;
import static org.mockito.BDDMockito.given;

import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.ResourceResponse;
import org.forgerock.selfservice.core.ProcessContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ValidateActiveAccountStage}.
 *
 * @since 22.0.0
 */
public final class ValidateActiveAccountStageTest {
    private ValidateActiveAccountStage validateActiveAccountStage;
    @Mock
    private ProcessContext context;

    private ValidateActiveAccountConfig config;
    @Mock
    private Connection connection;
    @Mock
    private ResourceResponse queryResponse;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        config = newValidateActiveAccountConfig();
        validateActiveAccountStage = new ValidateActiveAccountStage();
    }

    private ValidateActiveAccountConfig newValidateActiveAccountConfig() {
        return new ValidateActiveAccountConfig()
                .setAccountStatusField("accountStatus")
                .setValidStatusValue("active");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Validate active account stage expects identity account status")
    public void testGatherInitialRequirementsNoUserState() throws Exception {
        // Given
        given(context.getState(ACCOUNTSTATUS_FIELD)).willReturn(null);

        // When
        validateActiveAccountStage.gatherInitialRequirements(context, config);
    }

    @Test
    public void testGatherInitialRequirements() throws Exception {
        // Given
        given(context.containsState(ACCOUNTSTATUS_FIELD)).willReturn(true);
        given(context.getState(ACCOUNTSTATUS_FIELD)).willReturn(newEmptyJsonValue());

        // When
        JsonValue jsonValue = validateActiveAccountStage.gatherInitialRequirements(context, config);

        // Then
        assertThat(jsonValue).isEmpty();
    }

    @Test(expectedExceptions = BadRequestException.class,
            expectedExceptionsMessageRegExp = "You do not have permissions to reset password. "
                    + "Please contact administrator")
    public void testAdvanceInactiveAccount() throws Exception {
        // Given
        //config = new ValidateActiveAccountConfig().setValidStatusValue("active");
        given(context.getInput()).willReturn(newJsonValueUser());
        given(context.getState(ACCOUNTSTATUS_FIELD)).willReturn(json("inactive"));
        // When
        validateActiveAccountStage.advance(context, config);
    }

    @Test(expectedExceptions = InternalServerErrorException.class,
            expectedExceptionsMessageRegExp = "Account status is null")
    public void testAdvanceNoStatus() throws Exception {
        // Given
        given(context.getInput()).willReturn(newJsonValueUserNoStatus());
        // When
        validateActiveAccountStage.advance(context, config);
    }

    @Test
    public void testAdvance() throws Exception {
        // Given
        config = new ValidateActiveAccountConfig().setValidStatusValue("active");
        given(context.getInput()).willReturn(newJsonValueUser());
        given(context.getState(ACCOUNTSTATUS_FIELD)).willReturn(json("active"));
        // When
        validateActiveAccountStage.advance(context, config);
    }

    private JsonValue newEmptyJsonValue() {
        return json(object());
    }

    private JsonValue newJsonValueUser() {
        return json(object(
                field("firstName", "first name"),
                field("_Id", "user1"),
                field("username", "Alice"),
                field("accountStatus", "active"),
                field("email", "email1")));
    }

    private JsonValue newJsonValueUserNoStatus() {
        return json(object(
                field("firstName", "first name"),
                field("_Id", "user1"),
                field("username", "Alice"),
                field("email", "email1")));
    }
}

