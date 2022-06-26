package pie.activity_recognition.sensors_service

import java.lang.Exception

object SensorsUtils {
    fun compassDirection(angle: Double): String {
        if (angle >= 350 || angle <= 10) // from { 10 - 0|360 - 350 }
            return "N"
        else if (280 < angle && angle < 350) // { 280. - 350. }
            return "NW"
        else if (260 < angle && angle <= 280) // { 260. - 280 }
            return "W"
        else if (190 < angle && angle <= 260) // { 190. - 260 }
            return "SW"
        else if (170 < angle && angle <= 190) // { 170. - 190 }
            return "S"
        else if (100 < angle && angle <= 170) // { 100. - 170 }
            return "SE"
        else if (80 < angle && angle <= 170) // { 80. - 170 }
            return "E"
        else if (10 < angle && angle <= 80) // { 10. - 80 }
            return "NE"
        throw Exception("Compass got unknown angle")
    }
}