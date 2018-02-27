var ajaxMethod = "POST";
var ajaxId = "";
var bild;
var submit = false;

$(document).ready(function()
{
    //ingrediens-knapp
    document.getElementById("addIngredient").addEventListener("click", function()
    {
        addIngredientInput("", "");
    });
    
    //skicka-knapp
    document.getElementById("postRecipe").addEventListener("click", addRecipe);
    
    //fixa kategoridropdownlistan
    getCategories();
    
    //om urlen har ett ID ska receptet hämtas och alla fält ska fyllas i
    var url = window.location.href;
    var id = url.substring(url.indexOf("=") + 1);
    if(0 < id)
    {
        ajaxMethod = "PUT";
        ajaxId = "/" + id;
        getData(id, fillForm);
    }
    addIngredientInput("", "");
});

function addIngredientInput(mängd, ingrediens)
{
    var output = "<li><input type='text' value='" + mängd + "' class='mängd' placeholder='mängd'><input type='text' value='" + ingrediens + "' class='ingrediens' placeholder='ingrediens'></li>";
    $("#ingredienser").append(output);
}

function addRecipe()
{
    //om bilden inte är färdig än sätts submit till true, annars läggs receptet till direkt
    if(bild === "")
    {
        submit = true;
    }
    else
    {
        var titel = $("#titel").val();
        var beskrivning = $("#beskrivning").val();
        var instruktioner = $("#instruktioner").val();
        var kategori = parseInt($("#kategorier").val());
        var ingrediensInput = $(".ingrediens").map(function()
        {
            return this.value;
        }).toArray();
        var mängdInput = $(".mängd").map(function()
        {
            return this.value;
        }).toArray();

        var ingredienser = [];
        for(var i = 0; i < ingrediensInput.length; i++)
        {
            var ingrediens = ingrediensInput[i];
            var mängd = mängdInput[i];

            if(ingrediens)
            {
                ingredienser.push({"mängd":mängd,"ingrediens":ingrediens});
            }
        }

        var recept = {"titel":titel, "beskrivning":beskrivning,
                        "instruktioner":instruktioner,"kategori":kategori,
                        "ingredienser":ingredienser, "bild": bild};

        postRecipe(recept);
    }
}

//omvandlar en bildfil till base64
function encodeImageFileAsURL(element)
{
    var file = element.files[0];
    var reader = new FileReader();
    reader.onloadend = function()
    {
        bild = reader.result;
        console.log("image finished");
        //om användaren redan har tryckt på knappen ska addRecipe köras när bilden är färdig
        if(submit)
        {
            addRecipe();
        }
    };
    reader.readAsDataURL(file);
}

//används för att fylla i alla fält med värden från ett hämtat recept.
function fillForm(recept)
{
    console.log(recept);
    $("#titel").val(recept.titel);
    $("#beskrivning").val(recept.beskrivning);
    $("#instruktioner").val(recept.instruktioner);
    for(var i = 0; i < recept.ingredienser.length; i++)
    {
        addIngredientInput(recept.ingredienser[i].mängd, recept.ingredienser[i].ingrediens);
    }
}

function getCategories()
{
    //hämta kategorier
    $.ajax
    ({
        type: "GET",
        url: "http://94.46.140.3:8080/sebastian_recipe_backend/api/categories",
        success: function(data)
        {
            buildCategories(data);
        }
    });
}

function buildCategories(data)
{
    for(var i = 0; i < data.length; i++)
    {
        var category = "<option value='" + data[i].id + "'>" + data[i].namn + "</option>";
        $("#kategorier").append(category);
    }
}

function postRecipe(recipe)
{
    var session = getSession();
    if(session)
    {
        console.log(recipe);
        $.ajax
        ({
            type: ajaxMethod,
            url: "http://94.46.140.3:8080/sebastian_recipe_backend/api/Recipe" + ajaxId,
            headers:
            {
                "Authorization": "Basic " + btoa(session.username + ":" + session.password),
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            async: false,
            data: JSON.stringify(recipe),
            success: function()
            {
                window.location.replace("index.html");
            }
        });
    }
    else
    {
        window.location.replace("login.html");
    }
    
}