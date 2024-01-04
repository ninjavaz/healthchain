'use strict';

const Resource = require('./Resource.js');

class PractitionerResource extends Resource {
    constructor(resourceType, id, gender, telecom, name, birthDate, managingOrganization) {
        super(resourceType, id, gender, telecom, name, birthDate, managingOrganization);
    }
}

module.exports = PractitionerResource;