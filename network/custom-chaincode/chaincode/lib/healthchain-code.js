// "use strict";

// const { Contract } = require("fabric-contract-api");
// const ClientIdentity = require("fabric-shim").ClientIdentity;

// class HealthchainCode extends Contract {
//     async initLedger(ctx) {
//         console.info("============= Initialize Ledger ===========");
//         // Check if the counter already exists
//         const counterAsBytes = await ctx.stub.getState("counter");
//         if (!counterAsBytes || counterAsBytes.length === 0) {
//             let counterValue = 0;

//             // Create or update the counter JSON object
//             let counter = counterValue

//             // Store the counter JSON in the ledger
//             await ctx.stub.putState(
//                 "counter",
//                 Buffer.from(JSON.stringify(counter))
//             );
//             console.info("Counter not found, initializing to 0");
//         } else {

//             // let counterValue = 0;

//             // // Create or update the counter JSON object
//             // let counter = counterValue

//             // // Store the counter JSON in the ledger
//             // await ctx.stub.putState(
//             //     "counter",
//             //     Buffer.from(JSON.stringify(counter))
//             // );

//             console.info(`Counter found, current value is ${counterAsBytes.toString()}`);
//         }

//         // let counter = await ctx.stub.getState("counter");
//         // if (counter.toString() === 'NaN') {
//         //     console.log("NULL");
//         //     // await ctx.stub.putState('counter', Buffer.from("0"));
//         // } else {
//         //     console.log((counter+1).toString());
//         //     // await ctx.stub.putState('counter', Buffer.from((counter+1).toString()));
//         // }
//         // console.log(counter);
//         // console.log("COUNTER");
//         // console.log(counter.toString());
//         // await ctx.stub.putState("counter", Buffer.from(counter.toString()));
//     }

//     async createResource(ctx, resource) {
//         resource = JSON.parse(resource);
//         this.authorize(ctx, "ADMIN");

//         if (resource.resourceType === null || resource.resourceType === "") {
//             throw new Error("resourceType should not be null or empty");
//         }

//         if (resource.id === null || resource.id === "") {
//             throw new Error("id should not be null or empty");
//         }

//         const exists = await this.resourceExists(ctx, resource.id);
//         if (exists) {
//             throw new Error(`The resource ${resource.id} already exists`);
//         }

//         const buffer = Buffer.from(JSON.stringify(resource));
//         await ctx.stub.putState(resource.id, buffer);

//         return resource;
//     }

//     async createConsent(ctx, consent) {
//         this.authorize(ctx, "PATIENT");

//         consent = JSON.parse(consent);
//         console.log("CONSENT");
//         console.log(consent);
//         const granteeId = consent.grantee[0].reference;

//         if (
//             granteeId === null ||
//             granteeId === ""
//         ) {
//             throw new Error("fields must not be null or empty");
//         }

//         const userId = consent.subject.reference;
//         this.authenticate(ctx, userId);

//         const queryString = {
//             selector: {
//                 resourceType: "Consent",
//                 "subject.reference": userId,
//                 grantee: {
//                     $elemMatch: {
//                         reference: granteeId,
//                     },
//                 },
//             },
//         };
//         console.log("QUERY_SELECTOR");
//         console.log(queryString);
//         let consents = await this.getObjectsByQueryString(ctx, queryString);
//         if (consents.length !== 0) {
//             throw new Error(`Consent of ${userId} for practitioner ${granteeId} already exists`);
//         }

//         consent.id = (await this.getNextCounter(ctx)).toString();
//         console.log("CONSENT1");
//         console.log(consent);
//         console.log(consent.id);

//         const buffer = Buffer.from(JSON.stringify(consent));
//         await ctx.stub.putState(consent.id, buffer);

//         return consent;
//     }

//     async createDocumentReference(ctx, docRef) {
//         this.authorize(ctx, "PRACTITIONER");

//         //1. sprawdzenie czy pacjent o danym id istnieje
//         //jesli tak => utworz DocRef gdzie subject.reference = patientID i id_DocRef = getNextCounter
//         //jesli nie => zwroc info ze pacjent o danym id nie istnieje

//         console.log("DOCUMENT_REF_HELLO");

//         docRef = JSON.parse(docRef);

//         if (
//             docRef.resourceType === null ||
//             docRef.resourceType == ""
//         ) {
//             throw new Error(
//                 "Field resourceType must not be null or empty"
//             );
//         }

//         const userId = docRef.author[0].reference;
//         this.authenticate(ctx, userId);

//         const exists = await this.resourceExists(ctx, docRef.subject.reference);
//         if (!exists) {
//             throw new Error(
//                 `Patient: ${docRef.subject.reference} does not exists`
//             );
//         }

//         console.log("DOCUMENT_REF_HELLO");
//         console.log(docRef);

//         // docRef.author[0].reference = userId;
//         docRef.id = (await this.getNextCounter(ctx)).toString();;

//         console.log(docRef);

//         const buffer = Buffer.from(JSON.stringify(docRef));
//         await ctx.stub.putState(docRef.id, buffer);

//         return docRef;
//     }

//     async getResourceForUser(ctx, userId) {
//         this.authenticate(ctx, userId);

//         const resource = await ctx.stub.getState(userId.replace(/"/g, ""));
//         if (!resource || resource.length === 0) {
//             throw new Error(`Resource for: ${userId} does not exist`);
//         }

//         return JSON.parse(resource.toString());
//     }

//     async getConsentsForPatient(ctx, userId) {
//         this.authorize(ctx, "PATIENT");

//         this.authenticate(ctx, userId);

//         const queryString = {
//             selector: {
//                 resourceType: "Consent",
//                 "subject.reference": userId.replace(/"/g, ""),
//             },
//         };

//         return this.getObjectsByQueryString(ctx, queryString);
//     }

//     async getDocumentReferencesForPatient(ctx, userId) {
//         this.authorize(ctx, "PATIENT");

//         this.authenticate(ctx, userId);

//         const queryString = {
//             selector: {
//                 resourceType: "DocumentReference",
//                 "subject.reference": userId.replace(/"/g, ""),
//             },
//         };

//         return this.getObjectsByQueryString(ctx, queryString);
//     }

//     async getDocumentReferencesForPractitioner(ctx, userId) {
//         this.authorize(ctx, "PRACTITIONER");

//         // this.authenticate(ctx, userId);

//         const practitionerId = await this.getClientId(ctx);

//         const queryString = {
//             selector: {
//                 resourceType: "Consent",
//                 grantee: {
//                     $elemMatch: {
//                         reference: practitionerId,
//                     },
//                 },
//             },
//         };

//         let consents = await this.getObjectsByQueryString(ctx, queryString);
//         const results = [];

// // let valueToCheck = "someValue"; // replace with the value you're looking for

//         let hasValue = consents.some(elem => {
//             // Checking if elem.subject and elem.subject.reference exist and then comparing
//             console.log(elem.subject.reference);
//             console.log(elem.subject);
//             return elem.subject && elem.subject.reference === userId.replace(/"/g, "");
//         });

//         if (!hasValue) {
//             throw new Error(`You do not have access to: ${userId} EHR`);
//         }

//         const queryString1 = {
//             selector: {
//                 resourceType: "DocumentReference",
//                 "subject.reference": userId.replace(/"/g, "")
//             },
//         };

//         return this.getObjectsByQueryString(ctx, queryString1);

//     }

//     async getPatientResourcesForPractitioner(ctx, userId) {
//         this.authorize(ctx, "PRACTITIONER");

//         this.authenticate(ctx, userId);

//         const queryString = {
//             selector: {
//                 resourceType: "Consent",
//                 grantee: {
//                     $elemMatch: {
//                         reference: userId.replace(/"/g, ""),
//                     },
//                 },
//             },
//         };

//         let consents = await this.getObjectsByQueryString(ctx, queryString);

//         console.log("getPatientResourcesForPractitioner");
//         console.log(consents);

//         const results = [];

//         for (const elem of consents) {
//             console.log(elem);
//             console.log(elem.subject.reference);
//             console.log(elem.subject.reference.toString());

//             const buffer = await ctx.stub.getState(elem.subject.reference);
//             console.log(buffer.toString());
//             if (!!buffer && buffer.length > 0) {
//                 console.log("WRZUCAMY");
//                 console.log(buffer);
//                 results.push(JSON.parse(buffer.toString()));
//             }
//         }

//         console.log("REZULTATY");
//         console.log(results);

//         return results;
//     }

//     //ONCHAIN METHODS
//     async getObjectsByQueryString(ctx, queryString) {
//         const queryResultsIterator = await ctx.stub.getQueryResult(
//             JSON.stringify(queryString)
//         );
//         const results = [];

//         let result = await queryResultsIterator.next();
//         while (!result.done) {
//             const object = JSON.parse(result.value.value.toString("utf8"));
//             results.push(object);
//             result = await queryResultsIterator.next();
//         }

//         await queryResultsIterator.close();

//         // if (results.length === 0) {
//         //     throw new Error(`No results found for queryString: ${queryString}`);
//         // }

//         return results;
//     }

//     async getNextCounter(ctx) {
//         let counter = await ctx.stub.getState("counter");
//         console.log(counter.toString());

//         counter = parseInt(counter.toString()) + 1;

//         console.log("COUNTERA");
//         console.log(counter);
//         console.log(counter.toString());

//         // counter = counter + 1;
//         console.log("COUNTER_TO_STRING");
//         console.log(counter);
//         console.log(counter.toString());

//         // Store the counter JSON in the ledger
//         await ctx.stub.putState(
//             "counter",
//             Buffer.from(JSON.stringify(counter))
//         );

//         // await ctx.stub.putState("counter", Buffer.from(counter.toString()));
//         return counter;
//     }

//     //UTILS
//     async resourceExists(ctx, resourceId) {
//         const buffer = await ctx.stub.getState(resourceId);
//         return !!buffer && buffer.length > 0;
//     }

//     getUserRole(ctx) {
//         let clientIdentity = new ClientIdentity(ctx.stub);
//         const role = clientIdentity.getAttributeValue("role");
//         return role;
//     }

//     authorize(ctx, role) {
//         if (role !== this.getUserRole(ctx)) {
//             throw new Error("User does not have enough permission.");
//         }
//     }

//     authenticate(ctx, userId) {
//         if (userId.replace(/"/g, "") !== this.getClientId(ctx)) {
//             throw new Error(
//                 `You do not have permission to data of user: ${userId.replace(/"/g, "")}`
//             );
//         }
//     }

//     getClientId(ctx) {
//         let clientIdentity = new ClientIdentity(ctx.stub);
//         let identity = clientIdentity.getID().split("::");
//         identity = identity[1].split("/")[2].split("=");
//         return identity[1].toString("utf8");
//     }
// }

// module.exports = HealthchainCode;

"use strict";

const { Contract } = require("fabric-contract-api");
const ClientIdentity = require("fabric-shim").ClientIdentity;

class HealthchainCode extends Contract {
    //INIT LEDGER
    async initLedger(ctx) {
        const counterAsBytes = await ctx.stub.getState("counter");
        if (!counterAsBytes || counterAsBytes.length === 0) {
            await ctx.stub.putState("counter", Buffer.from(JSON.stringify(0)));
        }
    }

    //ADMIN METHODS
    async createResource(ctx, resource) {
        resource = JSON.parse(resource);
        this.authorize(ctx, "ADMIN");

        if (!resource.resourceType || !resource.id) {
            throw new Error("resourceType and id should not be null or empty");
        }

        const exists = await this.resourceExists(ctx, resource.id);
        if (exists) {
            throw new Error(`The resource ${resource.id} already exists`);
        }

        await ctx.stub.putState(
            resource.id,
            Buffer.from(JSON.stringify(resource))
        );
        return resource;
    }

    //PATIENT METHODS
    async createConsent(ctx, consent) {
        this.authorize(ctx, "PATIENT");
        consent = JSON.parse(consent);

        const granteeId = consent.grantee[0].reference;
        if (!granteeId) {
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
            throw new Error(
                `Consent of ${userId} for practitioner ${granteeId} already exists`
            );
        }

        consent.id = (await this.getNextCounter(ctx)).toString();
        await ctx.stub.putState(
            consent.id,
            Buffer.from(JSON.stringify(consent))
        );
        return consent;
    }

    async getConsentsForPatient(ctx, userId) {
        this.authorize(ctx, "PATIENT");
        this.authenticate(ctx, userId);
        const queryString = {
            selector: {
                resourceType: "Consent",
                "subject.reference": userId,
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
                "subject.reference": userId,
            },
        };
        return this.getObjectsByQueryString(ctx, queryString);
    }

    //PRACTITIONER METHODS
    async createDocumentReference(ctx, docRef) {
        this.authorize(ctx, "PRACTITIONER");
        docRef = JSON.parse(docRef);

        if (!docRef.resourceType) {
            throw new Error("Field resourceType must not be null or empty");
        }

        const userId = docRef.author[0].reference;
        this.authenticate(ctx, userId);

        const exists = await this.resourceExists(ctx, docRef.subject.reference);
        if (!exists) {
            throw new Error(
                `Patient: ${docRef.subject.reference} does not exists`
            );
        }

        docRef.id = (await this.getNextCounter(ctx)).toString();
        await ctx.stub.putState(docRef.id, Buffer.from(JSON.stringify(docRef)));
        return docRef;
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
        let hasAccess = consents.some(
            (elem) => elem.subject && elem.subject.reference === userId
        );

        if (!hasAccess) {
            throw new Error(`You do not have access to: ${userId} EHR`);
        }

        const queryString1 = {
            selector: {
                resourceType: "DocumentReference",
                "subject.reference": userId,
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
                        reference: userId,
                    },
                },
            },
        };
        let consents = await this.getObjectsByQueryString(ctx, queryString);
        const results = consents.map(async (elem) => {
            const buffer = await ctx.stub.getState(elem.subject.reference);
            return buffer && buffer.length > 0
                ? JSON.parse(buffer.toString())
                : null;
        });
        return Promise.all(results);
    }

    //UNIVERSAL METHODS
    async getResourceForUser(ctx, userId) {
        this.authenticate(ctx, userId);
        const resource = await ctx.stub.getState(userId);
        if (!resource || resource.length === 0) {
            throw new Error(`Resource for: ${userId} does not exist`);
        }
        return JSON.parse(resource.toString());
    }

    //ONCHAIN METHODS
    async getObjectsByQueryString(ctx, queryString) {
        const queryResultsIterator = await ctx.stub.getQueryResult(
            JSON.stringify(queryString)
        );
        const results = [];
        for (
            let result = await queryResultsIterator.next();
            !result.done;
            result = await queryResultsIterator.next()
        ) {
            const object = JSON.parse(result.value.value.toString("utf8"));
            results.push(object);
        }
        await queryResultsIterator.close();
        return results;
    }

    async getNextCounter(ctx) {
        let counter = await ctx.stub.getState("counter");
        counter = parseInt(counter.toString()) + 1;
        await ctx.stub.putState(
            "counter",
            Buffer.from(JSON.stringify(counter))
        );
        return counter;
    }

    async resourceExists(ctx, resourceId) {
        const buffer = await ctx.stub.getState(resourceId);
        return !!buffer && buffer.length > 0;
    }

    //UTIL METHODS
    getUserRole(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        return clientIdentity.getAttributeValue("role");
    }

    authorize(ctx, role) {
        if (role !== this.getUserRole(ctx)) {
            throw new Error("User does not have enough permission.");
        }
    }

    authenticate(ctx, userId) {
        if (userId !== this.getClientId(ctx)) {
            throw new Error(
                `You do not have permission to access data of user: ${userId}`
            );
        }
    }

    getClientId(ctx) {
        let clientIdentity = new ClientIdentity(ctx.stub);
        let identity = clientIdentity
            .getID()
            .split("::")[1]
            .split("/")[2]
            .split("=")[1];
        return identity;
    }
}

module.exports = HealthchainCode;
