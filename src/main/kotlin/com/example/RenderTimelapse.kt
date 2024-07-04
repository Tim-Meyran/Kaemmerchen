package com.example

import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

fun main() {
    createTimelapse()
}

private fun createTimelapse() {
    val directory = File("timelapse")
    directory.mkdir()

    val directory2 = File("timelapse_repaired")
    directory2.mkdir()

    val imageFileList = File("timelapse_repaired/image-list.txt")
    if (!imageFileList.exists()) imageFileList.createNewFile()

    imageFileList.writeText("")

    val outFiles = mutableListOf<File>()
    val files = directory.listFiles()?.filter { it.isFile }
    files?.filter { it.isFile && it.name.endsWith(".png") }
        ?.forEach { file ->
            try {
                val outFile = File(directory2.absoluteFile.path + "/" + file.nameWithoutExtension + ".jpg")
                println(outFile)
                if (!outFile.exists()) {
                    val picture: BufferedImage = ImageIO.read(file)
                    ImageIO.write(picture, "jpg", outFile);
                }
                outFiles.add(outFile)
            } catch (ex: Exception) {
            }

        } ?: println("No files found or directory does not exist.")

    directory2.listFiles()?.filter { it.isFile }?.filter { it.name.endsWith("jpg") }?.sortedBy { file ->
        val split = file.nameWithoutExtension.split("_")
        if (split.size > 1) {
            split[1].toLong()
        } else {
            0 //file.toPath().getLastModifiedTime()
        }
    }?.forEach { file -> imageFileList.appendText("file '${file.name}'\n") }

    val timelapseCmd = listOf(
        "ffmpeg",
        "-f",
        "concat",
        "-i",
        "image-list.txt",
        "-s:v",
        "720x1280",
        "-c:v",
        "libx264",
        "-crf",
        "17",
        "-pix_fmt",
        "yuv420p",
        "-y",
        "-aspect",
        "720/1280",
        "timelapse.mp4"
    )
    //PropertiesReader.getProperty("TIMELAPSE_CMD")

    println("Executing <$timelapseCmd>")
    val p: Process = ProcessBuilder(timelapseCmd)
        .directory(directory2)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()

    p.waitFor(240, TimeUnit.SECONDS)
}
