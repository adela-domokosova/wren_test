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
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2016-2017 ForgeRock AS.
 */
package org.forgerock.api.commons;

import org.forgerock.api.models.ApiDescription;
import org.forgerock.api.models.ApiError;
import org.forgerock.api.models.Errors.Builder;
import org.forgerock.util.i18n.LocalizableString;

/** Commons ForgeRock API description. */
public final class CommonsApi {
    /** The api description of "frapi:common" which only contains errors so far. */
    public static final ApiDescription COMMONS_API_DESCRIPTION = buildCommonsApi();

    /** Base-prefix for common API error definition JSON references. */
    private static final String BASE_ERRORS_REF = "frapi:common#/errors/";

    /** JSON Reference to {@link Errors#BAD_REQUEST} in an API Description. */
    public static final String BAD_REQUEST_REF = BASE_ERRORS_REF + "badRequest";

    /** JSON Reference to {@link Errors#UNAUTHORIZED} in an API Description. */
    public static final String UNAUTHORIZED_REF = BASE_ERRORS_REF + "unauthorized";

    /** JSON Reference to {@link Errors#PAYMENT_REQUIRED} in an API Description. */
    public static final String PAYMENT_REQUIRED_REF = BASE_ERRORS_REF + "paymentRequired";

    /** JSON Reference to {@link Errors#FORBIDDEN} in an API Description. */
    public static final String FORBIDDEN_REF = BASE_ERRORS_REF + "forbidden";

    /** JSON Reference to {@link Errors#NOT_FOUND} in an API Description. */
    public static final String NOT_FOUND_REF = BASE_ERRORS_REF + "notFound";

    /** JSON Reference to {@link Errors#METHOD_NOT_ALLOWED} in an API Description. */
    public static final String METHOD_NOT_ALLOWED_REF = BASE_ERRORS_REF + "methodNotAllowed";

    /** JSON Reference to {@link Errors#NOT_ACCEPTABLE} in an API Description. */
    public static final String NOT_ACCEPTABLE_REF = BASE_ERRORS_REF + "notAcceptable";

    /** JSON Reference to {@link Errors#PROXY_AUTH_REQUIRED} in an API Description. */
    public static final String PROXY_AUTH_REQUIRED_REF = BASE_ERRORS_REF + "proxyAuthRequired";

    /** JSON Reference to {@link Errors#REQUEST_TIMEOUT} in an API Description. */
    public static final String REQUEST_TIMEOUT_REF = BASE_ERRORS_REF + "requestTimeout";

    /** JSON Reference to {@link Errors#CONFLICT} in an API Description. */
    public static final String CONFLICT_REF = BASE_ERRORS_REF + "conflict";

    /** JSON Reference to {@link Errors#GONE} in an API Description. */
    public static final String GONE_REF = BASE_ERRORS_REF + "gone";

    /** JSON Reference to {@link Errors#LENGTH_REQUIRED} in an API Description. */
    public static final String LENGTH_REQUIRED_REF = BASE_ERRORS_REF + "lengthRequired";

    /** JSON Reference to {@link Errors#VERSION_MISMATCH} in an API Description. */
    public static final String VERSION_MISMATCH_REF = BASE_ERRORS_REF + "versionMismatch";

    /** JSON Reference to {@link Errors#PRECONDITION_FAILED} in an API Description. */
    public static final String PRECONDITION_FAILED_REF = BASE_ERRORS_REF + "preconditionFailed";

    /** JSON Reference to {@link Errors#REQUEST_ENTITY_TOO_LARGE} in an API Description. */
    public static final String REQUEST_ENTITY_TOO_LARGE_REF = BASE_ERRORS_REF + "requestEntityTooLarge";

    /** JSON Reference to {@link Errors#REQUEST_URI_TOO_LARGE} in an API Description. */
    public static final String REQUEST_URI_TOO_LARGE_REF = BASE_ERRORS_REF + "requestUriTooLarge";

    /** JSON Reference to {@link Errors#UNSUPPORTED_MEDIA_TYPE} in an API Description. */
    public static final String UNSUPPORTED_MEDIA_TYPE_REF = BASE_ERRORS_REF + "unsupportedMediaType";

    /** JSON Reference to {@link Errors#RANGE_NOT_SATISFIABLE} in an API Description. */
    public static final String RANGE_NOT_SATISFIABLE_REF = BASE_ERRORS_REF + "rangeNotSatisfiable";

    /** JSON Reference to {@link Errors#EXPECTATION_FAILED} in an API Description. */
    public static final String EXPECTATION_FAILED_REF = BASE_ERRORS_REF + "expectationFailed";

    /** JSON Reference to {@link Errors#VERSION_REQUIRED} in an API Description. */
    public static final String VERSION_REQUIRED_REF = BASE_ERRORS_REF + "versionRequired";

    /** JSON Reference to {@link Errors#PRECONDITION_REQUIRED} in an API Description. */
    public static final String PRECONDITION_REQUIRED_REF = BASE_ERRORS_REF + "preconditionRequired";

    /** JSON Reference to {@link Errors#INTERNAL_SERVER_ERROR} in an API Description. */
    public static final String INTERNAL_SERVER_ERROR_REF = BASE_ERRORS_REF + "internalServerError";

    /** JSON Reference to {@link Errors#NOT_SUPPORTED} in an API Description. */
    public static final String NOT_SUPPORTED_REF = BASE_ERRORS_REF + "notSupported";

    /** JSON Reference to {@link Errors#BAD_GATEWAY} in an API Description. */
    public static final String BAD_GATEWAY_REF = BASE_ERRORS_REF + "badGateway";

    /** JSON Reference to {@link Errors#UNAVAILABLE} in an API Description. */
    public static final String UNAVAILABLE_REF = BASE_ERRORS_REF + "unavailable";

    /** JSON Reference to {@link Errors#GATEWAY_TIMEOUT} in an API Description. */
    public static final String GATEWAY_TIMEOUT_REF = BASE_ERRORS_REF + "gatewayTimeout";

    /** JSON Reference to {@link Errors#HTTP_VERSION_NOT_SUPPORTED} in an API Description. */
    public static final String HTTP_VERSION_NOT_SUPPORTED_REF = BASE_ERRORS_REF + "httpVersionNotSupported";

    /** Common api errors. */
    public enum Errors {
        // NOTE: if you add to this enum, please also define a corresponding _REF above

        /** The "bad request" error. */
        BAD_REQUEST                (400, "badRequest"),
        /** The "unauthorized" error. */
        UNAUTHORIZED               (401, "unauthorized"),
        /** The "payment required" error. */
        PAYMENT_REQUIRED           (402, "paymentRequired"),
        /** The "forbidden" error. */
        FORBIDDEN                  (403, "forbidden"),
        /** The "not found" error. */
        NOT_FOUND                  (404, "notFound"),
        /** The "method not allowed" error. */
        METHOD_NOT_ALLOWED         (405, "methodNotAllowed"),
        /** The "not acceptable" error. */
        NOT_ACCEPTABLE             (406, "notAcceptable"),
        /** The "proxy auth required" error. */
        PROXY_AUTH_REQUIRED        (407, "proxyAuthRequired"),
        /** The "request timeout" error. */
        REQUEST_TIMEOUT            (408, "requestTimeout"),
        /** The "conflict" error. */
        CONFLICT                   (409, "conflict"),
        /** The "gone" error. */
        GONE                       (410, "gone"),
        /** The "length required" error. */
        LENGTH_REQUIRED            (411, "lengthRequired"),
        /** The "version mismatch" error. */
        VERSION_MISMATCH           (412, "versionMismatch"),
        /** The "precondition failed" error. */
        PRECONDITION_FAILED        (412, "preconditionFailed"),
        /** The "request entity too large" error. */
        REQUEST_ENTITY_TOO_LARGE   (413, "requestEntityTooLarge"),
        /** The "request uri too large" error. */
        REQUEST_URI_TOO_LARGE      (414, "requestUriTooLarge"),
        /** The "unsupported media type" error. */
        UNSUPPORTED_MEDIA_TYPE     (415, "unsupportedMediaType"),
        /** The "range not satisfiable" error. */
        RANGE_NOT_SATISFIABLE      (416, "rangeNotSatisfiable"),
        /** The "expectation failed" error. */
        EXPECTATION_FAILED         (417, "expectationFailed"),
        /** The "version required " error. */
        VERSION_REQUIRED           (428, "versionRequired"),
        /** The "precondition required" error. */
        PRECONDITION_REQUIRED      (428, "preconditionRequired"),
        /** The "internal server error" error. */
        INTERNAL_SERVER_ERROR      (500, "internalServerError"),
        /** The "not supported" error. */
        NOT_SUPPORTED              (501, "notSupported"),
        /** The "bad gateway" error. */
        BAD_GATEWAY                (502, "badGateway"),
        /** The "unavailable" error. */
        UNAVAILABLE                (503, "unavailable"),
        /** The "gateway timeout" error. */
        GATEWAY_TIMEOUT            (504, "gatewayTimeout"),
        /** The "http version not supported" error. */
        HTTP_VERSION_NOT_SUPPORTED (505, "httpVersionNotSupported");

        private String camelCaseName;
        private String reference;
        private int code;
        private String translationKey;

        private Errors(int errorCode, String camelCaseName) {
            this.code = errorCode;
            this.camelCaseName = camelCaseName;
            this.reference = BASE_ERRORS_REF + camelCaseName;
            this.translationKey = "error." + camelCaseName + ".description";
        }

        /**
         * The reference to use in an API description.
         *
         * @return the reference of this error
         */
        public String getReference() {
            return reference;
        }

        private ApiError toApiError() {
            return ApiError.apiError().code(code).description(i18n(translationKey)).build();
        }
    }

    private static ApiDescription buildCommonsApi() {
        final Builder commonErrors = org.forgerock.api.models.Errors.errors();
        for (Errors error : Errors.values()) {
            commonErrors.put(error.camelCaseName, error.toApiError());
        }

        return ApiDescription
            .apiDescription()
            .id("frapi:common")
            .description(i18n("commonApi.description"))
            .version("1.0.0")
            .errors(commonErrors.build())
            .build();
    }

    private static LocalizableString i18n(String translationKey) {
        String value = "i18n:org/forgerock/api/commons/frapiCommons#" + translationKey;
        return new LocalizableString(value, CommonsApi.class.getClassLoader());
    }

    private CommonsApi() {
        // private for utility classes
    }
}
