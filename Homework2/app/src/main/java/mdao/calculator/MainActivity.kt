package mdao.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvOperation: TextView
    private lateinit var tvResult: TextView
    private var currentOperation = ""
    private var currentResult = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvOperation = findViewById(R.id.tvOperation)
        tvResult = findViewById(R.id.tvResult)

        val buttonErase = findViewById<Button>(R.id.erase)
        val buttonAC = findViewById<Button>(R.id.buttonAC)
        val buttonEquals = findViewById<Button>(R.id.buttonEquals)
        val buttonSmile1 = findViewById<Button>(R.id.buttonSmile1)

        val numberButtons = listOf(
            findViewById<Button>(R.id.button0),
            findViewById<Button>(R.id.button1),
            findViewById<Button>(R.id.button2),
            findViewById<Button>(R.id.button3),
            findViewById<Button>(R.id.button4),
            findViewById<Button>(R.id.button5),
            findViewById<Button>(R.id.button6),
            findViewById<Button>(R.id.button7),
            findViewById<Button>(R.id.button8),
            findViewById<Button>(R.id.button9)
        )

        val operatorButtons = listOf(
            findViewById<Button>(R.id.buttonAdd),
            findViewById<Button>(R.id.buttonSubtract),
            findViewById<Button>(R.id.buttonMultiply),
            findViewById<Button>(R.id.buttonDivide)
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                currentOperation += button.text.toString()
                updateOperationDisplay()
            }
        }

        operatorButtons.forEach { button ->
            button.setOnClickListener {
                if (currentOperation.isNotEmpty() && currentOperation.last().isDigit()) {
                    currentOperation += button.text.toString()
                    updateOperationDisplay()
                }
            }
        }

        buttonAC.setOnClickListener {
            currentOperation = ""
            currentResult = ""
            updateOperationDisplay()
            updateResultDisplay()
        }

        buttonEquals.setOnClickListener {
            calculateResult()
        }

        buttonErase.setOnClickListener {
            if (currentOperation.isNotEmpty()) {
                currentOperation = currentOperation.dropLast(1)
                updateOperationDisplay()
            }
        }

        buttonErase.setOnLongClickListener {
            currentOperation = ""
            updateOperationDisplay()
            true
        }

        buttonSmile1.setOnClickListener {
            currentOperation = currentResult
            currentResult = ""
            updateOperationDisplay()
            updateResultDisplay()
        }
    }

    private fun calculateResult() {
        try {
            val result = evaluateExpression(currentOperation)
            currentResult = formatNumber(result)
        } catch (e: Exception) {
            currentResult = "Error"
        }
        updateResultDisplay()
    }

    private fun formatNumber(number: Double): String {
        return if (number.isInfinite() || number.isNaN()) {
            "Error"
        } else {
            val upperThreshold = 1e10
            val lowerThreshold = 1e-5

            if (number > upperThreshold || number < -upperThreshold ||
                (number != 0.0 && (number < lowerThreshold && number > -lowerThreshold))) {
                    String.format("%.6E", number)
            } else {
                if (number % 1 == 0.0) {
                    number.toLong().toString()
                } else {
                    String.format("%.6f", number).trimEnd('0').trimEnd('.')
                }
            }
        }
    }


    private fun evaluateExpression(expression: String): Double {
        val formattedExpression = expression.replace("x", "*")
        return eval(formattedExpression)
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                return if (ch == charToEat) {
                    nextChar()
                    true
                } else {
                    false
                }
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: ${ch.toChar()}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }
                return x
            }
        }.parse()
    }

    private fun updateOperationDisplay() {
        tvOperation.text = currentOperation
    }

    private fun updateResultDisplay() {
        tvResult.text = currentResult
    }
}