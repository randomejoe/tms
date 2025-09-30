package joe.davtian.tms;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.DeleteResult;

public class TicketRepository {

    public List<Ticket> readTickets() {
        try (MongoClient mongoClient = QuickStart.createClient()) {
            MongoCollection<Document> collection = QuickStart.getTicketCollection(mongoClient);
            List<Ticket> tickets = new ArrayList<>();
            for (Document document : collection.find()) {
                tickets.add(toTicket(document));
            }
            return tickets;
        }
    }

    public Ticket createTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket must not be null");
        }

        try (MongoClient mongoClient = QuickStart.createClient()) {
            MongoCollection<Document> collection = QuickStart.getTicketCollection(mongoClient);
            Document document = toDocument(ticket);
            collection.insertOne(document);

            Object insertedId = document.get("_id");
            if (insertedId instanceof ObjectId objectId) {
                ticket.setId(objectId.toHexString());
            } else if (insertedId != null) {
                ticket.setId(insertedId.toString());
            }

            return toTicket(document);
        }
    }

    public Ticket updateTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket must not be null");
        }
        if (ticket.getId() == null || ticket.getId().isBlank()) {
            throw new IllegalArgumentException("Ticket id must not be null or blank");
        }

        try (MongoClient mongoClient = QuickStart.createClient()) {
            MongoCollection<Document> collection = QuickStart.getTicketCollection(mongoClient);
            Document filter = buildIdFilter(ticket.getId());
            Document updateDocument = toDocument(ticket);
            updateDocument.remove("_id");

            if (updateDocument.isEmpty()) {
                throw new IllegalArgumentException("Ticket does not contain any updatable fields");
            }

            FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
            Document updated = collection.findOneAndUpdate(filter, new Document("$set", updateDocument), options);
            return updated != null ? toTicket(updated) : null;
        }
    }

    public boolean deleteTicket(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Ticket id must not be null or blank");
        }

        try (MongoClient mongoClient = QuickStart.createClient()) {
            MongoCollection<Document> collection = QuickStart.getTicketCollection(mongoClient);
            Document filter = buildIdFilter(ticketId);
            DeleteResult result = collection.deleteOne(filter);
            return result.getDeletedCount() > 0;
        }
    }

    private Ticket toTicket(Document document) {
        Ticket ticket = new Ticket();

        Object identifier = document.get("_id");
        if (identifier instanceof ObjectId objectId) {
            ticket.setId(objectId.toHexString());
        } else if (identifier != null) {
            ticket.setId(identifier.toString());
        } else {
            ticket.setId(getString(document, "id", "Id", "ticketId", "TicketId"));
        }

        ticket.setStatus(getString(document, "Status", "status"));
        ticket.setDateOfSubmission(getDate(document,
            "DateOfSubmission",
            "dateOfSubmission",
            "DateOfSubmition",
            "dateOfSubmition",
            "Date Of Submission",
            "date_of_submission"));
        ticket.setDeadline(getDate(document,
            "Deadline",
            "deadline",
            "DueDate",
            "dueDate",
            "due_date"));
        ticket.setPriority(getString(document, "Priority", "priority"));

        Number employeeId = getNumber(document, "EmployeeID", "employeeId", "EmployeeId");
        if (employeeId != null) {
            ticket.setEmployeeID(employeeId.intValue());
        }

        ticket.setType(getString(document, "Type", "type"));
        ticket.setSubject(getString(document, "Subject", "subject"));
        ticket.setDescription(getString(document, "Description", "description"));

        return ticket;
    }

    private Document toDocument(Ticket ticket) {
        Document document = new Document();

        Object identifier = toIdentifier(ticket.getId());
        if (identifier != null) {
            document.put("_id", identifier);
        }

        putIfNotNull(document, "status", ticket.getStatus());
        putIfNotNull(document, "dateOfSubmission", ticket.getDateOfSubmission());
        putIfNotNull(document, "deadline", ticket.getDeadline());
        putIfNotNull(document, "priority", ticket.getPriority());
        document.put("employeeId", ticket.getEmployeeID());
        putIfNotNull(document, "type", ticket.getType());
        putIfNotNull(document, "subject", ticket.getSubject());
        putIfNotNull(document, "description", ticket.getDescription());

        return document;
    }

    private void putIfNotNull(Document document, String key, Object value) {
        if (value != null) {
            document.put(key, value);
        }
    }

    private Document buildIdFilter(String ticketId) {
        Object identifier = toIdentifier(ticketId);

        List<Document> orFilters = new ArrayList<>();
        if (identifier != null) {
            orFilters.add(new Document("_id", identifier));
        }
        orFilters.add(new Document("id", ticketId));
        orFilters.add(new Document("Id", ticketId));
        orFilters.add(new Document("ticketId", ticketId));
        orFilters.add(new Document("TicketId", ticketId));

        if (orFilters.size() == 1) {
            return orFilters.get(0);
        }

        return new Document("$or", orFilters);
    }

    private Object toIdentifier(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return null;
        }
        if (ObjectId.isValid(ticketId)) {
            return new ObjectId(ticketId);
        }
        return ticketId;
    }

    private String getString(Document document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value instanceof String stringValue && !stringValue.isBlank()) {
                return stringValue;
            }
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private Number getNumber(Document document, String... keys) {
        for (String key : keys) {
            Number number = document.get(key, Number.class);
            if (number != null) {
                return number;
            }
            Object value = document.get(key);
            if (value instanceof Number numericValue) {
                return numericValue;
            }
            if (value instanceof String stringValue) {
                try {
                    return Integer.parseInt(stringValue);
                } catch (NumberFormatException ignored) {
                    // fall through and try the next key/value
                }
            }
        }
        return null;
    }

    private Date getDate(Document document, String... keys) {
        for (String key : keys) {
            Object value = document.get(key);
            if (value == null) {
                continue;
            }

            if (value instanceof Date dateValue) {
                return dateValue;
            }

            if (value instanceof BsonDateTime bsonDateTime) {
                return new Date(bsonDateTime.getValue());
            }

            if (value instanceof Document nested) {
                Object nestedDate = nested.get("$date");
                if (nestedDate instanceof Date dateFromNested) {
                    return dateFromNested;
                }
                if (nestedDate instanceof Number numberFromNested) {
                    return new Date(numberFromNested.longValue());
                }
                if (nestedDate instanceof String stringFromNested) {
                    Date parsed = parseDateString(stringFromNested);
                    if (parsed != null) {
                        return parsed;
                    }
                }

                Object numberLong = nested.get("$numberLong");
                if (numberLong instanceof String numberLongString) {
                    try {
                        return new Date(Long.parseLong(numberLongString));
                    } catch (NumberFormatException ignored) {
                        // fall through
                    }
                }
            }

            if (value instanceof Number numericValue) {
                return new Date(numericValue.longValue());
            }

            if (value instanceof String stringValue) {
                Date parsed = parseDateString(stringValue);
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return null;
    }

    private Date parseDateString(String value) {
        String trimmed = value == null ? null : value.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return null;
        }

        try {
            return Date.from(Instant.parse(trimmed));
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        try {
            return Date.from(OffsetDateTime.parse(trimmed).toInstant());
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(trimmed);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        try {
            LocalDate localDate = LocalDate.parse(trimmed);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        try {
            long epochMillis = Long.parseLong(trimmed);
            return new Date(epochMillis);
        } catch (NumberFormatException ignored) {
            // fall through
        }

        DateTimeFormatter[] dateTimeFormatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        };

        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(trimmed, formatter);
                return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
                // try next pattern
            }
        }

        DateTimeFormatter[] dateFormatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        };

        for (DateTimeFormatter formatter : dateFormatters) {
            try {
                LocalDate localDate = LocalDate.parse(trimmed, formatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
                // try next pattern
            }
        }

        return null;
    }
}
