$(document).ready(function()
{
    var session = getSession();
    if(session)
    {
        $("#login").append("Hej " + session.username);
        //knappen ska inte göra något om man är inloggad
    }
    else
    {
        $("#login").append("Logga in");
        document.getElementById("loginlink").setAttribute("href", "login.html");
    }
});