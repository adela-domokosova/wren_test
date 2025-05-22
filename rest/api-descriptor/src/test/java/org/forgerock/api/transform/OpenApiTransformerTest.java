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
 * Portions Copyright 2018 Wren Security.
 */
package org.forgerock.api.transform;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.atIndex;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.forgerock.api.models.Paths.paths;
import static org.forgerock.api.models.Query.query;
import static org.forgerock.api.models.Reference.reference;
import static org.forgerock.api.models.Resource.resource;
import static org.forgerock.api.models.Schema.schema;
import static org.forgerock.api.models.VersionedPath.versionedPath;
import static org.forgerock.json.JsonValue.array;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.assertj.core.api.iterable.Extractor;
import org.forgerock.api.ApiTestUtil;
import org.forgerock.api.enums.CountPolicy;
import org.forgerock.api.enums.PatchOperation;
import org.forgerock.api.enums.QueryType;
import org.forgerock.api.enums.ReadPolicy;
import org.forgerock.api.enums.WritePolicy;
import org.forgerock.api.jackson.JacksonUtils;
import org.forgerock.api.models.ApiDescription;
import org.forgerock.api.models.ApiError;
import org.forgerock.api.models.Definitions;
import org.forgerock.api.models.Reference;
import org.forgerock.api.models.Resource;
import org.forgerock.api.models.Schema;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.util.i18n.LocalizableString;
import org.forgerock.util.i18n.PreferredLocales;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.refs.RefType;

@SuppressWarnings("javadoc")
public class OpenApiTransformerTest {

    public static final PreferredLocales PREFERRED_LOCALES = new PreferredLocales();

    @Test
    public void testUserAndDevicesExample() throws Exception {
        final ApiDescription apiDescription = ApiTestUtil.createUserAndDeviceExampleApiDescription();
        final Swagger swagger = OpenApiTransformer.execute(apiDescription);

        assertThat(swagger.getTags()).hasSize(2);
        assertTag(swagger, 0, "User Service v1.0");
        assertTag(swagger, 1, "User-Device Service v1.0");
        assertThat(swagger.getPaths()).containsOnlyKeys(
                "/admins#1.0_create_post",
                "/admins#1.0_query_filter",
                "/admins/{userId}#1.0_create_put",
                "/admins/{userId}#1.0_read",
                "/admins/{userId}#1.0_update",
                "/admins/{userId}#1.0_delete",
                "/admins/{userId}#1.0_patch",
                "/admins/{userId}#1.0_action_resetpassword",
                "/admins/{userId}/devices#1.0_create_post",
                "/admins/{userId}/devices#1.0_query_filter",
                "/admins/{userId}/devices/{deviceId}#1.0_create_put",
                "/admins/{userId}/devices/{deviceId}#1.0_read",
                "/admins/{userId}/devices/{deviceId}#1.0_update",
                "/admins/{userId}/devices/{deviceId}#1.0_delete",
                "/admins/{userId}/devices/{deviceId}#1.0_patch",
                "/admins/{userId}/devices/{deviceId}#1.0_action_markasstolen",
                "/users#1.0_create_post",
                "/users#1.0_query_filter",
                "/users/{userId}#1.0_create_put",
                "/users/{userId}#1.0_read",
                "/users/{userId}#1.0_update",
                "/users/{userId}#1.0_delete",
                "/users/{userId}#1.0_patch",
                "/users/{userId}#1.0_action_resetpassword",
                "/users/{userId}/devices#1.0_create_post",
                "/users/{userId}/devices#1.0_query_filter",
                "/users/{userId}/devices/{deviceId}#1.0_create_put",
                "/users/{userId}/devices/{deviceId}#1.0_read",
                "/users/{userId}/devices/{deviceId}#1.0_update",
                "/users/{userId}/devices/{deviceId}#1.0_delete",
                "/users/{userId}/devices/{deviceId}#1.0_patch",
                "/users/{userId}/devices/{deviceId}#1.0_action_markasstolen");
    }

    @Test
    public void testTransformWithUnversionedPaths() throws Exception {
        final ApiDescription apiDescription = ApiTestUtil.createApiDescription(false);
        final Swagger swagger = OpenApiTransformer.execute(apiDescription);

        assertThat(swagger.getTags()).hasSize(1);
        assertTag(swagger, 0, "Resource title");
        assertThat(swagger.getPaths()).containsOnlyKeys(
                "/testPath",
                "/testPath#_action_action1",
                "/testPath#_query_expression",
                "/testPath#_query_filter",
                "/testPath#_query_id_id1",
                "/testPath#_query_id_id2");
    }

    @Test
    public void testTransformWithVersionedPaths() throws Exception {
        final ApiDescription apiDescription = ApiTestUtil.createApiDescription(true);
        final Swagger swagger = OpenApiTransformer.execute(apiDescription);

        // decorate Swagger object with application-specific features like auth headers, after this class completes
        final HeaderParameter usernameHeader = new HeaderParameter();
        usernameHeader.setName("X-OpenAM-Username");
        usernameHeader.setDefault("openam-admin");
        usernameHeader.setType("string");
        usernameHeader.required(true);
        OpenApiHelper.addHeaderToAllOperations(usernameHeader, swagger);

        final HeaderParameter passwordHeader = new HeaderParameter();
        passwordHeader.setName("X-OpenAM-Password");
        passwordHeader.setDefault("openam-admin");
        passwordHeader.setType("string");
        passwordHeader.required(true);
        OpenApiHelper.addHeaderToAllOperations(passwordHeader, swagger);

        OpenApiHelper.visitAllOperations(
                new OpenApiHelper.OperationVisitor() {
                    @Override
                    public void visit(final Operation operation) {
                        // add header "Accept-API-Version: resource=XXX, protocol=1.0"
                        final String version = (String) operation.getVendorExtensions().get("x-resourceVersion");
                        assertThat(version).isIn("1.0", "2.0");
                        assertThat(operation.getParameters()).areAtLeastOne(new Condition<Parameter>() {
                            @Override
                            public boolean matches(Parameter parameter) {
                                if (!(parameter instanceof HeaderParameter)) {
                                    return false;
                                }
                                assertThat(((HeaderParameter) parameter).getEnum()).containsOnly("resource=" + version);
                                return true;
                            }
                        });
                    }
                }, swagger);

        assertThat(swagger.getTags()).hasSize(2);
        assertTag(swagger, 0, "Resource title v1.0");
        assertTag(swagger, 1, "Resource title v2.0");

        assertThat(swagger.getPaths()).containsOnlyKeys(
                "/testPath#1.0_create_post",
                "/testPath#1.0_read",
                "/testPath#1.0_update",
                "/testPath#1.0_delete",
                "/testPath#1.0_patch",
                "/testPath#1.0_action_action1",
                "/testPath#1.0_query_expression",
                "/testPath#1.0_query_filter",
                "/testPath#1.0_query_id_id1",
                "/testPath#1.0_query_id_id2",
                "/testPath#2.0_create_put",
                "/testPath#2.0_read",
                "/testPath#2.0_update",
                "/testPath#2.0_delete",
                "/testPath#2.0_patch",
                "/testPath#2.0_action_action1",
                "/testPath#2.0_action_action2",
                "/testPath#2.0_query_expression",
                "/testPath#2.0_query_filter",
                "/testPath#2.0_query_id_id1",
                "/testPath#2.0_query_id_id2");
    }

    private void assertTag(Swagger swagger, int tagNumber, String expected) {
        assertThat(swagger.getTags().get(tagNumber)).isInstanceOf(LocalizableTag.class);
        LocalizableTag tag = (LocalizableTag) swagger.getTags().get(tagNumber);
        assertThat(tag.getLocalizableName().toTranslatedString(PREFERRED_LOCALES)).isEqualTo(expected);
    }

    @Test
    public void testBuildPatchRequestPayload() {
        final OpenApiTransformer transformer = new OpenApiTransformer();
        final Schema schema = transformer.buildPatchRequestPayload(
                new PatchOperation[]{PatchOperation.ADD, PatchOperation.REMOVE});

        final List<Object> enumList = schema.getSchema().get(
                new JsonPointer("/items/properties/operation/enum")).asList();
        assertThat(enumList).containsOnly("add", "remove");
        assertThat(schema.getSchema().get("id").asString())
                .isEqualTo("frapi:models:Patch:add_remove");
    }

    @Test
    public void testBuildInfo() {
        final ApiDescription apiDescription = ApiDescription.apiDescription()
                .id("frapi:test")
                .version("2.0")
                .description(new LocalizableString("My Description"))
                .build();
        final OpenApiTransformer transformer = new OpenApiTransformer(new LocalizableString("Test"), "localhost:8080",
                "/", false, apiDescription);

        final Info info = transformer.buildInfo(new LocalizableString("My Title"));

        assertThat(info).isEqualTo(new LocalizableInfo()
                .title(new LocalizableString("My Title"))
                .description(new LocalizableString("My Description"))
                .version("2.0"));
    }

    @Test
    public void testBuildDefinitions() {
        final Definitions definitions = Definitions.definitions()
                .put("myDef", schema().schema(json(object(field("type", "object")))).build())
                .build();
        final ApiDescription apiDescription = ApiDescription.apiDescription()
                .id("frapi:test")
                .version("2.0")
                .description(new LocalizableString("My Description"))
                .definitions(definitions)
                .build();
        final OpenApiTransformer transformer = new OpenApiTransformer(new LocalizableString("Test"), "localhost:8080",
                "/", false, apiDescription);

        transformer.buildDefinitions();

        assertThat(transformer.swagger.getDefinitions()).containsEntry("myDef",
                new LocalizableModelImpl().type("object"));
    }

    @DataProvider(name = "buildModelData")
    public Object[][] buildModelData() {
        return new Object[][]{
                {null, null, NullPointerException.class},
                {json(null), null, TransformerException.class},
                {json(object(field("type", "not_a_json_schema_type"))), null, TransformerException.class},
                {json(object(field("type", "object"))), new LocalizableModelImpl().type("object"), null},
                {json(object(
                        field("type", "object"),
                        field("properties", object(field("name", object(field("type", "string"))))),
                        field("required", array("name")),
                        field("title", "My Title"),
                        field("description", "My Description"))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final Map<String, Property> properties = new HashMap<>();
                                properties.put("name", new LocalizableStringProperty());

                                final ModelImpl o = new LocalizableModelImpl();
                                o.type("object");
                                o.setProperties(properties);
                                o.addRequired("name");
                                o.setTitle("My Title");
                                o.setDescription("My Description");
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "array"))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final ArrayModel o = new LocalizableArrayModel();
                                final LocalizableObjectProperty itemsProperty = new LocalizableObjectProperty();
                                itemsProperty.setType("any");
                                o.setItems(itemsProperty);
                                return o;
                            }
                        }.get(), null},
                {json(object(
                        field("type", "array"),
                        field("items", object(field("type", "string"))),
                        field("title", "My Title"),
                        field("description", "My Description"))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final ArrayModel o = new LocalizableArrayModel();
                                o.setItems(new LocalizableStringProperty());
                                o.setTitle("My Title");
                                o.setDescription("My Description");
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "boolean"))), new LocalizableModelImpl().type("boolean"), null},
                {json(object(field("type", "integer"))), new LocalizableModelImpl().type("integer"), null},
                {json(object(field("type", "number"))), new LocalizableModelImpl().type("number"), null},
                {json(object(field("type", "null"))), new ModelImpl().type("null"), null},
                {json(object(field("type", "any"))), new ModelImpl().type("any"), null},
                // array of non-"null" types defaults to "any"
                {json(object(field("type", array("string", "object")))), new ModelImpl().type("any"), null},
                // array of two types has "null" type removed, and single remaining type will be used
                {json(object(field("type", array("string", "null")))), new LocalizableModelImpl().type("string"), null},
                {json(object(field("type", "string"))), new LocalizableModelImpl().type("string"), null},
                {json(object(
                        field("type", "string"),
                        field("default", "my_default"))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final ModelImpl o = new LocalizableModelImpl();
                                o.type("string");
                                o.setDefaultValue("my_default");
                                return o;
                            }
                        }.get(), null},
                {json(object(
                        field("type", "string"),
                        field("format", "full-date"))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final ModelImpl o = new LocalizableModelImpl();
                                o.type("string");
                                o.setFormat("date");
                                return o;
                            }
                        }.get(), null},
                {json(object(
                        field("type", "string"),
                        field("enum", array("enum_1", "enum_2")),
                        field("options", object(field("enum_titles", array("enum_1_title", "enum_2_title")))))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final ModelImpl o = new LocalizableModelImpl();
                                o.type("string");
                                o.setEnum(Arrays.asList("enum_1", "enum_2"));
                                o.setVendorExtension("x-enum_titles", Arrays.asList("enum_1_title", "enum_2_title"));
                                return o;
                            }
                        }.get(), null},
                {json(object(
                        field("type", "object"),
                        field("additionalProperties", object(field("type", "string"))))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final ModelImpl o = new LocalizableModelImpl();
                                o.setAdditionalProperties(new LocalizableStringProperty());
                                return o;
                            }
                        }.get(), null},
                {json(object(
                        field("title", "This is a cool title"),
                        field("description", "This is a cool description"),
                        field("allOf", array(
                            object(field("$ref", "#/definitions/someDefinition")),
                            object(field("type", "object")))))),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                final RefModel m1 = new LocalizableRefModel();
                                m1.setReference("#/definitions/someDefinition");
                                final ModelImpl m2 = new LocalizableModelImpl();
                                m2.type("object");

                                final ComposedModel o = new LocalizableComposedModel();
                                o.setTitle("This is a cool title");
                                o.setDescription("This is a cool description");
                                o.setAllOf(asList(m1, m2));
                                return o;
                            }
                        }.get(), null},
                {json(object(
                        field("title", "This is a cool title"),
                        field("description", "This is a cool description"),
                        field("allOf", null))),
                 null, TransformerException.class},
                {json(object(
                        field("title", "This is a cool title"),
                        field("description", "This is a cool description"),
                        field("allOf", array()))),
                 null, TransformerException.class},
                {jsonValueForSchema(PojoOuter.class),
                        new Supplier<Model>() {
                            @Override
                            public Model get() {
                                // this one has a 'title' because it is the first encounter of PojoInner class
                                final LocalizableRefProperty pojoProp1 = new LocalizableRefProperty(
                                        "urn:jsonschema:org:forgerock:api:transform:PojoInner");
                                pojoProp1.description(PojoOuter.DESCRIPTION_1);
                                pojoProp1.title(PojoInner.TITLE);

                                // there is no 'title', because this second encounter was always a JSON Reference
                                final LocalizableRefProperty pojoProp2 = new LocalizableRefProperty(
                                        "urn:jsonschema:org:forgerock:api:transform:PojoInner");
                                pojoProp2.description(PojoOuter.DESCRIPTION_2);

                                final LocalizableModelImpl model = new LocalizableModelImpl();
                                model.type("object");
                                model.title(PojoOuter.TITLE);
                                model.addProperty("pojoProp1", pojoProp1);
                                model.addProperty("pojoProp2", pojoProp2);
                                return model;
                            }
                        }.get(), null
                },
        };
    }

    @Test(dataProvider = "buildModelData")
    public void testBuildModel(final JsonValue schema, final Model expectedReturnValue,
            final Class<? extends Throwable> expectedException) {
        final OpenApiTransformer transformer = new OpenApiTransformer();

        if (expectedException != null) {
            assertThatExceptionOfType(expectedException).isThrownBy(() -> {
                transformer.buildModel(schema);
            });
        } else {
            final Model actualReturnValue = transformer.buildModel(schema);

            // Compare the two models as JSON to avoid equality issues with "originalRef" values
            // being different between the two instances after Swagger 1.5.21.
            assertEqualAsJson(expectedReturnValue, actualReturnValue);
        }
    }

    @DataProvider(name = "buildPropertyData")
    public Object[][] buildPropertyData() {
        return new Object[][]{
                {null, null, null},
                {json(null), null, null},
                {json(object(field("type", "not_a_json_schema_type"))), null, TransformerException.class},
                {json(object(field("type", "null"))), null, null},
                {json(object(
                        field("type", "any"))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableObjectProperty o = new LocalizableObjectProperty();
                                o.setType("any");
                                return o;
                            }
                        }.get(), null},
                // array of non-"null" types defaults to "any"
                {json(object(
                        field("type", array("string", "object")))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableObjectProperty o = new LocalizableObjectProperty();
                                o.setType("any");
                                return o;
                            }
                        }.get(), null},
                // array of two types has "null" type removed, and single remaining type will be used
                {json(object(field("type", array("object", "null")))), new LocalizableObjectProperty(), null},
                {json(object(field("type", "object"))), new LocalizableObjectProperty(), null},
                {json(object(
                        field("type", "object"),
                        field("properties", object(field("name", object(field("type", "string"))))),
                        field("required", array("name")),
                        field("default", object(field("name", "myName"))))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final Map<String, Property> properties = new HashMap<>();
                                properties.put("name", new LocalizableStringProperty());

                                final LocalizableObjectProperty o = new LocalizableObjectProperty();
                                o.setProperties(properties);
                                o.setRequiredProperties(Arrays.asList("name"));
                                o.setDefault(object(field("name", "myName")));
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "array"))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableArrayProperty o = new LocalizableArrayProperty();
                                final LocalizableObjectProperty itemsProperty = new LocalizableObjectProperty();
                                itemsProperty.setType("any");
                                o.setItems(itemsProperty);
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "array"), field("items", null))), null, TransformerException.class},
                {json(object(
                        field("type", "array"),
                        field("items", object(field("type", "string"))),
                        field("minItems", 1),
                        field("maxItems", 10),
                        field("uniqueItems", true),
                        field("default", array("myValue")))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableArrayProperty o = new LocalizableArrayProperty();
                                o.setItems(new LocalizableStringProperty());
                                o.setMinItems(1);
                                o.setMaxItems(10);
                                o.setUniqueItems(true);
                                o.setDefault(array("myValue"));
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "boolean"))), new LocalizableBooleanProperty(), null},
                {json(object(
                        field("type", "boolean"),
                        field("default", true))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableBooleanProperty o = new LocalizableBooleanProperty();
                                o.setDefault(true);
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "integer"))), new LocalizableIntegerProperty(), null},
                {json(object(field("type", "integer"), field("format", "int32"))),
                    new LocalizableIntegerProperty(), null},
                {json(object(field("type", "integer"), field("format", "int64"))),
                    new LocalizableLongProperty(), null},
                {json(object(
                        field("type", "integer"),
                        field("format", "int64"),
                        field("minimum", 1.0),
                        field("maximum", 2.0),
                        field("exclusiveMinimum", true),
                        field("exclusiveMaximum", true),
                        field("readOnly", true),
                        field("default", 1))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableLongProperty o = new LocalizableLongProperty();
                                o.setMinimum(BigDecimal.valueOf(1.0d));
                                o.setMaximum(BigDecimal.valueOf(2.0d));
                                o.setExclusiveMinimum(true);
                                o.setExclusiveMaximum(true);
                                o.setReadOnly(true);
                                o.setDefault(Long.valueOf(1));
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "number"))), new LocalizableDoubleProperty(), null},
                {json(object(field("type", "number"), field("format", "int32"))),
                    new LocalizableIntegerProperty(), null},
                {json(object(field("type", "number"), field("format", "int64"))),
                    new LocalizableLongProperty(), null},
                {json(object(field("type", "number"), field("format", "float"))),
                    new LocalizableFloatProperty(), null},
                {json(object(field("type", "number"), field("format", "double"))),
                    new LocalizableDoubleProperty(), null},
                {json(object(
                        field("type", "number"),
                        field("format", "double"),
                        field("minimum", 1.0),
                        field("maximum", 2.0),
                        field("exclusiveMinimum", true),
                        field("exclusiveMaximum", true),
                        field("default", 1.1))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableDoubleProperty o = new LocalizableDoubleProperty();
                                o.setMinimum(BigDecimal.valueOf(1.0d));
                                o.setMaximum(BigDecimal.valueOf(2.0d));
                                o.setExclusiveMinimum(true);
                                o.setExclusiveMaximum(true);
                                o.setDefault(1.1);
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "string"))), new LocalizableStringProperty(), null},
                {json(object(field("type", "string"), field("format", "byte"))), new LocalizableByteArrayProperty(),
                    null},
                {json(object(
                        field("type", "string"),
                        field("format", "byte"),
                        field("default", "AA=="))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableByteArrayProperty o = new LocalizableByteArrayProperty();
                                o.setDefault("AA==");
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "string"), field("format", "binary"))), new LocalizableBinaryProperty(),
                    null},
                {json(object(
                        field("type", "string"),
                        field("format", "binary"),
                        field("default", "Rm9yZ2VSb2Nr"))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableBinaryProperty o = new LocalizableBinaryProperty();
                                o.setDefault("Rm9yZ2VSb2Nr");
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "string"), field("format", "date"))), new LocalizableDateProperty(),
                    null},
                {json(object(
                        field("type", "string"),
                        field("format", "full-date"),
                        field("default", "2010-11-17"))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableDateProperty o = new LocalizableDateProperty();
                                o.setFormat("full-date");
                                o.setDefault("2010-11-17");
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "string"), field("format", "date-time"))), new LocalizableDateTimeProperty(),
                    null},
                {json(object(
                        field("type", "string"),
                        field("format", "date-time"),
                        field("default", "2010-11-17T00:00:00Z"))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableDateTimeProperty o = new LocalizableDateTimeProperty();
                                o.setDefault("2010-11-17T00:00:00Z");
                                return o;
                            }
                        }.get(), null},
                {json(object(field("type", "string"), field("format", "password"))), new LocalizablePasswordProperty(),
                    null},
                {json(object(field("type", "string"), field("format", "uuid"))), new LocalizableUUIDProperty(),
                    null},
                {json(object(
                        field("type", "string"),
                        field("format", "an_unsupported_format"),
                        field("minLength", 1),
                        field("maxLength", 10),
                        field("pattern", "^[a-z]{1,10}$"),
                        field("default", "abc"),
                        field("title", "My Title"),
                        field("description", "My Description"),
                        field("readOnly", false),
                        field("readPolicy", ReadPolicy.USER.name()),
                        field("returnOnDemand", false),
                        field("writePolicy", WritePolicy.WRITABLE.name()),
                        field("errorOnWritePolicyFailure", false),
                        field("propertyOrder", 100))),
                        new Supplier<Property>() {
                            @Override
                            public Property get() {
                                final LocalizableStringProperty o = new LocalizableStringProperty();
                                o.setFormat("an_unsupported_format");
                                o.setMinLength(1);
                                o.setMaxLength(10);
                                o.setPattern("^[a-z]{1,10}$");
                                o.setDefault("abc");
                                o.setTitle("My Title");
                                o.setDescription("My Description");
                                o.setReadOnly(false);
                                o.setVendorExtension("x-readPolicy", ReadPolicy.USER.name());
                                o.setVendorExtension("x-returnOnDemand", false);
                                o.setVendorExtension("x-writePolicy", WritePolicy.WRITABLE.name());
                                o.setVendorExtension("x-errorOnWritePolicyFailure", false);
                                o.setVendorExtension("x-propertyOrder", 100);
                                return o;
                            }
                        }.get(), null},
        };
    }

    @Test(dataProvider = "buildPropertyData")
    public void testBuildProperty(final JsonValue schema, final Property expectedReturnValue,
            final Class<? extends Throwable> expectedException) {
        final OpenApiTransformer transformer = new OpenApiTransformer();
        final Property actualReturnValue;
        try {
            actualReturnValue = transformer.buildProperty(schema);
        } catch (final Exception e) {
            if (expectedException != null) {
                assertThat(e).isInstanceOf(expectedException);
            }
            return;
        }

        if (expectedException != null) {
            failBecauseExceptionWasNotThrown(expectedException);
        }

        assertThat(actualReturnValue).isEqualTo(expectedReturnValue);
    }

    @Test
    public void testBuildProperties() {
        // build schema with properties out-of-order (given propertyOrder field)
        final JsonValue schema = json(object(
                field("type", "object"),
                field("properties", object(
                        field("fieldOrderNone", object(
                                field("type", "string"))),
                        field("fieldOrder100", object(
                                field("type", "string"),
                                field("propertyOrder", 100))),
                        field("fieldOrder1", object(
                                field("type", "string"),
                                field("propertyOrder", 1))),
                        field("fieldOrderNoneToo", object(
                                field("type", "string")))
                ))));

        final OpenApiTransformer transformer = new OpenApiTransformer();
        final Map<String, Property> modelProperties = transformer.buildProperties(schema);

        // check that properties are now in correct order
        final Iterator<String> iterator = modelProperties.keySet().iterator();
        assertThat(iterator.next()).isEqualTo("fieldOrder1");
        assertThat(iterator.next()).isEqualTo("fieldOrder100");
        assertThat(iterator.next()).isEqualTo("fieldOrderNone");
        assertThat(iterator.next()).isEqualTo("fieldOrderNoneToo");
    }

    @Test
    public void testGetDefinitionsReference() {
        final OpenApiTransformer transformer = new OpenApiTransformer();
        final String reference = transformer.getDefinitionsReference(
                reference().value(RefType.DEFINITION.getInternalPrefix() + "myDef").build());
        assertThat(reference).isEqualTo("myDef");
        assertThat(transformer.getDefinitionsReference((Reference) null)).isNull();
    }

    @DataProvider
    public static Object[][] countPolicies() {
        // @Checkstyle:off
        return new Object[][] {
                { null, new String[] { "NONE" } },
                { new CountPolicy[] { }, new String[] { "NONE" } },
                { new CountPolicy[] { CountPolicy.NONE }, new String[] { "NONE" } },
                { new CountPolicy[] { CountPolicy.EXACT }, new String[] { "EXACT" } },
                { new CountPolicy[] { CountPolicy.EXACT, CountPolicy.ESTIMATE }, new String[] { "EXACT", "ESTIMATE" } }
        };
        // @Checkstyle:on
    }

    @Test(dataProvider = "countPolicies")
    public void testTotalPagedResultPolicyIsProperlyFilled(CountPolicy[] policies, String[] expected) throws Exception {
        Resource resource = resource()
                .resourceSchema(schema().schema(json(object(field("type", "object")))).build())
                .title("test")
                .mvccSupported(false)
                .query(query().queryId("test").countPolicies(policies).type(QueryType.ID).build())
                .build();
        final ApiDescription apiDescription = ApiDescription.apiDescription()
                .id("frapi:test")
                .version("1.0")
                .paths(paths().put("/test", versionedPath().put("1.0", resource).build()).build())
                .build();
        Swagger swagger = OpenApiTransformer.execute(new LocalizableString("Test"), "localhost:8080",
                "/", false, apiDescription);

        List<Parameter> parameters = swagger.getPath("/test#1.0_query_id_test").getGet().getParameters();
        assertThat(parameters)
                .filteredOn(totalPagedResultsPolicy())
                .hasSize(1)
                .hasOnlyElementsOfType(SerializableParameter.class)
                .extracting(enumValues())
                .has(countPolicies(expected), atIndex(0));
    }

    @Test
    public void testBuildErrorSchema() {
        final OpenApiTransformer transformer = new OpenApiTransformer();

        final ApiError apiError = ApiError.apiError()
                .code(404)
                .description("404 description")
                .build();
        final JsonValue apiErrorSchema = transformer.buildErrorSchema(apiError);

        final ApiError apiErrorWithCause = ApiError.apiError()
                .code(404)
                .description("404 description")
                .schema(Schema.schema()
                        .schema(json(object(field("type", "object"))))
                        .build())
                .build();
        final JsonValue apiErrorWithCauseSchema = transformer.buildErrorSchema(apiErrorWithCause);

        final String id = "frapi:models:ApiError";
        final JsonPointer causePointer = new JsonPointer("/properties/cause");

        assertThat(apiErrorSchema.get("id").asString()).isEqualTo(id);
        assertThat(apiErrorSchema.get(causePointer)).isNull();

        assertThat(apiErrorWithCauseSchema.get("id").asString()).startsWith(id).isNotEqualTo(id);
        assertThat(apiErrorWithCauseSchema.get(causePointer)).isNotNull();
    }

    private static void assertEqualAsJson(final Model expected, final Model actual) {
        final String    expectedJson    = objectToJsonString(expected),
                        actualJson      = objectToJsonString(actual);

        assertThat(actualJson).isEqualTo(expectedJson);
    }

    private static String objectToJsonString(final Model object) {
        String json = null;

        try {
            json = JacksonUtils.createGenericMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Could not serialize object to JSON", e);
        }

        return json;
    }

    private static JsonValue jsonValueForSchema(final Class<?> type) {
        try {
            final JsonSchema schema = JacksonUtils.schemaFor(type);
            final byte[] bytes = JacksonUtils.OBJECT_MAPPER.writeValueAsBytes(schema);
            return json(JacksonUtils.OBJECT_MAPPER.readValue(bytes, Object.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    private static Condition<List<String>> countPolicies(final String[] expected) {
        return new Condition<List<String>>() {
            @Override
            public boolean matches(final List<String> value) {
                return value.containsAll(Arrays.asList(expected)) && value.size() == expected.length;
            }
        };
    }

    private static Extractor<Parameter, List<String>> enumValues() {
        return new Extractor<Parameter, List<String>>() {
            @Override
            public List<String> extract(final Parameter input) {
                return ((SerializableParameter) input).getEnum();
            }
        };
    }

    private static Condition<Parameter> totalPagedResultsPolicy() {
        return new Condition<Parameter>() {
            @Override
            public boolean matches(final Parameter parameter) {
                return "_totalPagedResultsPolicy".equals(parameter.getName());
            }
        };
    }

    private interface Supplier<T> {
        T get();
    }
}
