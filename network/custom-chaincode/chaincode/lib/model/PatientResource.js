/**
 * @author Dominik Nuszkiewicz
 * @desc [The patient resource class]
 */
'use strict';

const Resource = require('./Resource.js');

class PatientResource extends Resource {
    constructor(resourceType, id, gender, telecom, name, birthDate, managingOrganization, generalPractitioner) {
        super(resourceType, id, gender, telecom, name, birthDate, managingOrganization);
        this.generalPractitioner = generalPractitioner;
    }

    // // Getters and setters for generalPractitioner can be added if needed
    // // Example of a getter
    // getGeneralPractitioner() {
    //     return this.generalPractitioner;
    // }

    // // Example of a setter
    // setGeneralPractitioner(generalPractitioner) {
    //     this.generalPractitioner = generalPractitioner;
    // }
}

module.exports = PatientResource;