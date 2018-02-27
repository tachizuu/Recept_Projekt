function getData(id, callback)
{
    var url = "http://94.46.140.3:8080/sebastian_recipe_backend/api/Recipe/" + id;
    
    $.ajax({
        url:url,
        success:function(data)
        {
            callback(data);
        },
        error:function(jqXHR, status, error)
        {
            $("#main").append("<h1>Kan inte ansluta till API</h1>");
            $("#main").append("<p>" + jqXHR + "</p>");
            $("#main").append("<p>" + status + "</p>");
            $("#main").append("<p>" + error + "</p>");
        }
    });
}