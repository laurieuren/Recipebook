package readerWriter
import recipebook._
import scala.collection.mutable.Map
import scala.collection.mutable.Buffer
import java.io.IOException
import java.io.Reader
import java.io.BufferedReader
import scala.io.Source
import java.io.PrintWriter
import java.io.FileReader
/*
 * Lukee tekstitiedostosta tarvittavan datan ohjelman käyttöön.
 * 
 * Tämän avulla ohjelma muistaa  arvot, jotka jäivät viime ajosta talteen. Kolme metodia:
 * getContent (Datan lukeminen), saveContent (Datan tallennus) ja readInstructions (ohjeiden lukemiseen). 
 * Metodikohtaisesti selitetään tarkka parseamisen syntaksi.
 */
class TextReader {
  /*
   * Lukee tekstitiedostosta datan ja antaa eteenpäin callBack-funktiolle.
   * 
   * Ainesosa-osa (deep) ts. ::INGREDIENTS::
   * 
   * Ainekset on ilmoitettu tekstitiedostossa grammoissa.
   * 
   * 1.Jokaisella ainesosalla on pakko olla nimi, joka on String-muotoa.
   * 2.Jos aineksella on määrä, ilmoitetaan se nimen jälkeen, ja sitä edeltää ','-merkki. Määrä luetaan Stringistä Doubleksi.
   * 3.Jos aineksella on allergeeni, tulee se nimen, tai määrän jälkeen, ja sitä edeltää '-' merkki. Muotoa String.
   * 4.Jos aines koostuu muista aineksista tulee se nimen, määrän, tai allergeenin jälkeen. 
   *   Sitä edeltää aina ':'-merkki ja jos koostuu monesta niin eri ainekset erotellaan viel ';' merkillä.
   *   
   *   Esim. kurkku
   *   			 maito-laktoosi
   *   			 voi,300
   *   			 kala,400-lohirasva
   *   			 juusto,400-maito:voi;maito
   *   
   *   Esim. 2 "juusto,400-maito:voi;maito".split(':)
   *   					=Array("juusto,400","voi;maito")(0).split(',')
   *   					=Array("juusto","400")(0) = aineksen nimi
   *   					jne.
   *   
   *   On yhteensä kahdeksan vaihtoehtoa, miten aineksia voi lukea:
   *   
   *   nimi	määrä	allergeeni sisältää
   * 1 	 x    x       x					x
   * 2   x    x       x					
   * 3   x    x									x
   * 4   x    x      
   * 5   x						x					x
   * 6   x						x
   * 7   x											x
   * 8   x
   * 
   *
   * Kaikki nämä vaihtoehdot käydään läpi
   */
  def getContent(inputFile: String, callback: (Buffer[Recipe], Buffer[Ingredient]) => Unit) {

    val lineReader = new BufferedReader(new FileReader(inputFile))
    var recipes = Buffer[Recipe]()
    var ingredients = Buffer[Ingredient]()

    var ingredientPart = false
    var recipePart = false
    var convertiblePart = false
    try {
      var currentLine = lineReader.readLine()
      // Luetaan riviä kunnes null
      while (currentLine != null) {
        /*
         * Aina kun saavutaan uudelle "alueelle" edellisen alueen totuusarvo laitetaan falseksi, jotta tiedetään
         * millä alueella liikutaan, eli miten tekstiä kuuluu parsia. Sitten luetaan yksi rivi, jotta päästään pois
         * alueen nimi-kohdasta.
         */
        if (currentLine.startsWith("::INGREDIENTS::")) {
          ingredientPart = true
          currentLine = lineReader.readLine
        }
        if (currentLine.startsWith("!CON")) {
          ingredientPart = false
          convertiblePart = true
          currentLine = lineReader.readLine
        }
        if (currentLine.startsWith("**KNOWNRECIPE**")) {
          recipePart = true
          ingredientPart = false
          currentLine = lineReader.readLine
        }
        if (currentLine == null) {
          recipePart = false
          ingredientPart = false
          convertiblePart = false
        }
        if (ingredientPart && currentLine != null) {
          if (!currentLine.startsWith("!")) {
            // Otetaan ensimmäiseksi filtteriksi, että aineessa on joko allergeenia tai ei (eli merkki '-' edeltää)
            if (currentLine.contains('-')) {
              val splittedLine = currentLine.split('-')
              /*
                  *  Käydään läpi kaikki kahdeksan vaihtoehtoa. Näyttää kamalalta, mutta käytännön toteutus on hyvin
                  *  yksinkertainen. Selityksessä olevan taulukon mukaan käydään läpi, ei kuitenkaan samassa järjestyksessä.
                  */
              if (splittedLine(0).contains(',') && !splittedLine(1).contains(':')) {
                val ingredientP = splittedLine(0).split(',')
                ingredients += new Ingredient(ingredientP(0), ingredientP(1).toDouble, splittedLine(1))
              } else if (splittedLine(0).contains(',') && splittedLine(1).contains(':')) {
                val allerAndConsists = splittedLine(1).split(':')
                val ingredientP = splittedLine(0).split(',')
                ingredients += new Ingredient(ingredientP(0), ingredientP(1).toDouble, allerAndConsists(0))
                var allConsistsValues = Buffer[String]()
                if (allerAndConsists(1).contains(';')) {
                  allConsistsValues = allerAndConsists(1).split(';').toBuffer
                } else {
                  allConsistsValues += allerAndConsists(1)
                }
                val ingred = ingredients.find(_.getName == ingredientP(0)).get
                ingred.changeConsists(allConsistsValues)
              } else if (splittedLine(1).contains(':')) {
                val name = splittedLine(0)
                val allerAndConsists = splittedLine(1).split(':')
                ingredients += new Ingredient(name, 0.0, allerAndConsists(0))
                var allConsistsValues = Buffer[String]()
                if (allerAndConsists(1).contains(';')) {
                  allConsistsValues = allerAndConsists(1).split(';').toBuffer
                } else {
                  allConsistsValues += allerAndConsists(1)
                }
                val ingred = ingredients.find(_.getName == name).get
                ingred.changeConsists(allConsistsValues)
              } else {
                ingredients += new Ingredient(splittedLine(0), 0.0, splittedLine(1))
              }
            } else {
              if (currentLine.contains(',') && currentLine.contains(':')) {
                val splitAtDots = currentLine.split(':')
                val nameAndAmount = splitAtDots(0).split(',')
                ingredients += new Ingredient(nameAndAmount(0), nameAndAmount(1).toDouble, "")
                var allConsistsValues = Buffer[String]()
                if (splitAtDots.contains(';')) {
                  allConsistsValues = splitAtDots(1).split(';').toBuffer
                } else {
                  allConsistsValues += splitAtDots(1)
                }
                val ingred = ingredients.find(_.getName == nameAndAmount(0)).get
                ingred.changeConsists(allConsistsValues)
              } else if (currentLine.contains(':')) {
                val splitAtDots = currentLine.split(':')
                ingredients += new Ingredient(splitAtDots(0), 0.0, "")
                var allConsistsValues = Buffer[String]()
                if (splitAtDots.contains(';')) {
                  allConsistsValues = splitAtDots(1).split(';').toBuffer
                } else {
                  allConsistsValues += splitAtDots(1)
                }
                val ingred = ingredients.find(_.getName == splitAtDots(0)).get
                ingred.changeConsists(allConsistsValues)
              } else if (currentLine.contains(',')) {
                ingredients += new Ingredient(currentLine.split(',')(0), currentLine.split(',')(1).toDouble, "")
              } else {
                ingredients += new Ingredient(currentLine, 0.0, "")
              }
            } // Jos ei ole mitään tavaraa kyseisessä osissa mennään suoraan seuraavaan.
          } else {
            ingredientPart = false
            convertiblePart = true
          }
        }
        /*
         * Tässä osiossa on ainesosan nimi, jos sille halutaan tehdä yksikönmuunnos ja samalla rivillä sen tiheys.
         * Syntaksi on aine1,tiheys
         * 
         *  esim. jauho,0.98
         *  "jauho,0.98".split(',')
         *  =Array("jauho","0.98")
         */

        if (convertiblePart && currentLine != null) {
          if (ingredients.find(_.getName == currentLine.split(',')(0)).isDefined) {
            val ingred = ingredients.find(_.getName == currentLine.split(',')(0)).get
            ingred.changeConvertible
            ingred.setDensity(currentLine.split(',')(1).toDouble)
            // Jos ei ole mitään tavaraa kyseisessä osissa mennään suoraan seuraavaan.
          } else {
            convertiblePart = false
            recipePart = true
          }
        }

        /*
         * Reseptit ovat tekstitiedostossa syntaksilla:
         * 
         * Reseptiosio alkaa aina rivillä **KNOWNRECIPES**
         * 
         * 	Reseptinnimi(aine1-määrä1,aine2-määrä2,...,aineN-määräX
         * 
         *  esim. makkarakeitto(makkara-400,vesi-1000,
         *  
         *  "makkarakeitto(makkara-400,vesi-1000,".split('(')
         *  =Array("makkarakeitto","makkara-400,vesi-100"), jonka arvo (0) on reseptin nimi
         *  Array("makkarakeitto","makkara-400,vesi-100")(1).split(',)
         *  =Array("makkara-400","vesi-100"), saadaan eri ainekset erilleen
         *  Array("makkara-400","vesi-100").foreach(_.split('-')) saadaan joka aineksen määrät ja nimen erikseen.
         */

        if (recipePart && currentLine != null) {
          val knownRecipes = Buffer[Buffer[String]]()
          val recipeName = currentLine.split('(')(0)

          var ingredientsAndAmounts = Map[Ingredient, Double]()
          val ingredients1 = currentLine.split('(')(1).split(';')

          var iName = ""
          var iAmount = 0.0
          for (each <- ingredients1) {
            val splitted = each.split('-')
            iName = splitted(0)
            if (splitted.size > 1) {
              iAmount = splitted(1).toDouble
              if (ingredients.map(_.getName).contains(iName)) {
                val ingredient = ingredients.find(_.getName == iName).get
                ingredientsAndAmounts += ingredient -> iAmount
              }
            } else if (splitted.size == 1) {
              if (ingredients.map(_.getName).contains(iName)) {
                val ingredient = ingredients.find(_.getName == iName).get
                ingredientsAndAmounts += ingredient -> 0
              }
            }
          }
          recipes += new Recipe(recipeName, ingredientsAndAmounts.toMap)
        } // Ei tehdä ylimääräistä rivinluua.
        if (currentLine != null) {
          if (!currentLine.startsWith("::INGREDIENTS") || !currentLine.startsWith("**KNOWNRECIPES**") || !currentLine.startsWith("!CONVERTIBLE!")) {
            currentLine = lineReader.readLine()
          }
        }
      }
    } finally {
      // Lopuksi lähetetään arvot Fridgelle ja suljetaan rivinlukija.
      callback(recipes, ingredients)
      lineReader.close()
    }
  }
  /*
     * SaveContent tallentaa datan tekstitiedostoon ja se on paljon yksinkertaisempi kuin getContent.
     * Muoto, johon halutaan päästä on:
     * 	
     * ::INGREDIENTPART::
     * 	ainesx...
     * 	ainesy...
     *	!CONVERTIBLE!
     *  ainesx
     *	**RECIPEPART**
     *	respetinxnimi(ainesx-määräx,ainesy-määräy...
     * 
     * Tiedot luetaan buffereista, jossa ne ovat tallessa ajon ajan.
     */
  def saveContent(ingredients: Buffer[Ingredient], recipes: Buffer[Recipe]) {
    val tiedostoNimi = "textContents/RecipeFile.txt"
    val tiedosto = new PrintWriter(tiedostoNimi)

    try {
      /*
       * Metodi, joka lukee aineksen muodostavat ainekset sen bufferista. Käytetyään ainesosaa
       * tallentaessa.
       */
      def printConsists(ingredient: Ingredient) = {
        var string = ""
        for (each <- 0 until ingredient.consists.size - 1) {
          if (ingredient.consists.size > 1) {
            string += ingredient.consists(each) + ";"
          }
        }
        string += ingredient.consists(ingredient.consists.size - 1)
        string
      }
      tiedosto.println("::INGREDIENTS::")
      for (each <- ingredients) {
        // Samat kahdeksan vaihtoehtoa ainesosan kirjoittamista varten.
        if (each.getAmount > 0 && each.getAllergen != "" && !each.consists.isEmpty) {
          tiedosto.println(each.getName + "," + each.getAmount + "-" + each.getAllergen + ":" + printConsists(each))
        } else if (each.getAmount > 0 && each.getAllergen == "" && !each.consists.isEmpty) {
          tiedosto.println(each.getName + "," + each.getAmount + ":" + printConsists(each))
        } else if (each.getAmount > 0 && each.getAllergen != "" && each.consists.isEmpty) {
          tiedosto.println(each.getName + "," + each.getAmount + "-" + each.getAllergen)
        } else if (each.getAmount > 0 && each.getAllergen == "" && each.consists.isEmpty) {
          tiedosto.println(each.getName + "," + each.getAmount)
        } else if (each.getAmount == 0 && each.getAllergen != "" && !each.consists.isEmpty) {
          tiedosto.println(each.getName + each.getAllergen + ":" + printConsists(each))
        } else if (each.getAmount == 0 && each.getAllergen != "" && each.consists.isEmpty) {
          tiedosto.println(each.getName + "-" + each.getAllergen)
        } else if (each.getAmount == 0 && each.getAllergen == "" && !each.consists.isEmpty) {
          tiedosto.println(each.getName + ":" + printConsists(each))
        } else {
          tiedosto.println(each.getName)
        }
      }
      tiedosto.println("!CONVERTIBLE!")
      // Printataan jos ykiskkömuunnettava
      val convertible = ingredients.filter(_.isConvertible)
      convertible.foreach(x => tiedosto.println(x.getName + "," + x.getDensity.get))
      //Reseptit printataan samaan syntaksiin.
      tiedosto.println("**KNOWNRECIPE**")
      /*
       * inner-funktiolla saadaan otettua reseptin ainesosien nimet ja määrät. Käytetään reseptiä lisättäessä.
       */
      def inner(Is: Buffer[Ingredient], amounts: Buffer[Double]) = {
        var string = ""
        for (each <- 0 until Is.size - 1) {
          if (each < Is.size && Is.size > 1) {
            if (amounts(each) > 0) {
              string += Is(each).getName + "-" + amounts(each).toString + ";"
            } else {
              string += Is(each).getName + ";"
            }
          } else if (amounts(each) > 0) {
            string += Is(each).getName + "-" + amounts(each).toString
          } else {
            string += Is(each).getName
          }
        }
        string
      }
      /*
       *  Viimeisellä elementille ainesosien bufferissa on viel oma hyvin kevyt metodi, koska viimeisen jälkeen
       *  ei tule enää ';'- merkkiä.
       */
      def lastOne(Is: Buffer[Ingredient], amounts: Buffer[Double]) = {
        if (amounts(amounts.size - 1) > 0) {
          Is(Is.size - 1).getName + "-" + amounts(amounts.size - 1)
        } else {
          Is(Is.size - 1).getName
        }
      } // Haetaan ainesten nimet ja määrät buffereihin.
      var Is = Buffer[Ingredient]()
      var amounts = Buffer[Double]()
      for (each <- recipes) {
        val IAA = each.getIandA
        for (each <- IAA) {
          Is += each._1
          amounts += each._2
        }
        // Kirjoitetaan tekstitiedostoon reseptit ja käytetään metodeita inner ja lastOne.
        val name = each.getName
        tiedosto.println(name + "(" + inner(Is, amounts) + lastOne(Is, amounts))
        Is = Buffer[Ingredient]()
        amounts = Buffer[Double]()
      }
    } finally {
      tiedosto.close()
    }
  }
  /*
   * Metodi, joka lukee kutsuttaessa tiedoston "Ohjeet" ja näyttää sen GUIssa.
   */
  def readInstructions(inputFile: String, call: String => Unit) = {
    var instructions = ""
    val lineReader = new BufferedReader(new FileReader(inputFile))
    var currentLine = lineReader.readLine()
    try {
      while (currentLine != null) {
        instructions += currentLine + "\n\n"
        currentLine = lineReader.readLine
      }
    } finally {
      call(instructions)
      lineReader.close()
    }
  }

}