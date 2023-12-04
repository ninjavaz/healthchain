/*
 * Copyright IBM Corp. All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

"use strict";

const { Contract } = require("fabric-contract-api");

class FabCar extends Contract {
    async initLedger(ctx) {
        console.info("============= START : Initialize Ledger ===========");
        const cars = [
            {
                size: "small",
                color: "blue",
                make: "Toyota",
                model: "Prius",
                owner: "Tomoko",
            },
            {
                size: "small",
                color: "red",
                make: "Ford",
                model: "Mustang",
                owner: "Brad",
            },
            {
                size: "small",
                color: "green",
                make: "Hyundai",
                model: "Tucson",
                owner: "Jin Soo",
            },
            {
                size: "small",
                color: "yellow",
                make: "Volkswagen",
                model: "Passat",
                owner: "Max",
            },
            {
                size: "small",
                color: "black",
                make: "Tesla",
                model: "S",
                owner: "Adriana",
            },
            {
                size: "small",
                color: "purple",
                make: "Peugeot",
                model: "205",
                owner: "Michel",
            },
            {
                size: "small",
                color: "white",
                make: "Chery",
                model: "S22L",
                owner: "Aarav",
            },
            {
                size: "small",
                color: "violet",
                make: "Fiat",
                model: "Punto",
                owner: "Pari",
            },
            {
                size: "huge",
                color: "indigo",
                make: "Tata",
                model: "Nano",
                owner: "Valeria",
            },
            {
                size: "small",
                color: "brown",
                make: "Holden",
                model: "Barina",
                owner: "Shotaro",
            },
        ];

        for (let i = 0; i < cars.length; i++) {
            cars[i].docType = "car";
            await ctx.stub.putState(
                "CAR" + i,
                Buffer.from(JSON.stringify(cars[i]))
            );
            console.info("Added <--> ", cars[i]);
        }
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

    async getUserRoles(ctx) {
        const roles = await ctx.clientIdentity.getAttributeValue("hf.Affiliation");
        const clientId = ctx.clientIdentity.getID();
        return JSON.stringify(roles + " || " + clientId);

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
}

module.exports = FabCar;
