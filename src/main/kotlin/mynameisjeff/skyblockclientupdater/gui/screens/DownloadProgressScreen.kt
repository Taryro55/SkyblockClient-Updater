package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.api.utils.Multithreading
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.BasicState
import gg.essential.universal.UMatrixStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import mynameisjeff.skyblockclientupdater.utils.TickTask
import mynameisjeff.skyblockclientupdater.utils.UpdateChecker
import net.minecraft.client.gui.GuiMainMenu
import org.apache.logging.log4j.LogManager
import java.awt.Color
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class DownloadProgressScreen(
    private val updating: HashSet<Triple<File, String, String>>
) : BaseScreen(
    useContentContainer = true
) {
    private val successfullyUpdated = mutableSetOf<Triple<File, String, String>>()
    private val failedUpdated = mutableSetOf<Triple<File, String, String>>()

    private var exited = false

    val headerText = UIText("Updating your mods...").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }.setTextScale(1.25f.pixels()) childOf headerContainer

    val currentlyUpdatingText = UIText().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf contentContainer
    val progressBarContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = SiblingConstraint(5f)
        width = 200.pixels()
        height = 10.pixels()
    } effect OutlineEffect(Color.BLACK, 2f) childOf contentContainer
    val progressBar = UIBlock(SkyClientUpdater.accentColor).constrain {
        height = 10.pixels()
    } childOf progressBarContainer

    private val buttonContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf footerContainer
    private val cancelButton = SexyButton(
        text = "Cancel",
        outlineColor = Color.RED,
        primary = false
    ).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        exited = true
    } childOf buttonContainer

    init {
        thread(name = "SkyClient Updater Thread") {
            runBlocking {
                try {
                    val directory = File(File(SkyClientUpdater.mc.mcDataDir, "skyclientupdater"), "updates")
                    directory.mkdirs()
                    for (update in updating) {
                        launch(Dispatchers.IO) {
                            val jarName = update.second
                            val file = File(directory, jarName)
                            downloadUpdate(update, file)
                            if (!failedUpdated.contains(update)) {
                                UpdateChecker.deleteFileOnShutdown(update.first, jarName)
                                successfullyUpdated.add(update)
                            }
                        }
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun downloadUpdate(update: Triple<File, String, String>, file: File) {
        try {
            currentlyUpdatingText.setText(update.second)
            val logger = LogManager.getLogger("SkyClientUpdater (Update Downloader)")
            val st = URL(update.third).openConnection() as HttpURLConnection
            st.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"
            )
            st.connect()
            if (st.responseCode != HttpURLConnection.HTTP_OK) {
                failedUpdated.add(update)
                logger.error(update.third + " returned status code " + st.responseCode)
                return
            }
            update.first.parentFile.mkdirs()
            if (!update.first.exists() && !update.first.createNewFile()) {
                failedUpdated.add(update)
                logger.error("Couldn't create update directory/file for ${update.second}.")
                return
            }

            st.inputStream.use { stream ->
                BufferedInputStream(stream).use { bufferedStream ->
                    FileOutputStream(file).use { output ->
                        val data = ByteArray(1024)
                        var count: Int
                        val progress = BasicState(0L)
                        progress.onSetValue {
                            progressBar.animate {
                                Multithreading.runAsync {
                                    setWidthAnimation(
                                        Animations.OUT_EXP,
                                        0.5f,
                                        ((if (it == 0L) 1 else it / st.contentLengthLong) * 200).pixels()
                                    )
                                }
                            }
                        }

                        while (bufferedStream.read(data, 0, 1024).also { count = it } != -1) {
                            progress.set(progress.get() + count)
                            output.write(data, 0, count)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            failedUpdated.add(update)
        }
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        when {
            exited -> {
                val directory = File(File(SkyClientUpdater.mc.mcDataDir, "skyclientupdater"), "updates")
                if (directory.exists()) {
                    directory.listFiles()?.let {
                        for (file in it) {
                            file.delete()
                        }
                    }
                }
                displayScreen(GuiMainMenu())
            }
            successfullyUpdated.size + failedUpdated.size == updating.size -> {
                TickTask(5) {
                    displayScreen(
                        UpdateSummaryScreen(
                            successfullyUpdated as HashSet<Triple<File, String, String>>,
                            failedUpdated as HashSet<Triple<File, String, String>>
                        )
                    )
                }
            }
        }
    }
}