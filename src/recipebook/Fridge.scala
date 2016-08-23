package recipebook
import scala.collection.mutable.Buffer
import scala.collection.mutable.Map
import java.io.PrintWriter
import readerWriter.TextReader
import collection.mutable.{ HashMap, MultiMap, Set }
/*
 * Kuvaa älykästä jääkaappia, joka osaa laskea sen sisältämillä ainesosilla erilaisia toimituksia
 * ja muodostaa niistä reseptejä.
 */
class Fridge {

  /*
    * Yksityisiä muuttujia. Fridgestä on pääsy Yksikönmuuntajaan sekä Tekstinlukutiedostoon, joten se on ikään
    * kuin kaiken funktionaalisuuden keskus tässä kokonaisuudessa. 
    */

  // Yksikönmuuntaja
  private val uc = new UnitChanger
  //tiedostonlukija
  private val tr = new TextReader()

  /*
   * Tietorakenteet joihin tallennetaan tekstitiedosto luettua ainesosat, reseptit, allergeenit ja ohjeet.
   */
  private var ingredients = Buffer[Ingredient]()
  private var recipes = Buffer[Recipe]()
  private var allergens = Buffer[String]()
  private var instructions = ""

  def getIngredients = ingredients
  def getRecipes = recipes
  def getAllergens = allergens
  def getInstructions = instructions

  /*
   * Aina ohjelman aluksi kutsutaan Textreaderin metodeita readInstructions ja getContent. Ensimmäinen
   * lukee tekstitedostosta graafisen käyttöliittymän ohjeet ja toinen lukee ainesosien ja reseptien tiedot.
   */
  tr.readInstructions("textContents/Ohjeet.txt", callBackInstructions)
  tr.getContent("textContents/RecipeFile.txt", callbackFunktio)
  ingredients.foreach(x => println(x.getName + " : " + x.getAmount + " : " + x.getAllergen))
  recipes.foreach(x => println(x.getName))
 
  /*
   * Toimii vastaanottimena Textreaderin getContent-metodille.
   */
  def callbackFunktio(reseptit: Buffer[Recipe], aineet: Buffer[Ingredient]): Unit = {
    this.recipes = reseptit
    this.ingredients = aineet
  }
  
  /*
   * Vastaanotin TextReaderin getInstructions metodille.
   */
  def callBackInstructions(instructions: String): Unit = {
    this.instructions = instructions
  }
 
  /*
   * Metodi toimii väliportaana GUIn ja Textreaderin välillä. Tallentaa tiedot tekstitiedostoon.
   */
  def saveToFile: Unit = tr.saveContent(ingredients, recipes)

  /*
   * Kaksi metodia reseptien ja oman jääkaapin vertailuun. Palauttaa totuusarvon.
   * Ensimmäisenä oleva tarkistaa onko kaikki reseptiin tarvittavat raaka-aineet kaapissa
   * vertailemalla reseptin tarvitsemia- sekä ingredients-bufferissa olevia arvoja.
   */
  def hasAllIngredients(recipe: Recipe): Boolean = {
    var hasAll = false
    var enough = true
    // Tarkistaa ensin, onko kaikki raaka-aineet yleensäkin kaapissa
    if (recipe.getIandA.keys.forall(x => ingredients.contains(x))) hasAll = true

    // Sitten tarkistaa erikseen yksikkömuunnettaville ja "normaaleille" aineksille, onko kaikkia määriä tarpeeksi
    for (each <- recipe.getIandA) {
      if (each._1.isConvertible) {
        if (uc.convertWOIngred(each._2, each._1.getDensity.get) > convertibleIngredAmount(each._1.getName).get) {
          enough = false
        }
      } else if (each._2 > howMuchIngredient(each._1)) {
        enough = false
      }
    }
    // Lopuksi tarkistaa onko "hasAll" ja "enough" muuttujat tosia. Jos on, niin kaikki reseptin ainesosat on kaapissa.
    if (hasAll && enough) {
      true
    } else {
      false
    }
  }
  /*
   * Tarkistaa, onko reseptiin ainakin jotain ainesosaa kaapissa. Eli käytännössä yksiainesosa riittää.
   */
  def hasPartiallyIngredients(recipe: Recipe): Boolean = {
    var hasAll = false
    var enough = false

    // Tarkistaa ensin löytyykö reseptiin kaikki raaka-aineet kaapista.
    if (hasAllIngredients(recipe)) hasAll = true

    // Tarkistaa sitten, onko tarvittavaa määrää raaka-ainetta kaapissa jonkin verran edes yhteen reseptin raaka-aineeseen.
    var ingredientCounter = 0
    var wasBigger = false
    for (each <- recipe.getIandA) {
      for (every <- ingredients) {
        if (every == each._1) {
          if (every.isConvertible) {
            if (convertibleIngredAmount(every.getName).get >= uc.convertWOIngred(each._2, every.getDensity.get) ||
              every.getAmount == 0) {
              wasBigger = true
            }
          } else {
            if (every.getAmount >= each._2) wasBigger = true
          }
          if (wasBigger) ingredientCounter += 1
        }
      }
    }
    // Finally checks, if there was at least one ingredient and that the fridge didnt contain enough of all ingredients.
    if (ingredientCounter > 0 && !hasAll) true
    else false
  }

  /*
   * Ottaa parametrikseen kokonaisluvun x, ja palauttaa kaikki reseptit, joista puuttuu tasan x-määrä jotain
   * ainesosia. 
   * 
   * Esim. Kaapissa on ainekset makkara ja peruna ja kumpaakin 100g. Makkarakeitto vaatii näitä kahta enemmän ja
   * lisäksi muita aineksia, jotka löytyvät jo kaapista. Kun kutsutaan metodia arvolla 2. Palautusarvo sisältää ainakin
   * reseptin "makkarakeitto".
   */
  def howManyIngredientsMissing(howMany: Int): Map[Recipe, Int] = {
    var theseRecipes = Map[Recipe, Int]()
    var amount = 0
    for (each <- recipes) {

      for (every <- each.getIngredientsPls) {
        if (every.isConvertible) {
          val amountInL = convertibleIngredAmount(every.getName).get
          if (amountInL < uc.convertWOIngred(each.getIandA(every), every.getDensity.get)) {
            amount += 1
          }
        } else if (every.getAmount < each.getIandA(every)) {
          amount += 1
        }
      }
      theseRecipes += each -> amount
      amount = 0
    }
    theseRecipes
  }

  /*
   * Seuraavat kaksi metodia käsittelevät ainesosien "consistOf"-ominaisuutta, ensimmäinen palauttaa
   * totuusarvon, toinen Bufferin. eli sitä miten ne voivat koostua toisista aineksista.
   *
   *
   * Kaksi metodia tekee tarkistuksia, josko kaapista löytyy ainekset tämän ainekseen kokoamiseen, mikäli kys. ainesta ei itsessään
   * ole jo kaapissa. Toinen metodeista palauttaa vielä ainekset, josta tämä kys. aines voidaan valmistaa.
   */

  def ifIngredientCanBeMade(ingredient: Ingredient): Boolean = {
    val containedThis = Buffer[Ingredient]()
    if (!ingredient.consists.isEmpty) {
      // Tarkistetaan, josko ainesosan consistOf-bufferin kaikki ainekset löytyvät ingredients-bufferista.
      for (each <- ingredient.consists) {
        for (every <- ingredients) {
          if (every.getName == each) {
            if (ingredients.contains(every))
              containedThis += every
          }
        }
      }
      // tässä verrataan alkuperäsistä consistsOf-bufferia kokoa ja uuden bufferin kokoa, joka sisältää ainesosia.
      if (containedThis.size == ingredient.consists.size) true
      else false
    } else {
      false
    }
  }
  /*
    * Käyttää edellisen metodin palauttamaa totuusarvoa hyväkseen, ja tarkistaa reseptistä kaikki ainesosat, jotka
    * pystytään kokoamaan sen bufferin consistOf sisältämistä aineksista. Palauttaa Bufferin.
    */
  def ingredientsThatCanBeMade(recipe: Recipe): Buffer[Ingredient] = {
    var toBeChecked = Buffer[Ingredient]()
    // Adds amounts of ingredients in fridge to a buffer
    var wasBigger = false
    /*
     * Tarkistaa kaikille ainesosille, onko sitä vähemmän kuin sille reseptissä sovittu määrä. Jos ei, 
     * lisätään se bufferiin, jonka arvot syötetään parametriksi ylemmälle ifIngredientsCanBeMade-metodille.
     */
    for (each <- recipe.getIandA) {
      for (every <- ingredients) {
        if (every == each._1) {
          if (every.isConvertible) {
            if (this.convertibleIngredAmount(every.getName).get <=
              uc.convertWOIngred(each._2, every.getDensity.get)) {
              wasBigger = true
            }
          } else {
            if (every.getAmount <= each._2) wasBigger = true
          }
          if (wasBigger) {
            toBeChecked += every
          }
        }
      }
    }
    //canMakeFromConsists-bufferiin siis tulee vain ainesosat jonka kaikki consistsOf-bufferin aineet löytyvät kaapista.
    var canMakeFromConsists = Buffer[Ingredient]()
    for (each <- toBeChecked) {
      if (ifIngredientCanBeMade(each)) canMakeFromConsists += each
    }
    canMakeFromConsists
  }
  /*
   * Palauttaa kaikki reseptit, jotka sisältävät jotain tiettyä ainesosaa.
   */
  def allThatContainIngredient(recipes: Buffer[Recipe], ingredient: Ingredient): Buffer[Recipe] = {
    recipes.filter(x => x.getIngredientsPls.toBuffer.contains(ingredient))
  }
  /*
   * Palauttaa kaikki reseptit, joissa ei ole jotain titettyä allergeenia.
   */
  def allThatDontContainAllergen(recipes: Buffer[Recipe], allergen: String): Buffer[Recipe] = {
    var newRecipes = Buffer[Recipe]()
    for (each <- recipes) {
      if (each.getIngredientsPls.forall(x => x.getAllergen != allergen)) newRecipes += each
    }
    newRecipes
  }

  /*
   * Palauttaa kaikki resepti, joihin on kaikki ainekset.
   */
  def thatCanBeMade: Buffer[Recipe] = {
    var ableToMake: Buffer[Recipe] = Buffer()
    for (each <- getRecipes) {
      if (hasAllIngredients(each)) {
        ableToMake += each
      }
    }
    ableToMake
  }

  /*
   * Palauttaa kaikki reseptit, jotka pystytään osittain tekemään.
   */
  def partiallyAbleToMake: Buffer[Recipe] = {
    var ableToMake: Buffer[Recipe] = Buffer()
    for (each <- recipes) {
      if (hasPartiallyIngredients(each)) {
        ableToMake += each
      }
    }
    ableToMake
  }

  /*
   * Tarkistaa löytyykö kaapista tietyn nimen omaavaa reseptiä. Palauttaa totuusarvon.
   */
  def hasIngredient(ingredientName: String): Boolean = ingredients.find(_.getName == ingredientName.toLowerCase).isDefined

  /*
   * Etsii kaapista tietyn nimisen reseptin, jos tälläinen resepti on yleensäkin olemassa.
   */
  def findIngredient(ingredientName: String): Option[Ingredient] = {
    if (hasIngredient(ingredientName)) ingredients.find(_.getName == ingredientName.toLowerCase)
    else None
  }

  /*
   * Tarkistaa paljonko kaapissa on tiettyä ainesosaa grammoissa. Palauttaa summan, tai 0.0.
   */
  def howMuchIngredient(ingredient: Ingredient): Double = {
    var amount = 0.0
    for (each <- ingredients) {
      if (each == ingredient) {
        amount = each.getAmount
      }
    }
    amount
  }
  /*
   * Tarkistaa montako ml kaapissa on yksikkömuunnettavaa ainesosaa. Eli siis metodi käyttää hyväkseen
   * UnitChanger-luokan metodia. Palauttaa Option[Double]:n
   */
  def convertibleIngredAmount(ingredientName: String): Option[Double] = {
    if (hasIngredient(ingredientName)) {
      val ingred = findIngredient(ingredientName).get
      if (ingred.isConvertible) Some(uc.fromGramsToDl(ingred, ingred.getDensity.get))
      else None
    } else None
  }
  /*
   * Lisää käyttäjän syötteiden mukaan kaappiin uuden ainesosan. Tarkistaa ensin, ettei samaa reseptiä ole jo olemassa.
   */
  def addIngredient(ingredientName: String, amount: Double, allergen: String, consistsOf1: Buffer[String]): Unit = {
    if (ingredients.forall(_.getName != ingredientName.toLowerCase)) {
      val ingredient = new Ingredient(ingredientName, amount, allergen)
      ingredients += ingredient
      ingredient.changeConsists(consistsOf1)
    }
  }
  /*
   * Lisää tietoihin uuden reseptin, jos sen nimistä reseptiä ei vielä ole, ja kaikki sen ainesosat löytyvät kaapista.
   */
  def addRecipe(recipeName: String, ingredientsAndAmounts: Map[Ingredient, Double]): Unit = {
    var newIaA = Map[Ingredient, Double]()
    if (recipes.forall(_.getName != recipeName.toLowerCase) && ingredientsAndAmounts.keys.forall(ingredients.contains(_))) {
      for (each <- ingredientsAndAmounts.keys) {
        if (ingredients.contains(each)) {
          newIaA += ingredients(0) -> ingredientsAndAmounts(each)
        }
      }
    }
    recipes += new Recipe(recipeName, ingredientsAndAmounts.toMap)
  }
  /*
   * Lisää jo olemassaolevaa ainesoaa halutun määrän "amount", jos tällainen ainesosa löytyy kaapista. Palauttaa
   * Option[Double]n
   */
  def addExisting(ingredient: String, amount: Double): Option[Double] = {
    if (hasIngredient(ingredient)) {
      val ingred = ingredients.find(_.getName == ingredient.toLowerCase).get
      ingred.changeAmount(amount)
      Some(amount)
    } else None

  }
  /*
   * Metodin avulla voi käyttää ainesosaa jos ainesosa löytyy kaapista ja määrä - vähennys >= 0. 
   * Palauttaa vähennyksen määrän, 0.0, tai -1.
   */
  def useIngredient(ingredient: String, amount: Double): Double = {
    var bool = false
    if (hasIngredient(ingredient)) {
      val ingredient1 = findIngredient(ingredient).get
      var amount1 = ingredient1.getAmount
      if (amount1 - amount >= 0) {
        ingredient1.changeAmount(-amount)
        amount1
      } else {
        0.0
      }
    } else {
      -1.0
    }
  }
  /*
   * Metodi tarkistaa onko kaikkia reseptin tarvitsemia ainesosia tarpeeksi kaapissa. Palautta totuusarvon. 
   */
  def canMakeRecipe(recipe: Recipe): Boolean = {
    if (!recipes.contains(recipe)) {
    var bool = true
    val recipesIngreds = recipe.getIandA
    for (each <- recipesIngreds) {
      if (each._2 > each._1.getAmount) {
        bool = false
      }
    }
    if (bool) true
    else false
  } else {
    false}
  }
  
  /*
   * Metodin avulla voi käyttää kaikki reseptin vaatimat ainesosat, eli siis valmistaa ruuan. Kutsuu jokaiselle
   * ainesosalle useIngredientMetodia.
   */
  def makeRecipe(recipe: Recipe): Unit = {
    if (canMakeRecipe(recipe)) {
      recipe.getIngredientsPls.foreach(x => useIngredient(x.getName, recipe.getIandA(x)))
    }
  }
  /*
   * Jos tämänniminen ainesosa löytyy kaapista, poistetaan se.
   */
  def deleteIngredient(ingredient: String): Unit = {
    if (hasIngredient(ingredient)) {
      ingredients -= findIngredient(ingredient).get
    }
  }
  /*
   * Poistaa reseptin tiedoista, jos sen niminen resepti yleensäkin löytyy.
   */
  def deleteRecipe(recipe: String): Unit = {
    if (recipes.find(_.getName == recipe).isDefined) {
      recipes -= recipes.find(_.getName == recipe).get
    }
  }
}