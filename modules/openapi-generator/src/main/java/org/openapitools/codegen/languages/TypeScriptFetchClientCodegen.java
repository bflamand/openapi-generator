/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.languages;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.openapitools.codegen.*;
import org.openapitools.codegen.meta.features.DocumentationFeature;
import org.openapitools.codegen.templating.mustache.IndentedLambda;
import org.openapitools.codegen.utils.ModelUtils;

import java.io.File;
import java.util.*;

public class TypeScriptFetchClientCodegen extends AbstractTypeScriptClientCodegen {
    private static final String X_IS_UNIQUE_ID = "x-isUniqueId";
    private static final String X_ENTITY_ID = "x-entityId";
    private static final String X_IS_META_DATA_RESPONSE = "x-isMetaDataResponse";

    public static final String NPM_REPOSITORY = "npmRepository";
    public static final String WITH_INTERFACES = "withInterfaces";
    public static final String USE_SINGLE_REQUEST_PARAMETER = "useSingleRequestParameter";
    public static final String PREFIX_PARAMETER_INTERFACES = "prefixParameterInterfaces";
    public static final String TYPESCRIPT_THREE_PLUS = "typescriptThreePlus";
    public static final String WITHOUT_RUNTIME_CHECKS = "withoutRuntimeChecks";
    public static final String SAGAS_AND_RECORDS = "sagasAndRecords";

    protected String npmRepository = null;
    private boolean useSingleRequestParameter = true;
    private boolean prefixParameterInterfaces = false;
    protected boolean addedApiIndex = false;
    protected boolean addedModelIndex = false;
    protected boolean typescriptThreePlus = false;
    protected boolean withoutRuntimeChecks = false;
    protected boolean sagasAndRecords = false;


    public TypeScriptFetchClientCodegen() {
        super();

        modifyFeatureSet(features -> features.includeDocumentationFeatures(DocumentationFeature.Readme));

        // clear import mapping (from default generator) as TS does not use it
        // at the moment
        importMapping.clear();

        outputFolder = "generated-code/typescript-fetch";
        embeddedTemplateDir = templateDir = "typescript-fetch";

        this.apiTemplateFiles.put("apis.mustache", ".ts");

        this.addExtraReservedWords();

        typeMapping.put("date", "Date");
        typeMapping.put("DateTime", "Date");

        supportModelPropertyNaming(CodegenConstants.MODEL_PROPERTY_NAMING_TYPE.camelCase);
        this.cliOptions.add(new CliOption(NPM_REPOSITORY, "Use this property to set an url your private npmRepo in the package.json"));
        this.cliOptions.add(new CliOption(WITH_INTERFACES, "Setting this property to true will generate interfaces next to the default class implementations.", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(CodegenConstants.USE_SINGLE_REQUEST_PARAMETER, CodegenConstants.USE_SINGLE_REQUEST_PARAMETER_DESC, SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.TRUE.toString()));
        this.cliOptions.add(new CliOption(PREFIX_PARAMETER_INTERFACES, "Setting this property to true will generate parameter interface declarations prefixed with API class name to avoid name conflicts.", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(TYPESCRIPT_THREE_PLUS, "Setting this property to true will generate TypeScript 3.6+ compatible code.", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(WITHOUT_RUNTIME_CHECKS, "Setting this property to true will remove any runtime checks on the request and response payloads. Payloads will be casted to their expected types.", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
        this.cliOptions.add(new CliOption(SAGAS_AND_RECORDS, "Setting this property to true will generate additional files for use with redux-saga and immutablejs.", SchemaTypeUtil.BOOLEAN_TYPE).defaultValue(Boolean.FALSE.toString()));
    }

    @Override
    public String getName() {
        return "typescript-fetch";
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript client library using Fetch API (beta).";
    }

    public String getNpmRepository() {
        return npmRepository;
    }

    public void setNpmRepository(String npmRepository) {
        this.npmRepository = npmRepository;
    }

    public Boolean getTypescriptThreePlus() {
        return typescriptThreePlus;
    }

    public void setTypescriptThreePlus(Boolean typescriptThreePlus) {
        this.typescriptThreePlus = typescriptThreePlus;
    }

    public Boolean getWithoutRuntimeChecks(){
        return withoutRuntimeChecks;
    }

    public void setWithoutRuntimeChecks(Boolean withoutRuntimeChecks){
        this.withoutRuntimeChecks = withoutRuntimeChecks;
    }

    public Boolean getSagasAndRecords() {
        return sagasAndRecords;
    }

    public void setSagasAndRecords(Boolean sagasAndRecords) {
        this.sagasAndRecords = sagasAndRecords;
    }

    @Override
    public void processOpts() {
        super.processOpts();
        additionalProperties.put("isOriginalModelPropertyNaming", getModelPropertyNaming() == CodegenConstants.MODEL_PROPERTY_NAMING_TYPE.original);
        additionalProperties.put("modelPropertyNaming", getModelPropertyNaming().name());

        String sourceDir = "";
        if (additionalProperties.containsKey(NPM_NAME)) {
            sourceDir = "src" + File.separator;
        }

        this.apiPackage = sourceDir + "apis";
        this.modelPackage = sourceDir + "models";

        supportingFiles.add(new SupportingFile("index.mustache", sourceDir, "index.ts"));
        supportingFiles.add(new SupportingFile("runtime.mustache", sourceDir, "runtime.ts"));

        if (additionalProperties.containsKey(CodegenConstants.USE_SINGLE_REQUEST_PARAMETER)) {
            this.setUseSingleRequestParameter(convertPropertyToBoolean(CodegenConstants.USE_SINGLE_REQUEST_PARAMETER));
        }
        writePropertyBack(CodegenConstants.USE_SINGLE_REQUEST_PARAMETER, getUseSingleRequestParameter());

        if (additionalProperties.containsKey(PREFIX_PARAMETER_INTERFACES)) {
            this.setPrefixParameterInterfaces(convertPropertyToBoolean(PREFIX_PARAMETER_INTERFACES));
        }
        writePropertyBack(PREFIX_PARAMETER_INTERFACES, getPrefixParameterInterfaces());

        if (additionalProperties.containsKey(NPM_NAME)) {
            addNpmPackageGeneration();
        }

        if (additionalProperties.containsKey(TYPESCRIPT_THREE_PLUS)) {
            this.setTypescriptThreePlus(convertPropertyToBoolean(TYPESCRIPT_THREE_PLUS));
        }

        if (additionalProperties.containsKey(WITHOUT_RUNTIME_CHECKS)) {
            this.setWithoutRuntimeChecks(convertPropertyToBoolean(WITHOUT_RUNTIME_CHECKS));
        }

        if(!withoutRuntimeChecks){
            this.modelTemplateFiles.put("models.mustache", ".ts");
        }

        if (additionalProperties.containsKey(SAGAS_AND_RECORDS)) {
            this.setSagasAndRecords(convertPropertyToBoolean(SAGAS_AND_RECORDS));
            if (this.getSagasAndRecords()) {
                apiTemplateFiles.put("sagas.mustache", "Sagas.ts");
                modelTemplateFiles.put("records.mustache", "Record.ts");
                supportingFiles.add(new SupportingFile("runtimeSagasAndRecords.mustache", sourceDir, "runtimeSagasAndRecords.ts"));
            }
        }
    }

    @Override
    protected ImmutableMap.Builder<String, Mustache.Lambda> addMustacheLambdas() {
        ImmutableMap.Builder<String, Mustache.Lambda> lambdas = super.addMustacheLambdas();
        lambdas.put("indented_star_1", new IndentedLambda(1, " ", "* "));
        lambdas.put("indented_star_4", new IndentedLambda(5, " ", "* "));
        return lambdas;
    }

    @Override
    public String getTypeDeclaration(Schema p) {
        if (ModelUtils.isFileSchema(p)) {
            return "Blob";
        } else if (ModelUtils.isBinarySchema(p)) {
            return "Blob";
        }
        return super.getTypeDeclaration(p);
    }

    @Override
    protected void addAdditionPropertiesToCodeGenModel(CodegenModel codegenModel, Schema schema) {
        codegenModel.additionalPropertiesType = getTypeDeclaration(getAdditionalProperties(schema));
        addImport(codegenModel, codegenModel.additionalPropertiesType);
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        List<Object> models = (List<Object>) postProcessModelsEnum(objs).get("models");

        // process enum and custom properties in models
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            ExtendedCodegenModel cm = (ExtendedCodegenModel) mo.get("model");
            cm.imports = new TreeSet(cm.imports);
            this.processCodeGenModel(cm);
        }

        return objs;
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        List<ExtendedCodegenModel> allModels = new ArrayList<ExtendedCodegenModel>();
        List<String> entityModelClassnames = new ArrayList<String>();

        Map<String, Object> result = super.postProcessAllModels(objs);
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            for (Map<String, Object> model : models) {
                ExtendedCodegenModel codegenModel = (ExtendedCodegenModel) model.get("model");
                model.put("hasImports", codegenModel.imports.size() > 0);

                allModels.add(codegenModel);
                if (codegenModel.isEntity) {
                    entityModelClassnames.add(codegenModel.classname);
                }
            }
        }

        for (ExtendedCodegenModel rootModel : allModels) {
            for (String curImport : rootModel.imports) {
                boolean isModelImport = false;
                for (ExtendedCodegenModel model : allModels) {
                    if (model.classname.equals(curImport) && !model.isEnum) {
                        isModelImport = true;
                        break;
                    }
                }
                if (isModelImport) {
                    rootModel.modelImports.add(curImport);
                }
            }

            for (CodegenProperty var : rootModel.vars) {
                if (var.isModel && entityModelClassnames.indexOf(var.dataType) != -1) {
                    var.isEntity = true;
                } else if (var.isArray && var.items.isModel && entityModelClassnames.indexOf(var.items.dataType) != -1) {
                    var.items.isEntity = true;
                }
            }
        }
        return result;
    }

    private void autoSetDefaultValueForProperty(CodegenProperty var) {
        if (var.isArray || var.isModel) {
            var.defaultValue = var.dataTypeAlternate + "()";
        } else if (var.isUniqueId) {
            var.defaultValue = "\"-1\"";
        } else if (var.isEnum) {
            var.defaultValue = "'" + var._enum.get(0) + "'";
            updateCodegenPropertyEnum(var);
        } else if (var.dataType.equalsIgnoreCase("string")) {
            var.defaultValue = "\"\"";
        } else if (var.dataType.equalsIgnoreCase("integer")) {
            var.defaultValue = "0";
        }
    }

    private void addNpmPackageGeneration() {

        if (additionalProperties.containsKey(NPM_REPOSITORY)) {
            this.setNpmRepository(additionalProperties.get(NPM_REPOSITORY).toString());
        }

        //Files for building our lib
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("package.mustache", "", "package.json"));
        supportingFiles.add(new SupportingFile("tsconfig.mustache", "", "tsconfig.json"));
        supportingFiles.add(new SupportingFile("npmignore.mustache", "", ".npmignore"));
        supportingFiles.add(new SupportingFile("gitignore", "", ".gitignore"));
    }

    @Override
    public ExtendedCodegenModel fromModel(String name, Schema model) {
        CodegenModel cm = super.fromModel(name, model);
        return new ExtendedCodegenModel(cm);
    }

    @Override
    public ExtendedCodegenOperation fromOperation(String path, String httpMethod, Operation operation, List<Server> servers) {
        CodegenOperation superOp = super.fromOperation(path, httpMethod, operation, servers);
        ExtendedCodegenOperation op = new ExtendedCodegenOperation(superOp);

        if (this.getSagasAndRecords()) {
            ApiResponse methodResponse = findMethodResponse(operation.getResponses());
            if (methodResponse != null) {
                Map<String, Schema> schemas = ModelUtils.getSchemas(this.openAPI);
                Schema schema = null;
                if (schemas != null) {
                    schema = schemas.get(op.returnBaseType);
                }

                ExtendedCodegenModel cm = null;
                if (schema != null) {
                    cm = fromModel(op.returnBaseType, schema);

                    if (Boolean.TRUE.equals(cm.vendorExtensions.get(X_IS_META_DATA_RESPONSE))) {
                        if (cm.vars.size() == 1 && "meta".equals(cm.vars.get(0).name)) {
                            op.returnTypeIsMetaOnlyResponse = true;
                            op.returnType = null; // changing the return so that it's as if it was void.
                        }
                        if (cm.vars.size() == 2 && "data".equals(cm.vars.get(1).name)) {
                            op.returnTypeIsMetaDataResponse = true;
                        }
                    }
                }

                if (!op.returnTypeIsMetaOnlyResponse) {
                    Schema responseSchema = unaliasSchema(ModelUtils.getSchemaFromResponse(methodResponse), importMapping);
                    CodegenProperty cp = null;
                    if (op.returnTypeIsMetaDataResponse && cm != null) {
                        cp = this.processCodeGenModel(cm).vars.get(1);
                    } else if (responseSchema != null) {
                        cp = fromProperty("response", responseSchema);
                        this.processCodegenProperty(cp, "", null);
                    }

                    op.returnBaseTypeAlternate = null;
                    if (cp != null) {
                        op.returnTypeAlternate = cp.dataTypeAlternate;
                        op.returnTypeIsModel = cp.isModel;
                        op.returnTypeIsArray = cp.isArray;
                        if (cp.isArray && cp.items.isModel) {
                            op.returnTypeSupportsEntities = true;
                            op.returnBaseTypeAlternate = cp.items.dataType + "Record";
                        } else if (cp.isModel) {
                            op.returnTypeSupportsEntities = true;
                        }

                    }
                }
            }
        }

        return op;
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> operations, List<Object> allModels) {
        // Add supporting file only if we plan to generate files in /apis
        if (operations.size() > 0 && !addedApiIndex) {
            addedApiIndex = true;
            supportingFiles.add(new SupportingFile("apis.index.mustache", apiPackage().replace('.', File.separatorChar), "index.ts"));
            if (this.getSagasAndRecords()) {
                supportingFiles.add(new SupportingFile("sagaApiManager.mustache", apiPackage().replace('.', File.separatorChar), "SagaApiManager.ts"));
            }
        }

        // Add supporting file only if we plan to generate files in /models
        if (allModels.size() > 0 && !addedModelIndex) {
            addedModelIndex = true;
            supportingFiles.add(new SupportingFile("models.index.mustache", modelPackage().replace('.', File.separatorChar), "index.ts"));
        }

        this.addOperationModelImportInfomation(operations);
        this.updateOperationParameterForEnum(operations);
        if (this.getSagasAndRecords()) {
            this.updateOperationParameterForSagaAndRecords(operations);
        }
        this.addOperationObjectResponseInformation(operations);
        this.addOperationPrefixParameterInterfacesInformation(operations);
        this.escapeOperationIds(operations);
        return operations;
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        Map<String, Object> parentObjs = super.postProcessSupportingFileData(objs);

        parentObjs.put("useSagaAndRecords", this.getSagasAndRecords());

        return parentObjs;
    }

    private ExtendedCodegenModel processCodeGenModel(ExtendedCodegenModel cm) {
        Object xEntityId = cm.vendorExtensions.get(X_ENTITY_ID);
        for (CodegenProperty var : cm.vars) {
            boolean parentIsEntity = this.processCodegenProperty(var, cm.classname, xEntityId);
            if (parentIsEntity) {
                cm.isEntity = true;
            }
            ;
        }

        if (Boolean.TRUE.equals(cm.vendorExtensions.get(X_IS_META_DATA_RESPONSE))) {
            if (cm.vars.size() == 1 && "meta".equals(cm.vars.get(0).name)) {
                cm.isMetaOnlyResponse = true;
            }
            if (cm.vars.size() == 2 && "data".equals(cm.vars.get(1).name)) {
                cm.isMetaDataResponse = true;
            }
        }

        if (cm.parent != null) {
            for (CodegenProperty var : cm.allVars) {
                if (Boolean.TRUE.equals(var.isEnum)) {
                    var.datatypeWithEnum = var.datatypeWithEnum
                            .replace(var.enumName, cm.classname + var.enumName);
                }
            }
        }
        if (!cm.oneOf.isEmpty()) {
            // For oneOfs only import $refs within the oneOf
            TreeSet<String> oneOfRefs = new TreeSet<>();
            for (String im : cm.imports) {
                if (cm.oneOf.contains(im)) {
                    oneOfRefs.add(im);
                }
            }
            cm.imports = oneOfRefs;
        }
        return cm;
    }

    private boolean processCodegenProperty(CodegenProperty var, String parentClassName, Object xEntityId) {
        boolean parentIsEntity = false;
        // name enum with model name, e.g. StatusEnum => PetStatusEnum
        if (Boolean.TRUE.equals(var.isEnum)) {
            // behaviour for enum names is specific for Typescript Fetch, not using namespaces
            var.datatypeWithEnum = var.datatypeWithEnum.replace(var.enumName, parentClassName + var.enumName);

            // need to post-process defaultValue, was computed with previous var.datatypeWithEnum
            if (var.defaultValue != null && !var.defaultValue.equals("undefined")) {
                int dotPos = var.defaultValue.indexOf(".");
                if (dotPos != -1) {
                    var.defaultValue = var.datatypeWithEnum + var.defaultValue.substring(dotPos);
                }
            }
        }

        var.isUniqueId = Boolean.TRUE.equals(var.vendorExtensions.get(X_IS_UNIQUE_ID));
        if (var.isUniqueId && xEntityId != null && xEntityId.equals(var.name)) {
            parentIsEntity = true;
        }

        if (this.getSagasAndRecords()) {
            var.dataTypeAlternate = var.dataType;
            if (var.isArray) {
                var.dataTypeAlternate = var.dataType.replace("Array<", "List<");
                if (var.items.isModel) {
                    String itemsDataType = var.items.dataType + "Record";
                    var.dataTypeAlternate = var.dataTypeAlternate.replace(var.items.dataType, itemsDataType);
                } else if (var.items.isEnum) {
                    var.dataTypeAlternate = var.dataTypeAlternate.replace(var.items.dataType, var.items.datatypeWithEnum);
                }
                if (var.isUniqueId) {
                    var.dataTypeAlternate = var.dataTypeAlternate.replace("number", "string");
                }
            } else if (var.isEnum) {
                var.dataTypeAlternate = var.datatypeWithEnum;
            } else if (var.isModel) {
                var.dataTypeAlternate = var.dataType + "Record";
            } else if (var.isUniqueId) {
                var.dataTypeAlternate = "string";
            }
            if (var.defaultValue == null || var.defaultValue.equals("undefined")) {
                this.autoSetDefaultValueForProperty(var);
            }
        }
        return parentIsEntity;
    }

    private void escapeOperationIds(Map<String, Object> operations) {
        Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
        List<ExtendedCodegenOperation> operationList = (List<ExtendedCodegenOperation>) _operations.get("operation");
        for (ExtendedCodegenOperation op : operationList) {
            String param = op.operationIdCamelCase + "Request";
            if (op.imports.contains(param)) {
                // we import a model with the same name as the generated operation, escape it
                op.operationIdCamelCase += "Operation";
                op.operationIdLowerCase += "operation";
                op.operationIdSnakeCase += "_operation";
            }
        }
    }

    private void addOperationModelImportInfomation(Map<String, Object> operations) {
        // This method will add extra infomation to the operations.imports array.
        // The api template uses this infomation to import all the required
        // models for a given operation.
        List<Map<String, Object>> imports = (List<Map<String, Object>>) operations.get("imports");
        for (Map<String, Object> im : imports) {
            im.put("className", im.get("import").toString().replace(modelPackage() + ".", ""));
        }
    }

    private void updateOperationParameterForEnum(Map<String, Object> operations) {
        // This method will add extra infomation as to whether or not we have enums and
        // update their names with the operation.id prefixed.
        // It will also set the uniqueId status if provided.
        Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
        List<ExtendedCodegenOperation> operationList = (List<ExtendedCodegenOperation>) _operations.get("operation");
        boolean hasEnum = false;
        for (ExtendedCodegenOperation op : operationList) {
            for (CodegenParameter param : op.allParams) {
                if (Boolean.TRUE.equals(param.isEnum)) {
                    hasEnum = true;
                    param.datatypeWithEnum = param.datatypeWithEnum
                            .replace(param.enumName, op.operationIdCamelCase + param.enumName);
                }
            }
        }

        operations.put("hasEnums", hasEnum);
    }

    private void updateOperationParameterForSagaAndRecords(Map<String, Object> operations) {
        // This method will add extra infomation as to whether or not we have enums and
        // update their names with the operation.id prefixed.
        // It will also set the uniqueId status if provided.
        Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
        List<ExtendedCodegenOperation> operationList = (List<ExtendedCodegenOperation>) _operations.get("operation");
        for (ExtendedCodegenOperation op : operationList) {
            for (CodegenParameter param : op.allParams) {
                if (Boolean.TRUE.equals(param.vendorExtensions.get(X_IS_UNIQUE_ID))) {
                    param.isUniqueId = true;
                }

                param.dataTypeAlternate = param.dataType;
                if (param.isArray) {
                    if (param.items.isModel) {
                        String itemsDataType = param.items.dataType + "Record";
                        param.dataTypeAlternate = param.dataType.replace("Array<", "List<");
                        param.dataTypeAlternate = param.dataTypeAlternate.replace(param.items.dataType, itemsDataType);
                    } else if (param.items.isEnum) {
                        param.dataTypeAlternate = param.datatypeWithEnum.replace("Array<", "List<");
                    } else {
                        param.dataTypeAlternate = param.dataType.replace("Array<", "List<");
                    }
                    if (param.isUniqueId) {
                        param.dataTypeAlternate = param.dataTypeAlternate.replace("number", "string");
                    }
                } else if (param.isEnum) {
                    param.dataTypeAlternate = param.datatypeWithEnum;
                } else if (param.isModel) {
                    param.dataTypeAlternate = param.dataType + "Record";
                } else if (param.isUniqueId) {
                    param.dataTypeAlternate = "string";
                }
            }
        }
    }

    private void addOperationObjectResponseInformation(Map<String, Object> operations) {
        // This method will modify the infomation on the operations' return type.
        // The api template uses this infomation to know when to return a text
        // response for a given simple response operation.
        Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
        List<ExtendedCodegenOperation> operationList = (List<ExtendedCodegenOperation>) _operations.get("operation");
        for (ExtendedCodegenOperation op : operationList) {
            if("object".equals(op.returnType)) {
                op.isMap = true;
                op.returnSimpleType = false;
            }
        }
    }

    private void addOperationPrefixParameterInterfacesInformation(Map<String, Object> operations) {
        Map<String, Object> _operations = (Map<String, Object>) operations.get("operations");
        operations.put("prefixParameterInterfaces", getPrefixParameterInterfaces());
    }

    private void addExtraReservedWords() {
        this.reservedWords.add("BASE_PATH");
        this.reservedWords.add("BaseAPI");
        this.reservedWords.add("RequiredError");
        this.reservedWords.add("COLLECTION_FORMATS");
        this.reservedWords.add("FetchAPI");
        this.reservedWords.add("ConfigurationParameters");
        this.reservedWords.add("Configuration");
        this.reservedWords.add("configuration");
        this.reservedWords.add("HTTPMethod");
        this.reservedWords.add("HTTPHeaders");
        this.reservedWords.add("HTTPQuery");
        this.reservedWords.add("HTTPBody");
        this.reservedWords.add("ModelPropertyNaming");
        this.reservedWords.add("FetchParams");
        this.reservedWords.add("RequestOpts");
        this.reservedWords.add("exists");
        this.reservedWords.add("RequestContext");
        this.reservedWords.add("ResponseContext");
        this.reservedWords.add("Middleware");
        this.reservedWords.add("ApiResponse");
        this.reservedWords.add("ResponseTransformer");
        this.reservedWords.add("JSONApiResponse");
        this.reservedWords.add("VoidApiResponse");
        this.reservedWords.add("BlobApiResponse");
        this.reservedWords.add("TextApiResponse");
        // "Index" would create a file "Index.ts" which on case insensitive filesystems
        // would override our "index.js" file
        this.reservedWords.add("Index");
    }

    private boolean getUseSingleRequestParameter() {
        return useSingleRequestParameter;
    }

    private void setUseSingleRequestParameter(boolean useSingleRequestParameter) {
        this.useSingleRequestParameter = useSingleRequestParameter;
    }

    private boolean getPrefixParameterInterfaces() {
        return prefixParameterInterfaces;
    }

    private void setPrefixParameterInterfaces(boolean prefixParameterInterfaces) {
        this.prefixParameterInterfaces = prefixParameterInterfaces;
    }

    class ExtendedCodegenOperation extends CodegenOperation {
        boolean returnTypeIsMetaDataResponse, returnTypeIsMetaOnlyResponse, returnTypeSupportsEntities, returnTypeIsModel, returnTypeIsArray;
        String returnTypeAlternate, returnBaseTypeAlternate;

        public ExtendedCodegenOperation(CodegenOperation o) {
            super();

            this.responseHeaders.addAll(o.responseHeaders);
            this.hasAuthMethods = o.hasAuthMethods;
            this.hasConsumes = o.hasConsumes;
            this.hasProduces = o.hasProduces;
            this.hasParams = o.hasParams;
            this.hasOptionalParams = o.hasOptionalParams;
            this.hasRequiredParams = o.hasRequiredParams;
            this.returnTypeIsPrimitive = o.returnTypeIsPrimitive;
            this.returnSimpleType = o.returnSimpleType;
            this.subresourceOperation = o.subresourceOperation;
            this.isMap = o.isMap;
            this.isArray = o.isArray;
            this.isMultipart  = o.isMultipart;
            this.isResponseBinary = o.isResponseBinary;
            this.isResponseFile = o.isResponseFile;
            this.hasReference = o.hasReference;
            this.isRestfulIndex = o.isRestfulIndex;
            this.isRestfulShow = o.isRestfulShow;
            this.isRestfulCreate = o.isRestfulCreate;
            this.isRestfulUpdate = o.isRestfulUpdate;
            this.isRestfulDestroy = o.isRestfulDestroy;
            this.isRestful = o.isRestful;
            this.isDeprecated = o.isDeprecated;
            this.isCallbackRequest = o.isCallbackRequest;
            this.uniqueItems = o.uniqueItems;
            this.path = o.path;
            this.operationId = o.operationId;
            this.returnType = o.returnType;
            this.returnFormat = o.returnFormat;
            this.httpMethod = o.httpMethod;
            this.returnBaseType = o.returnBaseType;
            this.returnContainer = o.returnContainer;
            this.summary = o.summary;
            this.unescapedNotes = o.unescapedNotes;
            this.notes = o.notes;
            this.baseName = o.baseName;
            this.defaultResponse = o.defaultResponse;
            this.discriminator = o.discriminator;
            this.consumes = o.consumes;
            this.produces = o.produces;
            this.prioritizedContentTypes = o.prioritizedContentTypes;
            this.servers = o.servers;
            this.bodyParam = o.bodyParam;
            this.allParams = o.allParams;
            this.bodyParams = o.bodyParams;
            this.pathParams = o.pathParams;
            this.queryParams = o.queryParams;
            this.headerParams = o.headerParams;
            this.formParams = o.formParams;
            this.cookieParams = o.cookieParams;
            this.requiredParams = o.requiredParams;
            this.optionalParams = o.optionalParams;
            this.authMethods = o.authMethods;
            this.tags = o.tags;
            this.responses = o.responses;
            this.callbacks = o.callbacks;
            this.imports = o.imports;
            this.examples = o.examples;
            this.requestBodyExamples = o.requestBodyExamples;
            this.externalDocs = o.externalDocs;
            this.vendorExtensions = o.vendorExtensions;
            this.nickname = o.nickname;
            this.operationIdOriginal = o.operationIdOriginal;
            this.operationIdLowerCase = o.operationIdLowerCase;
            this.operationIdCamelCase = o.operationIdCamelCase;
            this.operationIdSnakeCase = o.operationIdSnakeCase;
        }

        @Override
        public boolean equals(Object o) {
            boolean result = super.equals(o);
            ExtendedCodegenOperation that = (ExtendedCodegenOperation) o;
            return result &&
                    returnTypeIsMetaDataResponse == that.returnTypeIsMetaDataResponse &&
                    returnTypeIsMetaOnlyResponse == that.returnTypeIsMetaOnlyResponse &&
                    returnTypeSupportsEntities == that.returnTypeSupportsEntities &&
                    returnTypeIsArray == that.returnTypeIsArray &&
                    returnTypeIsModel == that.returnTypeIsModel &&
                    Objects.equals(returnTypeAlternate, that.returnTypeAlternate) &&
                    Objects.equals(returnBaseTypeAlternate, that.returnBaseTypeAlternate);
        }

        @Override
        public int hashCode() {
            int superHash = super.hashCode();
            return Objects.hash(superHash, returnTypeIsMetaDataResponse, returnTypeIsMetaOnlyResponse, returnTypeSupportsEntities, returnTypeIsArray, returnTypeIsModel, returnTypeAlternate, returnBaseTypeAlternate);
        }

        @Override
        public String toString() {
            String superString = super.toString();
            final StringBuilder sb = new StringBuilder(superString);
            sb.append(", returnTypeIsMetaDataResponse=").append(returnTypeIsMetaDataResponse);
            sb.append(", returnTypeIsMetaOnlyResponse=").append(returnTypeIsMetaOnlyResponse);
            sb.append(", returnTypeSupportsEntities=").append(returnTypeSupportsEntities);
            sb.append(", returnTypeIsArray=").append(returnTypeIsArray);
            sb.append(", returnTypeIsModel=").append(returnTypeIsModel);
            sb.append(", returnTypeAlternate='").append(returnTypeAlternate).append('\'');
            sb.append(", returnBaseTypeAlternate='").append(returnBaseTypeAlternate).append('\'');
            return sb.toString();
        }
    }
    class ExtendedCodegenModel extends CodegenModel {
        public Set<String> modelImports = new TreeSet<String>();
        public boolean isEntity; // Is a model containing an "id" property marked as isUniqueId
        public boolean isMetaDataResponse;
        public boolean isMetaOnlyResponse;

        public ExtendedCodegenModel(CodegenModel cm) {
            super();

            this.parent = cm.parent;
            this.parentSchema = cm.parentSchema;
            this.interfaces = cm.interfaces;
            this.allParents = cm.allParents;
            this.parentModel = cm.parentModel;
            this.interfaceModels = cm.interfaceModels;
            this.children = cm.children;
            this.anyOf = cm.anyOf;
            this.oneOf = cm.oneOf;
            this.allOf = cm.allOf;
            this.name = cm.name;
            this.classname = cm.classname;
            this.title = cm.title;
            this.description = cm.description;
            this.classVarName = cm.classVarName;
            this.modelJson = cm.modelJson;
            this.dataType = cm.dataType;
            this.xmlPrefix = cm.xmlPrefix;
            this.xmlNamespace = cm.xmlNamespace;
            this.xmlName = cm.xmlName;
            this.classFilename = cm.classFilename;
            this.unescapedDescription = cm.unescapedDescription;
            this.discriminator = cm.discriminator;
            this.defaultValue = cm.defaultValue;
            this.arrayModelType = cm.arrayModelType;
            this.isAlias = cm.isAlias;
            this.isString = cm.isString;
            this.isInteger = cm.isInteger;
            this.isLong = cm.isLong;
            this.isNumber = cm.isNumber;
            this.isNumeric = cm.isNumeric;
            this.isFloat = cm.isFloat;
            this.isDouble = cm.isDouble;
            this.isDate = cm.isDate;
            this.isDateTime = cm.isDateTime;
            this.vars = cm.vars;
            this.allVars = cm.allVars;
            this.requiredVars = cm.requiredVars;
            this.optionalVars = cm.optionalVars;
            this.readOnlyVars = cm.readOnlyVars;
            this.readWriteVars = cm.readWriteVars;
            this.parentVars = cm.parentVars;
            this.allowableValues = cm.allowableValues;
            this.mandatory = cm.mandatory;
            this.allMandatory = cm.allMandatory;
            this.imports = cm.imports;
            this.hasVars = cm.hasVars;
            this.emptyVars = cm.emptyVars;
            this.hasMoreModels = cm.hasMoreModels;
            this.hasEnums = cm.hasEnums;
            this.isEnum = cm.isEnum;
            this.isNullable = cm.isNullable;
            this.hasRequired = cm.hasRequired;
            this.hasOptional = cm.hasOptional;
            this.isArray = cm.isArray;
            this.hasChildren = cm.hasChildren;
            this.isMap = cm.isMap;
            this.isDeprecated = cm.isDeprecated;
            this.hasOnlyReadOnly = cm.hasOnlyReadOnly;
            this.externalDocumentation = cm.externalDocumentation;

            this.vendorExtensions = cm.vendorExtensions;
            this.additionalPropertiesType = cm.additionalPropertiesType;
            this.isAdditionalPropertiesTrue = cm.isAdditionalPropertiesTrue;
            this.setMaxProperties(cm.getMaxProperties());
            this.setMinProperties(cm.getMinProperties());
            this.setUniqueItems(cm.getUniqueItems());
            this.setMaxItems(cm.getMaxItems());
            this.setMinItems(cm.getMinItems());
            this.setMaxLength(cm.getMaxLength());
            this.setMinLength(cm.getMinLength());
            this.setExclusiveMinimum(cm.getExclusiveMinimum());
            this.setExclusiveMaximum(cm.getExclusiveMaximum());
            this.setMinimum(cm.getMinimum());
            this.setMaximum(cm.getMaximum());
            this.setPattern(cm.getPattern());
            this.setMultipleOf(cm.getMultipleOf());
            this.setItems(cm.getItems());
            this.setAdditionalProperties(cm.getAdditionalProperties());
            this.setIsModel(cm.getIsModel());
        }

        public Set<String> getModelImports() {
            return modelImports;
        }

        public void setModelImports(Set<String> modelImports) {
            this.modelImports = modelImports;
        }

        @Override
        public boolean equals(Object o) {
            boolean result = super.equals(o);
            ExtendedCodegenModel that = (ExtendedCodegenModel) o;
            return result &&
                    isEntity == that.isEntity &&
                    isMetaDataResponse == that.isMetaDataResponse &&
                    isMetaOnlyResponse == that.isMetaOnlyResponse &&
                    Objects.equals(modelImports, that.modelImports);

        }
        @Override
        public int hashCode() {
            int superHash = super.hashCode();
            return Objects.hash(superHash, isEntity, isMetaDataResponse, isMetaOnlyResponse, getModelImports());
        }

        @Override
        public String toString() {
            String superString = super.toString();
            final StringBuilder sb = new StringBuilder(superString);
            sb.append(", modelImports=").append(modelImports);
            sb.append(", isEntity=").append(isEntity);
            sb.append(", isMetaDataResponse=").append(isMetaDataResponse);
            sb.append(", isMetaOnlyResponse=").append(isMetaOnlyResponse);
            return sb.toString();
        }

    }

}
