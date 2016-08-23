package recipebook

import scala.collection.mutable.Buffer

/*
 * Tämä yksinkertainen luokka kuvaa reseptiä. Reseptillä on nimi, sekä Map, josta löytyy ainesosat ja niiden
 * reseptiin tarvittavat määrät. Reseptiin voi lisätä vain ainesosia, jotka ovat jo olemassa.
 * 
 * esim. 
 * new Recipe(makkarakeitto, Map(makkara -> 400, peruna -> 300, vesi -> 1l, keittojuurekset -> 400, suola, pippuri)
 * 
 * Map ingredientsAndAmounts on kätevä, koska avaimena on ainesosa, jolla on määrä, jota sitä löytyy kaapista. Arvona tälle
 * on määrä, jota sitä ainetta tarvitaan reseptissä, jolloin vertailu on helppoa näiden kahden arvon välillä.
 */

class Recipe(name: String, ingredientsAndAmounts: Map[Ingredient, Double]) {
  
  def getName: String = this.name

  def getIngredientsPls: Buffer[Ingredient] = ingredientsAndAmounts.keys.toBuffer

  def getIandA: Map[Ingredient,Double] = this.ingredientsAndAmounts

}