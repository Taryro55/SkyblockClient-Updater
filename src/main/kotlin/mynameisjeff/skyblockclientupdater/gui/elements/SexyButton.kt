package mynameisjeff.skyblockclientupdater.gui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import java.awt.Color

class SexyButton(
    text: String,
    outlineColor: Color = SkyClientUpdater.accentColor,
    playClickSound: Boolean = true
) : UIComponent() {
    private val background = UIBlock(Color(31, 31, 31)).constrain {
        width = RelativeConstraint()
        height = RelativeConstraint()
    } effect OutlineEffect(Color(0, 0, 0, 0), 1f) childOf this
    private val textComponent = UIText(text).constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf background

    init {
        val outlineEffect = background.effects[0] as OutlineEffect
        onMouseEnter {
            outlineEffect::color.animate(Animations.OUT_EXP, 1f, outlineColor)
        }.onMouseLeave {
            outlineEffect::color.animate(Animations.OUT_EXP, 1f, Color(0, 0, 0, 0))
        }.onMouseClick {
            if (playClickSound) {
                Minecraft.getMinecraft().soundHandler.playSound(
                    PositionedSoundRecord.create(
                        ResourceLocation("gui.button.press"),
                        1f
                    )
                )
            }
        }
    }
}