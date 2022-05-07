package cryptography

import javax.imageio.ImageIO
import java.io.File
import java.awt.Color
import kotlin.experimental.xor

fun main() {
    do {
        println("Task (hide, show, exit):")
        val input = readln()
        when (input) {
            "exit" -> println("Bye!")
            "hide" -> {
                try {
                    println("Input image file:")
                    val inputImageFile = readln()
                    val bufferedImage = ImageIO.read(File(inputImageFile))
                    println("Output image file:")
                    val outputImageFile = readln()
                    val outputFile = File(outputImageFile)
                    if (outputFile.isDirectory) throw Exception("This is not a file, is a directory!")
                    else if (!outputFile.exists()) {
                        outputFile.createNewFile()
                    }
                    if (outputFile.canWrite()) {
                        println("Message to hide:")
                        val message = readln()
                        val array = message.encodeToByteArray()
                        println("Password:")
                        val password = readln()
                        val passwordArray = password.encodeToByteArray()
                        val encryptedBytes = mutableListOf<Byte>()
                        var indexEncryptedBytesArray = 0
                        for (i in array) {
                            encryptedBytes.add(i xor passwordArray[indexEncryptedBytesArray])
                            ++indexEncryptedBytesArray
                            if (indexEncryptedBytesArray == passwordArray.size) indexEncryptedBytesArray = 0
                        }
                        encryptedBytes.addAll("\u0000\u0000\u0003".encodeToByteArray().toMutableList())
                        val bits = mutableListOf<Int>()
                        for (i in encryptedBytes) {
                            val bitString = i.toString(2).padStart(8,'0')
                            for (j in bitString) {
                                bits.add(j.code - 48)
                            }
                        }
                        if (bits.size > bufferedImage.width * bufferedImage.height) {
                            println("The input image is not large enough to hold this message.")
                        } else {
                            var position = 0
                            loop@ for (y in 0 until bufferedImage.height) {          // For every row
                                for (x in 0 until bufferedImage.width) {               // For every column.
                                    val color = Color(bufferedImage.getRGB(x, y))  // Read color from the (x, y) position
                                    val r = color.red
                                    val g = color.green
                                    val b = color.blue and 254 or bits[position] % 256     // Access the Blue color value
                                    // Use color.red in case the Red color is needed

                                    val colorNew =
                                        Color(r, g, b)  // Create a new Color instance with the red value equal to 255
                                    bufferedImage.setRGB(x, y, colorNew.rgb)  // Set the new color at the (x, y) position
                                    ++position
                                    if (position >= bits.size) break@loop
                                }
                            }
                        }
                        ImageIO.write(bufferedImage, "png", outputFile)
                        println("Input Image: $inputImageFile")
                        println("Output Image: $outputImageFile")
                        println("Message saved in $outputImageFile image.")
                    }
                    else throw Exception("Can't write to this file!")
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            "show" -> {
                try {
                    println("Input image file:")
                    val inputImageFile = readln()
                    val bufferedImage = ImageIO.read(File(inputImageFile))
                    val bits = mutableListOf<Int>()
                    val bytes = mutableListOf<Byte>()
                    var count = 1
                    loop@ for (y in 0 until bufferedImage.height) {          // For every row
                        for (x in 0 until bufferedImage.width) {
                            val color = Color(bufferedImage.getRGB(x, y))  // Read color from the (x, y) position
                            bits.add(color.blue and 1)            // Access the Blue color value
                            if (count % 8 == 0) {
                                bytes.add(bits.joinToString("").toByte(2))
                                bits.clear()
                            }
                            ++count
                            if (count >= 32 && bytes.subList(bytes.size-3, bytes.size) == mutableListOf<Byte>(0,0,3))
                                break@loop
                        }
                    }
                    repeat(3) {
                        bytes.removeLast()
                    }
                    // bytes contains the encrypted bytes.
                    println("Password:")
                    val password = readln()
                    val passwordArray = password.encodeToByteArray()
                    val decryptedBytes = mutableListOf<Byte>()
                    var indexDecryptedBytesArray = 0
                    for (i in bytes) {
                        decryptedBytes.add(i xor passwordArray[indexDecryptedBytesArray])
                        ++indexDecryptedBytesArray
                        if (indexDecryptedBytesArray == passwordArray.size) indexDecryptedBytesArray = 0
                    }
                    val message = decryptedBytes.toByteArray().toString(Charsets.UTF_8)
                    println("Message: $message")
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            else -> println("Wrong task: $input")
        }
    } while(input != "exit")
}

