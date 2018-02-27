$(document).ready(function()
{
    var url = window.location.href;
    var id = url.substring(url.indexOf("=") + 1, url.length);
    getData(id, renderData);
});

function renderData(data)
{
    $("#bild").attr("src", data.bild);
    $("#bild").attr("alt", "Bild på " + data.titel);
    $("#titel").append(data.titel);
    $("#kategori").append(data.kategori);
    $("#beskrivning").append(data.beskrivning);
    $("#instruktioner").append(data.instruktioner);
    var ingredienser = data.ingredienser;
    for(var i = 0; i < ingredienser.length; i++)
    {
        var ingrediens = "<li>" + ingredienser[i].mängd + " " + ingredienser[i].ingrediens + "</li>";
        $("#ingredienser").append(ingrediens);
    }
}