/**
 * @author Dominik Nuszkiewicz
 * @desc [The patient resource class]
 */
'use strict';

const Resource = require('./Resource.js');
// const ContactPoint = require('./Resource.js');
// const Name = require('./Resource.js');
// const Reference = require('./Resource.js');

class PractitionerResource extends Resource {
    constructor(resourceType, id, gender, telecom, name, birthDate, managingOrganization) {
        super(resourceType, id, gender, telecom, name, birthDate, managingOrganization);
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

module.exports = PractitionerResource;