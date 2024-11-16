package com.example.tictactoe

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityGameBinding
    private var gameModel: GameModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        GameData.fetchGameModel()

        // Set click listeners for all buttons
        listOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8
        ).forEach { it.setOnClickListener(this) }

        // Start Game button click listener
        binding.startGameBtn.setOnClickListener {
            startGame()
        }

        // Observe game model changes
        GameData.gameModel.observe(this) { model ->
            gameModel = model
            setUI()
        }
    }

    private fun setUI() {
        gameModel?.let { model ->
            // Update button texts based on filled positions
            val buttons = listOf(
                binding.btn0, binding.btn1, binding.btn2,
                binding.btn3, binding.btn4, binding.btn5,
                binding.btn6, binding.btn7, binding.btn8
            )

            buttons.forEachIndexed { index, button ->
                button.text = model.filledPos[index]
            }

            // Update game status and start button visibility
            binding.startGameBtn.visibility =
                if (model.gameStatus == GameStatus.CREATED || model.gameStatus == GameStatus.JOINED) View.VISIBLE
                else View.INVISIBLE

            binding.gameStatusText.text = when (model.gameStatus) {
                GameStatus.CREATED -> "Game ID: ${model.gameId}"
                GameStatus.JOINED -> "Click on start game"
                GameStatus.INPROGRESS -> {
                    val turnMessage = if (GameData.myID == model.currentPlayer) "Your turn" else "${model.currentPlayer}'s turn"
                    turnMessage
                }
                GameStatus.FINISHED -> {
                    if (model.winner.isNotEmpty()) {
                        if (GameData.myID == model.winner) "You won!" else "${model.winner} won!"
                    } else "It's a draw!"
                }
            }
        }
    }

    private fun startGame() {
        gameModel?.let { model ->
            updateGameData(model.copy(gameStatus = GameStatus.INPROGRESS))
        }
    }

    private fun updateGameData(model: GameModel) {
        GameData.saveGameModel(model)
    }

    private fun checkForWinner() {
        val winningPositions = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        gameModel?.let { model ->
            for (positions in winningPositions) {
                val (a, b, c) = positions
                if (model.filledPos[a] == model.filledPos[b] &&
                    model.filledPos[b] == model.filledPos[c] &&
                    model.filledPos[a].isNotEmpty()
                ) {
                    model.gameStatus = GameStatus.FINISHED
                    model.winner = model.filledPos[a]
                    updateGameData(model)
                    return
                }
            }

            // Check for a draw
            if (model.filledPos.none { it.isEmpty() }) {
                model.gameStatus = GameStatus.FINISHED
                updateGameData(model)
            }
        }
    }

    override fun onClick(v: View?) {
        gameModel?.let { model ->
            if (model.gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(this, "Game not started", Toast.LENGTH_SHORT).show()
                return
            }

            if (model.currentPlayer != GameData.myID) {
                Toast.makeText(this, "Not your turn", Toast.LENGTH_SHORT).show()
                return
            }

            val clickedPosition = (v?.tag as? String)?.toIntOrNull()
            if (clickedPosition == null || model.filledPos[clickedPosition].isNotEmpty()) {
                return
            }

            model.filledPos[clickedPosition] = model.currentPlayer
            model.currentPlayer = if (model.currentPlayer == "X") "O" else "X"
            checkForWinner()
            updateGameData(model)
        }
    }
}
