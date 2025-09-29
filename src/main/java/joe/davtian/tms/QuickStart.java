package joe.davtian.tms;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.List;

import org.bson.Document;

public class QuickStart {
    static final String CONNECTION_STRING = "mongodb+srv://joedavtian_db_user:lz74kNWb2GITybZ9@cluster0.p92yeyr.mongodb.net/";
    static final String DATABASE_NAME = "IncidentManagment";
    static final String TICKETS_COLLECTION = "Tickets";

    private QuickStart() {
        // utility class
    }

    public static void main(String[] args) {
        TicketRepository repository = new TicketRepository();
        try {
            List<Ticket> tickets = repository.readTickets();
            if (tickets.isEmpty()) {
                System.out.println("No tickets found.");
            } else {
                tickets.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Failed to read tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static MongoClient createClient() {
        return MongoClients.create(CONNECTION_STRING);
    }

    static MongoDatabase getDatabase(MongoClient mongoClient) {
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    static MongoCollection<Document> getTicketCollection(MongoClient mongoClient) {
        return getDatabase(mongoClient).getCollection(TICKETS_COLLECTION);
    }
}
