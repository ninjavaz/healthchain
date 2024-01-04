'use strict';

class Consent {
    constructor(id, subject, date, grantee, decision) {
        this.resourceType = 'Consent';
        this.id = id;
        this.subject = subject;
        this.date = date;
        this.grantee = grantee;
        this.decision = decision;
    }

}

class Subject {
    constructor(reference, display) {
        this.reference = reference;
        this.display = display;
    }
}

class Grantee {
    constructor(reference, display) {
        this.reference = reference;
        this.display = display;
    }
}

module.exports = {
    Consent,
    Subject,
    Grantee
};