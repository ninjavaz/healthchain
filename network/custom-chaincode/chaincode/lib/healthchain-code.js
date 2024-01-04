/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

'use strict';

const { Contract } = require('fabric-contract-api');
// const PatientResource = require('./model/PatientResource');
const { Subject, Consent, Grantee } = require('./model/Consent');
const ClientIdentity = require('fabric-shim').ClientIdentity;

class HealthchainCode extends Contract {
    async initLedger(ctx) {
        console.info('============= Initialize Ledger ===========');
    }

    async queryCar(ctx, carNumber) {
        const carAsBytes = await ctx.stub.getState(carNumber); // get the car from chaincode state
        if (!carAsBytes || carAsBytes.length === 0) {
            throw new Error(`${carNumber} does not exist`);
        }
        console.log(carAsBytes.toString());
        return carAsBytes.toString();
    }

    async queryCarBySize(ctx, size) {
        let queryString = {};
        queryString.selector = {};
        queryString.selector.size = size;
        console.log('Querying by size');
        const buffer = await this.getQueryResultForQueryString(
            ctx,
            JSON.stringify(queryString)
        );
        let asset = JSON.parse(buffer.toString());
        if (!asset || asset.length === 0) {
            throw new Error(`${size} does not exist`);
        }
        return asset;
    }

    async getQueryResultForQueryString(ctx, queryString) {
        let resultsIterator = await ctx.stub.getQueryResult(queryString);
        console.info('getQueryResultForQueryString <--> ', resultsIterator);
        let results = await this.getAllPatientResults(resultsIterator, false);
        return JSON.stringify(results);
    }

    async getAllPatientResults(iterator, isHistory) {
        let allResults = [];
        while (true) {
            let res = await iterator.next();

            if (res.value && res.value.value.toString()) {
                let jsonRes = {};
                console.log(res.value.value.toString('utf8'));

                if (isHistory && isHistory === true) {
                    jsonRes.Timestamp = res.value.timestamp;
                }
                jsonRes.Key = res.value.key;

                try {
                    jsonRes.Record = JSON.parse(
                        res.value.value.toString('utf8')
                    );
                } catch (err) {
                    console.log(err);
                    jsonRes.Record = res.value.value.toString('utf8');
                }
                allResults.push(jsonRes);
            }
            if (res.done) {
                console.log('end of data');
                await iterator.close();
                console.info(allResults);
                return allResults;
            }
        }
    }

    // //Read patients based on firstName
    // async queryPatientsByFirstName(ctx, firstName) {
    //     let queryString = {};
    //     queryString.selector = {};
    //     queryString.selector.docType = 'patient';
    //     queryString.selector.firstName = firstName;
    //     const buffer = await this.getQueryResultForQueryString(ctx, JSON.stringify(queryString));
    //     let asset = JSON.parse(buffer.toString());
    //     return asset;

    //     // return this.fetchLimitedFields(asset);
    // }

    async createCar(ctx, carNumber, make, model, color, owner) {
        console.info('============= START : Create Car ===========');

        const car = {
            color,
            docType: 'car',
            make,
            model,
            owner,
        };

        await ctx.stub.putState(carNumber, Buffer.from(JSON.stringify(car)));
        console.info('============= END : Create Car ===========');
    }

    // async queryAllCars(ctx) {
    //     const startKey = "";
    //     const endKey = "";
    //     const allResults = [];
    //     for await (const { key, value } of ctx.stub.getStateByRange(
    //         startKey,
    //         endKey
    //     )) {
    //         const strValue = Buffer.from(value).toString("utf8");
    //         let record;
    //         try {
    //             record = JSON.parse(strValue);
    //         } catch (err) {
    //             console.log(err);
    //             record = strValue;
    //         }
    //         allResults.push({ Key: key, Record: record });
    //     }
    //     console.info(allResults);
    //     return JSON.stringify(allResults);
    // }

    // async createUser(ctx, user) {
    //     // let clientIdentity = new ClientIdentity(ctx.stub);
    //     const clientId = getClientId(ctx)
    //     const user = {
    //         color,
    //         docType: "car",
    //         make,
    //         model,
    //         owner,
    //     };

    //     await ctx.stub.putState(clientId, Buffer.from(JSON.stringify(user)));
    // }

    // async changeCarOwner(ctx, carNumber, newOwner) {
    //     console.info("============= START : changeCarOwner ===========");

    //     const carAsBytes = await ctx.stub.getState(carNumber); // get the car from chaincode state
    //     if (!carAsBytes || carAsBytes.length === 0) {
    //         throw new Error(`${carNumber} does not exist`);
    //     }
    //     const car = JSON.parse(carAsBytes.toString());
    //     car.owner = newOwner;

    //     await ctx.stub.putState(carNumber, Buffer.from(JSON.stringify(car)));
    //     console.info("============= END : changeCarOwner ===========");
    // }

    async getResourceForUser(ctx) {
        const id = this.getClientId(ctx);
        const resource = await ctx.stub.getState(id);

        if (!resource || resource.length === 0) {
            throw new Error(`Resource: ${id} does not exist`);
        }

        return resource.toString();
    }

    async createResource(ctx, resource) {
        resource = JSON.parse(resource);
        this.authorize(ctx, 'ADMIN');

        if (resource.resourceType === null || resource.resourceType === '') {
            throw new Error('resourceType should not be null or empty');
        }

        if (resource.id === null || resource.id === '') {
            throw new Error('id should not be null or empty');
        }

        const exists = await this.resourceExists(ctx, resource.id);
        if (exists) {
            throw new Error(`The resource ${resource.id} already exists`);
        }

        const buffer = Buffer.from(JSON.stringify(resource));
        await ctx.stub.putState(resource.id, buffer);

        return resource;
    }

    async createConsent(ctx, practitionerId, decision, date) {
        this.authorize(ctx, 'PATIENT');

        if (
            practitionerId === null ||
            practitionerId === '' ||
            decision === null ||
            practitionerId === ''
        ) {
            throw new Error('fields must not be null or empty');
        }

        console.log(practitionerId);
        console.log(practitionerId.toString());
        console.log(JSON.stringify(practitionerId));
        console.log(JSON.stringify(decision));
        console.log(JSON.stringify(date));
        console.log(date);
        console.log(date.toString());

        let subject = new Subject(this.getClientId(ctx), '');
        let grantee = new Grantee(practitionerId.replace(/"/g, ''), '');
        let grantees = [grantee];

        let consent = new Consent(
            subject.reference + '_' + grantee.reference,
            subject,
            date,
            grantees,
            decision.replace(/"/g, '')
        );

        console.log('createConsent');
        console.log(consent.toString());
        console.log(grantee.reference.replace(/"/g, ''));
        console.log(grantee.reference);

        const buffer = Buffer.from(JSON.stringify(consent));
        await ctx.stub.putState(consent.id, buffer);

        return consent;
    }

    async createDocumentReference(ctx, patientId, date, description, content) {
        this.authorize(ctx, 'PRACTITIONER');



        // if (
        //     practitionerId === null ||
        //     practitionerId === '' ||
        //     decision === null ||
        //     practitionerId === ''
        // ) {
        //     throw new Error('fields must not be null or empty');
        // }

        // console.log(practitionerId);
        // console.log(practitionerId.toString());
        // console.log(JSON.stringify(practitionerId));
        // console.log(JSON.stringify(decision));
        // console.log(JSON.stringify(date));
        // console.log(date);
        // console.log(date.toString());

        // let subject = new Subject(this.getClientId(ctx), '');
        // let grantee = new Grantee(practitionerId.replace(/"/g, ''), '');
        // let grantees = [grantee];

        // let consent = new Consent(
        //     subject.reference + '_' + grantee.reference,
        //     subject,
        //     date,
        //     grantees,
        //     decision.replace(/"/g, '')
        // );

        // console.log('createConsent');
        // console.log(consent.toString());
        // console.log(grantee.reference.replace(/"/g, ''));
        // console.log(grantee.reference);

        // const buffer = Buffer.from(JSON.stringify(consent));
        // await ctx.stub.putState(consent.id, buffer);

        return documentRef;

    }

    async getConsentsForPatient(ctx) {
        this.authorize(ctx, 'PATIENT');

        console.log('getConsentsForPatient');

        const patientId = this.getClientId(ctx);
        // const resource = await ctx.stub.getState(id);

        // Używamy zapytania do znalezienia wszystkich zgód, które mają w polu Subject.reference id pacjenta
        const queryString = {
            selector: {
                resourceType: 'Consent',
                'subject.reference': patientId,
            },
        };


        return this.getObjectsByQueryString(ctx, queryString);
    }

    // getConsentsForPractitioner(ctx) {

    // }

    async getPatientResourcesForPractitioner(ctx) {
        this.authorize(ctx, 'PRACTITIONER');
        const practitionerId = this.getClientId(ctx);

        const queryString = {
            selector: {
                grantee: {
                    $elemMatch: {
                        reference: practitionerId
                    }
                }
            }
        };

        let consents = await this.getObjectsByQueryString(ctx, queryString);

        // consents = consents.map(elem=>{elem.subject.reference;});
        console.log('getPatientResourcesForPractitioner');
        console.log(consents);

        const results = [];

        for (const elem of consents) {
            console.log(elem);
            console.log(elem.subject.reference);
            console.log(elem.subject.reference.toString());

            const buffer = await ctx.stub.getState(elem.subject.reference);
            console.log(buffer.toString());
            if (!!buffer && buffer.length > 0) {
                console.log('WRZUCAMY');
                console.log(buffer);
                results.push(JSON.parse(buffer.toString()));
            }
        }

        console.log('REZULTATY');
        console.log(results);

        return results;
    }

    async getObjectsByQueryString(ctx, queryString) {
        const queryResultsIterator = await ctx.stub.getQueryResult(JSON.stringify(queryString));
        const results = [];

        let result = await queryResultsIterator.next();
        while (!result.done) {
            const object = JSON.parse(result.value.value.toString('utf8'));
            results.push(object);
            result = await queryResultsIterator.next();
        }

        await queryResultsIterator.close();

        if (results.length === 0) {
            throw new Error(
                `No results found for queryString: ${queryString}`
            );
        }
        console.log(results);
        return results;
    }

    async getCounter(ctx) {
        return await ctx.stub.getState('counter');
    }

    async increaseCounter(ctx) {
        let counter = parseInt(await this.getCounter(ctx)) + 1;
        await ctx.stub.putState('counter', Buffer.from(counter.toString()));
    }


    //UTILS
    async resourceExists(ctx, resourceId) {
        const buffer = await ctx.stub.getState(resourceId);
        return !!buffer && buffer.length > 0;
    }

    getUserRole(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        const role = clientIdentity.getAttributeValue('role');
        return role;
    }

    authorize(ctx, role) {
        if (role !== this.getUserRole(ctx)) {
            throw new Error('User does not have enough permission.');
        }
    }

    getClientId(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        let identity = clientIdentity.getID().split('::');
        identity = identity[1].split('/')[2].split('=');
        return identity[1].toString('utf8');
    }
}

module.exports = HealthchainCode;

