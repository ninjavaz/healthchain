/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

"use strict";

const { Contract } = require("fabric-contract-api");
const PatientResource = require("./model/PatientResource");
const ClientIdentity = require("fabric-shim").ClientIdentity;

class HealthchainCode extends Contract {
    async initLedger(ctx) {
        console.info("============= START : Initialize Ledger ===========");
        // const cars = [
        //     {
        //         size: "small",
        //         color: "blue",
        //         make: "Toyota",
        //         model: "Prius",
        //         owner: "Tomoko",
        //     },
        //     {
        //         size: "small",
        //         color: "red",
        //         make: "Ford",
        //         model: "Mustang",
        //         owner: "Brad",
        //     },
        //     {
        //         size: "small",
        //         color: "green",
        //         make: "Hyundai",
        //         model: "Tucson",
        //         owner: "Jin Soo",
        //     },
        //     {
        //         size: "small",
        //         color: "yellow",
        //         make: "Volkswagen",
        //         model: "Passat",
        //         owner: "Max",
        //     },
        //     {
        //         size: "small",
        //         color: "black",
        //         make: "Tesla",
        //         model: "S",
        //         owner: "Adriana",
        //     },
        //     {
        //         size: "small",
        //         color: "purple",
        //         make: "Peugeot",
        //         model: "205",
        //         owner: "Michel",
        //     },
        //     {
        //         size: "small",
        //         color: "white",
        //         make: "Chery",
        //         model: "S22L",
        //         owner: "Aarav",
        //     },
        //     {
        //         size: "small",
        //         color: "violet",
        //         make: "Fiat",
        //         model: "Punto",
        //         owner: "Pari",
        //     },
        //     {
        //         size: "huge",
        //         color: "indigo",
        //         make: "Tata",
        //         model: "Nano",
        //         owner: "Valeria",
        //     },
        //     {
        //         size: "small",
        //         color: "brown",
        //         make: "Holden",
        //         model: "Barina",
        //         owner: "Shotaro",
        //     },
        // ];

        // for (let i = 0; i < cars.length; i++) {
        //     cars[i].docType = "car";
        //     await ctx.stub.putState(
        //         "CAR" + i,
        //         Buffer.from(JSON.stringify(cars[i]))
        //     );
        //     console.info("Added <--> ", cars[i]);
        // }
        console.info("============= END : Initialize Ledger ===========");
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
        console.log("Querying by size");
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
                    jsonRes.Record = JSON.parse(res.value.value.toString('utf8'));
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
        console.info("============= START : Create Car ===========");

        const car = {
            color,
            docType: "car",
            make,
            model,
            owner,
        };

        await ctx.stub.putState(carNumber, Buffer.from(JSON.stringify(car)));
        console.info("============= END : Create Car ===========");
    }

    async queryAllCars(ctx) {
        const startKey = "";
        const endKey = "";
        const allResults = [];
        for await (const { key, value } of ctx.stub.getStateByRange(
            startKey,
            endKey
        )) {
            const strValue = Buffer.from(value).toString("utf8");
            let record;
            try {
                record = JSON.parse(strValue);
            } catch (err) {
                console.log(err);
                record = strValue;
            }
            allResults.push({ Key: key, Record: record });
        }
        console.info(allResults);
        return JSON.stringify(allResults);
    }




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


    async getClientId(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        let identity = clientIdentity.getID().split('::');
        identity = identity[1].split('/')[2].split('=');
        return identity[1].toString('utf8');
    }



    async changeCarOwner(ctx, carNumber, newOwner) {
        console.info("============= START : changeCarOwner ===========");

        const carAsBytes = await ctx.stub.getState(carNumber); // get the car from chaincode state
        if (!carAsBytes || carAsBytes.length === 0) {
            throw new Error(`${carNumber} does not exist`);
        }
        const car = JSON.parse(carAsBytes.toString());
        car.owner = newOwner;

        await ctx.stub.putState(carNumber, Buffer.from(JSON.stringify(car)));
        console.info("============= END : changeCarOwner ===========");
    }

    async getResourceForUser(ctx) {
        console.info("============= START : getResourceForUser ===========");
        const id = await this.getClientId(ctx);
        const patient = await ctx.stub.getState(id);

        if (!patient || patient.length === 0) {
            throw new Error(`${id} does not exist`);
        }

        return patient.toString();
    }

    async createPatientResource(ctx, patientResource) {
        console.info("============= START : Create PatientResource ===========");

        patientResource = JSON.parse(patientResource);
        this.authorize(ctx, 'ADMIN');

        if (patientResource.resourceType === null || patientResource.resourceType === '') {
            throw new Error(`resourceType should not be null or empty`);
        }

        if (patientResource.id === null || patientResource.id === '') {
            throw new Error(`id should not be null or empty`);
        }

        const exists = await this.patientResourceExists(ctx, patientResource.id);
        if (exists) {
            throw new Error(`The patientResource ${patientResource.id} already exists`);
        }

        const buffer = Buffer.from(JSON.stringify(patientResource));
        await ctx.stub.putState(patientResource.id, buffer);

        console.info("============= END : Create PatientResource ===========");
    }

    async patientResourceExists(ctx, patientResourceId) {
        const buffer = await ctx.stub.getState(patientResourceId);
        return (!!buffer && buffer.length > 0);
    }

    async getUserRole(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        const role = clientIdentity.getAttributeValue("role");
        return role;
    }

    async authorize(ctx, role) {
        if (role !== await this.getUserRole(ctx)) {
            throw new Error(`User does not have enough permission.`);
        }
    }

}

module.exports = HealthchainCode;
