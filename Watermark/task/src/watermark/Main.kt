package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.exitProcess

val scanner = Scanner(System.`in`)

class Watermark {
    fun cmd() {
        println("Input the image filename:")
        val imagePath = scanner.nextLine()
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            val image = ImageIO.read(imageFile)
            if (image.colorModel.numColorComponents == 3) if (image.colorModel.pixelSize == 24 || image.colorModel.pixelSize == 32) {
                println("Input the watermark image filename:")
                val watermarkPath = scanner.nextLine()
                val watermarkFile = File(watermarkPath)
                if (watermarkFile.exists()) {
                    val watermark = ImageIO.read(watermarkFile)
                    if (watermark.colorModel.numColorComponents == 3) if (watermark.colorModel.pixelSize == 24 || watermark.colorModel.pixelSize == 32) if (image.width == watermark.width && image.height == watermark.height) {
                        println("Input the watermark transparency percentage (Integer 0-100):")
                        val weightStr = scanner.nextLine()
                        if (Regex("\\d+").matches(weightStr)) {
                            val weight = weightStr.toInt()
                            if (weight in 0..100) {
                                println("Input the output image filename (jpg or png extension):")
                                val outputPath = scanner.nextLine()
                                if (Regex(".+(\\.jpg|\\.png)$").matches(outputPath)) {
                                    val outputFormat = outputPath.substring(outputPath.length - 3, outputPath.length)
                                    val output = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
                                    for (x in 0 until image.width) for (y in 0 until image.height) {
                                        val imageColor = Color(image.getRGB(x, y))
                                        val watermarkColor = Color(watermark.getRGB(x, y))
                                        val r = (weight * watermarkColor.red + (100 - weight) * imageColor.red) / 100
                                        val g =
                                            (weight * watermarkColor.green + (100 - weight) * imageColor.green) / 100
                                        val b = (weight * watermarkColor.blue + (100 - weight) * imageColor.blue) / 100
                                        val outputColor = Color(r, g, b)
                                        output.setRGB(x, y, outputColor.rgb)
                                    }
                                    val outputFile = File(outputPath)
                                    ImageIO.write(output, outputFormat, outputFile)
                                    println("The watermarked image $outputPath has been created.")
                                } else terminate("The output file extension isn't \"jpg\" or \"png\".", 10)
                            } else terminate("The transparency percentage is out of range.", 9)
                        } else terminate("The transparency percentage isn't an integer number.", 8)
                    } else terminate("The image and watermark dimensions are different.", 7)
                    else terminate("The watermark isn't 24 or 32-bit.", 6)
                    else terminate("The number of watermark color components isn't 3.", 5)
                } else terminate("The file $watermarkPath doesn't exist.", 4)
            } else terminate("The image isn't 24 or 32-bit.", 3)
            else terminate("The number of image color components isn't 3.", 2)
        } else terminate("The file $imagePath doesn't exist.", 1)
    }

    private fun terminate(message: String, status: Int): Nothing {
        println(message)
        exitProcess(status)
    }
}

fun main() {
    val watermark = Watermark()
    watermark.cmd()
}
