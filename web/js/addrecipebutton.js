$(document).ready(function()
{
    var session = getSession();
    if(session)
    {
        document.getElementById("addlink").setAttribute("href", "addrecipe.html");
    }
    else
    {
        document.getElementById("addlink").setAttribute("href", "login.html");
    }
});