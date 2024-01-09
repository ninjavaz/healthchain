'use strict';

class DocumentReference {
    constructor(id, subject, date, author, description, content) {
        this.resourceType = 'DocumentReference';
        this.id = id;
        this.subject = subject; //Reference
        this.date = date;
        this.author = author;
        this.description = description;
        this.content = content; //List<Attachment>
    }
}

class Reference {
    constructor(reference) {
        this.reference = reference;
    }
}

class Content {
    constructor(attachment) {
        this.attachment = attachment;
    }
}

class Attachment {
    constructor(contentType, url, data, title) {
        this.contentType = contentType;
        this.url = url;
        this.data = data;
        this.title = title;
    }
}

module.exports = {
    DocumentReference,
    Reference,
    Content,
    Attachment
};