$(document).ready(function()
{
    $("#login").on("click", function()
    {
        var username = $("#username").val();
        var password = $("#password").val();
        addUser(username, password);
    });
});

function addUser(username, password)
{
    $.ajax
    ({
        type: "GET",
        url: "http://94.46.140.3:8080/sebastian_recipe_backend/api/login",
        headers:
        {
        "Authorization": "Basic " + btoa(username + ":" + password)
        },
        async: false,
        success: function()
        {
            setSession(username, password);
            window.location.replace("index.html");
        }
    });
}