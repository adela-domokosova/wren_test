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
 * Copyright 2013-2016 ForgeRock AS.
 * Portions Copyright 2018 Wren Security.
 */

package org.forgerock.caf.authentication.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.forgerock.caf.authentication.framework.AuthenticationFilter.AuthenticationModuleBuilder.configureModule;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;

import org.forgerock.caf.authentication.api.AsyncServerAuthModule;
import org.forgerock.caf.authentication.api.AuthenticationException;
import org.forgerock.caf.authentication.framework.AuthenticationFilter.AuthenticationFilterBuilder;
import org.forgerock.http.protocol.Request;
import org.forgerock.http.protocol.Response;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class AuthenticationFilterTest {

    @Test(expectedExceptions = IllegalStateException.class)
    public void attemptingToBuildFilterWithNoAuditApiShouldThrowIllegalStateException() throws AuthenticationException {

        //Given
        AuthenticationFilterBuilder builder = spy(AuthenticationFilter.builder());

        //When
        builder.build();

        //Then
        failBecauseExceptionWasNotThrown(IllegalStateException.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldBuildFilterWithAuditApiAndDefaultLogger() throws AuthenticationException {

        //Given
        AuditApi auditApi = mock(AuditApi.class);
        AsyncServerAuthModule sessionAuthModule = null;

        AuthenticationFilterBuilder builder = spy(AuthenticationFilter.builder());

        //When
        builder.auditApi(auditApi)
                .build();

        //Then
        ArgumentCaptor<Logger> loggerCaptor = ArgumentCaptor.forClass(Logger.class);
        ArgumentCaptor<ResponseHandler> responseHandlerCaptor =
                ArgumentCaptor.forClass(ResponseHandler.class);
        ArgumentCaptor<Subject> serviceSubjectCaptor = ArgumentCaptor.forClass(Subject.class);
        ArgumentCaptor<List> authModulesCaptor = ArgumentCaptor.forClass(List.class);

        verify(builder).createFilter(loggerCaptor.capture(), eq(auditApi), responseHandlerCaptor.capture(),
                serviceSubjectCaptor.capture(), eq(sessionAuthModule), authModulesCaptor.capture());

        assertThat(loggerCaptor.getValue()).isNotNull();
        assertThat(responseHandlerCaptor.getValue()).isNotNull();
        assertThat(serviceSubjectCaptor.getValue()).isNotNull();
        assertThat(authModulesCaptor.getValue()).isEmpty();
    }

    @Test
    public void shouldBuildFilterWithNamedLogger() throws AuthenticationException {

        //Given
        AuditApi auditApi = mock(AuditApi.class);
        AsyncServerAuthModule sessionAuthModule = null;

        AuthenticationFilterBuilder builder = spy(AuthenticationFilter.builder());

        //When
        builder.auditApi(auditApi)
                .named("NAMED_LOGGER")
                .build();

        //Then
        ArgumentCaptor<Logger> loggerCaptor = ArgumentCaptor.forClass(Logger.class);

        verify(builder).createFilter(loggerCaptor.capture(), eq(auditApi), any(ResponseHandler.class),
                any(Subject.class), eq(sessionAuthModule), anyList());

        assertThat(loggerCaptor.getValue()).isNotNull();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldBuildFullyConfiguredFilter() throws AuthenticationException {

        //Given
        Logger logger = mock(Logger.class);
        AuditApi auditApi = mock(AuditApi.class);
        Subject serviceSubject = new Subject();
        ResponseWriter responseWriter = mock(ResponseWriter.class);

        AsyncServerAuthModule sessionAuthModule = mockAuthModule();
        MessagePolicy sessionAuthModuleRequestPolicy = mock(MessagePolicy.class);
        MessagePolicy sessionAuthModuleResponsePolicy = mock(MessagePolicy.class);
        CallbackHandler sessionAuthModuleHandler = mock(CallbackHandler.class);
        Map<String, Object> sessionAuthModuleSettings = new HashMap<>();

        AsyncServerAuthModule authModuleOne = mockAuthModule();
        MessagePolicy authModuleOneRequestPolicy = mock(MessagePolicy.class);
        MessagePolicy authModuleOneResponsePolicy = mock(MessagePolicy.class);
        CallbackHandler authModuleOneHandler = mock(CallbackHandler.class);
        Map<String, Object> authModuleOneSettings = new HashMap<>();

        AsyncServerAuthModule authModuleTwo = mockAuthModule();
        MessagePolicy authModuleTwoRequestPolicy = mock(MessagePolicy.class);
        MessagePolicy authModuleTwoResponsePolicy = mock(MessagePolicy.class);
        CallbackHandler authModuleTwoHandler = mock(CallbackHandler.class);
        Map<String, Object> authModuleTwoSettings = new HashMap<>();

        AuthenticationFilterBuilder builder = spy(AuthenticationFilter.builder());

        //When
        builder.logger(logger)
                .auditApi(auditApi)
                .serviceSubject(serviceSubject)
                .responseHandler(responseWriter)
                .sessionModule(
                        configureModule(sessionAuthModule)
                                .requestPolicy(sessionAuthModuleRequestPolicy)
                                .responsePolicy(sessionAuthModuleResponsePolicy)
                                .callbackHandler(sessionAuthModuleHandler)
                                .withSettings(sessionAuthModuleSettings))
                .authModules(
                        configureModule(authModuleOne)
                                .requestPolicy(authModuleOneRequestPolicy)
                                .responsePolicy(authModuleOneResponsePolicy)
                                .callbackHandler(authModuleOneHandler)
                                .withSettings(authModuleOneSettings),
                        configureModule(authModuleTwo)
                                .requestPolicy(authModuleTwoRequestPolicy)
                                .responsePolicy(authModuleTwoResponsePolicy)
                                .callbackHandler(authModuleTwoHandler)
                                .withSettings(authModuleTwoSettings))
                .build();

        //Then
        ArgumentCaptor<Logger> loggerCaptor = ArgumentCaptor.forClass(Logger.class);
        ArgumentCaptor<ResponseHandler> responseHandlerCaptor =
                ArgumentCaptor.forClass(ResponseHandler.class);
        ArgumentCaptor<List> authModulesCaptor = ArgumentCaptor.forClass(List.class);

        verify(builder).createFilter(loggerCaptor.capture(), eq(auditApi), responseHandlerCaptor.capture(),
                eq(serviceSubject), eq(sessionAuthModule), authModulesCaptor.capture());

        assertThat(loggerCaptor.getValue()).isNotNull();
        assertThat(responseHandlerCaptor.getValue()).isNotNull();
        assertThat(authModulesCaptor.getValue()).hasSize(2).contains(authModuleOne, authModuleTwo);

        verify(sessionAuthModule).initialize(sessionAuthModuleRequestPolicy, sessionAuthModuleResponsePolicy,
                sessionAuthModuleHandler, sessionAuthModuleSettings);
        verify(authModuleOne).initialize(authModuleOneRequestPolicy, authModuleOneResponsePolicy, authModuleOneHandler,
                authModuleOneSettings);
        verify(authModuleTwo).initialize(authModuleTwoRequestPolicy, authModuleTwoResponsePolicy, authModuleTwoHandler,
                authModuleTwoSettings);
    }

    @Test
    public void shouldBuildFilterWithAdaptedServerAuthContext() throws Exception {

        //Given
        AuditApi auditApi = mock(AuditApi.class);
        ServerAuthModule authModule = mock(ServerAuthModule.class);
        MessagePolicy authModuleRequestPolicy = mock(MessagePolicy.class);
        MessagePolicy authModuleResponsePolicy = mock(MessagePolicy.class);
        CallbackHandler authModuleHandler = mock(CallbackHandler.class);
        Map<String, Object> authModuleSettings = new HashMap<>();

        given(authModule.getSupportedMessageTypes()).willReturn(new Class[]{Request.class, Response.class});

        AuthenticationFilterBuilder builder = spy(AuthenticationFilter.builder());

        //When
        builder.auditApi(auditApi)
                .authModules(
                        configureModule(authModule)
                                .requestPolicy(authModuleRequestPolicy)
                                .responsePolicy(authModuleResponsePolicy)
                                .callbackHandler(authModuleHandler)
                                .withSettings(authModuleSettings))
                .build();

        //Then
        verify(builder).createFilter(any(Logger.class), eq(auditApi), any(ResponseHandler.class), any(Subject.class),
                any(), anyList());

        verify(authModule).initialize(authModuleRequestPolicy, authModuleResponsePolicy,
                authModuleHandler, authModuleSettings);
    }

    private AsyncServerAuthModule mockAuthModule() throws AuthenticationException {
        AsyncServerAuthModule authModule = mock(AsyncServerAuthModule.class);

        Collection<Class<?>> supportedMessageTypes = new HashSet<>();
        supportedMessageTypes.add(Request.class);
        supportedMessageTypes.add(Response.class);
        given(authModule.getSupportedMessageTypes()).willReturn(supportedMessageTypes);

        authModule.initialize(any(MessagePolicy.class), any(MessagePolicy.class), any(CallbackHandler.class),
                anyMap());

        return authModule;
    }
}
