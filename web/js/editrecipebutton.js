$(document).ready(function()
{
    var session = getSession();
    var editButton = document.getElementById("editlink");
    var deleteButton = document.getElementById("deletebutton");
    if(!session)
    {
        editButton.setAttribute("href", "login.html");
        
        deleteButton.addEventListener("click", function()
        {
            window.location.replace("login.html");
        });
    }
    else
    {
        //om man är inloggad hämtas id från url och läggs på ändra recept-knappens länk
        var url = window.location.href;
        var id = url.substring(url.indexOf("?"));
        editButton.setAttribute("href", "addrecipe.html" + id);
        
        
        deleteButton.addEventListener("click", function()
        {
            id = id.substring(id.indexOf("=") + 1);
            console.log(id);
            $.ajax
            ({
                type: "DELETE",
                url: "http://94.46.140.3:8080/sebastian_recipe_backend/api/Recipe/" + id ,
                headers:
                {
                    "Authorization": "Basic " + btoa(session.username + ":" + session.password),
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                async: false,
                success: function()
                {
                    window.location.replace("index.html");
                }
            });
        });
    }
    
    
});