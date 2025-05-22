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
 * Copyright 2016 ForgeRock AS.
 */

package org.forgerock.audit.benchmark;

import static org.forgerock.audit.benchmark.CsvAuditEventHandlerWriteBenchmarkTest.MAX_FILE_SIZE;
import static org.forgerock.audit.events.AuditEventBuilder.TIMESTAMP;
import static org.forgerock.audit.events.AuditEventBuilder.TRANSACTION_ID;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.resource.ResourceResponse.FIELD_CONTENT_ID;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.forgerock.audit.events.handlers.AuditEventHandler;
import org.forgerock.audit.handlers.json.JsonAuditEventHandler;
import org.forgerock.audit.handlers.json.JsonAuditEventHandlerConfiguration;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ResourceResponse;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Write-throughput benchmarks for {@link JsonAuditEventHandler}.
 */
public class JsonAuditEventHandlerWriteBenchmarkTest extends BenchmarkBase {

    static final String FIELD_HAS_A_PERIOD = "has.a.period";
    private static final String ACCESS = "access";
    private static final String ACTIVITY = "activity";
    private static final Set<String> TOPICS_SET = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(new String[]{ACCESS, ACTIVITY})));

    static class DefaultState extends AuditEventHandlerBenchmarkState<JsonAuditEventHandlerConfiguration> {
        final AtomicInteger counter = new AtomicInteger();

        @Override
        public JsonAuditEventHandlerConfiguration buildBaseConfiguration() {
            final JsonAuditEventHandlerConfiguration configuration = new JsonAuditEventHandlerConfiguration();
            configuration.setName("json");
            configuration.setEnabled(true);
            configuration.setLogDirectory(getLogDirectory());
            configuration.setTopics(JsonAuditEventHandlerWriteBenchmarkTest.TOPICS_SET);
            configuration.getBuffering().setMaxSize(2_000_000);
            configuration.getBuffering().setWriteInterval("1 millis");
            return configuration;
        }

        @Override
        public AuditEventHandler buildAuditEventHandler(final JsonAuditEventHandlerConfiguration configuration)
            throws Exception {
            return new JsonAuditEventHandler(configuration, getEventTopicsMetaData("/events.json"));
        }

        /**
         * Builds a simple, unique event instance.
         *
         * @return Event instance
         */
        protected JsonValue buildUniqueEvent() {
            final String simpleId = Long.toString(counter.getAndIncrement());
            return json(object(field(FIELD_CONTENT_ID, simpleId), field(TIMESTAMP, simpleId),
                    field(TRANSACTION_ID, simpleId)));
        }
    }

    @State(Scope.Benchmark)
    public static class WriteState extends DefaultState {
        // empty
    }

    @Benchmark
    public final ResourceResponse write(final WriteState state) throws Exception {
        return state.handler.publishEvent(null, ACCESS, state.buildUniqueEvent()).getOrThrow();
    }

    @State(Scope.Benchmark)
    public static class RotatedWriteState extends WriteState {
        @Override
        protected void updateConfiguration(final JsonAuditEventHandlerConfiguration configuration) {
            configuration.getFileRotation().setRotationEnabled(true);
            configuration.getFileRotation().setMaxFileSize(MAX_FILE_SIZE);
        }
    }

    @Benchmark
    public ResourceResponse rotatedWrite(final RotatedWriteState state) throws Exception {
        return write(state);
    }

    @State(Scope.Benchmark)
    public static class ElasticsearchCompatibleBestCaseWriteState extends WriteState {
        @Override
        protected void updateConfiguration(final JsonAuditEventHandlerConfiguration configuration) {
            configuration.setElasticsearchCompatible(true);
        }
    }

    @Benchmark
    public ResourceResponse elasticsearchCompatibleBestCaseWrite(final ElasticsearchCompatibleBestCaseWriteState state)
            throws Exception {
        return write(state);
    }

    @State(Scope.Benchmark)
    public static class ElasticsearchCompatibleWorstCaseWriteState extends WriteState {
        @Override
        protected void updateConfiguration(final JsonAuditEventHandlerConfiguration configuration) {
            configuration.setElasticsearchCompatible(true);
        }

        /**
         * Builds a simple, unique event instance, that contains periods in one of the field-names, which will be
         * normalized for ElasticSearch compatibility.
         * <p>
         * This is a worst-case scenario, because every single event will need to be normalized-on-write.
         *
         * @return Event instance
         */
        @Override
        protected JsonValue buildUniqueEvent() {
            final String simpleId = Long.toString(counter.getAndIncrement());
            return json(object(field(FIELD_CONTENT_ID, simpleId), field(TIMESTAMP, simpleId),
                    field(FIELD_HAS_A_PERIOD, simpleId)));
        }
    }

    @Benchmark
    public ResourceResponse elasticsearchCompatibleWorstCaseWrite(
            final ElasticsearchCompatibleWorstCaseWriteState state) throws Exception {
        return write(state);
    }
}
