import org.bson.Document;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ConexaoMongo {
	public static void main(String[] args) {
		MongoClient mongo = new MongoClient("18.231.62.135", 27017);
//		
//		MongoDatabase database = mongo.getDatabase("admin");
//		
//		MongoCollection<Document> document = database.getCollection("usuarios");
		
		
//		Mongo mongo = new Mongo("18.231.62.135", 27017);
		
		
		DB db = mongo.getDB("admin");
		DBCollection table = db.getCollection("usuarios");
		DBCursor cur = table.find();
		while (cur.hasNext()){
			System.out.println(cur.next());
		}
		
	}
}