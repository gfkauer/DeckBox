package com.r0adkll.deckbuilder.arch.ui.features.importer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.core.text.HtmlCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.ftinc.kit.arch.presentation.BaseActivity
import com.ftinc.kit.arch.presentation.delegates.StatefulActivityDelegate
import com.ftinc.kit.extensions.color
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.r0adkll.deckbuilder.DeckApp
import com.r0adkll.deckbuilder.R
import com.r0adkll.deckbuilder.arch.domain.features.cards.model.PokemonCard
import com.r0adkll.deckbuilder.arch.ui.features.importer.di.DeckImportModule
import com.r0adkll.deckbuilder.util.ClipboardHelper
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_deck_importer.*
import javax.inject.Inject

class DeckImportActivity : BaseActivity(), DeckImportUi, DeckImportUi.Intentions, DeckImportUi.Actions {

    override var state: DeckImportUi.State = DeckImportUi.State.DEFAULT

    @Inject lateinit var renderer: DeckImportRenderer
    @Inject lateinit var presenter: DeckImportPresenter

    private val importDeck: Relay<String> = PublishRelay.create()

    private var clipboardSnackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deck_importer)

        appbar?.setNavigationOnClickListener { supportFinishAfterTransition() }

        actionImport?.setOnClickListener {
            val text = deckList.text.toString().trim()
            if (text.isNotBlank()) {
                importDeck.accept(text)
            }
        }

        deckList.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                invalidateOptionsMenu()
                action_layout?.isVisible = deckList.text.isNotBlank()
                action_divider?.isVisible = deckList.text.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        deckList.hint = HtmlCompat.fromHtml(getString(R.string.hint_import_field), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun setupComponent() {
        DeckApp.component.plus(DeckImportModule(this))
            .inject(this)

        delegates += StatefulActivityDelegate(renderer, Lifecycle.Event.ON_START)
        delegates += StatefulActivityDelegate(presenter, Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        detectClipBoard()
    }

    override fun onDestroy() {
        clipboardSnackBar?.dismiss()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_deck_import, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val importItem = menu.findItem(R.id.action_import)
        importItem?.isVisible = deckList.text.isNotBlank()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import -> {
                val text = deckList.text.toString().trim()
                if (text.isNotBlank()) {
                    importDeck.accept(text)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun importDeckList(): Observable<String> {
        return importDeck
    }

    override fun render(state: DeckImportUi.State) {
        this.state = state
        renderer.render(state)
    }

    override fun setResults(cards: List<PokemonCard>) {
        val data = Intent()
        data.putParcelableArrayListExtra(KEY_RESULTS, ArrayList(cards))
        setResult(RESULT_OK, data)

        if (cards.isNotEmpty()) {
            supportFinishAfterTransition()
        }
    }

    override fun showLoading(isLoading: Boolean) {
        loading.isVisible = isLoading
        deckList.isVisible = !isLoading
    }

    override fun showError(description: String) {
        errorMessage.text = description
        error.isVisible = true
    }

    override fun hideError() {
        error.isGone = true
    }

    private fun detectClipBoard() {
        val deckText = ClipboardHelper.getDeckInClipboard(this)
        if (deckText != null) {
            // Some valid deck format found, suggest paste
            if (clipboardSnackBar?.isShownOrQueued == false) {
                clipboardSnackBar?.setText(R.string.import_clipboard_found_message)
                clipboardSnackBar?.setActionTextColor(color(R.color.primaryColor))
                clipboardSnackBar?.setAction(R.string.action_paste) {
                    deckList.setText(deckText)
                }
            } else {
                clipboardSnackBar = Snackbar.make(deckList, R.string.import_clipboard_found_message,
                    Snackbar.LENGTH_INDEFINITE)
                clipboardSnackBar?.setActionTextColor(color(R.color.primaryColor))
                clipboardSnackBar?.setAction(R.string.action_paste) {
                    deckList.setText(deckText)
                }
                clipboardSnackBar?.show()
            }
        }
    }

    companion object {
        private const val RC_IMPORT = 200
        private const val KEY_RESULTS = "DeckImportActivity.Results"

        fun show(activity: Activity) {
            val intent = Intent(activity, DeckImportActivity::class.java)
            activity.startActivityForResult(intent, RC_IMPORT)
        }

        fun parseResults(resultCode: Int, requestCode: Int, data: Intent?): List<PokemonCard>? {
            if (resultCode == Activity.RESULT_OK && requestCode == RC_IMPORT) {
                return data?.getParcelableArrayListExtra(KEY_RESULTS)
            }
            return null
        }
    }
}
