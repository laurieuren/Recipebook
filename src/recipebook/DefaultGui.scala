package recipebook
import readerWriter._
import scala.swing._
import scala.swing.event._
import java.awt.Dimension
import java.awt.BorderLayout
import java.awt.Desktop
import scala.collection.mutable.Buffer
import GridBagPanel._
import java.awt.Color
import scala.swing.event._
import Swing._
import ListView._

/*
 * Ohjelman graafinen käyttöliittymä ja siihen kuuluvat reaktiot.
 * 
 * DefaultGuissa on MainFrame, frame johon kuuluu erilaisia komponentteja, kuten paneeleja ym. Ohjelma on rakennettu niin,
 * että käyttäjä pystyy pelkästään GUI:ta hallinnoimalla käyttämään ja toimimaan ohjelmassa.
 */
object DefaultGui extends SimpleSwingApplication {

  var fridge = new Fridge

  val recipes = fridge.getRecipes
  val ingredients = fridge.getIngredients

  /*
   * Pääikkuna, jossa sulkemismetodi ylikirjoitettu. Siihen on lisätty tietojen kirjoittaminen tekstitiedostoon.
   */
  private var frame = new MainFrame {
    title = "Reseptikirja"
    preferredSize = new Dimension(1200, 820)
    minimumSize = new Dimension(600, 450)
    override def closeOperation() {
      fridge.saveToFile
      super.closeOperation()
    }
  }

  /*
   * Seuraavana vuorossa on alimpaan BorderPaneliin sijoittuvat komponentit. Ensimmäisenä Ainesosan lisäys, toisena reseptin.
   * 
   * Tässä borderPanelissa tapahtuu ainesosien ja reseptien lisäykset tietokantaan,
   * joten komponentit ovat pääasiassa Textfieldejä, muutamaa nappia ja yhtä checkBoxia lukuunottamatta.
   */

  // Ensin tekstikentät
  val ingredientName = new TextField("", 12)
  this.listenTo(ingredientName)
  val iNameL = new Label("Aineksen nimi:")

  val ingredientAmount = new TextField("", 8)
  this.listenTo(ingredientAmount)
  val iAmountL = new Label("Aineksen määrä:")

  val allergenName = new TextField("", 15)
  this.listenTo(allergenName)

  // Mistä aineista ainesosa mahdollisesti koostuu syötetään syntaksilla: aines1,aines2,...,ainesN
  val consists = new TextField("", 15)
  this.listenTo(consists)

  //Jos halutaan, että aineen ysikkö muunnetaan tilavuudeksi, niin tähän syötetään tilavuus desimaalina.
  val density = new TextField("", 8)
  density.name_=("dens")
  this.listenTo(density)

  val addIngredient = new Button("Lisää aines")
  addIngredient.name_=("addI")
  this.listenTo(addIngredient)

  //Checkbox, josko halutaan aineesta yksikkömuunnettava.
  val convertible = new CheckBox
  this.listenTo(convertible)
  //Labelit
  val aNameL = new Label("Allergeenin nimi:")
  val cNameL = new Label("Sisältää:")
  val densL = new Label("Tiheys: ")
  val convertLabel = new Label("Yksikönmuunnos:")
  /*
   *  Tässä paneelissa on aineksen lisäämiseen löytyvät arvot.
   *  Paneeli on sisemmän BorderPanelin sisällä pohjoisempana, joka on uloimman Borderpanelin sisällä eteläisimpän.
   */
  val addIPanel = new BoxPanel(Orientation.Horizontal) {
    background = new Color(201, 230, 255)
    border = Swing.EmptyBorder(1)
    contents += iNameL
    contents += ingredientName
    contents += iAmountL
    contents += ingredientAmount
    contents += aNameL
    contents += allergenName
    contents += cNameL
    contents += consists
    contents += densL
    contents += density
    contents += convertLabel
    contents += convertible
    contents += addIngredient
  }
  this.listenTo(addIPanel)

  /*
   * Reseptin lisäys-osioon menevää tavaraa. Sijaitsee aineksen lisäys-osion alapuolella. Layout täysin samalla logiikalla
   * muuten. Sisältää vähemmän tavaraa kuin aineksen lisäämispaneeli, koska respetin lisääminen yksinkertaisempaa.
   */
  // Syötetään reseptin nimi
  val recipeName = new TextField("", 15)
  this.listenTo(recipeName)
  val rNameL = new Label("Reseptin nimi:")
  // Syötetään reseptin ainekset ja määrä syntaksilla: aines1-maara1,aines2-maara2,...,ainesN-maaraX
  val ingredientsAmounts = new TextField("", 10)
  this.listenTo(ingredientAmount)

  val addRecipe = new Button("Lisää resepti")
  addRecipe.name_=("addR")
  this.listenTo(addRecipe)

  val isAsL = new Label("Tarvittavat ainekset ja määrät:")
  // Tässä paneelissa on siis reseptin lisäämiseen vaaditut komponentit.
  val addRPanel = new BoxPanel(Orientation.Horizontal) {
    background = new Color(201, 230, 255)
    border = Swing.EmptyBorder(1)
    contents += rNameL
    contents += recipeName
    contents += isAsL
    contents += ingredientsAmounts
    contents += addRecipe
  }
  // Tämä BorderPanel sisältää kaksi edellistä BoxPanelia ja on sijoitettu "pää"BorderPaneliin alimmas.
  val IandRBorderP = new BorderPanel {
    layout(addIPanel) = BorderPanel.Position.North
    layout(addRPanel) = BorderPanel.Position.Center
  }
  this.listenTo(IandRBorderP)

  /*
   *  The menuBar on Uloimmassa borderPanelissa ylimpänä. Sisältää nappeja ja tekstikenttiä, joiden avulla voi hakea resepetejä
   *  sekä aineksia, ja rajoittaa hakua esim allergeenin mukaan.
   *
   *  Ensinnä on tekstikentät ja nappulat, jonka jälkeen ne sijoitetaan menuun.
   */
  val searchBar = new TextField("", 15)
  this.listenTo(searchBar)
  val findARecipe = new Label("Etsi Resepti")

  val allergenBar = new TextField("", 15)
  this.listenTo(allergenBar)
  val withoutAlergen = new Label("Ilman allergenia")

  val aineksenMukaan = new TextField("", 15)
  this.listenTo(aineksenMukaan)
  val ainesLabel = new Label("Aineksen mukaan")

  val hae = new Button("Etsi Resepti")
  hae.name_=("hae")
  this.listenTo(hae)

  val allergenButton = new Button("AllergeninMukaan")
  allergenButton.name_=("aller")
  this.listenTo(allergenButton)

  val haeAineksenMukaan = new Button("AineksenMukaan")
  haeAineksenMukaan.name_=("aines")
  this.listenTo(haeAineksenMukaan)
  // Menubar on ylimpänä BorderPanelissa oleva paneeli, joka sisältää Reseptien ja ainesosien hakuun liittyviä komponentteja.
  val menuBar = new BoxPanel(Orientation.Horizontal) {
    border = Swing.LineBorder(java.awt.Color.black, 1)
    background = new Color(201, 230, 255)
    contents += findARecipe
    contents += searchBar
    contents += hae
    contents += withoutAlergen
    contents += allergenBar
    contents += allergenButton
    contents += ainesLabel
    contents += aineksenMukaan
    contents += haeAineksenMukaan
  }

  /*
   * Metodi fillRecipeBar täyttää Vasemman osan BorderPanelista resepteille omilla napeilla.
   * 
   * Se käy läpi recipes bufferia ja lisäämällä jokaisesta reseptistä oman napin paneeliin.
   * Aina kun muutoksia tapahtuu reseptien tiloihin, kutsutaan tätä metodia uudestaan ja mahdollisesti
   * päivitetään se. Napit sijoitetaan FlowPaneleihin, joten näkymä mukautuu sen mukaan, kuinka monta nappia
   * paneeliin tulee. Metodi on samankaltainen kuin Ohjelmointistudio 1-projektin 4. kierroksen luokan RSS Browser metodi.
   */
  def fillRecipeBar: BoxPanel = {
    val buttonBoxPanel = new BoxPanel(Orientation.Vertical) {
      background = new Color(201, 230, 255)
    }
    val ingredientPanel = new FlowPanel {
      minimumSize_=(new Dimension(100, 20))
      background = new Color(201, 230, 255)
    }
    for (each <- recipes) {
      val recipeButton = new Button(each.getName)
      recipeButton.name_=("rcp")

      val recipePanel = new FlowPanel {
        minimumSize_=(new Dimension(100, 20))
        background = new Color(201, 230, 255)
        contents += recipeButton
      }
      this.listenTo(recipeButton)
      buttonBoxPanel.contents += ingredientPanel
      buttonBoxPanel.contents += recipePanel
    }
    buttonBoxPanel
  }
  val returnedBoxPanel = this.fillRecipeBar
  // Lisää boxPanelin ScrollPaneen, jotta jos nappeja tulee enemmän kuin guin koko, pystyy scrollaamaan.
  val scrollForButtonBoxPanel = new ScrollPane() {
    border = Swing.EmptyBorder(15, 15, 15, 15)
    background = new Color(201, 230, 255)
    contents = returnedBoxPanel
  }

  /*
   * Seuraavaksi Borderpaneelin oikeanpuolimmainen osa.
   * 
   * Sisältää monia eri komponentteja, jotka vaihtelevat hyvin paljon. Selitetään tarkemmin kohdalla.
   */

  //Labeleita.
  val ingredi = new Label("Ainesosa:")
  val ingredi1 = new Label("Ainesosa:")
  val recipe = new Label("Resepti:")
  val recipe1 = new Label("Resepti:")
  val amount = new Label("Määrä")
  val kpl = new Label("kpl")
  val poista = new Label("POISTA AINESOSIA JA RESEPTEJÄ")

  //Painamalla recipePagelle ilmestyy ohjeet ohjelman käyttöä varten.
  val instructions = new Button("Ohjeet")
  instructions.name_=("Ohjeet")
  this.listenTo(instructions)

  //Nappia painamalla recipePage näkymään tulee kaikki ainekset ja määrät.
  val ingredientButton = new Button("Ainekset")
  ingredientButton.name_=("Ingredients")
  this.listenTo(ingredientButton)

  //Nappia painamalla kaikki reseptit jotka pystytään osittain tekemään ilmestyvät recipePagelle.
  val partlyAble = new Button("Vajavaiset")
  partlyAble.name_=("Vajavaiset")
  this.listenTo(partlyAble)

  //Täysin valmistettavissa olevat reseptit ilmestyvät recipePagelle.
  val completelyAble = new Button("Kokonaan")
  completelyAble.name_=("Kokonaan")
  this.listenTo(completelyAble)

  //Pystytään palauttamaan reseptit, joista puuttuu täsmälleen x määrä ainesosia...
  val exactlyMissing = new Button("Puuttuu Täsmälleen")
  exactlyMissing.name_=("Täsmälleen")
  this.listenTo(exactlyMissing)

  //...Ja kuinka monta puuttuu, syötetään tähän tekstikenttään.
  val howManyMissing = new TextField("", 2)
  howManyMissing.name_=("Puuttuu")
  this.listenTo(howManyMissing)

  //Käytetään tietty ainesosa, eli vähennetän määrä, joka syötetään eri tekstikenttään...
  val useIngredient = new Button("Käytä ainesosa")
  useIngredient.name_=("useI")
  this.listenTo(useIngredient)

  //Päinvastoin lisää tietyn määrän johonkin ainesosaan...
  val addExisting = new Button("Lisää ainesosaa")
  addExisting.name_=("add")
  this.listenTo(addExisting)

  //..Syötetään, mitä ainetta halutaan lisätä tai vähentää...
  val whichIngredient = new TextField("", 18)
  whichIngredient.name_=("whichI")
  this.listenTo(whichIngredient)

  //...ja tänne kuinka paljon. Syötetään desimaalina, tai kokonaislukuna.
  val howMuchIngredient = new TextField("", 5)
  howMuchIngredient.name_=("muchI")
  this.listenTo(howMuchIngredient)
  //gramma-label
  val g = new Label("g")

  //Valmista resepti-nappi, eli käytä kaikki reseptin tarvitsemat ainesosat.
  val makeRecipe = new Button("valmista")
  makeRecipe.name_=("makeRcp")
  this.listenTo(makeRecipe)

  //Ja mikä resepti on kyseessä.
  val makeRcpField = new TextField("", 15)
  makeRcpField.name_=("makeRcp")
  this.listenTo(makeRcpField)

  //Mitä ainetta halutaan poistaa
  val deleteIField = new TextField("", 18)
  this.listenTo(deleteIField)

  // Mikä resepti halutaan poistaa tiedoista.
  val deleteRField = new TextField("", 18)
  this.listenTo(deleteIField)

  //Poistaa halutun aineen
  val deleteIButton = new Button("Poista Ainesosa")
  deleteIButton.name_=("poistaI")
  this.listenTo(deleteIButton)

  val deleteRButton = new Button("Poista Resepti")
  deleteRButton.name_=("poistaR")
  this.listenTo(deleteRButton)

  //Sisältää ylimmät toiminnallisuusnapit
  val upperButtons = new FlowPanel {
    maximumSize = new Dimension(259, 150)
    background = new Color(201, 230, 255)
    border = Swing.LineBorder(java.awt.Color.BLACK)
    contents += instructions
    contents += ingredientButton
    contents += partlyAble
    contents += completelyAble
  }
  // Sisältää komponentit, joita tarvitaan kun määritetään reseptejä joista puuttuu täsmälleen x määrä aineksia.
  val exactlyThisMuch = new FlowPanel {
    maximumSize = new Dimension(259, 100)
    background = new Color(201, 230, 255)
    border = Swing.LineBorder(java.awt.Color.BLACK)
    contents += howManyMissing
    contents += kpl
    contents += exactlyMissing
  }
  // Sisältää aineksen käyttämiseen liittyviä komponentteja.
  val usePanel = new FlowPanel {
    maximumSize = new Dimension(259, 170)
    minimumSize = new Dimension(255, 165)
    background = new Color(201, 230, 255)
    border = Swing.LineBorder(java.awt.Color.BLACK)
    contents += ingredi
    contents += whichIngredient
    contents += amount
    contents += howMuchIngredient
    contents += g
    contents += useIngredient
    contents += addExisting
  }
  // Sisältää reseptin valmistukseen liittyviä komponentteja
  val makeRecipePanel = new FlowPanel {
    maximumSize = new Dimension(259, 170)
    background = new Color(201, 230, 255)
    border = Swing.LineBorder(java.awt.Color.BLACK)
    contents += recipe
    contents += makeRcpField
    contents += makeRecipe
  }
  // Sisältää ainesosien ja reseptien poistamiseen vaadittavia komponentteja.
  val deletePanel = new FlowPanel {
    maximumSize = new Dimension(259, 200)
    minimumSize = new Dimension(255, 165)
    background = new Color(201, 230, 255)
    border = Swing.LineBorder(java.awt.Color.BLACK)
    contents += ingredi1
    contents += deleteIField
    contents += recipe1
    contents += deleteRField
    contents += deleteIButton
    contents += deleteRButton
  }
  // BoxPanel, johon sijoitetaan kaikki edelliset FlowPanelit. Sijoitetaan BorderPanelissa suuntaan: East.
  val canMakeBox = new BoxPanel(Orientation.Vertical) {
    maximumSize = new Dimension(201, 1000)
    preferredSize = new Dimension(260, 600)
    background = new Color(201, 230, 255)
    contents += upperButtons
    contents += exactlyThisMuch
    contents += usePanel
    contents += makeRecipePanel
    contents += deletePanel
  }
  /*
   * Kenttä, johon ilmestyy kaikki käyttäjän kannalta olennainen informaatio resepteistä ja ainesosista ja niihin
   * liittyvistä toiminnoista, jotka määritellään reactions-osiossa.
   */
  var recipePage = new TextArea("", 150, 500) {
    maximumSize_=(new Dimension(200, 100))
    preferredSize_=(new Dimension(100, 66))
    editable_=(false)
    font = new Font("Arial", 14, 16)
    border = Swing.EmptyBorder(8, 8, 8, 8)
  }
  /*
   * Sijoitetaan recipePage vielä ScrollPaneen, jotta saadaan scrollattua, jos tekstiä tulee yli Guin reunojen. Sijoitetaan
   * BorderPaneliin kohtaan Center.
   */
  val recipePane = new ScrollPane() {
    border = Swing.EmptyBorder(15, 15, 15, 15)
    contents = recipePage
  }

  /*
   * Tässä on paneeli, johon sijoitetaan kaikki edelliset isommat komponentit. Se on uloin BorderPanel koko GUIssa
   */
  val borderPanel = new BorderPanel() {
    background = new Color(201, 230, 255)
    layout(menuBar) = BorderPanel.Position.North
    layout(scrollForButtonBoxPanel) = BorderPanel.Position.West
    layout(recipePane) = BorderPanel.Position.Center
    layout(IandRBorderP) = BorderPanel.Position.South
    layout(canMakeBox) = BorderPanel.Position.East
  }
  // Lisätään BorderPaneel pääikkunaan
  top.contents = borderPanel
  // Ylikirjoitetaan lopuksi metodi top
  override def top = this.frame
  recipePage.text = fridge.getInstructions

  /*
 * GUI COMPONENTS END HERE
 * ----------------------------------------------------------------------------------------------------------------------------
 * REACTIONS START FROM HERE
 */

  /*
   * Kaikki paitsi yksi reaktio ovat napinpainalluksia, useimmiten siten, että ne reagoivat tekstikentissä oleviin teksteihin.
   * 
   * Tekstien yleinen määräytymistapa:
   *
   * 1. Tarkistetaan oikeinkirjoitus
   * 2. Löytyikö kyseisellä nimellä reseptiä, tai ainesosaa
   * 3. Täyttääkö tämä kyseinen resepti tai ainesosa sille asetetut ehdot.
   * 4. Tulostetaan haluttu tieto.
   * 
   * Aina jos johonkin kohtaan jäädään, haarautuu lauseke, ja tulostetaan jokin virheilmoitus.
   */
  reactions += {

    /*
     * Kun painetaan nappia Ingredients, recipePagelle ilmestyy kaikki ainesosat ja niiden määrät.
     */
    case ekaPainallus: ButtonClicked if ekaPainallus.source.name == "Ingredients" => {
      recipePage.text = ""
      recipePage.text += "Jääkaapissasi on tällä hetkellä " + ingredients.size.toString + " ainesta, jotka on listattu alapuolella: \n\n"
      recipePage.text += "------------------------------------------ \n\n"
      /*
       * Riippuen, onko ainesosa yksikkömuunnettava vai ei määrittyy, näytetäänkö aine listalla grammoissa vai litroissa.
       * Eli tarkistetaan muuttujalla conIngreds, kuuluuko ingredient tähän bufferiin.
       * 
       * BigDecimal(fridge.convertibleIngredAmount(each.getName).get).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble
       * pyöristää arvon yhden desimaalin tarkkuudella, koska ei ole tarkoituksenmukaista näyttää enempää desimaaleja.
       * 
       * Tekstit määräytyvät tässä ja yleisesti sen mukaa
       * 
       * Kun GUI muokkautuu, kutsutaan BorderPanel.revalidate
       */
      val conIngreds = ingredients.filter(_.isConvertible)
      for (each <- ingredients) {
        if (each.getAmount > 0) {
          if (conIngreds.contains(each)) {
            recipePage.text += each.getName + " " + BigDecimal(fridge.convertibleIngredAmount(each.getName).get)
              .setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble + "ml" + "\n\n"
          } else {
            recipePage.text += each.getName + " " + each.getAmount.toString + "g" + "\n\n"
          }
        } else {
          recipePage.text += each.getName + "\n\n"
        }
        if (!each.consists.isEmpty) {
          recipePage.text += "Tämä ainesosa koostuu seuraavista aineksista:  \n"
          for (every <- each.consists) {
            recipePage.text += every + "\n"

          }
        }
        recipePage.text += "------------------------------------------"
        recipePage.text += "\n"
      }
      borderPanel.revalidate
    }
    /*
     * Tämä case tapahtuu, kun jotain reseptinappia painetaan. Ruudulle ilmestyy silloin kyseisen reseptin tiedot.
     */
    case painallus: ButtonClicked if painallus.source.name == "rcp" => {
      recipePage.text = ""
      val thisRecipe = Buffer[Recipe]()
      // Tarkistetaan löytyikö resepti nimellä
      recipes.foreach(x => if (x.getName == painallus.source.text) thisRecipe += x)
      val thisRecipe1 = thisRecipe(0)

      recipePage.text = thisRecipe1.getName + ":  \n"
      /*
       * Käydään yksitellen läpi reseptin tarvitsemat ainekset ja vielä erikseen yksikkömuunnettavat ja "Normaalit"
       * Jos ei ole määrää printataan vain ainesosan nimi recipePagelle.
       */
      for (each <- thisRecipe1.getIandA) {
        recipePage.text += "\n"
        recipePage.text += each._1.getName + " "
        if (each._1.isConvertible) {
          if (each._2 > 0) recipePage.text += BigDecimal(fridge.convertibleIngredAmount(each._1.getName).get)
            .setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble + " ml"
        } else if (each._2 > 0) {
          recipePage.text += each._2.toString + " g"
        }
      }
      // Tehdään vielä lisäykset, jos omisti kaikki reseptit, tai vain osan
      if (fridge.hasAllIngredients(thisRecipe1)) {
        recipePage.text += "\n\n" + "Sinulla on kaikki ainekset tähän reseptiin!"
      } else if (fridge.hasPartiallyIngredients(thisRecipe1)) {
        recipePage.text += "\n\n" + "Sinulta puuttuu joitain aineksia tähän reseptiin!"
      }
      borderPanel.revalidate
    }
    /*
     * Kun haetaan jotain reseptiä, tämä case aktivoituu.
     */
    case haeResepti: ButtonClicked if haeResepti.source.name == "hae" => {
      if (recipes.map(_.getName).contains(searchBar.text.toLowerCase)) {
        recipePage.text = ""
        val thisRecipe = Buffer[Recipe]()
        //Lisätään taas kyseinen resepti väliaikaiseen varastoon.
        recipes.foreach(x => if (x.getName == searchBar.text.toLowerCase) thisRecipe += x)
        val thisRecipe1 = thisRecipe(0)
        val recipesIs = thisRecipe1.getIngredientsPls
        var noAllergens = true

        /* Tässä tarkistetaan, sisältääkö resepti jotain allergisoivaa tekijää vertaamalla
        * allergenBar-kentän tekstiä, kaikkiin reseptissä olevien ainesosien sisältämiin allergeeneihin.
        */
        val allergen = allergenBar.text.trim.toLowerCase
        for (each <- recipesIs) {
          if (each.getAllergen == allergen && each.getAllergen != "") {
            noAllergens = false
            recipesIs.foreach(x => println(x.getName))
          }
        }

        /* Jos ei sisällä Allergeenia, printataan normaalisti reseptin tiedot ja jos ei,
        * niin tulee teksti, että resepti sisälsi sitä tiettyä allergeenia. Taas verrataab
        * erikseen yksikkömuunnettavia ja "normaaleja".
        */
        if (noAllergens) {
          recipePage.text = thisRecipe1.getName + ":  \n"
          for (each <- thisRecipe1.getIandA) {
            recipePage.text += "\n"
            recipePage.text += each._1.getName + " "
            if (each._1.isConvertible) {
              if (each._2 > 0) recipePage.text += BigDecimal(fridge.convertibleIngredAmount(each._1.getName).get)
                .setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble + " ml"
            } else if (each._2 > 0) {
              recipePage.text += each._2.toString + " g"
            }
          }
          // Tarkistukset, onko kaikki, tai esim vain osa reseptin aineksista.
          if (fridge.hasAllIngredients(thisRecipe1)) {
            recipePage.text += "\n\n" + "Sinulla on kaikki ainekset tähän reseptiin!"
          } else if (fridge.hasPartiallyIngredients(thisRecipe1)) {
            recipePage.text += "\n\n" + "Sinulta puuttuu joitain aineksia tähän reseptiin!"
          }
          // Onnistunut haku!
        } else {
          recipePage.text += thisRecipe1.getName + "-reseptissä oli allergeenia:  " + allergenBar.text
        } // Virheilmoituksia!
      } else if (searchBar.text == "") {
        recipePage.text = "Kirjoita ensin jotain reseptinhaku-kenttään!"
      } else {
        recipePage.text = "Reseptiä " + searchBar.text + " ei löytynyt tunnetuista resepteistäsi.	"
      }
      borderPanel.revalidate
    }
    /*
     * Case, kun painetaan nappia Vajavaiset. Etsii kaikki reseptit, joihin puuttuu osa aineksista. Käyttää apunaan
     * mm. Fridgen metodeja hasAllIngredients, partiallyAbleToMake ja IngredientsThatCanBeMade
     */
    case partiallyAble: ButtonClicked if partiallyAble.source.name == "Vajavaiset" => {
      val partially = fridge.partiallyAbleToMake
      if (!partially.isEmpty) {
        recipePage.text = ""
        recipePage.text += "Omistat osan tähän reseptiin tarvittavista aineksista. \n\n"
        for (each <- partially) {
          if (!fridge.hasAllIngredients(each)) {
            recipePage.text += each.getName + "\n\n"
            val a = fridge.ingredientsThatCanBeMade(each)
            if (!a.isEmpty) {
              recipePage.text += "Nämä ainesosat voidaan valmistaa niiden sisältämistä raaka-aineista reseptissä: \n\n"
              for (every <- a) {
                recipePage.text += every.getName + "\n"
              }
            }
          }
        } // Virheimoitusta
      } else {
        recipePage.text = "Et omista mitään aineksia, joita käytetään tuntemissasi respeteissä."
      }
      borderPanel.revalidate
    }
    /*
     * Reseptit, jotka pystytään kokonaan tekemään ilmestyvät ruudulle. Käyttää Fridgen metodia thatCanBeMade
     */
    case ableToMake: ButtonClicked if ableToMake.source.name == "Kokonaan" => {
      recipePage.text = ""
      if (!fridge.thatCanBeMade.isEmpty) {
        recipePage.text += "Kaapista löytyvillä aineksilla pystytään tekemään seuraavat reseptit kokonaan: \n\n"
        for (each <- fridge.thatCanBeMade) {
          recipePage.text += each.getName + "\n\n"
        } // Virheilmoitusta.
      } else {
        recipePage.text += "Kaapista löytyvillä aineksilla ei voi tehdä yhtäkään reseptiä kokonaisuudessaan."
      }
    }
    /*
      * Ruudulle kaikki reseptit joista puutuu tasan x-ainetta, jotka syötetty kenttään. Käyttää Fridgen metodia
      * howManyIngredientsMissing.
      */
    case missingExactly: ButtonClicked if missingExactly.source.name == "Täsmälleen" => {
      if (howManyMissing.text != "") {
        //Tallennetaan tekstikentän arvolla Mappiin reseptit ja puuttuvat ainekset.
        val recipesAndAmounts = fridge.howManyIngredientsMissing(howManyMissing.text.toInt)
        if (!recipesAndAmounts.isEmpty) {
          if (!recipesAndAmounts.filter(_._2 == howManyMissing.text.toInt).isEmpty) {
            val missingHowMany = recipesAndAmounts.filter(_._2 == howManyMissing.text.toInt)
            recipePage.text = ""
            recipePage.text += "Reseptit, joista puuttuu täsmälleen " + howManyMissing.text + " ainesosaa, joko niin, että" +
              " ainesosa puuttuu kokonaan, tai sitä ei ole tarpeeksi, ovat:" + "\n\n"
            missingHowMany.foreach(recipePage.text += _._1.getName + "\n\n")
            //virheilmoituksia
          } else {
            recipePage.text = "Yhdestäkään reseptistä ei puutu tasan " + howManyMissing.text + " ainesosaa."
          }
        } else {
          recipePage.text = "Yhdestäkään resepetistä ei puuttunut tasan tarkkaan " + howManyMissing.text + " ainesosaa."
        }
      } else {
        recipePage.text = "Laita numero hakukenttään"
      }
    }
    /*
     * Näyttää kaikki reseptit jotka sisältävät tiettyä ainesta. Käyttää fridgen metodia
     */
    case byIngredient: ButtonClicked if byIngredient.source.name == "aines" => {
      var bool = false
      for (each <- recipes) {
        for (every <- each.getIngredientsPls) {
          if (every.getName == aineksenMukaan.text.toLowerCase) {
            bool = true
          }
        }
      }
      // Löytyykö tällä nimellä resepti?
      val found = fridge.hasIngredient(aineksenMukaan.text.toLowerCase)
      val recipesIs = Buffer[Ingredient]()
      //Lisää reseptin ainesosat bufferiin
      recipes.foreach(x => x.getIngredientsPls.foreach(recipesIs += _))
      // Löytyikö reseptejä jossa tätä kyseistä ainesta?
      val foundIngredient = recipesIs.find(x => x.getName == aineksenMukaan.text).isDefined
      if (foundIngredient) {
        recipePage.text = "Reseptit, joissa oli ainesta " + aineksenMukaan.text + " ovat seuraavat: \n\n"
        // Kaikki reseptit, josta löytyi tiettyä ainesta.
        val recipesThatContain = fridge.allThatContainIngredient(recipes, recipesIs.find(_.getName == aineksenMukaan.text.toLowerCase).get)
        for (each <- recipesThatContain) {
          recipePage.text += each.getName + "\n\n"
        } //VIrheilmoituksia
      } else if (aineksenMukaan.text == "") {
        recipePage.text = "Kirjoita ensin jotain hakukenttään."
      } else if (found && !bool)
        recipePage.text = "Ainesosaa " + aineksenMukaan.text + " ei löytynyt mistään reseptistä"
      else if (!found && aineksenMukaan.text != "") {
        recipePage.text = "Oletko varma, että kirjoitit aineksen nimen: " + aineksenMukaan.text + " oikein?"
      }
      borderPanel.revalidate
    }
    /*
     * Tiputtaa pois kaikki reseptit joisa on tämä kyseinene allergeeni. Käyttää fridgen metodia allThatDontContainAllergen
     */
    case byAllergen: ButtonClicked if byAllergen.source.name == "aller" => {
      if (allergenBar.text != "") {
        //Löytyikö allereeni tällä nimellä.
        val foundAllergen = ingredients.find(_.getAllergen == allergenBar.text.toLowerCase).isDefined
        //Onko reseptejä jossa tätä allergeenia ei ole
        val recipesNotContaining = fridge.allThatDontContainAllergen(recipes, allergenBar.text)
        //Tämä reseptilista ei saa olla tyhjä, allergeni tällä nimellä täytyy löytyä.
        if (!recipesNotContaining.isEmpty && foundAllergen) {
          recipePage.text = ""
          recipePage.text += "Reseptit, joista ei löydy allergeenia " + allergenBar.text + " ovat: \n\n"
          for (each <- recipesNotContaining) {
            recipePage.text += each.getName + "\n\n"
          } //virheilmoituksia
        } else {
          recipePage.text = ""
          recipePage.text += "Kirjoititko varmasti allergeenin nimen oikein?"
        }
      } else {
        recipePage.text = "Kirjoita jotain ensin allergeeni-kenttään."
      }
      borderPanel.revalidate
    }
    /*
     * Lisätään kyseistä ainesosaa tietty määrä, joiden arvot luetaan tekstikentistä. Käyttää hyväkseen Fridgen metodia 
     * addInrgredient ja Ingredientin metodeja changeConvertible ja setDensity.
     * 
     * Consists-kenttä splitataan pilkun kohdalta, koska käyttäjän kirjoittama syntaksi on aine1,aine2,...,aineN
     */
    case addIngredient: ButtonClicked if addIngredient.source.name == "addI" => {
      var consistsBuffer = Buffer[String]()
      for (each <- consists.text.split(',')) {
        consistsBuffer += each
      }
      //On yhteensä kuusi variaatiota, minkälaisen aineksen voi tehdä, tässä on listattuna kaikki.
      if (ingredientName.text != "") {

        if (ingredientAmount.text != "" && allergenName.text != "" && consists.text != "") {

          fridge.addIngredient(ingredientName.text, ingredientAmount.text.toDouble, allergenName.text, consistsBuffer)

        } else if (ingredientAmount.text != "" && allergenName.text != "" && consists.text == "") {
          fridge.addIngredient(ingredientName.text, ingredientAmount.text.toDouble, allergenName.text, Buffer())

        } else if (ingredientAmount.text != "" && allergenName.text == "" && consists.text == "") {
          fridge.addIngredient(ingredientName.text, ingredientAmount.text.toDouble, allergenName.text, Buffer())

        } else if (ingredientAmount.text == "" && allergenName.text != "" && consists.text != "") {
          fridge.addIngredient(ingredientName.text, 0.0, allergenName.text, consistsBuffer)

        } else if (ingredientAmount.text == "" && allergenName.text == "" && consists.text != "") {
          fridge.addIngredient(ingredientName.text, 0.0, "", consistsBuffer)

        } else if (ingredientAmount.text == "" && allergenName.text != "" && consists.text == "") {
          fridge.addIngredient(ingredientName.text, 0.0, allergenName.text, Buffer())

        } // Plus vielä katostaan, onko ainesosa yksikkömuutettava.
        if (convertible.selected) {
          val ingred = fridge.findIngredient(ingredientName.text.toLowerCase)
          ingred.get.changeConvertible
          ingred.get.setDensity(density.text.toDouble)
          convertible.selected_=(false)
          density.text = ""
        }
        recipePage.text = ""
        recipePage.text = "Lisättiin uusi ainesosa: " + ingredientName.text
        ingredientName.text = ""
        ingredientAmount.text = ""
        allergenName.text = ""
        consists.text = ""

      } else {
        recipePage.text = "Ainesosa tarvitsee nimen."
      }
      borderPanel.revalidate()
    }
    /*
     * Lisätään resepti tietokantaan. Käyttää fridgen metodeita findIngredient ja hasIngredient.
     * 
     * Reseptin ainekset parsetaan tekstikentästä syntaksista aine1-määrä1,aine2-määrä2,...,aineN-määräX, joka on käyttä-
     * jälle ohjeissa ilmoitettu.
     */
    case addRecipe: ButtonClicked if addRecipe.source.name == "addR" => {
      var isAndAs = Map[Ingredient, Double]()
      var success = true
      var ingredient = ""
      var notFound = Buffer[String]()
      if (recipeName.text != "") {
        if (!recipes.find(_.getName == recipeName.text.toLowerCase).isDefined) {
          // Näin saadaan erilleen yhden ainesosan nimet ja määrät
          val splitted = ingredientsAmounts.text.split(',')
          for (each <- splitted) {
            val trimmed = each.trim
            // Jos määrä on ilmoitettu eli on aine1-määrä, niin seuraavanlainen lisäys Mappiin.
            if (trimmed.contains('-')) {
              if (fridge.hasIngredient(trimmed.split('-')(0).trim)) {
                isAndAs += fridge.findIngredient(trimmed.split('-')(0).trim).get -> trimmed.split('-')(1).trim.toDouble
              } else {
                notFound += each
                success = false
              }
              // Muuten hoidetaan ilman splittiä, each on sillon vain esim: maito 
            } else {
              if (ingredients.map(_.getName).contains(trimmed)) {
                isAndAs += fridge.findIngredient(trimmed).get -> 0.0
              } else {
                notFound += each
                success = false
              }
            }
          }

        } else {
          success = false
        } //Virheilmoitus
      } else {
        recipePage.text = "Kirjoita jotain reseptin nimi-kenttään."
      }
      /*
       *  Täällä tapahtuu itse reseptin lisääminen. Käytetään siinä juuri uutta mappiamme. Tässä täytyy muistaa
       *  borderpanelin revalidaus ja fillRecipeBar-metodin kutsuminen.
       */
      if (success && recipeName.text != "") {
        recipes += new Recipe(recipeName.text, isAndAs)
        scrollForButtonBoxPanel.contents = this.fillRecipeBar
        borderPanel.layout(scrollForButtonBoxPanel) = BorderPanel.Position.West
        recipePage.text = "Lisättiin resepti " + recipeName.text + " tietoihin."
      } //Virheilmoituksia
      else if (recipeName.text == "") {
        recipePage.text = "Täytyisiköhän resepti nimetä jotenkin... ?"
      } else if (ingredientsAmounts.text == "") {
        recipePage.text = "Ei ole reseptiä ilman yhtäkään ainetta!"
      } else if (recipes.find(_.getName == recipeName.text.toLowerCase).isDefined) {
        recipePage.text = "Samanniminen resepti löytyi jo kaapista."
      } // Printataan ainekset joita ei löytynyt kaapista.
      else if (notFound.size == 1) {
        recipePage.text = "Ainesosaa " + notFound(0) + " ei löytnyt kaapistasi."
      } else {
        var stringgi = ""
        for (each <- 0 until notFound.size) {
          if (each < notFound.size - 1) {
            stringgi += notFound(each) + ", "
          } else {
            stringgi += notFound(each)
          }
        }
        recipePage.text = "Ainesosia " + stringgi + " ei löytynyt kaapistasi."
      }
      recipeName.text = ""
      ingredientsAmounts.text = ""
      scrollForButtonBoxPanel.contents = this.fillRecipeBar
      borderPanel.layout(scrollForButtonBoxPanel) = BorderPanel.Position.West
      borderPanel.revalidate
    }
    /*
     * Käytetään ainesosaa tietty määrä. Fridgen metodit howMuchIngrredient ja usIngredient ovat käyössä tässä. 
     * Ainesosaa ei voi käyttää siten, että määrän ja vähennyksen erotus olisi vähemmän kuin nolla.
     */
    case useI: ButtonClicked if useI.source.name == "useI" => {
      try {
        if (fridge.hasIngredient(whichIngredient.text.toLowerCase)) {
          if (howMuchIngredient.text != "") {
            if (howMuchIngredient.text.toDouble >= 0) {
              // Kuinka paljon on tiettyä ainesosaa jäljellä?
              val ingredAmount = fridge.howMuchIngredient(fridge.findIngredient(whichIngredient.text.toLowerCase).get)
              recipePage.text = ""
              if (ingredAmount < howMuchIngredient.text.toDouble) {
                recipePage.text = "Et voi käyttää enemmän ainesta kuin sinulla on jäljellä!"
                // Käytetään tietty määrä ainesosaa.
              } else if (howMuchIngredient.text.toDouble <= ingredAmount) {
                fridge.useIngredient(whichIngredient.text.toLowerCase, howMuchIngredient.text.toDouble)
                recipePage.text += "Sinulla on jäljellä: \n\n " + fridge.findIngredient(whichIngredient.text).get.getAmount +
                  "g ainesta " + whichIngredient.text
              } // Virheilmoituksia 
              else {
                recipePage.text = "Reseptiä " + whichIngredient.text + " ei löytnyt."
              }
            } else {
              recipePage.text = "Syötä kenttään vain positiivisa numeroita"
            }
          } else {
            recipePage.text = "Täytä kummatkin kentät!"
          }
        } else {
          recipePage.text = "Ainesosaa ei löyd kaapistasi"
        }
        whichIngredient.text = ""
        howMuchIngredient.text = ""
      } catch {
        case e: Exception => 
          throw new IllegalArgumentException("Vain numerot hyväksytään määrä-kentässä")
          recipePage.text = "Vain numerot hyväksytään määrä-kentässä"
      }
    }
    /*
     * Tehdään resepti, eli kutsutaan metodia useIngredient, jokaiselle reseptin ainesosista. Käytetään fridgen
     * metodeja makeRecipe ja canMakeRecipe
     */
    case makeR: ButtonClicked if makeR.source.name == "makeRcp" => {
      // Löytyykö kys. resepti
      if (recipes.find(_.getName == makeRcpField.text.toLowerCase).isDefined) {
        // Etsitään kys. resepti.
        val recipe = recipes.find(_.getName == makeRcpField.text.toLowerCase).get
        // Käytetään kaikki ainesosat
        if (fridge.canMakeRecipe(recipe)) {
          fridge.makeRecipe(recipe)
          recipePage.text = "Valmistit reseptin " + recipe.getName + " !  \n\n ja siihen tarvittavat aineet vähennettiin kaapistasi."
        } // Virheilmoituksia 
        else {
          recipePage.text = "Sinulla ei ollut tarpeeksi aineksia tähän reseptiin."
        }
      } else recipePage.text = "Kirjoititko varmasti reseptin nimen oikein?"

    }

    /*
     * Lisätään tiettyä ainesosaa tietty määrä. Käytetään friden metodia addExisting. Ei normaalista
     * poikkeavia rajotteita reaktiolle.
     */
    case addE: ButtonClicked if addE.source.name == "add" => {
      try {
        if (!howMuchIngredient.text.isEmpty && !whichIngredient.text.isEmpty) {
          // Tarkistetaan, onko kys. ainesosaa.
          if (fridge.hasIngredient(whichIngredient.text.toLowerCase) && howMuchIngredient.text.toDouble >= 0) {
            //Lisätään haluttu määrä
            fridge.addExisting(whichIngredient.text.toLowerCase, howMuchIngredient.text.toDouble).get
            recipePage.text = "Lisättiin " + howMuchIngredient.text + " grammaa ainesosaan " + whichIngredient.text.toLowerCase() + "."
            this.ingredientButton.action
          } //Virheilmoituksia
          else if (howMuchIngredient.text.toDouble < 0) {
            recipePage.text = "Kirjoita määrä-kenttään pelkkiä positiivisa numeroita!"
          }
        } else {
          recipePage.text = "Täytä ensin kummatkin kentät!"
        }
        howMuchIngredient.text = ""
        whichIngredient.text = ""
      } catch {
        case e: Exception =>
          throw new IllegalArgumentException("Vain numerot hyväksytään määrä-kentässä")
          recipePage.text = "Vain numerot hyväksytään määrä-kentässä"
      }
    }
    /*
    * Lukee ohjeet TextWriterista fridgen kautta. Ilmestyy recipePagelle.
    */
    case instructions: ButtonClicked if instructions.source.name == "Ohjeet" => {
      recipePage.text = fridge.getInstructions
    }
   
    /*
     * Poistaa ainesosan kaapista. Käyttää fridgen metodeita deleteIngredient, findIngredient ja hasIngredient.
     * recipePagelle ilmestyy tiedot poistetusta aineesta.
     */
    case deleteI: ButtonClicked if deleteI.source.name == "poistaI" => {
      if (fridge.hasIngredient(deleteIField.text.toLowerCase)) {
        // Tarkistaa ettei missään reseptissä käytetä kys. ainesta.
        if (recipes.forall(!_.getIandA.contains(fridge.findIngredient(deleteIField.text.toLowerCase).get))) {
          fridge.deleteIngredient(deleteIField.text)
          recipePage.text = "Aines " + deleteIField.text + " poistettiin onnistuneesti."
        } // Virheilmoituksia, printtaa missä resepteissä käytetään kys. ainesosaa.
        else {
          recipePage.text = "Poista ensin reseptit "
          for (each <- recipes) {
            if (each.getIandA.contains(fridge.findIngredient(deleteIField.text.toLowerCase).get)) {
              recipePage.text += each.getName + " ,"
            }
          }
          recipePage.text += " Koska ne sisältävät ainesosaa " + deleteIField.text + "."

        }
      } else {
        recipePage.text = "Ainesta: " + deleteIField.text + " ei löytynyt kaapistasi."
      }
      deleteIField.text = ""
      borderPanel.revalidate
    }
    /*
     * Poistaa reseptin, mikäli mahdollista. tää fridgen metodia deleteRecipe. Pitää muistaa kutsua metodia
     * fillRecipeBar. Ei siis poista tietokannasta ainesosia, vaan vaan tiedot siitä mitä ainesosia kys.
     * reseptissä tarvitaan ja itse reseptin.
     */
    case deleteR: ButtonClicked if deleteR.source.name == "poistaR" => {
      if (recipes.find(_.getName == deleteRField.text.toLowerCase).isDefined) {
        fridge.deleteRecipe(deleteRField.text)
        recipePage.text = "Resepti " + deleteRField.text + " poistettiin onnistuneesti."
      } else {
        recipePage.text = "Reseptiä " + deleteRField.text + " ei löytynyt kaapistasi."
      }

      scrollForButtonBoxPanel.contents = this.fillRecipeBar
      borderPanel.layout(scrollForButtonBoxPanel) = BorderPanel.Position.West
      borderPanel.revalidate
    }
  }
}