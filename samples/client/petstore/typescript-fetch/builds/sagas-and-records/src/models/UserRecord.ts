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

import { User } from './';



export const UserRecordProps = {
	recType: "UserRecord" as "UserRecord",
    id: "-1",
    username: null as string | null,
    firstName: null as string | null,
    lastName: null as string | null,
    email: null as string | null,
    password: null as string | null,
    phone: null as string | null,
    userStatus: null as number | null,
    subUser: null as User | null,
    subUser2: {} as any as User,
};

export type UserRecordPropsType = typeof UserRecordProps;
export const UserRecord = Record(UserRecordProps, UserRecordProps.recType);
export type UserRecord = RecordOf<UserRecordPropsType>;

knownRecordFactories.set(UserRecordProps.recType, UserRecord);

export const UserRecordEntityProps = {
	...UserRecordProps,
	recType: "UserRecordEntity" as "UserRecordEntity",
    subUser: null as string | null,
    subUser2: "-1",
};

export type UserRecordEntityPropsType = typeof UserRecordEntityProps;
export const UserRecordEntity = Record(UserRecordEntityProps, UserRecordEntityProps.recType);
export type UserRecordEntity = RecordOf<UserRecordEntityPropsType>;

knownRecordFactories.set(UserRecordEntityProps.recType, UserRecordEntity);

class UserRecordUtils extends ApiRecordUtils<User, UserRecord> {
	public normalize(apiObject: User, asEntity?: boolean): User {
		(apiObject as any).recType = asEntity ? "UserRecordEntity" : "UserRecord";
        (apiObject as any).id = apiObject.id.toString();
		return apiObject;
	}

	public getSchema(): Schema {
	    return new schema.Entity("user", {
            subUser: userRecordUtils.getSchema(),
            subUser2: userRecordUtils.getSchema(),
		});
	}
}

export const userRecordUtils = new UserRecordUtils();
