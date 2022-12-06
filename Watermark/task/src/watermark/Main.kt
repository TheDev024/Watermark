package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.system.exitProcess

val scanner = Scanner(System.`in`)

object Reader {
    fun getString(prompt: String): String {
        println(prompt)
        return scanner.nextLine()
    }

    fun getInt(prompt: String): Int {
        println(prompt)
        return scanner.nextInt()
    }

    fun getDouble(prompt: String): Double {
        println(prompt)
        return scanner.nextDouble()
    }
}

class Watermark {

    fun cmd() {
        val imagePath = Reader.getString("Input the image filename:") // get input image path
        val imageFile = File(imagePath)
        if (imageFile.exists()) { // check for the existence of input image
            val image = ImageIO.read(imageFile) // create a buffered image
            if (image.colorModel.numColorComponents == 3) if (image.colorModel.pixelSize == 24 || image.colorModel.pixelSize == 32) { // check for color components and pixel size
                val watermarkPath = Reader.getString("Input the watermark image filename:") // get watermark image path
                val watermarkFile = File(watermarkPath)
                if (watermarkFile.exists()) { // check for watermark image existence
                    val watermark = ImageIO.read(watermarkFile) // create buffered image
                    if (watermark.colorModel.numColorComponents == 3) if (watermark.colorModel.pixelSize == 24 || watermark.colorModel.pixelSize == 32) if (image.width == watermark.width && image.height == watermark.height) {
                        val translucent = watermark.transparency == Transparency.TRANSLUCENT
                        val useAlphaChannel =
                            if (translucent) Reader.getString("Do you want to use the watermark's Alpha channel?")
                                .lowercase() == "yes" else false
                        val backgroundColor = if (translucent) null else askBackgroundColor()
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
                                    if (translucent && useAlphaChannel) for (x in 0 until image.width) for (y in 0 until image.height) {
                                        val imageColor = Color(image.getRGB(x, y))
                                        val watermarkColor = Color(watermark.getRGB(x, y), true)
                                        val transparent = watermarkColor.alpha / 255 == 0
                                        val r =
                                            if (transparent) imageColor.red else (weight * watermarkColor.red + (100 - weight) * imageColor.red) / 100
                                        val g =
                                            if (transparent) imageColor.green else (weight * watermarkColor.green + (100 - weight) * imageColor.green) / 100
                                        val b =
                                            if (transparent) imageColor.blue else (weight * watermarkColor.blue + (100 - weight) * imageColor.blue) / 100
                                        val outputColor = Color(r, g, b)
                                        output.setRGB(x, y, outputColor.rgb)
                                    } else if (backgroundColor != null) for (x in 0 until image.width) for (y in 0 until image.height) {
                                        val imageColor = Color(image.getRGB(x, y))
                                        val watermarkColor = Color(watermark.getRGB(x, y))
                                        val transparent = watermarkColor == backgroundColor
                                        val r =
                                            if (transparent) imageColor.red else (weight * watermarkColor.red + (100 - weight) * imageColor.red) / 100
                                        val g =
                                            if (transparent) imageColor.green else (weight * watermarkColor.green + (100 - weight) * imageColor.green) / 100
                                        val b =
                                            if (transparent) imageColor.blue else (weight * watermarkColor.blue + (100 - weight) * imageColor.blue) / 100
                                        val outputColor = Color(r, g, b)
                                        output.setRGB(x, y, outputColor.rgb)
                                    } else for (x in 0 until image.width) for (y in 0 until image.height) {
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

    private fun askBackgroundColor(): Color? {
        return if (Reader.getString("Do you want to set a transparency color?") == "yes") {
            println()
            try {
                val rgbString = Reader.getString("Input a transparency color ([Red] [Green] [Blue]):")
                if (Regex("\\d+\\s\\d+\\s\\d+").matches(rgbString)) {
                    val rgb = rgbString.split(" ")
                    val r = rgb[0].toInt()
                    val g = rgb[1].toInt()
                    val b = rgb[2].toInt()
                    return Color(r, g, b)
                } else terminate("The transparency color input is invalid.", 13)
            } catch (e: Exception) {
                terminate("The transparency color input is invalid.", 11)
            }
        } else null
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
