function getSession()
{
    var currentTime = (new Date()).getTime();
    var session = JSON.parse(localStorage.getItem("session"));
    if(session !== null)
    {
        if(session.expires > currentTime)
        {
            return session;
        }
    }
    return false;
}

function setSession(username, password)
{
    var currentDate = new Date();
    var sessionExpiration = currentDate.getTime() + (1000*60*5);    //5 minuter = 5 * 60 * 1000 ms
    
    var sessionObject = {"expires":sessionExpiration, "username":username, "password":password};
    
    localStorage.setItem("session", JSON.stringify(sessionObject));
}