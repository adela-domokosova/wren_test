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

import java.util.Objects;

import org.forgerock.selfservice.core.config.StageConfig;

/**
 * Configuration for the validate active account stage.
 *
 * @since 22.0.0
 */
public final class ValidateActiveAccountConfig implements StageConfig {

    /**
     * Name of the stage configuration.
     */
    public static final String NAME = "validateActiveAccount";

    private String accountStatusField;
    private String validStatusValue;

    /**
     * Gets the field name for the account status.
     *
     * @return the account status
     */
    public String getAccountStatusField() {
        return accountStatusField;
    }

    /**
     * Sets the field name for the account status.
     *
     * @param accountStatusField
     *         the account status
     *
     * @return this config instance
     */
    public ValidateActiveAccountConfig setAccountStatusField(String accountStatusField) {
        this.accountStatusField = accountStatusField;
        return this;
    }

    /**
     * Gets the field name for the valid status value.
     *
     * @return the valid status value
     */
    public String getValidStatusValue() {
        return validStatusValue;
    }

    /**
     * Sets the field name for the valid status value.
     *
     * @param validStatusValue
     *         the valid status value
     *
     * @return this config instance
     */
    public ValidateActiveAccountConfig setValidStatusValue(String validStatusValue) {
        this.validStatusValue = validStatusValue;
        return this;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getProgressStageClassName() {
        return ValidateActiveAccountStage.class.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ValidateActiveAccountConfig)) {
            return false;
        }

        ValidateActiveAccountConfig that = (ValidateActiveAccountConfig) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getProgressStageClassName(), that.getProgressStageClassName())
                && Objects.equals(accountStatusField, that.accountStatusField)
                && Objects.equals(validStatusValue, that.validStatusValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getProgressStageClassName(), accountStatusField, validStatusValue);
    }

}
