package mynameisjeff.skyblockclientupdater.gui

import mynameisjeff.skyblockclientupdater.utils.UpdateChecker
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen

class PromptUpdateScreen : GuiScreen() {
    override fun initGui() {
        buttonList.add(GuiButton(0, this.width / 2 - 50, this.height / 2 - 50, 100, 20, "Update Mods"))
        buttonList.add(GuiButton(1, this.width / 2 - 50, this.height / 2 + 5, 100, 20, "Main Menu"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(ChooseUpdatedModsScreen())
        } else if (button.id == 1) {
            UpdateChecker.needsUpdate.clear()
            mc.displayGuiScreen(GuiMainMenu())
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(mc.fontRendererObj, "${UpdateChecker.needsUpdate.size} mod updates are available!", this.width / 2, this.height / 2 - 100, 0xFFFFFF)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}