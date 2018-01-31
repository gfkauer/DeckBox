package com.r0adkll.deckbuilder.arch.ui.features.decks


import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.r0adkll.deckbuilder.BuildConfig
import com.r0adkll.deckbuilder.R
import com.r0adkll.deckbuilder.arch.data.AppPreferences
import com.r0adkll.deckbuilder.arch.domain.features.decks.model.Deck
import com.r0adkll.deckbuilder.arch.ui.components.BaseFragment
import com.r0adkll.deckbuilder.arch.ui.components.ListRecyclerAdapter
import com.r0adkll.deckbuilder.arch.ui.features.deckbuilder.DeckBuilderActivity
import com.r0adkll.deckbuilder.arch.ui.features.decks.DecksUi.State
import com.r0adkll.deckbuilder.arch.ui.features.decks.adapter.DecksRecyclerAdapter
import com.r0adkll.deckbuilder.arch.ui.features.decks.di.DecksModule
import com.r0adkll.deckbuilder.arch.ui.features.exporter.DeckExportActivity
import com.r0adkll.deckbuilder.arch.ui.features.exporter.MultiExportActivity
import com.r0adkll.deckbuilder.arch.ui.features.home.di.HomeComponent
import com.r0adkll.deckbuilder.internal.analytics.Analytics
import com.r0adkll.deckbuilder.internal.analytics.Event
import com.r0adkll.deckbuilder.util.ScreenUtils
import com.r0adkll.deckbuilder.util.extensions.isVisible
import com.r0adkll.deckbuilder.util.extensions.plusAssign
import com.r0adkll.deckbuilder.util.extensions.snackbar
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_decks.*
import javax.inject.Inject


class DecksFragment : BaseFragment(), DecksUi, DecksUi.Intentions, DecksUi.Actions {

    override var state: State = State.DEFAULT

    @Inject lateinit var renderer: DecksRenderer
    @Inject lateinit var presenter: DecksPresenter
    @Inject lateinit var preferences: AppPreferences

    private val shareClicks: Relay<Deck> = PublishRelay.create()
    private val duplicateClicks: Relay<Deck> = PublishRelay.create()
    private val deleteClicks: Relay<Deck> = PublishRelay.create()

    private lateinit var adapter: DecksRecyclerAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_decks, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        renderer.start()
        presenter.start()

        adapter = DecksRecyclerAdapter(activity!!, shareClicks, duplicateClicks, deleteClicks)
        adapter.setOnItemClickListener(object : ListRecyclerAdapter.OnItemClickListener<Deck> {
            override fun onItemClick(v: View, item: Deck, position: Int) {
                Analytics.event(Event.SelectContent.Deck(item.id))
                startActivity(DeckBuilderActivity.createIntent(activity!!, item))
            }
        })

        adapter.setEmptyView(empty_view)
        recycler.layoutManager = GridLayoutManager(activity, if (ScreenUtils.smallestWidth(resources, ScreenUtils.Config.TABLET_10)) 6 else 2)
        recycler.adapter = adapter

        fab.setOnClickListener {
            if (quickTip.isVisible()) {
                quickTip.hide(fab)
            }

            Analytics.event(Event.SelectContent.Action("new_deck"))
            startActivity(DeckBuilderActivity.createIntent(activity!!))
        }

        if (preferences.quickStart) {
            fab.postDelayed({ quickTip.show(fab, R.string.deck_quickstart_message) }, 300L)
            preferences.quickStart = false
        }

        disposables += shareClicks
                .subscribe {
                    Analytics.event(Event.SelectContent.Action("export_decklist"))
                    val intent = MultiExportActivity.createIntent(activity!!, it)
                    startActivity(intent)
                }
    }


    override fun onDestroy() {
        renderer.stop()
        presenter.stop()
        super.onDestroy()
    }


    override fun setupComponent() {
        getComponent(HomeComponent::class)
                .plus(DecksModule(this))
                .inject(this)
    }


    override fun render(state: State) {
        this.state = state
        renderer.render(state)
    }


    override fun shareClicks(): Observable<Deck> = shareClicks
    override fun duplicateClicks(): Observable<Deck> = duplicateClicks
    override fun deleteClicks(): Observable<Deck> = deleteClicks


    override fun showLoading(isLoading: Boolean) {
        empty_view.setLoading(isLoading)
    }


    override fun showError(description: String) {
        snackbar(description)
    }


    override fun hideError() {
    }


    override fun showDecks(decks: List<Deck>) {
        adapter.showDecks(decks)
    }


    companion object {

        fun newInstance(): DecksFragment = DecksFragment()
    }
}