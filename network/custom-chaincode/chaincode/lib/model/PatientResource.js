'use strict';

const Resource = require('./Resource.js');

class PatientResource extends Resource {
    constructor(resourceType, id, gender, telecom, name, birthDate, managingOrganization, generalPractitioner) {
        super(resourceType, id, gender, telecom, name, birthDate, managingOrganization);
    }
}

module.exports = PatientResource;