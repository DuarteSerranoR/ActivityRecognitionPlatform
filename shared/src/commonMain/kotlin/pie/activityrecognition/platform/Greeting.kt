package pie.activityrecognition.platform

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}