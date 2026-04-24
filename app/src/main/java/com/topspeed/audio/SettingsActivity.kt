package com.topspeed.audio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.topspeed.audio.audio.AudioEngine
import com.topspeed.audio.audio.TTSManager
import com.topspeed.audio.network.UserPreferences

/**
 * Activity cho màn hình Cài Đặt
 * Màn hình dọc để dễ nhập liệu
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: UserPreferences
    private lateinit var ttsManager: TTSManager
    private lateinit var audioEngine: AudioEngine

    private lateinit var editPlayerName: EditText
    private lateinit var seekBarTTS: SeekBar
    private lateinit var seekBarMusic: SeekBar
    private lateinit var seekBarSFX: SeekBar
    private lateinit var textTTSValue: TextView
    private lateinit var textMusicValue: TextView
    private lateinit var textSFXValue: TextView
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Get audio components from main activity
        val mainActivity = TopSpeedActivity.getInstance()
        if (mainActivity != null) {
            ttsManager = mainActivity.getTTSManager()
            audioEngine = mainActivity.getAudioEngine()
            prefs = UserPreferences(this)
        } else {
            // Fallback - create new instances
            prefs = UserPreferences(this)
            ttsManager = TTSManager(this)
            ttsManager.initialize()
            audioEngine = AudioEngine(this)
            audioEngine.initialize()
        }

        initViews()
        loadCurrentSettings()
        setupListeners()
    }

    private fun initViews() {
        editPlayerName = findViewById(R.id.editPlayerName)
        seekBarTTS = findViewById(R.id.seekBarTTS)
        seekBarMusic = findViewById(R.id.seekBarMusic)
        seekBarSFX = findViewById(R.id.seekBarSFX)
        textTTSValue = findViewById(R.id.textTTSValue)
        textMusicValue = findViewById(R.id.textMusicValue)
        textSFXValue = findViewById(R.id.textSFXValue)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadCurrentSettings() {
        // Load player name
        editPlayerName.setText(prefs.playerName)

        // Load TTS volume
        val ttsVolume = (prefs.ttsVolume * 100).toInt()
        seekBarTTS.progress = ttsVolume
        textTTSValue.text = "$ttsVolume%"

        // Load Music volume
        val musicVolume = (prefs.musicVolume * 100).toInt()
        seekBarMusic.progress = musicVolume
        textMusicValue.text = "$musicVolume%"

        // Load SFX volume
        val sfxVolume = (prefs.sfxVolume * 100).toInt()
        seekBarSFX.progress = sfxVolume
        textSFXValue.text = "$sfxVolume%"
    }

    private fun setupListeners() {
        // TTS SeekBar
        seekBarTTS.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textTTSValue.text = "$progress%"
                ttsManager.setVolume(progress / 100f)
                // Preview TTS
                if (fromUser) {
                    ttsManager.speak("$progress phần trăm", com.topspeed.audio.audio.TTSManager.Priority.HIGH)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Music SeekBar
        seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textMusicValue.text = "$progress%"
                audioEngine.setMusicVolume(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // SFX SeekBar
        seekBarSFX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSFXValue.text = "$progress%"
                audioEngine.setSfxVolume(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Save button
        btnSave.setOnClickListener {
            saveSettings()
        }

        // Back button
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveSettings() {
        // Save player name
        val name = editPlayerName.text.toString().trim()
        if (name.isNotBlank()) {
            prefs.playerName = name
            ttsManager.speak("Đã lưu tên $name", com.topspeed.audio.audio.TTSManager.Priority.HIGH)
        }

        // Save volumes
        prefs.ttsVolume = seekBarTTS.progress / 100f
        prefs.musicVolume = seekBarMusic.progress / 100f
        prefs.sfxVolume = seekBarSFX.progress / 100f

        ttsManager.speak("Đã lưu cài đặt", com.topspeed.audio.audio.TTSManager.Priority.HIGH)

        // Finish after short delay
        editPlayerName.postDelayed({
            finish()
        }, 500)
    }

    override fun onBackPressed() {
        // Save before exiting
        saveSettings()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup if we created new instances
        if (TopSpeedActivity.getInstance() == null) {
            ttsManager.shutdown()
            audioEngine.shutdown()
        }
    }
}