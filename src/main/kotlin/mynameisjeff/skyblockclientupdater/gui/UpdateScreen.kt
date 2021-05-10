package mynameisjeff.skyblockclientupdater.gui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.utils.UpdateChecker
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.EnumChatFormatting
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Taken from Skytils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE
 */
class UpdateScreen(private val updatingMods: List<Triple<File, String, String>>) : GuiScreen() {
    private var failed = false
    private var complete = false
    private var exited = false
    private var backButton: GuiButton = GuiButton(0, 0, 0, 200, 20, "")
    private var progress = 0f

    private var completedDownloads = 0

    override fun initGui() {
        backButton.xPosition = width / 2 - 100
        backButton.yPosition = height / 4 + 132
        buttonList.add(backButton)
        updateText()
    }

    private fun doUpdate() {
        thread(name = "SkyClient Updater Thread") {
            runBlocking {
                try {
                    val directory = File(File(SkyClientUpdater.mc.mcDataDir, "skyclientupdater"), "updates")
                    directory.mkdirs()
                    for (update in updatingMods) {
                        launch(Dispatchers.IO) {
                            val jarName = update.second
                            val url = update.third
                            downloadUpdate(url, File(directory, jarName))
                            if (!failed) {
                                UpdateChecker.deleteFileOnShutdown(update.first, jarName)
                            }
                        }.invokeOnCompletion {
                            completedDownloads++
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun updateText() {
        backButton.displayString = if (failed || complete) "Back" else "Cancel"
    }

    private fun downloadUpdate(url: String, location: File) {
        try {
            val st = URL(url).openConnection() as HttpURLConnection
            st.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"
            )
            st.connect()
            if (st.responseCode != HttpURLConnection.HTTP_OK) {
                failed = true
                updateText()
                println(url + " returned status code " + st.responseCode)
                return
            }
            location.parentFile.mkdirs()
            if (!location.exists() && !location.createNewFile()) {
                failed = true
                updateText()
                println("Couldn't create update file directory")
                return
            }
            val fileLength = st.contentLength.toFloat()
            val fis = st.inputStream
            val fos: OutputStream = FileOutputStream(location)
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            while (fis.read(data).also { count = it } != -1) {
                if (exited) {
                    // Cancelled
                    fos.close()
                    fis.close()
                    failed = true
                    return
                }
                total += count.toLong()
                progress = total / fileLength
                fos.write(data, 0, count)
            }
            fos.flush()
            fos.close()
            fis.close()
            if (exited) {
                failed = true
                return
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            failed = true
            updateText()
        }
    }

    public override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(null)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        when {
            failed -> drawCenteredString(
                mc.fontRendererObj,
                EnumChatFormatting.RED.toString() + "Update download failed",
                width / 2,
                height / 2,
                -0x1
            )
            complete -> drawCenteredString(
                mc.fontRendererObj,
                EnumChatFormatting.GREEN.toString() + "Update download complete",
                width / 2,
                height / 2,
                0xFFFFFF
            )
            else -> {
                drawCenteredString(mc.fontRendererObj,"Downloading... ${UpdateChecker.needsDelete.size} / ${updatingMods.size}", width / 2, height / 2, 0xFFFFFF)
                if (!failed && completedDownloads == updatingMods.size) {
                    mc.shutdown()
                    complete = true
                    updateText()
                }
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun onGuiClosed() {
        exited = true
    }

    init {
        doUpdate()
    }
}