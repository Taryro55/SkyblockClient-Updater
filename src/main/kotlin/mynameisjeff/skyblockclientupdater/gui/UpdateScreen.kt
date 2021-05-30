package mynameisjeff.skyblockclientupdater.gui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.utils.TickTask
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
    private var complete = false
    private var exited = false
    private var backButton: GuiButton = GuiButton(0, 0, 0, 200, 20, "")
    private var progress = 0f

    private var downloadedMods = arrayListOf<File>()
    private var failedDownloadingMods = arrayListOf<File>()

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
                            val file = File(directory, jarName)
                            downloadUpdate(url, file)
                            if (!failedDownloadingMods.contains(file)) {
                                downloadedMods.add(file)
                                UpdateChecker.deleteFileOnShutdown(update.first, jarName)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun updateText() {
        backButton.displayString = if (exited || complete) "Back" else "Cancel"
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
                failedDownloadingMods.add(location)
                updateText()
                println(url + " returned status code " + st.responseCode)
                return
            }
            location.parentFile.mkdirs()
            if (!location.exists() && !location.createNewFile()) {
                failedDownloadingMods.add(location)
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
                return
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            failedDownloadingMods.add(location)
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
            exited -> drawCenteredString(
                mc.fontRendererObj,
                EnumChatFormatting.RED.toString() + "Update download exited",
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
            downloadedMods.size + failedDownloadingMods.size == updatingMods.size -> {
                complete = true
                updateText()
                TickTask(1) {
                    mc.displayGuiScreen(UpdateSummaryScreen(downloadedMods, failedDownloadingMods))
                }
            }
            else -> {
                drawCenteredString(mc.fontRendererObj,"Downloading... ${UpdateChecker.needsDelete.size} / ${updatingMods.size}", width / 2, height / 2, 0xFFFFFF)
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