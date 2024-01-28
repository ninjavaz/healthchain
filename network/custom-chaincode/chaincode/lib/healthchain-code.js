"use strict";

const { Contract } = require("fabric-contract-api");
const ClientIdentity = require("fabric-shim").ClientIdentity;

class HealthchainCode extends Contract {
    async initLedger(ctx) {
        console.info("============= Initialize Ledger ===========");
        // Check if the counter already exists
        const counterAsBytes = await ctx.stub.getState("counter");
        if (!counterAsBytes || counterAsBytes.length === 0) {
            let counterValue = 0;

            // Create or update the counter JSON object
            let counter = counterValue

            // Store the counter JSON in the ledger
            await ctx.stub.putState(
                "counter",
                Buffer.from(JSON.stringify(counter))
            );
            console.info("Counter not found, initializing to 0");
        } else {
            console.info(`Counter found, current value is ${counterAsBytes.toString()}`);
        }

    }

    async createResource(ctx, resource) {
        resource = JSON.parse(resource);
        this.authorize(ctx, "ADMIN");

        if (resource.resourceType === null || resource.resourceType === "") {
            throw new Error("resourceType should not be null or empty");
        }

        if (resource.id === null || resource.id === "") {
            throw new Error("id should not be null or empty");
        }

        const exists = await this.resourceExists(ctx, resource.id);
        if (exists) {
            throw new Error(`The resource ${resource.id} already exists`);
        }

        const buffer = Buffer.from(JSON.stringify(resource));
        await ctx.stub.putState(resource.id, buffer);

        return resource;
    }

    async createConsent(ctx, consent) {
        this.authorize(ctx, "PATIENT");

        consent = JSON.parse(consent);

        const granteeId = consent.grantee[0].reference;

        if (
            granteeId === null ||
            granteeId === ""
        ) {
            throw new Error("fields must not be null or empty");
        }

        const userId = consent.subject.reference;
        this.authenticate(ctx, userId);

        const queryString = {
            selector: {
                resourceType: "Consent",
                "subject.reference": userId,
                grantee: {
                    $elemMatch: {
                        reference: granteeId,
                    },
                },
            },
        };

        let consents = await this.getObjectsByQueryString(ctx, queryString);
        if (consents.length !== 0) {
            throw new Error(`Consent of ${userId} for practitioner ${granteeId} already exists`);
        }

        consent.id = (await this.getNextCounter(ctx)).toString();

        const buffer = Buffer.from(JSON.stringify(consent));
        await ctx.stub.putState(consent.id, buffer);

        return consent;
    }

    async createDocumentReference(ctx, docRef) {
        this.authorize(ctx, "PRACTITIONER");

        docRef = JSON.parse(docRef);

        if (
            docRef.resourceType === null ||
            docRef.resourceType == ""
        ) {
            throw new Error(
                "Field resourceType must not be null or empty"
            );
        }

        const userId = docRef.author[0].reference;
        this.authenticate(ctx, userId);

        const exists = await this.resourceExists(ctx, docRef.subject.reference);
        if (!exists) {
            throw new Error(
                `Patient: ${docRef.subject.reference} does not exists`
            );
        }

        docRef.id = (await this.getNextCounter(ctx)).toString();;

        const buffer = Buffer.from(JSON.stringify(docRef));
        await ctx.stub.putState(docRef.id, buffer);

        return docRef;
    }

    async getResourceForUser(ctx, userId) {
        this.authenticate(ctx, userId);

        const resource = await ctx.stub.getState(userId.replace(/"/g, ""));
        if (!resource || resource.length === 0) {
            throw new Error(`Resource for: ${userId} does not exist`);
        }

        return JSON.parse(resource.toString());
    }

    async getConsentsForPatient(ctx, userId) {
        this.authorize(ctx, "PATIENT");

        this.authenticate(ctx, userId);

        const queryString = {
            selector: {
                resourceType: "Consent",
                "subject.reference": userId.replace(/"/g, ""),
            },
        };

        return this.getObjectsByQueryString(ctx, queryString);
    }

    async getDocumentReferencesForPatient(ctx, userId) {
        this.authorize(ctx, "PATIENT");

        this.authenticate(ctx, userId);

        const queryString = {
            selector: {
                resourceType: "DocumentReference",
                "subject.reference": userId.replace(/"/g, ""),
            },
        };

        return this.getObjectsByQueryString(ctx, queryString);
    }

    async getDocumentReferencesForPractitioner(ctx, userId) {
        this.authorize(ctx, "PRACTITIONER");

        const practitionerId = await this.getClientId(ctx);

        const queryString = {
            selector: {
                resourceType: "Consent",
                grantee: {
                    $elemMatch: {
                        reference: practitionerId,
                    },
                },
            },
        };

        let consents = await this.getObjectsByQueryString(ctx, queryString);
        const results = [];

        let hasValue = consents.some(elem => {
            // Checking if elem.subject and elem.subject.reference exist and then comparing
            return elem.subject && elem.subject.reference === userId.replace(/"/g, "");
        });

        if (!hasValue) {
            throw new Error(`You do not have access to: ${userId} EHR`);
        }

        const queryString1 = {
            selector: {
                resourceType: "DocumentReference",
                "subject.reference": userId.replace(/"/g, "")
            },
        };

        return this.getObjectsByQueryString(ctx, queryString1);

    }

    async getPatientResourcesForPractitioner(ctx, userId) {
        this.authorize(ctx, "PRACTITIONER");

        this.authenticate(ctx, userId);

        const queryString = {
            selector: {
                resourceType: "Consent",
                grantee: {
                    $elemMatch: {
                        reference: userId.replace(/"/g, ""),
                    },
                },
            },
        };

        let consents = await this.getObjectsByQueryString(ctx, queryString);

        const results = [];

        for (const elem of consents) {

            const buffer = await ctx.stub.getState(elem.subject.reference);
            if (!!buffer && buffer.length > 0) {
                results.push(JSON.parse(buffer.toString()));
            }
        }

        return results;
    }

    //ONCHAIN METHODS
    async getObjectsByQueryString(ctx, queryString) {
        const queryResultsIterator = await ctx.stub.getQueryResult(
            JSON.stringify(queryString)
        );
        const results = [];

        let result = await queryResultsIterator.next();
        while (!result.done) {
            const object = JSON.parse(result.value.value.toString("utf8"));
            results.push(object);
            result = await queryResultsIterator.next();
        }

        await queryResultsIterator.close();

        return results;
    }

    async getNextCounter(ctx) {
        let counter = await ctx.stub.getState("counter");

        // Store the counter JSON in the ledger
        await ctx.stub.putState(
            "counter",
            Buffer.from(JSON.stringify(counter))
        );

        return counter;
    }

    //UTILS
    async resourceExists(ctx, resourceId) {
        const buffer = await ctx.stub.getState(resourceId);
        return !!buffer && buffer.length > 0;
    }

    getUserRole(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        const role = clientIdentity.getAttributeValue("role");
        return role;
    }

    authorize(ctx, role) {
        if (role !== this.getUserRole(ctx)) {
            throw new Error("User does not have enough permission.");
        }
    }

    authenticate(ctx, userId) {
        if (userId.replace(/"/g, "") !== this.getClientId(ctx)) {
            throw new Error(
                `You do not have permission to data of user: ${userId.replace(/"/g, "")}`
            );
        }
    }

    getClientId(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        let identity = clientIdentity.getID().split("::");
        identity = identity[1].split("/")[2].split("=");
        return identity[1].toString("utf8");
    }
}

module.exports = HealthchainCode;
