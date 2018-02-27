package nu.te4.beans;

import java.io.StringReader;
import com.mysql.jdbc.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import nu.te4.utilities.ConnectionFactory;

@Stateless
public class RecipeBean
{
    public int addRecipe(String body)
    {
        try
        {
            //läsa in JSON
            JsonReader jsonReader = Json.createReader(new StringReader(body));
            JsonObject recept = jsonReader.readObject();
            jsonReader.close();
            
            String title = recept.getString("titel");
            String description = recept.getString("beskrivning");
            String instructions = recept.getString("instruktioner");
            int category = recept.getInt("kategori");
            JsonArray ingredients = recept.getJsonArray("ingredienser");
            String image = recept.getString("bild");
            
            if(checkData(title, description, instructions, category, ingredients, image))
            {
                //anslut till databasen
                Connection connection = ConnectionFactory.getConnection();
                connection.setAutoCommit(false);
                
                //SQL för att lägga till receptet
                PreparedStatement ps = connection.prepareStatement("INSERT INTO recept(id, titel, beskrivning, instruktioner, kategori, bild) VALUES(NULL, ?, ?, ?, ?, ?)");
                ps.setString(1, title);
                ps.setString(2, description);
                ps.setString(3, instructions);
                ps.setInt(4, category);
                ps.setString(5, image);
                ps.executeUpdate();
                
                //uppdaterar databasvariabeln @recept_id till det senast tillagda receptets id
                ps = connection.prepareStatement("SET @recept_id = last_insert_id()");
                ps.executeUpdate();
                
                //ingredienser & mängder
                for(int i = 0; i < ingredients.size(); i++)
                {
                    String ingredient = ingredients.getJsonObject(i).getString("ingrediens");
                    String amount = ingredients.getJsonObject(i).getString("mängd");
                    
                    //ingredienser
                    ps = connection.prepareStatement("INSERT IGNORE INTO ingredienser(id, namn) VALUES (null, ?)");
                    ps.setString(1, ingredient);
                    ps.executeUpdate();
                    
                    //relationer & mängder
                    ps = connection.prepareStatement("INSERT INTO innehåller(recept, ingrediens, mängd) VALUES (@recept_id, getIdOf(?), ?)");
                    ps.setString(1, ingredient);
                    ps.setString(2, amount);
                    ps.executeUpdate();
                }
                
                //exekverar transaktion och stänger anslutning
                connection.commit();
                connection.close();
            }
            else
            {
                return 400; //bad request
            }
            
        }
        catch(ClassNotFoundException | SQLException e)
        {
            System.out.println("ERROR: " + e.getMessage());
            return 500; //server error
        }
        return 201; //created
    }
    
    public int updateRecipe(int id, String body)
    {
        try
        {
            //läsa in JSON
            JsonReader jsonReader = Json.createReader(new StringReader(body));
            JsonObject recept = jsonReader.readObject();
            jsonReader.close();
            
            String title = recept.getString("titel");
            String description = recept.getString("beskrivning");
            String instructions = recept.getString("instruktioner");
            int category = recept.getInt("kategori");
            JsonArray ingredients = recept.getJsonArray("ingredienser");
            String image;
            try
            {
                image = recept.getString("bild");
            }
            catch(Exception e)
            {
                image = "";
            }
            
            String sqlDeleteIngredients = "DELETE FROM innehåller WHERE recept = " + id;
            String sqlUpdateRecipe = String.format("UPDATE recept set titel = '%s', beskrivning = '%s', instruktioner = '%s', kategori = %d", title, description, instructions, category);
            if(!image.equals(""))
            {
                sqlUpdateRecipe += String.format(",bild = '%s'", image);
            }
            sqlUpdateRecipe += " WHERE id = " + id;
            
            //SQL för att lägga till ingredienserna & relationerna
            String sqlAddIngredients = "INSERT IGNORE INTO ingredienser(id, namn) VALUES ";
            String sqlAddContains = "INSERT INTO innehåller(recept, ingrediens, mängd) VALUES ";
            for(int i = 0; i < ingredients.size(); i++)
            {
                //ingredienser
                String ingredient = ingredients.getJsonObject(i).getString("ingrediens");
                sqlAddIngredients += String.format("(null, '%s'),", ingredient);

                //relationer & mängder
                String amount = ingredients.getJsonObject(i).getString("mängd");
                sqlAddContains += String.format("(%d, getIdOf('%s'), '%s'),", id, ingredient, amount);
            }
            //ta bort sista kommatecknet
            sqlAddIngredients = sqlAddIngredients.substring(0, sqlAddIngredients.lastIndexOf(","));
            sqlAddContains = sqlAddContains.substring(0, sqlAddContains.lastIndexOf(","));
            
            //anslut till databasen
            Connection connection = ConnectionFactory.getConnection();
            Statement stmt = connection.createStatement();
                
            //starta transaktion och exekvera frågor
            connection.setAutoCommit(false);
            stmt.executeUpdate(sqlDeleteIngredients);   //tar bort alla ingredienser som tillhör receptet
            stmt.executeUpdate(sqlAddIngredients);      //lägger till alla ingredienser till receptet
            stmt.executeUpdate(sqlAddContains);      //lägger till alla relationer till receptet
            stmt.executeUpdate(sqlUpdateRecipe);        //uppdaterar receptet
            connection.commit();
            
            connection.close();
        }
        catch(Exception e)
        {
            return 500;
        }
        return 200;
    }
    
    private boolean checkData(String title, String description, String instructions, int category, List ingredients, String image)
    {
        if(title.isEmpty() || description.isEmpty() || instructions.isEmpty()
                || category < 0 || ingredients.isEmpty() || image.isEmpty())
        {
            //title, description, instructions, category, eller ingredients är tom/felaktig
            return false;
        }
        return true;
    }
    
    public JsonArray getAllRecipes()
    {
        JsonArrayBuilder response = Json.createArrayBuilder();
        try
        {
            Connection connection = ConnectionFactory.getConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT recept.id, titel, beskrivning, bild, namn"
                    + " FROM recept, kategorier WHERE kategori = kategorier.id ORDER BY id DESC";
            ResultSet data = stmt.executeQuery(sql);
            
            while(data.next())
            {
                JsonObject recipe = Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("titel", data.getString("titel"))
                        .add("beskrivning", data.getString("beskrivning"))
                        .add("kategori", data.getString("namn"))
                        .add("bild", data.getString("bild"))
                        .build();
                response.add(recipe);
            }
        }
        catch(Exception e)
        {
            response.add(Json.createObjectBuilder().add("error", e.getMessage()).build());
        }
        return response.build();
    }
    
    public JsonArray getCategories()
    {
        JsonArrayBuilder categories = Json.createArrayBuilder();
        try
        {
            Connection connection = ConnectionFactory.getConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT id, namn FROM kategorier";
            ResultSet data = stmt.executeQuery(sql);
            
            while(data.next())
            {
                JsonObject category = Json.createObjectBuilder()
                        .add("id", data.getInt("id"))
                        .add("namn", data.getString("namn"))
                        .build();
                categories.add(category);
            }
        }
        catch(Exception e)
        {
            categories.add(Json.createObjectBuilder().add("error", e.getMessage()).build());
        }
        return categories.build();
    }
    
    public JsonObject getRecipe(int id)
    {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        try
        {
            Connection connection = ConnectionFactory.getConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT recept.id, titel, beskrivning, instruktioner, bild, kategorier.namn, innehåller.mängd, ingredienser.namn AS i_namn"
                    + " FROM recept, kategorier, innehåller, ingredienser"
                    + " WHERE recept.id = %d AND kategori = kategorier.id"
                    + " AND recept = recept.id AND innehåller.ingrediens = ingredienser.id";
            sql = String.format(sql, id);
            ResultSet data = stmt.executeQuery(sql);
            if(data.next())
            {
                jsonBuilder
                        .add("titel", data.getString("titel"))
                        .add("beskrivning", data.getString("beskrivning"))
                        .add("instruktioner", data.getString("instruktioner"))
                        .add("bild", data.getString("bild"))
                        .add("kategori", data.getString("namn"));
                //  + array för ingredienser
                JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
                do
                {
                    JsonObject ingredient = Json.createObjectBuilder()
                            .add("mängd", data.getString("mängd"))
                            .add("ingrediens", data.getString("i_namn"))
                            .build();
                    jsonArrayBuilder.add(ingredient);
                }while(data.next());
                jsonBuilder.add("ingredienser", jsonArrayBuilder);
            }
            else
            {
                jsonBuilder.add("status", 404);
            }
            connection.close();
        }
        catch(Exception e)
        {
            jsonBuilder.add("status", 500).add("error", e.getMessage());
        }
        
        JsonObject response = jsonBuilder.build();
        return response;
    }
    
    public int deleteRecipe(int id)
    {
        try
        {
            Connection connection = ConnectionFactory.getConnection();
            Statement stmt = connection.createStatement();
            String sql = String.format("DELETE FROM recept WHERE id = %d", id);
            stmt.executeUpdate(sql);
            connection.close();
            return 204;
        }
        catch(Exception e)
        {
            return 500;     //serverfel
        }
    }
}
