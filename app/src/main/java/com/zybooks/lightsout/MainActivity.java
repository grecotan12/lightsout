package com.zybooks.lightsout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private final String GAME_STATE = "gameState";
    private final String COLOR_STATE = "colorState";
    private LightsOutGame mGame;
    private GridLayout mLightGrid;
    private int mLightOnColor;
    private int mLightOffColor;

    private int mLightOnColorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLightGrid = findViewById(R.id.light_grid);

        // Add the same click handler to all grid buttons
        for (int buttonIndex = 0; buttonIndex < mLightGrid.getChildCount(); buttonIndex++) {
            Button gridButton = (Button) mLightGrid.getChildAt(buttonIndex);
            gridButton.setOnClickListener(this::onLightButtonClick);
        }

        Button gridButtonZero = (Button) mLightGrid.getChildAt(0);
        gridButtonZero.setOnClickListener(this::turnOffAllLight);

        mLightOnColor = ContextCompat.getColor(this, R.color.yellow);
        mLightOffColor = ContextCompat.getColor(this, R.color.black);

        mGame = new LightsOutGame();
        if (savedInstanceState == null) {
            mLightOnColorId = R.color.yellow;
            startGame();
        }
        else {
            String gameState = savedInstanceState.getString(GAME_STATE);
            mGame.setState(gameState);
            mLightOnColorId = savedInstanceState.getInt(COLOR_STATE);
            mLightOnColor = ContextCompat.getColor(this, mLightOnColorId);
            setButtonColors();
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GAME_STATE, mGame.getState());
        outState.putInt(COLOR_STATE, mLightOnColorId);
    }

    private void turnOffAllLight(View view) {
        for (int buttonIndex = 0; buttonIndex < mLightGrid.getChildCount(); buttonIndex++) {
            Button gridButton = (Button) mLightGrid.getChildAt(buttonIndex);
            int row = buttonIndex / LightsOutGame.GRID_SIZE;
            int col = buttonIndex % LightsOutGame.GRID_SIZE;
            gridButton.setBackgroundColor(mLightOffColor);
            gridButton.setText("off");
            gridButton.setTextColor(getApplication().getResources().getColor(R.color.white));
        }
        mGame.setmLightsGridFalse();
        if (mGame.isGameOver()) {
            Toast.makeText(this, R.string.congrats, Toast.LENGTH_SHORT).show();
        }
    }

    private void startGame() {
        mGame.newGame();
        setButtonColors();
    }

    private void onLightButtonClick(View view) {

        // Find the button's row and col
        int buttonIndex = mLightGrid.indexOfChild(view);
        int row = buttonIndex / LightsOutGame.GRID_SIZE;
        int col = buttonIndex % LightsOutGame.GRID_SIZE;

        mGame.selectLight(row, col);
        setButtonColors();

        // Congratulate the user if the game is over
        if (mGame.isGameOver()) {
            Toast.makeText(this, R.string.congrats, Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonColors() {

        for (int buttonIndex = 0; buttonIndex < mLightGrid.getChildCount(); buttonIndex++) {
            Button gridButton = (Button) mLightGrid.getChildAt(buttonIndex);

            // Find the button's row and col
            int row = buttonIndex / LightsOutGame.GRID_SIZE;
            int col = buttonIndex % LightsOutGame.GRID_SIZE;

            if (mGame.isLightOn(row, col)) {
                gridButton.setBackgroundColor(mLightOnColor);
                gridButton.setText("on");
                gridButton.setTextColor(getApplication().getResources().getColor(R.color.black));
            } else {
                gridButton.setBackgroundColor(mLightOffColor);
                gridButton.setText("off");
                gridButton.setTextColor(getApplication().getResources().getColor(R.color.white));
            }
        }
    }

    public void onNewGameClick(View view) {
        startGame();
    }

    public void onHelpClick(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void onChangeColorClick(View view) {
        Intent intent = new Intent(this, ColorActivity.class);
        intent.putExtra(ColorActivity.EXTRA_COLOR, mLightOnColorId);
        mColorResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> mColorResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            mLightOnColorId = data.getIntExtra(ColorActivity.EXTRA_COLOR, R.color.yellow);
                            mLightOnColor = ContextCompat.getColor(MainActivity.this, mLightOnColorId);
                            setButtonColors();
                        }
                    }
                }
            });
}