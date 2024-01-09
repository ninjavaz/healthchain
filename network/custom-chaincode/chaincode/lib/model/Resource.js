'use strict';

class Resource {
    constructor(resourceType, id, gender, telecom, name, birthDate, managingOrganization) {
        this.resourceType = resourceType;
        this.id = id;
        this.gender = gender;
        this.telecom = telecom; //List<ContactPoint>
        this.name = name; //Name
        this.birthDate = birthDate;
        this.managingOrganization = managingOrganization; //Reference
    }
}

class ContactPoint {
    constructor(system, value) {
        this.system = system;
        this.value = value;
    }
}

class Name {
    constructor(use, family, given) {
        this.use = use;
        this.family = family;
        this.given = given;
    }
}

class Reference {
    constructor(identifier) {
        this.identifier = identifier;
    }
}

module.exports = {
    Resource,
    ContactPoint,
    Name,
    Reference
};