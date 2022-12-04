package watermark

import java.io.File
import java.util.*
import javax.imageio.ImageIO

val scanner = Scanner(System.`in`)

fun main() {
    print("Input the image filename: ")
    val path = scanner.nextLine()
    val file = File(path)
    if (file.exists()) {
        val image = ImageIO.read(file)
        println("Image file: $path")
        println("Width: ${image.width}")
        println("Height: ${image.height}")
        println("Number of components: ${image.colorModel.numComponents}")
        println("Number of color components: ${image.colorModel.numColorComponents}")
        println("Bits per pixel: ${image.colorModel.pixelSize}")
        println(
            "Transparency: ${
                when (image.transparency) {
                    1 -> "OPAQUE"
                    else -> "TRANSLUCENT"
                }
            }"
        )
    } else println("The file $path doesn't exist.")
}