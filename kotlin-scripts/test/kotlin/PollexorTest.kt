package pollexor

import com.squareup.pollexor.Thumbor
import com.squareup.pollexor.ThumborUrlBuilder
import io.kotlintest.specs.StringSpec
import osxOpenUrl
import com.squareup.pollexor.ThumborUrlBuilder.*
import okio.Buffer
import org.intellij.lang.annotations.Language
import org.zeroturnaround.exec.ProcessExecutor



class PollexorTest : StringSpec() { init {

    val painting_758x600 = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Eug%C3%A8ne_Delacroix_-_La_libert%C3%A9_guidant_le_peuple.jpg/758px-Eug%C3%A8ne_Delacroix_-_La_libert%C3%A9_guidant_le_peuple.jpg?uselang=en-gb"

    val carcassonne_2022x867 = "https://upload.wikimedia.org/wikipedia/commons/4/4d/Carcassonne.noc.2007.JPG"

    val thumbor = Thumbor.create("http://localhost:8888/")

    val installation_ok = 0 == ProcessExecutor()
        .command("which", "thumbor")
        .execute()
        .exitValue

    val buffer = Buffer()
    val launchThumbor = ProcessExecutor()
        .redirectOutput(buffer.outputStream())
        .command("thumbor")
        .executeNoTimeout()

    val testInstallationOk = if (installation_ok) "Check installation" else "Error: install http://thumbor.org/"
    testInstallationOk {
        installation_ok shouldBe true
    }

    "Simple resize" {
        val url = thumbor
            .buildImage(carcassonne_2022x867)
            .resize(1000, 0)
            .toUrl()
        osxOpenUrl(url)
    }.config(ignored = !installation_ok)


    "Resize and smart crop" {
        val url = thumbor.buildImage(painting_758x600)
            .crop(0, 200, 400, 600)
            .resize(200, 200)
            .smart()
            .toUrl()
        osxOpenUrl(url)
    }.config(ignored = !installation_ok)

    "Image formats" {
        val url = thumbor.buildImage(painting_758x600)
            .filter(
                roundCorner(10),
                format(ThumborUrlBuilder.ImageFormat.WEBP),
                watermark("https://licensebuttons.net/l/by-nc-sa/3.0/88x31.png")
            )
            .toUrl()
        osxOpenUrl(url)
    }.config(ignored = !installation_ok)

    "Circular image" {
        val obama = "http://365news.biz/uploads/posts/2016-11/1479160489_obama.jpeg"
        val url = thumbor.buildImage(obama)
            .resize(48, 48)
            .filter(
                roundCorner(24),
                format(ImageFormat.JPEG)
            )
            .toUrl()
        osxOpenUrl(url)

    }.config(ignored = !installation_ok)

    "Logs" {
        println(buffer.readUtf8())
    }

}
}