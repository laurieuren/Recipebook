package recipebook
/*
 * Tämä luokka suorittaa yksikönmuunnoksia aineksilla. Ainetta lisätessä voi valita, tekeekö siitä
 * yksikkömuunnettavan. Jos näin on, käytetään näitä metodeja aineen vertailuun.
 * Sillä on kaksi metodia, lähes samaan tarkoitukseen.
 * 
 * Aineella on olemassa massa ja tiheys  ρ = m / V, joten laskiessa tilavuuksia lasketaan V = m / ρ
 */
class UnitChanger {
  
  /*
   * fromGramsToDl 
   *
   * Metodi ottaa parametrikseen ainesosan ja tiheyden desimaalilukuna. Lasku palauttaa massan ja tiheyden osamäärän,
   * joka on aineen tilavuus, ja täten sitä voidaan käyttää resepteissä.
   * 
   * Esim. Henkilö lisää ainesosan "jauho", ja antaa tälle tiheyden 0.98 ja massan 1000g
   * 
   * Tilavuus millilitroissa 1000g/0.90 = 1111.1111. Tilavuus ilmoitetaan kahden desimaalin tarkkuudella GUIssa.
   * Joten millitroissa vastaus olisi 1111.11 ml = 1.11 l
   */
  def fromGramsToDl(ingredient: Ingredient, density: Double): Double = {
    var am = ingredient.getAmount
    am = am / density
    am
  }
    /*
     * Tekee saman kuin edellinen metodi, mutta ottaa arvokseen määrän ja tiheyden, joten ainesosaa ei tarvita.
     */
  def convertWOIngred(amount: Double, density: Double): Double = {
    var am = amount
    am = am / density
    am
  }
}