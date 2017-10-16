package com.r0adkll.deckbuilder.arch.domain.features.decks.model


import com.r0adkll.deckbuilder.arch.domain.PokemonCard
import io.pokemontcg.model.SuperType
import paperparcel.PaperParcel
import paperparcel.PaperParcelable


@PaperParcel
data class Deck(
        val id: Long,
        val name: String,
        val description: String,
        val cards: List<PokemonCard>
) : PaperParcelable {

    val standardLegal: Boolean get() = cards.none { !it.set.standardLegal }
    val expandedLegal: Boolean get() = cards.none { !it.set.expandedLegal }
    val pokemonCount: Int get() = cards.count { it.supertype == SuperType.POKEMON }
    val trainerCount: Int get() = cards.count { it.supertype == SuperType.TRAINER }
    val energyCount: Int get() = cards.count { it.supertype == SuperType.ENERGY }


    companion object {
        @JvmField val CREATOR = PaperParcelDeck.CREATOR
    }
}