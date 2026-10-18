package com.example.bookverse

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookverse.data.LibraryRepository
import com.example.bookverse.model.BookStatus
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val repository = LibraryRepository()
    private var currentTab = 0 // 0: Книги, 1: Цитати, 2: Вікторина

    // Змінні для вікторини
    private var quizIndex = 0
    private var quizScore = 0
    private var selectedOptionIndex: Int? = null
    private var isAnswered = false

    private lateinit var tvTitle: TextView
    private lateinit var btnAdd: ImageButton
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTitle = findViewById(R.id.tvScreenTitle)
        btnAdd = findViewById(R.id.btnAddAction)
        container = findViewById(R.id.containerContent)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> { currentTab = 0; loadBooksScreen(); true }
                R.id.nav_quotes -> { currentTab = 1; loadQuotesScreen(); true }
                R.id.nav_quiz -> { currentTab = 2; loadQuizScreen(); true }
                else -> false
            }
        }

        btnAdd.setOnClickListener {
            if (currentTab == 0) showAddBookDialog()
            else if (currentTab == 1) showAddQuoteDialog()
        }

        loadBooksScreen()
    }

    private fun loadBooksScreen() {
        tvTitle.text = "Моя Книжкова Полиця"
        btnAdd.visibility = View.VISIBLE
        container.removeAllViews()

        for (book in repository.books) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(android.graphics.Color.parseColor("#FDEBD0"))
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
            }

            val titleRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val tvBookTitle = TextView(this).apply {
                text = book.title
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#2C1D0C"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnDelete = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                setColorFilter(android.graphics.Color.parseColor("#D35400"))
                setOnClickListener {
                    repository.deleteBook(book.id)
                    loadBooksScreen()
                }
            }

            titleRow.addView(tvBookTitle)
            titleRow.addView(btnDelete)
            card.addView(titleRow)

            val tvAuthor = TextView(this).apply {
                text = book.author
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#784212"))
            }
            card.addView(tvAuthor)

            if (book.note.isNotEmpty()) {
                val tvNote = TextView(this).apply {
                    text = "💡 Враження: ${book.note}"
                    textSize = 13f
                    setTypeface(null, android.graphics.Typeface.ITALIC)
                    setTextColor(android.graphics.Color.parseColor("#784212"))
                    setPadding(0, 8, 0, 0)
                }
                card.addView(tvNote)
            }

            val statusText = when (book.status) {
                BookStatus.COMPLETED -> "✅ Прочитано"
                BookStatus.READING -> "📖 Читаю"
                BookStatus.PLANNED -> "⏳ У планах"
            }

            val tvProgress = TextView(this).apply {
                text = "Прогрес: ${book.currentPages} / ${book.totalPages} стор.    $statusText"
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#784212"))
                setPadding(0, 12, 0, 0)
            }
            card.addView(tvProgress)

            container.addView(card)
        }
    }

    private fun loadQuotesScreen() {
        tvTitle.text = "Колекція Цитат"
        btnAdd.visibility = View.VISIBLE
        container.removeAllViews()

        for (quote in repository.quotes) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(android.graphics.Color.parseColor("#FDEBD0"))
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
            }

            val tvText = TextView(this).apply {
                text = "“${quote.text}”"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.ITALIC)
                setTextColor(android.graphics.Color.parseColor("#784212"))
            }
            card.addView(tvText)

            val bottomRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 12, 0, 0)
            }

            val tvAuthor = TextView(this).apply {
                text = "— ${quote.bookTitle} (${quote.author})"
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#935116"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val btnDelete = ImageButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                background = null
                setColorFilter(android.graphics.Color.parseColor("#D35400"))
                setOnClickListener {
                    repository.deleteQuote(quote.id)
                    loadQuotesScreen()
                }
            }

            bottomRow.addView(tvAuthor)
            bottomRow.addView(btnDelete)
            card.addView(bottomRow)

            container.addView(card)
        }
    }

    private fun loadQuizScreen() {
        tvTitle.text = "Вікторина «Вгадай автора»"
        btnAdd.visibility = View.GONE
        container.removeAllViews()

        if (quizIndex >= repository.quizQuestions.size) {
            val tvFinished = TextView(this).apply {
                text = "🎉 Вікторину завершено!\nВаш результат: $quizScore / ${repository.quizQuestions.size}"
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#784212"))
                setPadding(0, 40, 0, 20)
            }
            val btnRestart = Button(this).apply {
                text = "Спробувати знову 🔄"
                setBackgroundColor(android.graphics.Color.parseColor("#D35400"))
                setTextColor(android.graphics.Color.WHITE)
                setOnClickListener {
                    quizIndex = 0
                    quizScore = 0
                    loadQuizScreen()
                }
            }
            container.addView(tvFinished)
            container.addView(btnRestart)
            return
        }

        val q = repository.quizQuestions[quizIndex]

        val topInfo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 12)
        }
        val tvCount = TextView(this).apply {
            text = "Питання ${quizIndex + 1} / ${repository.quizQuestions.size}"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#784212"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvScore = TextView(this).apply {
            text = "Рахунок: $quizScore"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#D35400"))
            setPadding(12, 6, 12, 6)
            setBackgroundColor(android.graphics.Color.parseColor("#FDEBD0"))
        }
        topInfo.addView(tvCount)
        topInfo.addView(tvScore)
        container.addView(topInfo)

        val quoteCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#FDEBD0"))
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        val tvQuote = TextView(this).apply {
            text = "“${q.quote}”"
            textSize = 15f
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.ITALIC)
            setTextColor(android.graphics.Color.parseColor("#784212"))
        }
        quoteCard.addView(tvQuote)
        container.addView(quoteCard)

        val tvPrompt = TextView(this).apply {
            text = "Хто автор цього висловлювання?"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#784212"))
            setPadding(0, 0, 0, 8)
        }
        container.addView(tvPrompt)

        for ((index, option) in q.options.withIndex()) {
            val btnOption = Button(this).apply {
                text = option
                textSize = 14f
                setBackgroundColor(android.graphics.Color.WHITE)
                setTextColor(android.graphics.Color.parseColor("#784212"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 4, 0, 4) }

                if (isAnswered) {
                    isEnabled = false
                    if (option == q.correctAuthor) {
                        setBackgroundColor(android.graphics.Color.parseColor("#A3E4D7"))
                    } else if (index == selectedOptionIndex) {
                        setBackgroundColor(android.graphics.Color.parseColor("#FADBD8"))
                    }
                } else {
                    setOnClickListener {
                        isAnswered = true
                        selectedOptionIndex = index
                        if (option == q.correctAuthor) {
                            quizScore++
                        }
                        loadQuizScreen()
                    }
                }
            }
            container.addView(btnOption)
        }

        if (isAnswered) {
            val btnNext = Button(this).apply {
                text = if (quizIndex < repository.quizQuestions.size - 1) "Наступне питання ➡️" else "Завершити вікторину 🏆"
                setBackgroundColor(android.graphics.Color.parseColor("#D35400"))
                setTextColor(android.graphics.Color.WHITE)
                setPadding(0, 16, 0, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 16, 0, 0) }
                setOnClickListener {
                    isAnswered = false
                    selectedOptionIndex = null
                    quizIndex++
                    loadQuizScreen()
                }
            }
            container.addView(btnNext)
        }
    }

    private fun showAddBookDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
        }
        val inputTitle = EditText(this).apply { hint = "Назва книги" }
        val inputAuthor = EditText(this).apply { hint = "Автор" }
        val inputPages = EditText(this).apply { hint = "Кількість сторінок"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val inputNote = EditText(this).apply { hint = "Особисті нотатки / враження" }

        layout.addView(inputTitle)
        layout.addView(inputAuthor)
        layout.addView(inputPages)
        layout.addView(inputNote)

        AlertDialog.Builder(this)
            .setTitle("Додати нову книгу")
            .setView(layout)
            .setPositiveButton("Додати") { _, _ ->
                val title = inputTitle.text.toString()
                val author = inputAuthor.text.toString().ifEmpty { "Невідомий автор" }
                val pages = inputPages.text.toString().toIntOrNull() ?: 100
                val note = inputNote.text.toString()
                if (title.isNotEmpty()) {
                    repository.addBook(title, author, pages, note)
                    loadBooksScreen()
                }
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    private fun showAddQuoteDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
        }
        val inputQuote = EditText(this).apply { hint = "Текст цитати"; maxLines = 3 }
        val inputBook = EditText(this).apply { hint = "Назва книги" }
        val inputAuthor = EditText(this).apply { hint = "Автор" }

        layout.addView(inputQuote)
        layout.addView(inputBook)
        layout.addView(inputAuthor)

        AlertDialog.Builder(this)
            .setTitle("Нова цитата")
            .setView(layout)
            .setPositiveButton("Зберегти") { _, _ ->
                val text = inputQuote.text.toString()
                val book = inputBook.text.toString().ifEmpty { "Невідома книга" }
                val author = inputAuthor.text.toString().ifEmpty { "Невідомий автор" }
                if (text.isNotEmpty()) {
                    repository.addQuote(text, book, author)
                    loadQuotesScreen()
                }
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }
}