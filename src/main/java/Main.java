import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

public class Main {
    static Connection conn = null;

    public static void main(String[] args) {
        connectDb();
        setupDB();
        port(5000);
        System.out.println("Listening on port 5000");

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
        });

        //API Endpoint
        post("/recommend", (req, res) -> {
            JSONObject json = new JSONObject(req.body());
            if (json.get("engine").equals("user-based")) {
                return userBasedRecommendations(json.getInt("id"), json.getInt("num"));
            } else if (json.get("engine").equals("item-based")) {
                return itemBasedRecommendations(json.getInt("id"), json.getInt("num"));
            } else {
                res.status(400); //400-Bad Request
                return new JSONObject();
            }
        });
    }

    private static void connectDb() {
        try {
            String url = "jdbc:sqlite:./recommendations.db";
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    private static void setupDB() {
        try {
            Statement st = conn.createStatement();
            BufferedReader br;
            if (!tablesExist("movies")) {
                br = new BufferedReader(new FileReader("dataset/movies.csv"));
                String table = "CREATE TABLE movies (movieId INTEGER PRIMARY KEY, title TEXT, genres TEXT);";
                st.execute(table);
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO movies values (?, ?, ?)");
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] row = line.split(",");
                    stmt.setInt(1, Integer.parseInt(row[0]));
                    stmt.setString(2, row[1]);
                    stmt.setString(3, row[2]);
                    stmt.executeUpdate();
                }
                System.out.println("Created Table: movies");
                stmt.close();
                br.close();
            }
            if (!tablesExist("users")) {
                br = new BufferedReader(new FileReader("dataset/users.csv"));
                String table = "CREATE TABLE users (userId INTEGER PRIMARY KEY, sex TEXT, age INTEGER, occupation TEXT, zipcode INTEGER);";
                st.execute(table);
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO users values (?, ?, ?, ?, ?)");
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] row = line.split(",");
                    stmt.setInt(1, Integer.parseInt(row[0]));
                    stmt.setString(2, row[1]);
                    stmt.setInt(3, Integer.parseInt(row[2]));
                    stmt.setString(4, row[3]);
                    stmt.setInt(5, Integer.parseInt(row[4]));
                    stmt.executeUpdate();
                }
                System.out.println("Created Table: users");
                stmt.close();
                br.close();
            }
            st.close();
        } catch (SQLException | IOException e) {
            System.out.println(e);
        }
    }

    private static boolean tablesExist(String tableName) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet resultSet = meta.getTables(null, null, tableName, new String[]{"TABLE"});
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }

    }

    private static JSONObject userBasedRecommendations(int userID, int numResults) {
        JSONObject json = new JSONObject();
        try {
            DataModel datamodel = new FileDataModel(new File("dataset/ratings.csv"));
            UserSimilarity similarity = new PearsonCorrelationSimilarity(datamodel);
            UserNeighborhood neighborhood = new NearestNUserNeighborhood(numResults, similarity, datamodel);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(datamodel, neighborhood, similarity);
            List<RecommendedItem> recommendations = recommender.recommend(userID, numResults);  //Movies
            long[] similarUsers = recommender.mostSimilarUserIDs(userID, 10);   //Top 10 similar users
            Statement st = conn.createStatement();
            List<JSONObject> movies = new ArrayList<>();
            for (RecommendedItem item : recommendations) {
                String fetchMovies = "SELECT * from movies where movieId=" + item.getItemID();
                ResultSet res = st.executeQuery(fetchMovies);
                JSONObject movie = new JSONObject();
                movie.put("movieID", res.getInt(1));
                movie.put("title", res.getString(2));
                movie.put("genres", res.getString(3));
                movies.add(movie);
            }
            List<JSONObject> users = new ArrayList<>();
            for (long item : similarUsers) {
                String fetchUsers = "SELECT * from users where userId=" + item;
                ResultSet res = st.executeQuery(fetchUsers);
                JSONObject user = new JSONObject();
                user.put("userID", res.getInt(1));
                user.put("sex", res.getString(2));
                user.put("age", res.getInt(3));
                user.put("occupation", res.getString(4));
                user.put("zipcode", res.getInt(5));
                users.add(user);
            }
            json.put("movies", movies);
            json.put("users", users);
            st.close();
        } catch (IOException | TasteException | SQLException | JSONException e) {
            System.out.println(e);
        }
        return json;
    }

    private static JSONObject itemBasedRecommendations(int userId, int numResults) {
        JSONObject json = new JSONObject();
        try {
            DataModel datamodel = new FileDataModel(new File("dataset/ratings.csv"));
            ItemSimilarity similarity = new PearsonCorrelationSimilarity(datamodel);
            GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(datamodel, similarity);
            List<RecommendedItem> recommendations = recommender.mostSimilarItems(userId, numResults);   //Movies
            Statement st = conn.createStatement();
            List<JSONObject> movies = new ArrayList<>();
            for (RecommendedItem item : recommendations) {
                String fetchMovies = "SELECT * from movies where movieId=" + item.getItemID();
                ResultSet res = st.executeQuery(fetchMovies);
                JSONObject movie = new JSONObject();
                movie.put("movieID", res.getInt(1));
                movie.put("title", res.getString(2));
                movie.put("genres", res.getString(3));
                movies.add(movie);
            }
            json.put("movies", movies);
            st.close();
        } catch (IOException | TasteException | JSONException | SQLException e) {
            System.out.println(e);
        }
        return json;
    }
}