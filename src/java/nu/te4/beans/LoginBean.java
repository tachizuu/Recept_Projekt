package nu.te4.beans;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Base64;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import nu.te4.utilities.ConnectionFactory;

@Stateless
public class LoginBean
{
    public int createUser(String body)
    {
        try
        {
            JsonReader jsonReader = Json.createReader(new StringReader(body));
            JsonObject credentials = jsonReader.readObject();
            jsonReader.close();
            
            String username = credentials.getString("username");
            String password = credentials.getString("password");
            
            //lägg till användare
            Connection connection = ConnectionFactory.getConnection();
                Statement stmt = connection.createStatement();
                
                String hash = BCrypt.hashpw(password, BCrypt.gensalt());
                
                String sql = String.format("INSERT INTO users VALUES('%s', '%s')", username, hash);
                stmt.executeUpdate(sql);
                connection.close();
                return 201;
        }
        catch(Exception e)
        {
            return 500;
        }
    }

    public boolean checkCredentials(String basic_auth)
    {
        if(basic_auth.isEmpty())
        {
            return false;
        }
        basic_auth = basic_auth.substring(basic_auth.indexOf(" ") + 1);
        byte[] decoded = Base64.getDecoder().decode(basic_auth);
        String credentials = new String(decoded);
        
        String username = credentials.substring(0, credentials.indexOf(":"));
        String password = credentials.substring(credentials.indexOf(":") + 1);
        
        try
        {
            Connection connection = ConnectionFactory.getConnection();
            Statement stmt = connection.createStatement();
            String sql = String.format("SELECT * FROM users WHERE username = '%s'", username);
            ResultSet result = stmt.executeQuery(sql);
            if(result.next())
            {
                if(BCrypt.checkpw(password, result.getString("hashed_password")))
                {
                    return true;
                }
            }
        }
        catch(Exception e)
        {
            
        }
        return false;
    }
}
