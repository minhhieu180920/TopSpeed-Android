package com.topspeed.audio.audio

/**
 * Các định nghĩa cho audio system
 * Port từ game gốc - mapping tên âm thanh với đường dẫn
 */
object AudioDefinitions {

    // Menu sounds - sẽ được TTS đọc
    object Menu {
        const val MAIN_MENU = "en/menu/main"
        const val QUICK_START = "en/menu/quickstart"
        const val TIME_TRIAL = "en/menu/timetrial"
        const val SINGLE_RACE = "en/menu/singlerace"
        const val MULTIPLAYER = "en/menu/multiplayergame"
        const val OPTIONS = "en/menu/options"
        const val EXIT = "en/menu/exitgame"
        const val ARE_YOU_SURE = "en/menu/areyousure"
        const val YES = "en/menu/yes"
        const val NO = "en/menu/no"
        const val GO_BACK = "en/menu/goback"
        const val SAVED = "en/menu/saved"
        const val SELECT_A_TRACK = "en/menu/selectatrack"
        const val SELECT_A_VEHICLE = "en/menu/selectavehicle"
        const val LAPS = "en/menu/laps"
        const val DIFFICULTY = "en/menu/difficulty"
        const val EASY = "en/menu/easy"
        const val NORMAL = "en/menu/normal"
        const val HARD = "en/menu/hard"
        const val AUTOMATIC = "en/menu/automatictransmission"
        const val MANUAL = "en/menu/manualtransmission"
        const val STARTING_SERVER = "en/menu/startingserver"
        const val JOINING_GAME = "en/menu/joininggame"
        const val SERVER_FOUND = "en/menu/serverfound"
        const val CONNECTION_TIMEOUT = "en/menu/connectiontimeout"
    }

    // Race announcements - sẽ được TTS đọc
    object Race {
        const val READY = "en/menu/ready"
        const val GO = "en/menu/go"
        const val RACE_IN_PROGRESS = "en/menu/raceinprogress"
        const val CURRENT_LAP = "en/menu/currentlapnr"
        const val CURRENT_TIME = "en/menu/currentracetime"
        const val POSITION = "en/menu/positiononly"
        const val LAP_COMPLETE = "en/menu/lapcomplete"
        const val RACE_FINISH = "en/menu/racefinish"
        const val NEW_BEST_TIME = "en/menu/newbesttime"
        const val CURRENT_GEAR = "en/menu/currentgear"
        const val CURVE_ANNOUNCEMENT = "en/menu/curveannouncement"
        const val PAUSED = "en/menu/paused"
    }

    // Gameplay sounds - giữ nguyên người lồng tiếng từ game gốc
    object Gameplay {
        const val ENGINE = "en/gameplay/engine"
        const val CAR_START = "en/gameplay/carstart"
        const val CRASH = "en/gameplay/crash"
        const val BUMP_LEFT = "en/gameplay/bumpleft"
        const val BUMP_RIGHT = "en/gameplay/bumpright"
        const val CURB_LEFT = "en/gameplay/curbleft"
        const val CURB_RIGHT = "en/gameplay/curbright"
        const val BRAKE = "en/gameplay/brake"
        const val HORN = "en/gameplay/horn"
        const val BACKFIRE = "en/gameplay/backfire"
        const val GRAVEL = "en/gameplay/gravel"
        const val WATER = "en/gameplay/water"
        const val SAND = "en/gameplay/sand"
        const val SNOW = "en/gameplay/snow"
        const val SKID = "en/gameplay/skid"
    }

    // Curve warning sounds
    object Curves {
        const val EASY_LEFT = "en/gameplay/easyleft"
        const val LEFT = "en/gameplay/left"
        const val HARD_LEFT = "en/gameplay/hardleft"
        const val HAIRPIN_LEFT = "en/gameplay/hairpinleft"
        const val EASY_RIGHT = "en/gameplay/easyright"
        const val RIGHT = "en/gameplay/right"
        const val HARD_RIGHT = "en/gameplay/hardright"
        const val HAIRPIN_RIGHT = "en/gameplay/hairpinright"
        const val ASPHALT = "en/gameplay/asphalt"
    }

    // Numbers for counting (TTS sẽ đọc)
    object Numbers {
        val ALL = (0..100).map { "en/numbers/$it" }
    }

    /**
     * Lấy đường dẫn âm thanh từ tên
     */
    fun getSoundPath(name: String): String {
        return "$name.ogg"
    }

    /**
     * Kiểm tra âm thanh có cần TTS không
     * Menu sounds và number announcements cần TTS
     */
    fun needsTTS(category: String): Boolean {
        return category.startsWith("en/menu") ||
               category.startsWith("en/numbers")
    }
}
