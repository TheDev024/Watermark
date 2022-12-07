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

object Terminator {
    fun terminate(message: String, status: Int): Nothing {
        println(message)
        exitProcess(status)
    }
}

class Watermark {

    fun cmd() {
        // get input image
        val imagePath = Reader.getString("Input the image filename:")
        val imageFile = File(imagePath)
        val image = if (imageFile.exists()) ImageIO.read(imageFile) else Terminator.terminate(
            "The file $imagePath doesn't exist.", 1
        )

        // validate input image
        if (image.colorModel.numColorComponents != 3) Terminator.terminate(
            "The number of image color components isn't 3.", 2
        )
        if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) Terminator.terminate(
            "The image isn't 24 or 32-bit.", 3
        )

        // get watermark image
        val watermarkPath = Reader.getString("Input the watermark image filename:")
        val watermarkFile = File(watermarkPath)
        val watermark = if (watermarkFile.exists()) ImageIO.read(watermarkFile) else Terminator.terminate(
            "The file $watermarkPath doesn't exist.", 4
        )

        // validate watermark
        if (watermark.colorModel.numColorComponents != 3) Terminator.terminate(
            "The number of watermark color components isn't 3.", 5
        )
        if (watermark.colorModel.pixelSize != 24 && watermark.colorModel.pixelSize != 32) Terminator.terminate(
            "The watermark isn't 24 or 32-bit.", 6
        )
        if (image.width < watermark.width || image.height < watermark.height) Terminator.terminate(
            "The watermark's dimensions are larger.", 7
        )

        // control for watermark transparency
        val translucent = watermark.transparency == Transparency.TRANSLUCENT
        val transparent = if (translucent) Reader.getString("Do you want to use the watermark's Alpha channel?")
            .lowercase() == "yes" else false

        // get watermark background color if needed
        val backgroundColor = if (translucent) null else askBackgroundColor()

        // get transparency weight
        val weightStr = Reader.getString("Input the watermark transparency percentage (Integer 0-100):")
        val weight = if (Regex("^\\d+$").matches(weightStr)) weightStr.toInt() else Terminator.terminate(
            "The transparency percentage isn't an integer number.", 8
        )
        if (weight !in 0..100) Terminator.terminate("The transparency percentage is out of range.", 9)

        val positionMethod = Reader.getString("Choose the position method (single, grid):").lowercase()
        if (positionMethod != "single" && positionMethod != "grid") Terminator.terminate(
            "The position method input is invalid.", 10
        )

        val watermarkPosition: Pair<Int, Int>? =
            if (positionMethod == "single") getWatermarkPosition(image, watermark) else null

        // get output filename
        val outputPath = Reader.getString("Input the output image filename (jpg or png extension):")
        val outputFormat: String
        val output = if (Regex(".+(\\.jpg|\\.png)$").matches(outputPath)) {
            outputFormat = outputPath.substring(outputPath.length - 3, outputPath.length)
            image
        } else Terminator.terminate("The output file extension isn't \"jpg\" or \"png\".", 11)

        val startX = watermarkPosition?.first ?: 0
        val startY = watermarkPosition?.second ?: 0

        val endX = watermarkPosition?.first?.plus(watermark.width) ?: image.width
        val endY = watermarkPosition?.second?.plus(watermark.height) ?: image.height

        for (x in startX until endX) for (y in startY until endY) {
            val imageColor = Color(image.getRGB(x, y))
            val watermarkColor = Color(watermark.getRGB((x - startX) % watermark.width, (y - startY) % watermark.height), true)
            val outputColor = getColor(imageColor, watermarkColor, weight, transparent, backgroundColor)
            output.setRGB(x, y, outputColor.rgb)
        }
        val outputFile = File(outputPath)
        ImageIO.write(output, outputFormat, outputFile)
        println("The watermarked image $outputPath has been created.")
    }

    private fun getWatermarkPosition(image: BufferedImage, watermark: BufferedImage): Pair<Int, Int> {
        val diffX = image.width - watermark.width
        val diffY = image.height - watermark.height

        val position = Reader.getString("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
        val x: Int
        val y: Int
        try {
            val positions = position.split(" ")
            x = positions[0].toInt()
            y = positions[1].toInt()
        } catch (e: Exception) {
            Terminator.terminate("The position input is invalid.", 13)
        }

        if (x !in 0..diffX || y !in 0..diffY) Terminator.terminate("The position input is out of range.", 14)

        return Pair(x, y)
    }

    private fun getColor(
        imageColor: Color, watermarkColor: Color, weight: Int, transparent: Boolean, backgroundColor: Color? = null
    ): Color {
        val imgR = imageColor.red
        val imgG = imageColor.green
        val imgB = imageColor.blue

        val wmkR = watermarkColor.red
        val wmkG = watermarkColor.green
        val wmkB = watermarkColor.blue
        val wmkA = watermarkColor.alpha

        return if ((transparent && wmkA == 0) || backgroundColor == watermarkColor) imageColor else {
            val r = (weight * wmkR + (100 - weight) * imgR) / 100
            val g = (weight * wmkG + (100 - weight) * imgG) / 100
            val b = (weight * wmkB + (100 - weight) * imgB) / 100
            Color(r, g, b)
        }
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
                } else Terminator.terminate("The transparency color input is invalid.", 12)
            } catch (e: Exception) {
                Terminator.terminate("The transparency color input is invalid.", 12)
            }
        } else null
    }

}

fun main() {
    val watermark = Watermark()
    watermark.cmd()
}
