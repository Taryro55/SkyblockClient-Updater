package mynameisjeff.skyblockclientupdater.gui.screens

import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import mynameisjeff.skyblockclientupdater.gui.elements.ModUpdateComponent
import mynameisjeff.skyblockclientupdater.gui.elements.SexyButton
import mynameisjeff.skyblockclientupdater.utils.UpdateChecker
import net.minecraft.client.gui.GuiMainMenu
import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class ModUpdateScreen(
    private val needsUpdate: HashSet<Triple<File, String, String>>
) : BaseScreen(
    useContentContainer = true
) {
    private val updating = needsUpdate.toMutableSet()

    val headerText = UIText("Some of your mods are outdated. Do you want to update?").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    }.setTextScale(1.25f.pixels()) childOf headerContainer

    private val updateScroller = ScrollComponent("There are no updates... How are you seeing this menu?!").constrain {
        width = RelativeConstraint()
        height = RelativeConstraint()
    } childOf contentContainer

    private val buttonContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf footerContainer
    val updateButton = SexyButton("Update").constrain {
        width = 150.pixels()
        height = 20.pixels()
    }.onMouseClick {
        displayScreen(DownloadProgressScreen(updating as HashSet<Triple<File, String, String>>))
    } childOf buttonContainer
    val exitButton = SexyButton("Main Menu", Color.RED).constrain {
        width = 150.pixels()
        height = 20.pixels()
        x = SiblingConstraint(7.5f)
    }.onMouseClick {
        UpdateChecker.ignoreUpdates()
        displayScreen(GuiMainMenu())
    } childOf buttonContainer

    init {
        for (update in needsUpdate) {
            ModUpdateComponent(update, updating).constrain {
                x = CenterConstraint()
                y = if (needsUpdate.indexOf(update) == 0) 5.pixels() else SiblingConstraint(1f)
            } childOf updateScroller
        }
    }
}