package recipebook

import scala.collection.mutable.Buffer

/* 
 * Tämä yksinkertainen luokka kuvaa yhtä ainesosaa. Ainesosalla on aina nimi. Sillä voi olla myös määrä ja se voi
 * sisältää allergeenia. Aines voi koostua muista aineksista ja sille voi syöttää tiheyden. Tässä luokassa on lähinnä
 * metodeita, joilla voi kutsua luokan muuttujia ja muuttaa niitä.
 * 
 * On tärkeää huomata, että tiedostoa tallentaessa ei käytetä arvoa "amount" vaan päivitettyä yksityisen muuttujen "theAmount"
 * arvoa. Myös muiden yksityisten muuttujien arvot tallennetaan tiedostoa tallentaessa tekstitiedostoon.
 * 
 * Käyttäjä voi halutessaan syöttää aineelle tiheyden, kun hän syöttää arvoja graafiseen käyttöliittymään.
 * Tällöin kaikki aineella tapahtuvat laskutoimitukset suoritetaan yksikönmuunnosluokka "UnitChangerin" kautta.
 * Kaikki arvot myös esitetään millilitroissa. Normaalisti aineet syötetään ja esitetään grammoissa.
 * 
 * esim. new Ingredient("banaani", 200, "") vastaa noin kahta banaania
 *       
 *       private var density  = None
 *       private var consistsOf = None
 *       
 *       new Ingredient("voitaikina", 100, "")
 *       
 *       private var density = Some(0.9)
 *       private var consistOf = Buffer("voi","jauho")
 */

class Ingredient(val name: String, amount: Double, allergen: String) {
  
  // Määrittää tehdäänkö ainekselle yksikönmuunnoksia vai ei. Guissa tapahtuu checkboxin rastittamalla ainesta lisättäessä
  private var convertible: Boolean = false
  
  // Aineella voi olla tiheys, jos sillä halutaan tehdä yksikönmuunnoksia.
  private var density: Option[Double] = None
  
  // Aines voi koostua monista eri aineksista. Ne tulevat tänne.
  private var consistsOf = Buffer[String]()
  
  // Palauttaa aineksen määrän grammoissa.
  private var theAmount = this.amount
  
  def consists: Buffer[String] = this.consistsOf
  
  // Metodilla voi muuttaa, mitä aine sisältää.
  def changeConsists(buffer: Buffer[String]): Unit = { consistsOf =  buffer}
  
  def getDensity = this.density
  
  def getAllergen = allergen
  
  def getName: String = this.name

  def getAmount: Double = theAmount
  
  def isConvertible: Boolean = convertible
  
  // Pystyy muuttamaan, onko aines yksikkömuunnettava, vai ei.
  def changeConvertible = { convertible = true }
  
  // Muuttaa aineen määrää halutun verran.
  def changeAmount(am: Double) = {
    theAmount = theAmount + am
  }
  
  //Asettaa aineelle tiheyden, joka on joko Some(Double), tai None.
  def setDensity(am: Double) = { this.density = Some(am) }

}