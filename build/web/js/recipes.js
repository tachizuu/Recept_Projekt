$(document).ready(function()
{
    getData("all", renderData);
});

function renderData(data)
{
    for(var i = 0; i < data.length; i++)
    {
        var recept = data[i];
        var output = "<a href='recipe.html?id=" + recept.id + "'><div class='recipe'><div class='col-lg-3 col-md-3 col-sm-3'>"
            +   "<img src='" + recept.bild + "' alt='Bild på " + recept.titel + "'></div><div class='col-lg-9 col-md-9 col-sm-9'><h3>" + recept.titel + "</h3><h5>" + recept.kategori + "</h5>"
            +   "<p>" + recept.beskrivning + "</p></div></div></a>";
    
        $("#recipes").append(output);
    }
}