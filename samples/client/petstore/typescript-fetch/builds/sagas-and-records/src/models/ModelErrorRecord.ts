/* tslint:disable */
/* eslint-disable */
/**
 * OpenAPI Petstore
 * This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import {ApiRecordUtils, knownRecordFactories} from "../runtimeSagasAndRecords";
import {List, Record, RecordOf} from 'immutable';
import {Schema, schema, NormalizedSchema} from "normalizr";

import {
    ModelError,
} from './ModelError';

import {
    ItemId,
} from './ItemId';

import {
    ItemIdRecord,
    itemIdRecordUtils,
} from './ItemIdRecord';

export const ModelErrorRecordProps = {
	recType: "ModelErrorApiRecord" as "ModelErrorApiRecord",
    type: 'GenericException',
    itemInfo: (ItemIdRecord(), null as ItemIdRecord | null),
    details: null as string | null,
    exception: null as string | null,
};

export type ModelErrorRecordPropsType = typeof ModelErrorRecordProps;
export const ModelErrorRecord = Record(ModelErrorRecordProps, ModelErrorRecordProps.recType);
export type ModelErrorRecord = RecordOf<ModelErrorRecordPropsType>;

knownRecordFactories.set(ModelErrorRecordProps.recType, ModelErrorRecord);


class ModelErrorRecordUtils extends ApiRecordUtils<ModelError, ModelErrorRecord> {
	public normalize(apiObject: ModelError, asEntity?: boolean): ModelError {
		(apiObject as any).recType = ModelErrorRecordProps.recType;
        if (apiObject.itemInfo) { itemIdRecordUtils.normalize(apiObject.itemInfo); } 
		return apiObject;
	}

	public toApi(record: ModelErrorRecord): ModelError {
        const apiObject = super.toApi(record);
        if (record.itemInfo) { apiObject.itemInfo = itemIdRecordUtils.toApi(record.itemInfo); } 
        return apiObject;
    }
}

export const modelErrorRecordUtils = new ModelErrorRecordUtils();
